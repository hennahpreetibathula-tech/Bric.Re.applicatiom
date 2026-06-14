package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.BricReViewModel
import com.example.viewmodel.EvidenceCard
import com.example.viewmodel.ResearchUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(viewModel: BricReViewModel) {
    val activeTab by viewModel.activeTab.collectAsState()
    val documents by viewModel.documents.collectAsState()
    val sessions by viewModel.sessions.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val isApiKeyAlertVisible by viewModel.isApiKeyAlertVisible.collectAsState()

    // Google Cloud Auth States
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userDisplayName by viewModel.userDisplayName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val gcpProjectId by viewModel.gcpProjectId.collectAsState()
    val gcpRegion by viewModel.gcpRegion.collectAsState()

    var isAuthDialogVisible by remember { mutableStateOf(false) }

    var inputName by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputProjId by remember { mutableStateOf("") }

    // Sync input fields when dialog opens
    LaunchedEffect(isAuthDialogVisible) {
        if (isAuthDialogVisible) {
            inputName = if (userDisplayName == "Guest Analyst") "" else userDisplayName
            inputEmail = userEmail ?: "hennahpreetibathula@gmail.com"
            inputProjId = gcpProjectId
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bric.Re",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "L4 RAG",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                actions = {
                    // Profile with Google Cloud account status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .clickable { isAuthDialogVisible = true }
                            .testTag("action_profile")
                    ) {
                        if (isLoggedIn) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val initials = userDisplayName.split(" ")
                                        .mapNotNull { it.firstOrNull()?.toString() }
                                        .take(2)
                                        .joinToString("")
                                        .uppercase()
                                    Text(
                                        text = if (initials.isEmpty()) "G" else initials,
                                        color = MaterialTheme.colorScheme.background,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        } else {
                            IconButton(onClick = { isAuthDialogVisible = true }) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "GCP Auth Setup",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.testTag("app_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == "dashboard",
                    onClick = { viewModel.selectTab("dashboard") },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Search Desk", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_dashboard")
                )
                NavigationBarItem(
                    selected = activeTab == "results",
                    onClick = { viewModel.selectTab("results") },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Intelligence") },
                    label = { Text("Research Desk", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_results")
                )
                NavigationBarItem(
                    selected = activeTab == "documents",
                    onClick = { viewModel.selectTab("documents") },
                    icon = { Icon(Icons.Default.Folder, contentDescription = "Library") },
                    label = { Text("Library", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_documents")
                )
                NavigationBarItem(
                    selected = activeTab == "image_analyst",
                    onClick = { viewModel.selectTab("image_analyst") },
                    icon = { Icon(Icons.Default.RemoveRedEye, contentDescription = "Visual Lab") },
                    label = { Text("Visual Lab", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("tab_image_analyst")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                "dashboard" -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        documents = documents,
                        sessions = sessions,
                        onNavigateToDocs = { viewModel.selectTab("documents") },
                        onViewSession = { session ->
                            viewModel.performResearch(session.queryText, session.researchMode)
                            viewModel.selectTab("results")
                        }
                    )
                }
                "results" -> {
                    ResearchViewScreen(
                        uiState = uiState,
                        isApiKeyAlertVisible = isApiKeyAlertVisible,
                        onCloseApiKeyAlert = { viewModel.closeApiKeyAlert() }
                    )
                }
                "documents" -> {
                    DocumentViewerScreen(
                        viewModel = viewModel,
                        documents = documents
                    )
                }
                "image_analyst" -> {
                    ImageAnalystScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Google Cloud Auth Dialog
    if (isAuthDialogVisible) {
        AlertDialog(
            onDismissRequest = { isAuthDialogVisible = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = "GCP Setup",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Google Cloud Console Auth",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoggedIn) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Status",
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "AUTHENTICATED CLIENT",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Operator: $userDisplayName",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Account: $userEmail",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                                Text(
                                    text = "Active Cloud Services:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "• Google Vertex AI Custom Search Index\n• Cloud BigQuery Knowledge Store\n• Firebase Secure Key Storage",
                                    fontSize = 10.sp,
                                    lineHeight = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Cloud Project ID:",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = gcpProjectId,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Deployment Region:",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Text(
                                        text = gcpRegion,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "Authenticate your local system with Google Cloud Console to sync scientific papers, index deep-RAG context segments, and execute online Vertex AI workflows.",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        OutlinedTextField(
                            value = inputName,
                            onValueChange = { inputName = it },
                            label = { Text("Operator Name") },
                            placeholder = { Text("e.g. Henna Analyst") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_owner_name"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = inputEmail,
                            onValueChange = { inputEmail = it },
                            label = { Text("Google Cloud Email Address") },
                            placeholder = { Text("e.g. email@gmail.com") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_owner_email"),
                            shape = RoundedCornerShape(8.dp)
                        )

                        OutlinedTextField(
                            value = inputProjId,
                            onValueChange = { inputProjId = it },
                            label = { Text("GCP Project ID") },
                            placeholder = { Text("e.g. bric-re-rag-index-104") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_gcp_project"),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                if (isLoggedIn) {
                    Button(
                        onClick = {
                            viewModel.signOut()
                            isAuthDialogVisible = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier.testTag("auth_disconnect_button")
                    ) {
                        Text("Disconnect Account")
                    }
                } else {
                    Button(
                        onClick = {
                            viewModel.signInWithGoogle(
                                name = if (inputName.isBlank()) "Henna Analyst" else inputName,
                                email = if (inputEmail.isBlank()) "hennahpreetibathula@gmail.com" else inputEmail,
                                projectId = if (inputProjId.isBlank()) "bric-re-rag-index-104" else inputProjId
                            )
                            isAuthDialogVisible = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("auth_gcp_connect_button")
                    ) {
                        Text("Authenticate with Google")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { isAuthDialogVisible = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
