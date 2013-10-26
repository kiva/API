class UpdateOauthTable10a < ActiveRecord::Migration
  def self.up
    add_column :consumer_tokens, :callback_url, :string
    add_column :consumer_tokens, :verifier, :string, :limit => 20
  end

  def self.down
    remove_column :consumer_tokens, :callback_url
    remove_column :consumer_tokens, :verifier
  end
end
