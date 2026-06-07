package com.example.medivault.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.medivault.data.database.Member
import com.example.medivault.ui.viewmodel.HealthViewModel
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberListScreen(
    viewModel: HealthViewModel,
    onMemberClick: (Long) -> Unit,
    onNavigateToLookup: () -> Unit
) {
    val members by viewModel.allMembers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "MediVault",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    Button(
                        onClick = onNavigateToLookup,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Tra cứu thuốc")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm thành viên")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            if (members.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Chưa có thành viên nào",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nhấp nút + để tạo hồ sơ thành viên mới",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(members) { member ->
                        MemberCard(member = member, onClick = { onMemberClick(member.id) })
                    }
                }
            }
        }

        if (showAddDialog) {
            AddMemberDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { name, dob, gender, bloodType, allergies, history, diseases ->
                    viewModel.addMember(name, dob, gender, bloodType, allergies, history, diseases)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun MemberCard(member: Member, onClick: () -> Unit) {
    val age = remember(member.dob) {
        try {
            val dobDate = LocalDate.parse(member.dob, DateTimeFormatter.ISO_LOCAL_DATE)
            Period.between(dobDate, LocalDate.now()).years
        } catch (e: Exception) {
            0
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    member.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    member.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tuổi: $age | Giới tính: ${member.gender} | Nhóm máu: ${member.bloodType}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (member.allergies.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Dị ứng: ${member.allergies}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Red.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemberDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("1990-01-01") }
    var gender by remember { mutableStateOf("Nam") }
    var bloodType by remember { mutableStateOf("O+") }
    var allergies by remember { mutableStateOf("") }
    var history by remember { mutableStateOf("") }
    var diseases by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tạo hồ sơ thành viên mới", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Họ tên") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Ngày sinh (YYYY-MM-DD)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Giới tính (Nam/Nữ)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = bloodType,
                        onValueChange = { bloodType = it },
                        label = { Text("Nhóm máu") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Dị ứng (nếu có)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = diseases,
                        onValueChange = { diseases = it },
                        label = { Text("Bệnh nền") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = history,
                        onValueChange = { history = it },
                        label = { Text("Hồ sơ bệnh án (Tóm tắt)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, dob, gender, bloodType, allergies, history, diseases)
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
