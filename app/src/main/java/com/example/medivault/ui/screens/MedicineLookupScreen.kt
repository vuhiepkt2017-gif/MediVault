package com.example.medivault.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.medivault.ui.viewmodel.HealthViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineLookupScreen(
    viewModel: HealthViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isOcrProcessing by viewModel.isOcrProcessing.collectAsState()

    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    val ocrCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            val bitmap = BitmapFactory.decodeFile(tempPhotoFile!!.absolutePath)
            if (bitmap != null) {
                viewModel.processOcr(bitmap)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tra cứu & Nhận dạng thuốc", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchMedicine(it) },
                    placeholder = { Text("Nhập tên thuốc để tra cứu...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = {
                        val directory = File(context.filesDir, "ocr_temp").apply { mkdirs() }
                        val file = File(directory, "ocr_${System.currentTimeMillis()}.jpg")
                        tempPhotoFile = file
                        val authority = "${context.packageName}.fileprovider"
                        try {
                            val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                            ocrCameraLauncher.launch(uri)
                        } catch (e: Exception) {
                            // Fallback simulation
                            viewModel.searchMedicine("Paracetamol")
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Quét ảnh")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("AI Quét")
                }
            }

            if (isOcrProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("AI đang nhận diện chữ viết trên hộp thuốc...", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Divider()

            // Search Results Section
            if (searchResults.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (searchQuery.isBlank()) "Nhập tên thuốc hoặc quét ảnh hộp thuốc bằng AI để bắt đầu tra cứu."
                        else "Không tìm thấy thông tin thuốc khớp với tìm kiếm của bạn.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                Text(
                    "Kết quả tìm thấy (${searchResults.size}):",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(searchResults) { medicine ->
                        MedicineInfoCard(medicine = medicine)
                    }
                }
            }
        }
    }
}

@Composable
fun MedicineInfoCard(medicine: com.example.medivault.ui.viewmodel.MedicineDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                medicine.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Hoạt chất: ${medicine.activeIngredient}",
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
            Divider()
            Text(
                "Chỉ định điều trị:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(medicine.indication, style = MaterialTheme.typography.bodyMedium)
            
            Text(
                "Hướng dẫn sử dụng:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(medicine.usageInstructions, style = MaterialTheme.typography.bodyMedium)

            Text(
                "Tác dụng phụ lưu ý:",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Text(medicine.sideEffects, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
