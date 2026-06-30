ALTER TABLE llm_provider_configs ADD CONSTRAINT chk_provider_type CHECK (type IN ('OLLAMA', 'OPENAI', 'ANTHROPIC', 'GOOGLE', 'DEEPSEEK', 'MISTRAL', 'XAI'))
