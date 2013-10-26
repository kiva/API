class AddOauthConsumerTable < ActiveRecord::Migration
  def self.up
    create_table :consumer_tokens do |t|
      t.integer :user_id
      t.string :type, :limit => 30
      t.string :token, :limit => 255
      t.string :secret
      t.timestamps
    end

    add_index :consumer_tokens, :token, :unique => true, :length => 100
  end

  def self.down
    drop_table :consumer_tokens
  end
end
