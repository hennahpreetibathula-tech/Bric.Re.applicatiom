package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "research_documents")
data class ResearchDocument(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val source: String,
    val fullText: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "document_chunks")
data class DocumentChunk(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val docId: Int,
    val docTitle: String,
    val docSource: String,
    val pageNumber: Int,
    val textContent: String
)

@Entity(tableName = "research_sessions")
data class ResearchSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val queryText: String,
    val timestamp: Long = System.currentTimeMillis(),
    val answerText: String,
    val evidenceSummary: String, // Stored as simple formatted text (or description) of supporting chunks
    val conflicts: String?,      // Stored discrepancies between sources
    val confidenceScore: String, // "High", "Medium", "Low"
    val researchMode: String     // "Quick", "Deep", "Report"
)

@Entity(tableName = "document_intelligence")
data class DocumentIntelligence(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val type: String, // "TEXT" or "VISUAL"
    val domainClassification: String,
    val domainReasoning: String,
    val visualFindings: String?,
    val researchInterpretation: String,
    val keyConceptsStr: String, // newline separated values
    val ragKeywordsStr: String, // newline separated values
    val researchQuestionsStr: String, // newline separated values
    val retrievedKnowledge: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val isBookmarked: Boolean = false,
    val customTags: String = "" // comma separated values
)

