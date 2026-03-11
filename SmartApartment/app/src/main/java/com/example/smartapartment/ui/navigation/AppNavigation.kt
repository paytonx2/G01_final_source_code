package com.example.smartapartment.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.smartapartment.ui.admin.AdminAnnouncementScreen
import com.example.smartapartment.ui.admin.AdminMaintenanceScreen
import com.example.smartapartment.ui.admin.AdminRoomDetailScreen
import com.example.smartapartment.ui.admin.AdminDashboardScreen
import com.example.smartapartment.ui.admin.ManagePaymentsScreen
import com.example.smartapartment.ui.admin.ManageRoomsScreen
import com.example.smartapartment.ui.auth.LoginScreen
import com.example.smartapartment.ui.auth.RegisterScreen
import com.example.smartapartment.ui.tenant.*
import com.example.smartapartment.utils.SessionManager

@Composable
fun AppNavigation(sessionManager: SessionManager) {
    val navController = rememberNavController()
    
    // Determine start destination
    val startDestination = if (sessionManager.isLoggedIn()) {
        if (sessionManager.getUserRole() == "admin") "admin_dashboard" else "tenant_dashboard"
    } else {
        "login"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        
        // Auth Routes
        composable("login") {
            LoginScreen(navController = navController, sessionManager = sessionManager)
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }

        // Tenant Routes
        composable("tenant_dashboard") {
            TenantDashboardScreen(navController = navController, sessionManager = sessionManager)
        }
        composable("my_room") {
            RoomInfoScreen(navController = navController, sessionManager = sessionManager)
        }
        composable("my_invoices") {
            PaymentScreen(navController = navController, sessionManager = sessionManager)
        }
        composable("maintenance") {
            MaintenanceScreen(navController = navController, sessionManager = sessionManager)
        }
        composable(
            "upload_slip/{invoiceId}/{amount}",
            arguments = listOf(
                navArgument("invoiceId") { type = NavType.IntType },
                navArgument("amount") { type = NavType.FloatType }
            )
        ) { backStackEntry ->
            val invoiceId = backStackEntry.arguments?.getInt("invoiceId") ?: 0
            val amount = backStackEntry.arguments?.getFloat("amount")?.toDouble() ?: 0.0
            UploadSlipScreen(
                invoiceId = invoiceId,
                amount = amount,
                navController = navController,
                sessionManager = sessionManager
            )
        }

        // Admin Routes
        composable("admin_dashboard") {
            AdminDashboardScreen(navController = navController, sessionManager = sessionManager)
        }
        composable("manage_rooms") {
            ManageRoomsScreen(navController = navController)
        }
        composable("manage_payments") {
            ManagePaymentsScreen(navController = navController)
        }
        composable("admin_maintenance") {
            AdminMaintenanceScreen(navController = navController)
        }
        composable("admin_announcements") {
            AdminAnnouncementScreen(navController = navController, sessionManager = sessionManager)
        }
        composable(
            "room_detail/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.IntType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getInt("roomId") ?: 0
            AdminRoomDetailScreen(roomId = roomId, navController = navController)
        }
    }
}
