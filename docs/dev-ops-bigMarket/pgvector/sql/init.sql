CREATE EXTENSION IF NOT EXISTS vector;

DROP TABLE IF EXISTS public.vector_store_chatglm_embedding3;

CREATE TABLE public.vector_store_chatglm_embedding3
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT NOT NULL,
    metadata JSONB,
    embedding VECTOR(1024)
);

-- Add a GIN index on the metadata column for efficient filtering by userId and context
CREATE INDEX idx_vector_store_metadata ON public.vector_store_chatglm_embedding3 USING GIN (metadata);

-- Add a comment to document the expected metadata structure
COMMENT ON COLUMN public.vector_store_chatglm_embedding3.metadata IS 'JSON metadata containing at minimum: {"userId": "string", "context": "string"}';