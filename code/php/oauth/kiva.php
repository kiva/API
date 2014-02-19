<?php
const ACCESS_TOKEN_PATH="/tmp/api-vm-accessToken.txt";
require_once("lib/OAuth.php");

// You need to set these
$key = 'com.your.app.name';
$secret = 'your secret';

// If you have set up a callback URL at build.kiva.org, you should enter it below. Otherwise leave it as 'oob'
$callback_url = 'oob';

// This is the URL of the protected resource you want to access
$resource_url = 'https://api-vm.kiva.org/v1/my/account.json';
//$resource_url = 'https://api-vm.kiva.org/v1/my/expected_repayments.json';

// These should stay the same, probably
$request_token_url = 'https://api-vm.kiva.org/oauth/request_token.json';
$authorization_url = 'https://dev-vm-01.kiva.org/oauth/authorize?response_type=code&client_id='.$key.'&type=web_server';
$access_token_url = 'https://api-vm.kiva.org/oauth/access_token.json';

// Leave everything below this line alone
$sig_method = new OAuthSignatureMethod_HMAC_SHA1();
$consumer = new OAuthConsumer($key, $secret, NULL);

// Get the request token
$req_req = OAuthRequest::from_consumer_and_token($consumer, NULL, "POST", $request_token_url, array('oauth_callback' => $callback_url));
$req_req->sign_request($sig_method, $consumer, NULL);

$req_arr = preg_split('/\?/',$req_req);

$opts = array('http' =>
array(
	'method'  => 'POST',
	'header'  => 'Content-type: application/x-www-form-urlencoded',
	'content' => $req_arr[1]
)
);

if(!file_exists(ACCESS_TOKEN_PATH) or filemtime(ACCESS_TOKEN_PATH) < (time()- (60*60*24*30*3))){
	// if we have an access token is more than about 3 months old, or if we don't have one...
	$context  = stream_context_create($opts);
	$request_token_response = file_get_contents($req_arr[0], false, $context);
	$request_token_obj = json_decode($request_token_response);

	if ($request_token_obj) {
		$request_token = new OAuthConsumer($request_token_obj->oauth_token, $request_token_obj->oauth_token_secret, $callback_url);
	} else {
		die('Error fetching request token');
	}

// Authorize the app
	$authorization_url .= "&oauth_token=".$request_token_obj->oauth_token."&oauth_callback=".urlencode($callback_url);

	fwrite(STDOUT, "\nVisit: $authorization_url\nEnter the code here: ");
	$params['oauth_verifier'] = trim(fgets(STDIN));

// Get the access token
	$acc_req = OAuthRequest::from_consumer_and_token($consumer, $request_token, "POST", $access_token_url, $params);
	$acc_req->sign_request($sig_method, $consumer, $request_token);

	$acc_arr = preg_split('/\?/',$acc_req);

	$opts = array('http' =>
	array(
		'method'  => 'POST',
		'header'  => 'Content-type: application/x-www-form-urlencoded',
		'content' => $acc_arr[1]
	)
	);

	$context  = stream_context_create($opts);
	$access_token_response = file_get_contents($acc_arr[0], false, $context);
	$access_token_obj = json_decode($access_token_response);

	if ($access_token_obj) {
		$access_token = new OAuthConsumer($access_token_obj->oauth_token, $access_token_obj->oauth_token_secret);
	} else {
		die('Error fetching access token');
	}

	file_put_contents(ACCESS_TOKEN_PATH,serialize($access_token));
} else {
	$access_token = unserialize(file_get_contents(ACCESS_TOKEN_PATH));
}

//Access secure content
$res_req = OAuthRequest::from_consumer_and_token($consumer, $access_token, "GET", $resource_url);
$res_req->sign_request($sig_method, $consumer, $access_token);

$resource_response = file_get_contents($res_req);

$resource_obj = json_decode($resource_response);

if ($resource_obj) {
	echo "The user account object:\n\n";
} else {
	die('Error fetching resource');
}


