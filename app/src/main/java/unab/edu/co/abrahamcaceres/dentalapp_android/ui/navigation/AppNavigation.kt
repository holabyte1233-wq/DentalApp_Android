package unab.edu.co.abrahamcaceres.dentalapp_android.ui.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.simulation.SaveSimulationState
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.simulation.SimulationSharedViewModel
import unab.edu.co.abrahamcaceres.dentalapp_android.presentation.simulation.SimulationState
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
    data object SimulationFlow : Screen("simulation_flow")
    data object NuevaSimulacion : Screen("nueva_simulacion")
    data object Processing : Screen("processing")
    data object Result : Screen("result/{originalPhotoUrl}/{generatedPhotoUrl}") {
        fun create(originalPhotoUrl: String, generatedPhotoUrl: String): String {
            val encOriginal = java.net.URLEncoder.encode(originalPhotoUrl, Charsets.UTF_8.name())
            val encGenerated = java.net.URLEncoder.encode(generatedPhotoUrl, Charsets.UTF_8.name())
            return "result/$encOriginal/$encGenerated"
        }
    }
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
                    navController.navigate(Screen.SimulationFlow.route)
                }
            )
        }
        composable(
            route = Screen.PatientDetails.route,
            arguments = listOf(navArgument("patientId") { type = NavType.StringType })
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getString("patientId")
            PatientDetailsScreen(
                patientId = patientId,
                onBack = { navController.popBackStack() },
                onNewSimulation = {
                    navController.navigate(Screen.SimulationFlow.route) {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Nested navigation graph for the simulation flow
        navigation(
            startDestination = Screen.NuevaSimulacion.route,
            route = Screen.SimulationFlow.route
        ) {
            composable(Screen.NuevaSimulacion.route) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.SimulationFlow.route)
                }
                val sharedVm: SimulationSharedViewModel = hiltViewModel(parentEntry)

                NuevaSimulacionScreen(
                    isGeminiActive = sharedVm.isGeminiActive,
                    onCancel = { navController.popBackStack(Screen.SimulationFlow.route, true) },
                    onStartProcessing = { name, age, photoUri, manualDesc ->
                        sharedVm.setPatientData(name, age)
                        sharedVm.setPhoto(photoUri)
                        sharedVm.setManualDescription(manualDesc)
                        sharedVm.startProcessing()
                        navController.navigate(Screen.Processing.route)
                    }
                )
            }
            composable(Screen.Processing.route) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.SimulationFlow.route)
                }
                val sharedVm: SimulationSharedViewModel = hiltViewModel(parentEntry)
                val simState by sharedVm.state.collectAsState()

                LaunchedEffect(simState) {
                    when (simState) {
                        is SimulationState.Success -> {
                            val s = simState as SimulationState.Success
                            val route = Screen.Result.create(
                                s.result.beforeImageUrl,
                                s.result.afterImageUrl
                            )
                            navController.navigate(route) {
                                popUpTo(Screen.Processing.route) { inclusive = true }
                            }
                        }
                        is SimulationState.Error -> {
                            val route = Screen.Result.create("", "")
                            navController.navigate(route) {
                                popUpTo(Screen.Processing.route) { inclusive = true }
                            }
                        }
                        else -> {}
                    }
                }

                ProcessingScreen()
            }
            composable(
                route = Screen.Result.route,
                arguments = listOf(
                    navArgument("originalPhotoUrl") { type = NavType.StringType; defaultValue = "" },
                    navArgument("generatedPhotoUrl") { type = NavType.StringType; defaultValue = "" }
                )
            ) { entry ->
                val parentEntry = remember(entry) {
                    navController.getBackStackEntry(Screen.SimulationFlow.route)
                }
                val sharedVm: SimulationSharedViewModel = hiltViewModel(parentEntry)
                val simState by sharedVm.state.collectAsState()
                val saveState by sharedVm.saveState.collectAsState()
                val context = LocalContext.current

                val successState = simState as? SimulationState.Success
                val errorState = simState as? SimulationState.Error
                val originalUrl = entry.arguments?.getString("originalPhotoUrl").orEmpty()
                val generatedUrl = entry.arguments?.getString("generatedPhotoUrl").orEmpty()
                val decodedOriginal = try {
                    java.net.URLDecoder.decode(originalUrl, Charsets.UTF_8.name())
                } catch (_: Exception) { originalUrl }
                val decodedGenerated = try {
                    java.net.URLDecoder.decode(generatedUrl, Charsets.UTF_8.name())
                } catch (_: Exception) { generatedUrl }

                LaunchedEffect(saveState) {
                    when (saveState) {
                        is SaveSimulationState.Success -> {
                            delay(2000) // Show success message before navigating
                            sharedVm.clearSaveState()
                            sharedVm.reset()
                            navController.popBackStack(Screen.Dashboard.route, false)
                        }
                        else -> {}
                    }
                }

                ResultScreen(
                    originalPhotoUrl = decodedOriginal,
                    generatedPhotoUrl = decodedGenerated,
                    patientName = successState?.patientName,
                    treatmentName = successState?.result?.treatmentName ?: "Diseño de Sonrisa IA",
                    description = successState?.result?.description ?: "",
                    expectedDuration = successState?.result?.expectedDuration ?: "2-3 sesiones",
                    estimatedCost = successState?.result?.estimatedCost ?: "Consultar con el doctor",
                    errorMessage = errorState?.message,
                    isSaving = saveState is SaveSimulationState.Saving,
                    isSaveSuccess = saveState is SaveSimulationState.Success,
                    saveError = (saveState as? SaveSimulationState.Error)?.message,
                    onBack = {
                        sharedVm.reset()
                        navController.popBackStack(Screen.SimulationFlow.route, true)
                    },
                    onShare = { previewData ->
                        val shareText = buildString {
                            previewData.patientName?.let { append("Paciente: $it\n") }
                            append("Tratamiento: ${previewData.treatmentName}\n")
                            append("Descripción: ${previewData.description}\n")
                            append("Duración: ${previewData.expectedDuration}\n")
                            append("Coste: ${previewData.estimatedCost}")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Simulación Dental ${previewData.patientName?.let { "- $it" } ?: ""}")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Compartir vía"))
                    },
                    onSaveResult = { finalDescription ->
                        sharedVm.updateDescription(finalDescription)
                        sharedVm.saveSimulationResult(finalDescription)
                    },
                    onDiscard = {
                        sharedVm.reset()
                        navController.popBackStack(Screen.SimulationFlow.route, true)
                    },
                    onDismissSaveError = { sharedVm.clearSaveState() }
                )
            }
        }
    }
}
