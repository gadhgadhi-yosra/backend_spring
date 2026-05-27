package com.elfaddoui.backend.ai.repository;

import com.elfaddoui.backend.ai.entity.RagChunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class RagChunkRepository {

    private final JdbcTemplate jdbcTemplate;

    public RagChunkRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<RagChunk> searchTopK(String lang, String queryEmbedding, int k) {
        return jdbcTemplate.query("""
                SELECT id, doc_id, chunk_index, source_type, lang, title, content, category, valid_until, embedding::text AS embedding_text, updated_at
                FROM rag_chunks
                WHERE lang = ?
                  AND (valid_until IS NULL OR valid_until >= now())
                ORDER BY embedding <=> CAST(? AS public.vector)
                LIMIT ?
                """, this::mapRow, lang, queryEmbedding, k);
    }

    public List<RagChunk> searchRecipesByKeyword(String keyword, int k) {
        String q = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return jdbcTemplate.query("""
                SELECT id, doc_id, chunk_index, source_type, lang, title, content, category, valid_until, embedding::text AS embedding_text, updated_at
                FROM rag_chunks
                WHERE source_type = 'recipe'
                  AND (lower(title) LIKE ? OR lower(content) LIKE ?)
                ORDER BY updated_at DESC
                LIMIT ?
                """, this::mapRow, q, q, k);
    }

    public List<RagChunk> searchProductsByKeyword(String keyword, int k) {
        String q = "%" + (keyword == null ? "" : keyword.trim().toLowerCase()) + "%";
        return jdbcTemplate.query("""
                SELECT id, doc_id, chunk_index, source_type, lang, title, content, category, valid_until, embedding::text AS embedding_text, updated_at
                FROM rag_chunks
                WHERE source_type = 'product'
                  AND (lower(title) LIKE ? OR lower(content) LIKE ?)
                ORDER BY updated_at DESC
                LIMIT ?
                """, this::mapRow, q, q, k);
    }

    public List<RagChunk> findByDocId(String docId) {
        return jdbcTemplate.query("""
                SELECT id, doc_id, chunk_index, source_type, lang, title, content, category, valid_until, embedding::text AS embedding_text, updated_at
                FROM rag_chunks
                WHERE doc_id = ?
                LIMIT 1
                """, this::mapRow, docId);
    }

    private RagChunk mapRow(ResultSet rs, int rowNum) throws SQLException {
        RagChunk chunk = new RagChunk();
        chunk.setId(rs.getLong("id"));
        chunk.setDocId(rs.getString("doc_id"));
        chunk.setChunkIndex(rs.getInt("chunk_index"));
        chunk.setSourceType(rs.getString("source_type"));
        chunk.setLang(rs.getString("lang"));
        chunk.setTitle(rs.getString("title"));
        chunk.setContent(rs.getString("content"));
        chunk.setCategory(rs.getString("category"));
        var validUntil = rs.getTimestamp("valid_until");
        if (validUntil != null) {
            chunk.setValidUntil(validUntil.toInstant());
        }
        chunk.setEmbedding(rs.getString("embedding_text"));
        var updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            chunk.setUpdatedAt(updatedAt.toInstant());
        }
        return chunk;
    }
}
