#!/usr/local/bin/ruby

require 'rubygems'
require 'oauth'
require 'json'
require 'net/http'
require 'net/https'

auth={}
auth["consumer_key"]    = ''
auth["consumer_secret"] = ''

event_url = "https://dev-vm-01.kiva.org/sites/kiva_api/oauth/request_token.json"
path = "/sites/kiva_api/oauth/request_token.json"
site = "https://dev-vm-01.kiva.org"
callback_url = 'oob'

#==============================
#=Set up signed request to get request token
consumer = OAuth::Consumer.new(
  auth["consumer_key"], 
  auth["consumer_secret"], {
    :oauth_version => "1.0",  
    :site   => site,
    :scheme => :body,
    :http_method => :post
})
req  = consumer.create_signed_request(:post, path)
data = URI.unescape(req.body)
data = Hash[*req.body.split(/=|&/)]

uri = URI.parse(event_url)
http = Net::HTTP.new(uri.host, uri.port)
http.use_ssl = true
http.verify_mode = OpenSSL::SSL::VERIFY_NONE 
request = Net::HTTP::Post.new(path)
request.set_form_data(data)
resp = http.request(request)
token_n_secret = JSON.parse resp.body


#===============================
#=Setup Access token
request_token_consumer = OAuth::Consumer.new(
     auth["consumer_key"], 
     auth["consumer_secret"],
    :oauth_version => "1.0",  
    :site   => site,
    :scheme => :body,
    :http_method => :post)

oauth_url = 'https://dev-vm-01.kiva.org/httpdocs/oauth/authorize?response_type=code&client_id='+auth["consumer_key"]+'&type=web_server'+'&oauth_token='+token_n_secret['oauth_token']+'&oauth_callback='+URI.escape(callback_url)

puts oauth_url
puts "\nEnter the Authorization Code:"
auth_code_from_browser = gets.chomp
ouath_hash_verifier = {:oauth_verifier => auth_code_from_browser.to_s}

t = OAuth::Token.new( token_n_secret['oauth_token'], token_n_secret['oauth_token_secret'])

req  = request_token_consumer.create_signed_request(:post, "/sites/kiva_api/oauth/access_token.json", t, ouath_hash_verifier)
data = URI.unescape(req.body)
data = Hash[*req.body.split(/=|&/)]
data.merge(ouath_hash_verifier)

uri = URI.parse(event_url)
http = Net::HTTP.new(uri.host, uri.port)
http.use_ssl = true
http.verify_mode = OpenSSL::SSL::VERIFY_NONE 
request = Net::HTTP::Post.new("/sites/kiva_api/oauth/access_token.json")
request.set_form_data(data)
resp = http.request(request)

#=====access token
access_token_consumer = OAuth::Consumer.new(
     auth["consumer_key"], 
     auth["consumer_secret"],
    :oauth_version => "1.0",  
    :site   => site,
    :scheme => :body,
    :http_method => :post)

access_token_obj = JSON.parse resp.body
puts access_token_obj.inspect

=begin
at = OAuth::Token.new(access_token_obj['oauth_token'], access_token_obj['oauth_token_secret'])
req  = access_token_consumer.create_signed_request(:post, "/sites/kiva_api/v1/my/account.json", at)
data = URI.unescape(req.body)
data = Hash[*req.body.split(/=|&/)]

uri = URI.parse(event_url)
http = Net::HTTP.new(uri.host, uri.port)
http.use_ssl = true
http.verify_mode = OpenSSL::SSL::VERIFY_NONE 
request = Net::HTTP::Post.new( "/sites/kiva_api/v1/my/account.json")
request.set_form_data(moredata)
resp = http.request(request)
puts resp.inspect
pp resp.inspect
=end
