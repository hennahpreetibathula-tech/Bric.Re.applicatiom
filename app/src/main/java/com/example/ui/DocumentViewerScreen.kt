package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ResearchDocument
import com.example.viewmodel.BricReViewModel

@Composable
fun DocumentViewerScreen(
    viewModel: BricReViewModel,
    documents: List<ResearchDocument>,
    modifier: Modifier = Modifier
) {
    var isAddDialogVisible by remember { mutableStateOf(false) }
    var inputTitle by remember { mutableStateOf("") }
    var inputSource by remember { mutableStateOf("") }
    var inputContent by remember { mutableStateOf("") }

    var selectedDocument by remember { mutableStateOf<ResearchDocument?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Description Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Source Document Repository",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${documents.size} Scientific Papers Active",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Button(
                onClick = { isAddDialogVisible = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_document_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Document")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Paper", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))

        if (documents.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Document Repository Empty",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Import academic text using the 'Add Paper' tab above.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("document_list"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(documents) { doc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedDocument = doc }
                            .testTag("document_item_${doc.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = doc.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = doc.source,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "${(doc.fullText.length / 5).coerceAtLeast(35)} words",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Indexed Segments",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.deleteDoc(doc.id) },
                                modifier = Modifier.testTag("delete_doc_${doc.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete document",
                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Document Dialog
    if (isAddDialogVisible) {
        AlertDialog(
            onDismissRequest = { isAddDialogVisible = false },
            title = {
                Text(
                    text = "Upload Academic Document",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputTitle,
                        onValueChange = { inputTitle = it },
                        label = { Text("Document Title") },
                        placeholder = { Text("e.g., Rates of Epistemic Hallucinations") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_doc_title"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = inputSource,
                        onValueChange = { inputSource = it },
                        label = { Text("Source Journal / Author") },
                        placeholder = { Text("e.g., Nature Climate 2026") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_doc_source"),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = inputContent,
                        onValueChange = { inputContent = it },
                        label = { Text("Full Scientific Text") },
                        placeholder = { Text("Abstract, paragraphs, and experimental conclusions...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("input_doc_content"),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputTitle.isNotEmpty() && inputContent.isNotEmpty()) {
                            viewModel.addCustomDocument(
                                title = inputTitle,
                                source = if (inputSource.isEmpty()) "Unknown Source" else inputSource,
                                content = inputContent
                            )
                            // Reset inputs and close
                            inputTitle = ""
                            inputSource = ""
                            inputContent = ""
                            isAddDialogVisible = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.testTag("save_doc_button")
                ) {
                    Text("Parse & Index")
                }
            },
            dismissButton = {
                TextButton(onClick = { isAddDialogVisible = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Document Preview Sheet / Modal
    if (selectedDocument != null) {
        val doc = selectedDocument!!
        AlertDialog(
            onDismissRequest = { selectedDocument = null },
            title = {
                Column {
                    Text(
                        text = doc.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = doc.source,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.02f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(10.dp)
                        ) {
                            item {
                                Text(
                                    text = doc.fullText,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { selectedDocument = null }) {
                    Text("Close Preview")
                }
            }
        )
    }
}
