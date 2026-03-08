/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import org.emunix.insteadlauncher.presentation.about.AboutScreen
import org.emunix.insteadlauncher.presentation.game.GameInfoScreen
import org.emunix.insteadlauncher.presentation.installedgames.InstalledGamesScreen
import org.emunix.insteadlauncher.presentation.launcher.AppArgumentViewModel
import org.emunix.insteadlauncher.presentation.repository.RepositoryScreen
import org.emunix.insteadlauncher.presentation.search.SearchScreen
import org.emunix.insteadlauncher.presentation.settings.SettingsScreen
import org.emunix.insteadlauncher.presentation.unpackresources.UnpackResourcesScreen

private const val SLIDE_DURATION = 400

@Composable
fun AppNavGraph(appArgumentViewModel: AppArgumentViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppScreen.UnpackResourcesScreen,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(SLIDE_DURATION)
            )
        },
        exitTransition = {
            ExitTransition.None
        },
        popEnterTransition = {
            EnterTransition.None
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(SLIDE_DURATION)
            )
        }
    ) {
        composable<AppScreen.UnpackResourcesScreen> {
            UnpackResourcesScreen(
                navigateToInstalledGamesScreen = {
                    navController.navigate(AppScreen.InstalledGamesScreen) {
                        popUpTo(AppScreen.UnpackResourcesScreen) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable<AppScreen.InstalledGamesScreen> {
            val uri = appArgumentViewModel.zipUri
            LaunchedEffect(uri) {
                if (uri != null) {
                    navController.navigate(AppScreen.RepositoryScreen)
                }
            }
            InstalledGamesScreen(
                navigateToGameInfoScreen = { gameName ->
                    navController.navigate(AppScreen.GameInfoScreen(gameName))
                },
                navigateToSettingsScreen = { navController.navigate(AppScreen.SettingsScreen)},
                navigateToAboutAppScreen = { navController.navigate(AppScreen.AboutAppScreen) },
                navigateToRepositoryScreen = { navController.navigate(AppScreen.RepositoryScreen)}
            )
        }

        composable<AppScreen.SettingsScreen> {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<AppScreen.AboutAppScreen> {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<AppScreen.RepositoryScreen> {
            val uri = appArgumentViewModel.zipUri
            appArgumentViewModel.zipUri = null
            RepositoryScreen(
                zipUriFromAppArgument = uri,
                onBackClick = { navController.popBackStack() },
                onSearchClick = { navController.navigate(AppScreen.SearchScreen) },
                onGameClick = { gameName ->
                    navController.navigate(AppScreen.GameInfoScreen(gameName))
                },
            )
        }

        composable<AppScreen.SearchScreen> {
            SearchScreen(
                onBackClick = { navController.popBackStack() },
                onGameClick = { gameName ->
                    navController.navigate(AppScreen.GameInfoScreen(gameName))
                },
            )
        }

        composable<AppScreen.GameInfoScreen>(
            deepLinks = listOf(
                navDeepLink<AppScreen.GameInfoScreen>(basePath = GAME_INFO_SCREEN_DEEPLINK)
            )
        ) { backStackEntry ->
            val route = backStackEntry.toRoute<AppScreen.GameInfoScreen>()
            GameInfoScreen(
                gameName = route.gameName,
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}