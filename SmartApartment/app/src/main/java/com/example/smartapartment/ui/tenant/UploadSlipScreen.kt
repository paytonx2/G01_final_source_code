package com.example.smartapartment.ui.tenant

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.smartapartment.ui.auth.UiState
import com.example.smartapartment.utils.SessionManager
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadSlipScreen(
    invoiceId: Int,
    amount: Double,
    navController: NavController,
    sessionManager: SessionManager,
    viewModel: TenantViewModel = viewModel()
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFile by remember { mutableStateOf<File?>(null) }
    val userId = sessionManager.getUserId()

    val uploadState by viewModel.uploadState.collectAsState()

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            selectedFile = getFileFromUri(context, it)
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
                title = { Text("Upload Payment Slip") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Invoice #${invoiceId}", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Amount Due: $${amount}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)

            Spacer(modifier = Modifier.height(16.dp))

            // ==========================================
            // ส่วนที่เพิ่มใหม่: ข้อมูลบัญชีธนาคาร (Bank Details)
            // ==========================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "Payment Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Text(text = "ธนาคาร: กสิกรไทย (KBank)")
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(text = "ชื่อบัญชี: นายภูวรินทร์ โอนไวใจเด็ด")
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "เลขบัญชี: 092-2-18827-0",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "*กรุณาโอนเงินตามยอดชำระและแนบสลิปด้านล่าง",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            // ==========================================

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { launcher.launch("image/*") }) {
                Text(if (selectedImageUri == null) "Select Image from Gallery" else "Change Image")
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .size(250.dp)
                        .padding(8.dp),
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
                    if (selectedFile != null) {
                        viewModel.uploadSlip(invoiceId, userId, amount, selectedFile!!)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedFile != null && uploadState !is UiState.Loading
            ) {
                if (uploadState is UiState.Loading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Confirm Upload")
                }
            }
        }
    }
}

// Helper function to create a temporary file from Uri for Retrofit multipart upload
fun getFileFromUri(context: Context, uri: Uri): File? {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    var fileName = "temp_image.jpg"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) fileName = cursor.getString(nameIndex)
        }
    }
    val file = File(context.cacheDir, fileName)
    val outputStream = FileOutputStream(file)
    inputStream.copyTo(outputStream)
    return file
}