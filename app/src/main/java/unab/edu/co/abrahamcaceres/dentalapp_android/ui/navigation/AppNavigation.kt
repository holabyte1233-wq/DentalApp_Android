package unab.edu.co.abrahamcaceres.dentalapp_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import unab.edu.co.abrahamcaceres.dentalapp_android.data.defaultSimulationResult
import unab.edu.co.abrahamcaceres.dentalapp_android.data.mockPatients
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens.DashboardScreen
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens.LoginScreen
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens.NuevaSimulacionScreen
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens.PatientDetailsScreen
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens.ProcessingScreen
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.screens.ResultScreen

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object PatientDetails : Screen("patient/{patientId}") {
        fun create(patientId: String) = "patient/$patientId"
    }
    data object NuevaSimulacion : Screen("nueva_simulacion")
    data object Processing : Screen("processing")
    data object Result : Screen("result")
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLogin = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                },
                onPatientClick = { patientId ->
                    navController.navigate(Screen.PatientDetails.create(patientId))
                },
                onNewDesign = {
                    navController.navigate(Screen.NuevaSimulacion.route)
                }
            )
        }
        composable(Screen.PatientDetails.route) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            val patient = mockPatients.find { it.id == patientId }
            PatientDetailsScreen(
                patient = patient,
                onBack = { navController.popBackStack() },
                onNewSimulation = {
                    navController.navigate(Screen.NuevaSimulacion.route) {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(Screen.NuevaSimulacion.route) {
            val prevEntry = navController.previousBackStackEntry
            val patientId = prevEntry?.arguments?.getString("patientId")
            val patient = patientId?.let { id -> mockPatients.find { it.id == id } }
            NuevaSimulacionScreen(
                initialPatientName = patient?.name,
                initialAge = patient?.age?.toString(),
                onCancel = { navController.popBackStack() },
                onHoldComplete = {
                    navController.navigate(Screen.Processing.route) {
                        popUpTo(Screen.NuevaSimulacion.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Processing.route) {
            ProcessingScreen(
                onComplete = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Processing.route) { inclusive = true }
                    }
                }
            )
        }
        composable(Screen.Result.route) {
            ResultScreen(
                result = defaultSimulationResult,
                patientName = null,
                onBack = { navController.popBackStack() },
                onShare = { },
                onSaveResult = { navController.popBackStack(Screen.Dashboard.route, false) },
                onDiscard = { navController.popBackStack() }
            )
        }
    }
}
