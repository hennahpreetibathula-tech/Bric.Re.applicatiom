package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BricReDao {

    // Document-related Queries
    @Query("SELECT * FROM research_documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<ResearchDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: ResearchDocument): Long

    @Query("DELETE FROM research_documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Int)

    // Chunk-related Queries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChunks(chunks: List<DocumentChunk>)

    @Query("DELETE FROM document_chunks WHERE docId = :docId")
    suspend fun deleteChunksByDocId(docId: Int)

    @Query("SELECT * FROM document_chunks")
    suspend fun getAllChunks(): List<DocumentChunk>

    // Session-related Queries
    @Query("SELECT * FROM research_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ResearchSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ResearchSession): Long

    @Query("DELETE FROM research_sessions WHERE id = :id")
    suspend fun deleteSessionById(id: Int)

    @Query("DELETE FROM research_sessions")
    suspend fun clearHistory()
}
