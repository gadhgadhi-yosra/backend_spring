CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS rag_chunks (
  id BIGSERIAL PRIMARY KEY,
  doc_id VARCHAR(120) NOT NULL,
  chunk_index INT NOT NULL,
  source_type VARCHAR(40) NOT NULL,
  lang VARCHAR(8) NOT NULL,
  title VARCHAR(255),
  content TEXT NOT NULL,
  category VARCHAR(80),
  valid_until TIMESTAMPTZ NULL,
  embedding VECTOR(1536) NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_rag_chunks_lang ON rag_chunks(lang);
CREATE INDEX IF NOT EXISTS idx_rag_chunks_source_type ON rag_chunks(source_type);
CREATE INDEX IF NOT EXISTS idx_rag_chunks_valid_until ON rag_chunks(valid_until);

CREATE INDEX IF NOT EXISTS idx_rag_chunks_embedding_ivfflat
ON rag_chunks USING ivfflat (embedding vector_cosine_ops)
WITH (lists = 100);
