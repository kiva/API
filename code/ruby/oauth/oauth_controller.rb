# if you are not using a Gemfile then uncomment the below 4 lines
# require 'rubygems'
# require 'oauth'
# require 'oauth/consumer'
# require 'json'

class OauthController < ApplicationController

  def oauth_kiva 
    oauth_client_id = 'put your client id from build.kiva.org here'
    oauth_client_secret = 'put your client secret from build.kiva.org here'
    # note that your callback should be like https://mydomain.com/oauth/callback
    oauth_callback_url = 'put your oauth callback from build.kiva.org here'

    @consumer = OAuth::Consumer.new(oauth_client_id,
                                 oauth_client_secret,
                                 { site: "https://api.kivaws.org",
                                   scheme: :header,
                                   request_token_path: '/oauth/request_token',
                                   access_token_path: '/oauth/access_token',
                                   authorize_url: 'https://www.kiva.org/oauth/authorize'
                                 })
    session[:consumer] = @consumer
    @request_token = @consumer.get_request_token({:oauth_callback => oauth_callback_url})
    session[:request_token] = @request_token
    session[:request_token_token] = @request_token.token
    session[:request_token_secret] = @request_token.secret
	redirect_to @request_token.authorize_url + '&response_type=code&client_id=' + oauth_client_id + '&scope=user_email&oauth_callback=' + oauth_callback_url
  end  

  def oauth_callback
    @request_token = session[:request_token]
    @access_token = @request_token.get_access_token({:oauth_verifier => params[:oauth_verifier]})
    response = @access_token.get('/v1/my/email.json')
    json = ActiveSupport::JSON.decode(response.body)
    session[:user_mail] = json["user_email"]["email"]
    json["user_email"]["email"]
  end
end
