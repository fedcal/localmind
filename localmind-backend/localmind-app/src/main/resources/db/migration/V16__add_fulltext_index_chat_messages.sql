ALTER TABLE chat_messages ADD FULLTEXT INDEX ft_chat_messages_content (content(512))
