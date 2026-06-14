package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.Locale

class BricReRepository(private val dao: BricReDao) {

    val allDocuments: Flow<List<ResearchDocument>> = dao.getAllDocuments()
    val allSessions: Flow<List<ResearchSession>> = dao.getAllSessions()

    // Add document with automated chunk splitting
    suspend fun addDocument(title: String, source: String, fullText: String) {
        val document = ResearchDocument(title = title, source = source, fullText = fullText)
        val docId = dao.insertDocument(document).toInt()

        // Split text on paragraphs to preserve semantic context
        val paragraphs = fullText.split(Regex("\\n\\s*\\n"))
        val chunks = mutableListOf<DocumentChunk>()
        var currentPage = 1
        val paragraphBuffer = java.lang.StringBuilder()
        var currentWordCount = 0

        for (paragraph in paragraphs) {
            val trimmedParagraph = paragraph.trim()
            if (trimmedParagraph.isEmpty()) continue

            paragraphBuffer.append(trimmedParagraph).append("\n\n")
            val pWords = trimmedParagraph.split(Regex("\\s+")).size
            currentWordCount += pWords

            // Chunk budget is around 200 - 300 words per "page" chunk
            if (currentWordCount >= 250) {
                chunks.add(
                    DocumentChunk(
                        docId = docId,
                        docTitle = title,
                        docSource = source,
                        pageNumber = currentPage,
                        textContent = paragraphBuffer.toString().trim()
                    )
                )
                paragraphBuffer.setLength(0) // clear buffer
                currentWordCount = 0
                currentPage++
            }
        }

        // Add residual content
        if (paragraphBuffer.trim().isNotEmpty()) {
            chunks.add(
                DocumentChunk(
                    docId = docId,
                    docTitle = title,
                    docSource = source,
                    pageNumber = currentPage,
                    textContent = paragraphBuffer.toString().trim()
                )
            )
        }

        dao.insertChunks(chunks)
    }

    // Delete single document and its child index chunks
    suspend fun deleteDocument(id: Int) {
        dao.deleteDocumentById(id)
        dao.deleteChunksByDocId(id)
    }

    // SQLite matching model for local Retrieval-Augmented Generation
    suspend fun retrieveChunks(query: String, maxResults: Int = 4): List<DocumentChunk> {
        val allChunks = dao.getAllChunks()
        if (allChunks.isEmpty()) return emptyList()

        // Extract normalized keywords to calculate match signals
        val queryKeys = query.lowercase(Locale.ROOT)
            .split(Regex("[^a-zA-Z0-9]+"))
            .filter { it.length > 2 } // filter short preposition noise
            .distinct()

        if (queryKeys.isEmpty()) {
            return allChunks.take(maxResults)
        }

        val scored = allChunks.map { chunk ->
            val contentLower = chunk.textContent.lowercase(Locale.ROOT)
            val titleLower = chunk.docTitle.lowercase(Locale.ROOT)
            val srcLower = chunk.docSource.lowercase(Locale.ROOT)
            var score = 0.0

            for (key in queryKeys) {
                // Primary weights for terms matched in title
                if (titleLower.contains(key)) {
                    score += 5.0
                }
                // Secondary weights for source match
                if (srcLower.contains(key)) {
                    score += 2.0
                }
                // Occurrence weight inside chunk body
                val occurrences = contentLower.split(key).size - 1
                if (occurrences > 0) {
                    score += 1.0 + (occurrences - 1) * 0.1
                }
            }
            chunk to score
        }

        val filtered = scored.filter { it.second > 0 }
            .sortedByDescending { it.second }
            .map { it.first }

        return if (filtered.isEmpty()) {
            // Fallback: Default to returning first items
            allChunks.take(maxResults)
        } else {
            filtered.take(maxResults)
        }
    }

    // Save session logs in Database
    suspend fun saveSession(session: ResearchSession) {
        dao.insertSession(session)
    }

    // Clear queries log history
    suspend fun clearQueryLogs() {
        dao.clearHistory()
    }
}
