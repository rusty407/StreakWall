package com.console.streakwall.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.console.streakwall.StreakWallApplication
import com.console.streakwall.ui.ViewModelFactory
import com.console.streakwall.ui.home.HomeScreen
import com.console.streakwall.ui.home.HomeViewModel
import com.console.streakwall.ui.onboarding.OnboardingScreen
import com.console.streakwall.ui.onboarding.OnboardingViewModel
import com.console.streakwall.ui.privacy.PrivacyPolicyScreen
import com.console.streakwall.ui.settings.SettingsScreen
import com.console.streakwall.ui.settings.SettingsViewModel
import kotlinx.coroutines.flow.first

private object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SETTINGS = "settings"
    const val PRIVACY_POLICY = "privacy"
}

@Composable
fun StreakWallApp(application: StreakWallApplication) {
    val repository = application.repository

    // Decided exactly once, from a one-shot read of persisted state — not from a live
    // collection of preferencesFlow. NavHost's startDestination is only honored on its
    // first composition; if this were re-derived on every preferences change (e.g. right
    // after onboarding writes isOnboarded=true), it would race the explicit
    // navController.navigate("home") call below and could crash NavHost.
    var startDestination by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        val initial = repository.preferencesFlow.first()
        startDestination = if (initial.isOnboarded) Routes.HOME else Routes.ONBOARDING
    }

    val resolvedStart = startDestination
    if (resolvedStart == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = resolvedStart) {
        composable(Routes.ONBOARDING) {
            val viewModel: OnboardingViewModel = viewModel(
                factory = ViewModelFactory(application, repository) { ctx, repo -> OnboardingViewModel(ctx, repo) }
            )
            OnboardingScreen(
                viewModel = viewModel,
                onOnboardingComplete = { navigateToHomeClearingBackStack(navController) }
            )
        }
        composable(Routes.HOME) {
            val viewModel: HomeViewModel = viewModel(
                factory = ViewModelFactory(application, repository) { ctx, repo -> HomeViewModel(ctx, repo) }
            )
            HomeScreen(
                viewModel = viewModel,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(Routes.SETTINGS) {
            val viewModel: SettingsViewModel = viewModel(
                factory = ViewModelFactory(application, repository) { ctx, repo -> SettingsViewModel(ctx, repo) }
            )
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpenPrivacyPolicy = { navController.navigate(Routes.PRIVACY_POLICY) }
            )
        }
        composable(Routes.PRIVACY_POLICY) {
            PrivacyPolicyScreen(onBack = { navController.popBackStack() })
        }
    }
}

private fun navigateToHomeClearingBackStack(navController: NavHostController) {
    navController.navigate(Routes.HOME) {
        popUpTo(Routes.ONBOARDING) { inclusive = true }
    }
}
