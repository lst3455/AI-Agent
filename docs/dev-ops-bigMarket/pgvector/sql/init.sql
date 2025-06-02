CREATE EXTENSION IF NOT EXISTS vector;

DROP TABLE IF EXISTS public.vector_store_chatglm_flash;

CREATE TABLE public.vector_store_chatglm_flash
(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content TEXT NOT NULL,
    metadata JSONB,
    embedding VECTOR(1024)
);