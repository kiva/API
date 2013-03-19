package org.kiva.api.scribe;

import org.scribe.builder.api.DefaultApi10a;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.scribe.utils.OAuthEncoder;
import org.scribe.utils.Preconditions;

public class KivaApi extends DefaultApi10a
{
  private static final String AUTHORIZE_URL = "https://www.kiva.org/oauth/authorize?response_type=code&type=web_server&oauth_token=%s&oauth_callback=oob";

  @Override
  public String getAccessTokenEndpoint()
  {
      return "https://api.kivaws.org/oauth/access_token";
  }

  @Override
  public String getRequestTokenEndpoint()
  {
	  return "https://api.kivaws.org/oauth/request_token";
  }
  
  
  @Override
  public String getAuthorizationUrl(Token requestToken)
  {
	  return String.format(AUTHORIZE_URL, requestToken.getToken());
  }

  @Override
  public OAuthService createService(OAuthConfig config) {
	  return super.createService(config);
  }

}

