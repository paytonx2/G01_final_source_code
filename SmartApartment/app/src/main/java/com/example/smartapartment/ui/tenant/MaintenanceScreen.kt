package com.example.smartapartment.ui.tenant

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartapartment.ui.auth.UiState
import com.example.smartapartment.utils.SessionManager
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    navController: NavController,
    sessionManager: SessionManager,
    viewModel: TenantViewModel = viewModel()
) {
    val context = LocalContext.current
    val userId = sessionManager.getUserId()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }

    var roomId by remember { mutableStateOf(0) }

    val roomState by viewModel.roomState.collectAsState()
    val uploadState by viewModel.uploadState.collectAsState()

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            selectedFile = getFileFromUri(context, it)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchMyRoom(userId)
    }

    LaunchedEffect(roomState) {
        if (roomState is UiState.Success) {
            val rooms = (roomState as UiState.Success).data
            if (rooms.isNotEmpty()) {
                roomId = rooms[0].room_id
            }
        }
    }

    LaunchedEffect(uploadState) {
        if (uploadState is UiState.Success) {
            viewModel.resetUploadState()
            navController.popBackStack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Maintenance") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        // --- Guard: no room assigned ---
        val hasNoRoom = roomState is UiState.Success &&
            (roomState as UiState.Success<List<com.example.smartapartment.models.MyRoom>>).data.isEmpty()

        if (hasNoRoom) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "ไม่สามารถแจ้งซ่อมได้",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "คุณยังไม่มีห้องพักที่ได้รับมอบหมาย\nกรุณาติดต่อแอดมินเพื่อรับห้องพักก่อน",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("กลับไปหน้าหลัก")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Describe the Issue", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Subject (e.g. Broken AC)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { launcher.launch("image/*") }) {
                Text(if (selectedImageUri == null) "Attach Image (Optional)" else "Change Image")
            }

            selectedImageUri?.let { uri ->
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(150.dp)
                        .padding(4.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uploadState is UiState.Error) {
                Text(text = (uploadState as UiState.Error).message, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(8.dp))
            }

            Button(
                onClick = { 
                    if (title.isNotEmpty() && description.isNotEmpty()) {
                        viewModel.submitMaintenanceRequest(roomId, userId, title, description, selectedFile)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = title.isNotEmpty() && description.isNotEmpty() && uploadState !is UiState.Loading
            ) {
                if (uploadState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Submit Request")
                }
            }
        }
    }
}
