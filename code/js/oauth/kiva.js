$(document).ready(function() {

	(function ($, global, undefined) {
		'use strict';

		// You need to set these
		var Consumer = {
			'key': '',
			'secret': '',
			// If you have set up a callback URL at build.kiva.org, you should enter it below. Otherwise leave it as 'oob'
			// Make sure it is URL encoded here
			'callbackUrl': encodeURIComponent('oob')
		};
	
		// This is the URL of the protected resource you want to access
		var resourceUrl = 'https://api.kivaws.org/v1/my/account.json';

		// These should stay the same, probably
		var requestTokenUrl = 'https://api.kivaws.org/oauth/request_token.json';
		var authorizeUrl = 'https://www.kiva.org/oauth/authorize?response_type=code&client_id='+Consumer.key+'&type=web_server&scope=access&oauth_callback='+Consumer.callbackUrl;
		var accessTokenUrl = 'https://api.kivaws.org/oauth/access_token.json';

		// Leave everything below this line alone
	
		// Helper functions
		function generateNonce() {
			return Math.random()*10000000;
		}

		function generateTimestamp() {
			var dateTime = new Date();
			var time = dateTime.getTime();
			return parseInt(time/1000, 10);
		}
	
		function generateSignature(httpMethod, url, nonce, timestamp, token, tokenSecret, verifier) {

			if (tokenSecret === undefined) {
				tokenSecret = '';
			}

			var base = httpMethod.toUpperCase() + "&" + encodeURIComponent(url).toString() + "&"
				+ encodeURIComponent("oauth_callback") + "%3D" + encodeURIComponent(Consumer.callbackUrl)
				+ "%26"
				+ encodeURIComponent("oauth_consumer_key") + "%3D" + encodeURIComponent(Consumer.key)
				+ "%26"
				+ encodeURIComponent("oauth_nonce") + "%3D" + encodeURIComponent(nonce)
				+ "%26"
				+ encodeURIComponent("oauth_signature_method") + "%3D" + encodeURIComponent("HMAC-SHA1")
				+ "%26"
				+ encodeURIComponent("oauth_timestamp") + "%3D" + encodeURIComponent(timestamp);

			if (token !== undefined) {
				base += "%26"
					+ encodeURIComponent("oauth_token") + "%3D" + encodeURIComponent(token);
			}

			if (verifier !== undefined) {
				base += "%26"
					+ encodeURIComponent("oauth_verifier") + "%3D" + encodeURIComponent(verifier);
			}

			base += "%26"
				+ encodeURIComponent("oauth_version") + "%3D" + encodeURIComponent("1.0");

			return global.b64_hmac_sha1(Consumer.secret+'&'+tokenSecret, base)+'=';
		}
	
		function generateHeader(nonce, timestamp, signature, token, verifier) {
			var header = 'OAuth oauth_nonce="'+nonce+'",oauth_callback="'+Consumer.callbackUrl+'",oauth_signature_method="HMAC-SHA1",oauth_timestamp="'+timestamp+'",oauth_consumer_key="'+Consumer.key+'",oauth_signature="'+signature+'"';

			if (token !== undefined) {
				header += ', oauth_token="'+token+'"';
			}

			if (verifier !== undefined) {
				header += ', oauth_verifier="'+verifier+'"';
			}

			header += ', oauth_version="1.0"';

			return header;
		}
	
		function fetchRequestToken(callback) {

			var nonce = generateNonce();
			var timestamp = generateTimestamp();
			var signature = generateSignature('POST', requestTokenUrl, nonce, timestamp);
			var header = generateHeader(nonce, timestamp, signature);

			$.ajax({
				url: requestTokenUrl,
				type: "POST",
				beforeSend: function (request)
				{
					request.setRequestHeader("Authorization", header);
				},
				dataType: "json",
				success: callback,
				error: function (data) {
					global.alert("Error fetching request token");
				}
			});
		}
	
		function fetchAccessToken(requestToken, requestTokenSecret, verifier, callback) {

			var nonce = generateNonce();
			var timestamp = generateTimestamp();
			var signature = generateSignature('POST', accessTokenUrl, nonce, timestamp, encodeURIComponent(requestToken), encodeURIComponent(requestTokenSecret), encodeURIComponent(verifier));
			var header = generateHeader(nonce, timestamp, signature, requestToken, verifier);

			$.ajax({
				url: accessTokenUrl,
				type: "POST",
				beforeSend: function (request)
				{
					request.setRequestHeader("Authorization", header);
				},
				dataType: "json",
				success: callback,
				error: function (data) {
					global.alert("Error fetching access token");
				}
			});
		}
	
		function fetchResource(url, accessToken, accessTokenSecret, callback) {

			var nonce = generateNonce();
			var timestamp = generateTimestamp();
			var signature = generateSignature('GET', url, nonce, timestamp, encodeURIComponent(accessToken), encodeURIComponent(accessTokenSecret));
			var header = generateHeader(nonce, timestamp, signature, accessToken);

			$.ajax({
				url: url,
				type: "GET",
				beforeSend: function (request)
				{
					request.setRequestHeader("Authorization", header);
				},
				dataType: "json",
				success: callback,
				error: function (data) {
					global.alert("Error fetching resource");
				}
			});
		}

		function init() {
			// This is the function that pulls it all together, through a series off callbacks triggered at every step
			$('body').append('<button id="authBtn">Authorize</button>');
			$('#authBtn').click(function() {
				fetchRequestToken(function(data) {
					global.alert('Redirecting to the authorize page. Copy the authorization code and paste it back on this page.');
					global.open(authorizeUrl+'&oauth_token='+data.oauth_token, '_blank');
					global.focus();

					$('#authBtn').remove();
					$('body').append('<input id="oauth_verifier" type="text"/>').append('<button id="accessBtn">Get data</button>');
					$('#accessBtn').click(function() {
						fetchAccessToken(data.oauth_token, data.oauth_token_secret, $('#oauth_verifier').val(), function(data) {
							fetchResource(resourceUrl, data.oauth_token, data.oauth_token_secret, function(data) {
								global.alert('Hello '+data.user_account.first_name+' '+data.user_account.last_name);
							});
						});
					});
				});
			});
		}

		init();

	}(jQuery, window));
});
