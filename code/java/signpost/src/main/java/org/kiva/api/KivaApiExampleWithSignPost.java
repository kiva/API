package org.kiva.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Scanner;


import org.apache.commons.io.IOUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class KivaApiExampleWithSignPost {
	public static final String RESOURCE_URL = "https://api.kivaws.org/v1/my/account.json";
	public static final String CALLBACK_URL = "oob";
	public static final String REQUEST_TOKEN_URL = "https://api.kivaws.org/oauth/request_token";
	public static final String ACCESS_TOKEN_URL = "https://api.kivaws.org/oauth/access_token";
	public static final String AUTHORIZATION_URL = "https://www.kiva.org/oauth/authorize?response_type=code&oauth_callback=" + CALLBACK_URL + "&scope=access,user_balance,user_email";
	
	public static void test1() throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException, IOException {
		Properties prop = new Properties();
		InputStream in = KivaApiExampleWithSignPost.class.getResourceAsStream("/signpost.properties");
		prop.load(in);
		String consumerKey = prop.getProperty("oauth.consumer.key");
		String consumerSecret = prop.getProperty("oauth.consumer.secret");
		in.close();
		
		System.setProperty("debug", "true");
		
		OAuthConsumer consumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);

		// Note: we have to add the consumer key in the authorization URL to make it work
		OAuthProvider provider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL,
					 									  ACCESS_TOKEN_URL,
														  AUTHORIZATION_URL + "&client_id=" + consumerKey);
		/*
		 *  This has to be done once to get request token
		 */
		provider.setOAuth10a(true);
		// for some reason, SignPost does not append oauth_callback so I
		// added it directly into the AUTHORIZATION_URL
		String requestUrl = provider.retrieveRequestToken(consumer, CALLBACK_URL);
		System.out.println("Copy/Paste the following URL in your browser: " + requestUrl);
		System.out.print("Enter your token: ");
		// read authorization code
		Scanner scanner = new Scanner(System.in);
		String authorizationCode = scanner.nextLine().trim();

		provider.retrieveAccessToken(consumer, authorizationCode);
        
		String accessToken = consumer.getToken();
		String tokenSecret = consumer.getTokenSecret();
		System.out.println("Token: " + accessToken + ". Secret: " + tokenSecret);

		// store token and secret somewhere in a database or in a file
		// so that it can be used later
        
		/*
		 * To be done whenever you use the Kiva API
		 */
		OAuthConsumer newConsumer = new CommonsHttpOAuthConsumer(consumerKey, consumerSecret);
		newConsumer.setTokenWithSecret(accessToken, tokenSecret);
		HttpGet request = new HttpGet(RESOURCE_URL);
		consumer.sign(request);

		HttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(request);
		// send request and get content
		String content = IOUtil.toString(response.getEntity().getContent());
		System.out.println(content);
	}
	
	public static void main(String[] args) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException, IOException {
		KivaApiExampleWithSignPost.test1();
	}
}
