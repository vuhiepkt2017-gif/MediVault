package com.example.medivault

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.medivault.ui.screens.MemberDetailScreen
import com.example.medivault.ui.screens.MemberListScreen
import com.example.medivault.ui.screens.MedicineLookupScreen
import com.example.medivault.ui.theme.MediVaultTheme
import com.example.medivault.ui.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request runtime Camera and Storage permissions
        checkAndRequestPermissions()

        setContent {
            MediVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MediVaultApp(viewModel)
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        
        // For Android 9 (API 28) and below, we need write external storage
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền camera đã được cấp", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Ứng dụng cần quyền camera để chụp đơn thuốc & tra cứu", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1001
    }
}

@Composable
fun MediVaultApp(viewModel: HealthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "memberList") {
        composable("memberList") {
            MemberListScreen(
                viewModel = viewModel,
                onMemberClick = { memberId ->
                    navController.navigate("memberDetail/$memberId")
                },
                onNavigateToLookup = {
                    navController.navigate("lookup")
                }
            )
        }
        composable(
            route = "memberDetail/{memberId}",
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getLong("memberId") ?: 0L
            MemberDetailScreen(
                viewModel = viewModel,
                memberId = memberId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        composable("lookup") {
            MedicineLookupScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
