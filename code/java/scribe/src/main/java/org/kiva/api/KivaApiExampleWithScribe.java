package org.kiva.api;

import org.kiva.api.scribe.KivaApi;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.SignatureType;
import org.scribe.model.Token;
import org.scribe.oauth.OAuthService;

/*
 * This does not work at all
 */
public class KivaApiExampleWithScribe {

	public static void testScribe() throws Exception {
                Properties prop = new Properties();
                InputStream in = KivaApiExampleWithSignPost.class.getResourceAsStream("/kiva.properties");
                prop.load(in);
                String consumerKey = prop.getProperty("oauth.consumer.key");
                String consumerSecret = prop.getProperty("oauth.consumer.secret");
                in.close();

		System.setProperty("debug", "true");
		OAuthService service = new ServiceBuilder()
					.provider(KivaApi.class)
					.apiKey(consumerKey)
					.apiSecret(consumerSecret)
				        .callback("oob")
					.debug()
//							        .signatureType(SignatureType.Header)
					.build();
		Token token = service.getRequestToken();
	}
	
	public static void main(String[] args) throws Exception {
		KivaApiExampleWithScribe.testScribe();
	}
}
