package org.kiva.api;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import java.util.Scanner;


import org.apache.commons.io.IOUtil;

import oauth.signpost.OAuth;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.OAuthProviderListener;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpParameters;
import oauth.signpost.http.HttpRequest;
import oauth.signpost.http.HttpResponse;
import oauth.signpost.signature.QueryStringSigningStrategy;
import oauth.signpost.signature.SigningStrategy;

/**
 * This is not working!!!
 * If someone manages to make it work let us know.
 * Relating issue with Digg: https://groups.google.com/forum/?fromgroups#!topic/diggapi/DxlMk0zZglo
 */

public class KivaApiExampleWithSignPost {
	public static final String RESOURCE_URL = "https://api.kivaws.org/v1/my/account.json";
	public static final String CALLBACK_URL = "oob";
	public static final String REQUEST_TOKEN_URL = "https://api.kivaws.org/oauth/request_token";
	public static final String ACCESS_TOKEN_URL = "https://api.kivaws.org/oauth/access_token";
	public static final String AUTHORIZATION_URL = "https://www.kiva.org/oauth/authorize?response_type=code&type=web_server&oauth_callback=" + CALLBACK_URL;
	
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

		// *** it fails here ***
		HttpParameters params = new HttpParameters();
		params.put("oauth_verifier", authorizationCode);
		consumer.setAdditionalParameters(params);
		provider.setOAuth10a(true);
		provider.retrieveAccessToken(consumer, authorizationCode);
        
        String accessToken = consumer.getToken();
        String tokenSecret = consumer.getTokenSecret();
        System.out.println("Token: " + accessToken + ". Secret: " + tokenSecret);

        // store token and secret somewhere in a database or in a file
        // so that it can be used later
        
        /*
         * To be done whenever you use the Kiva API
         */
        consumer.setTokenWithSecret(accessToken, tokenSecret);
        URL url = new URL(RESOURCE_URL);
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        consumer.sign(request);

        // send request and get content
        request.connect();
        String content = IOUtil.toString(request.getInputStream());
        System.out.println(content);
	}
	
	public static void main(String[] args) throws OAuthMessageSignerException, OAuthNotAuthorizedException, OAuthExpectationFailedException, OAuthCommunicationException, IOException {
		KivaApiExampleWithSignPost.test1();
	}
}
