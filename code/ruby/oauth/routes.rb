OauthTest::Application.routes.draw do
  get 'authorize_kiva' => 'oauth#oauth_kiva', as: :authorize_kiva
  get 'oauth/callback' => 'oauth#oauth_callback'
end
