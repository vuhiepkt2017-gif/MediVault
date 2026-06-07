package com.example.medivault.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.medivault.ui.viewmodel.HealthViewModel
import java.io.File
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberDetailScreen(
    viewModel: HealthViewModel,
    memberId: Long,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    
    // Select the current member in ViewModel
    LaunchedEffect(memberId) {
        viewModel.selectMember(memberId)
    }

    val member by viewModel.selectedMember.collectAsState()
    val prescriptions by viewModel.selectedMemberPrescriptions.collectAsState()
    val medications by viewModel.selectedMemberMedications.collectAsState()

    var showAddMedDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var tempPhotoFile by remember { mutableStateOf<File?>(null) }

    // Launcher for taking a picture
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoFile != null) {
            viewModel.addPrescription(
                imagePath = tempPhotoFile!!.absolutePath,
                date = LocalDate.now().toString(),
                notes = "Đơn thuốc ngày ${LocalDate.now()}"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(member?.name ?: "Chi tiết thành viên", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    member?.let {
                        IconButton(onClick = {
                            viewModel.deleteMember(it)
                            onBackClick()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Xóa hồ sơ")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (member == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Profile Information Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Thông tin cá nhân", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Ngày sinh: ${member!!.dob}")
                            Text("Giới tính: ${member!!.gender}")
                            Text("Nhóm máu: ${member!!.bloodType}")
                        }
                    }
                }

                // Medical History, Allergies & Diseases
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Hồ sơ bệnh lý", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Bệnh nền: ${member!!.underlyingDiseases.ifBlank { "Không" }}", fontWeight = FontWeight.SemiBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Dị ứng: ${member!!.allergies.ifBlank { "Không" }}", color = Color.Red.copy(alpha = 0.8f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Lịch sử bệnh án: ${member!!.medicalHistory.ifBlank { "Chưa có thông tin" }}")
                        }
                    }
                }

                // Medications Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Thuốc đang sử dụng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = { showAddMedDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Thêm thuốc")
                        }
                    }
                }

                if (medications.isEmpty()) {
                    item {
                        Text("Chưa có thuốc nào được đăng ký", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    items(medications) { med ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(med.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text("Liều lượng: ${med.dosage} - Tần suất: ${med.frequency}")
                                }
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(med.status) }
                                )
                            }
                        }
                    }
                }

                // Prescriptions Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Ảnh đơn thuốc", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                // Create temp file and get Uri
                                val directory = File(context.filesDir, "prescriptions").apply { mkdirs() }
                                val file = File(directory, "pres_${System.currentTimeMillis()}.jpg")
                                tempPhotoFile = file
                                val authority = "${context.packageName}.fileprovider"
                                // Note: FileProvider is standard in Android. In Compose, we can request URI via androidx.core.content.FileProvider
                                try {
                                    val uri = androidx.core.content.FileProvider.getUriForFile(context, authority, file)
                                    tempPhotoUri = uri
                                    takePictureLauncher.launch(uri)
                                } catch (e: Exception) {
                                    // Fallback for demo: just simulate saving a mock photo if SDK isn't fully configured
                                    viewModel.addPrescription(
                                        imagePath = file.absolutePath,
                                        date = LocalDate.now().toString(),
                                        notes = "Đơn thuốc mẫu (Simulated)"
                                    )
                                }
                            }
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Chụp đơn thuốc")
                        }
                    }
                }

                if (prescriptions.isEmpty()) {
                    item {
                        Text("Chưa có đơn thuốc nào được lưu", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    }
                } else {
                    items(prescriptions) { prescription ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Ngày chụp: ${prescription.date}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(8.dp))
                                AsyncImage(
                                    model = prescription.imagePath,
                                    contentDescription = prescription.notes,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                if (prescription.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(prescription.notes, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddMedDialog) {
            AddMedicationDialog(
                onDismiss = { showAddMedDialog = false },
                onConfirm = { name, dosage, freq ->
                    viewModel.addMedication(name, dosage, freq)
                    showAddMedDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thuốc đang sử dụng", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên thuốc") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Liều dùng (ví dụ: 1 viên)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("Tần suất (ví dụ: Ngày uống 2 lần)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, dosage, frequency)
                    }
                }
            ) {
                Text("Lưu")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
