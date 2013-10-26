require 'rubygems'
require 'oauth'
require 'oauth/consumer'
require 'json'

class OauthController
 
  unloadable

  def oauth_kiva 
    if :oauth
       @consumer=OAuth::Consumer.new :oauth_client_id,
                                     :oauth_client_secret,
                                     {:site=>"https://api.kivaws.org",
                                     :scheme => :header,
                                     :request_token_path => '/oauth/request_token',
                                     :access_token_path => '/oauth/access_token',
                                     :authorize_url => 'https://www.kiva.org/oauth/authorize'
                                     }
       session[:consumer] = @consumer
       @request_token=@consumer.get_request_token({:oauth_callback => :oauth_callback_url})
       session[:request_token] = @request_token
       session[:request_token_token] = @request_token.token
       session[:request_token_secret] = @request_token.secret
       @request_token.authorize_url
       redirect_to @request_token.authorize_url + '&response_type=code&client_id=' + :oauth_client_id + '&scope=user_email&oauth_callback=' + :oauth_callback_url
    else
      # If OAuth turned off then do something else
      # password_authentication
    end
  end

  def oauth_callback
    @request_token = session[:request_token]
    @access_token = @request_token.get_access_token({:oauth_verifier => params[:oauth_verifier]})
    response = @access_token.get('/v1/my/email.json')
    json = ActiveSupport::JSON.decode(response.body)
    mail = json["user_email"]["email"]
    # whereas this assumes a user model that you can lookup by email
    user = User.find_by_mail(mail)
    if user.nil?
      logger.warn "Failed login for '#{params[:username]}' from #{request.remote_ip} at #{Time.now.utc}"
      flash[:error] = "Failed to login. Make sure your credentials are correct"
    else
      # whereas successful_authentication is whatever function is successful in your app
      successful_authentication(user)
    end
  end

end
