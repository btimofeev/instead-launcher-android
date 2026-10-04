/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme as AppMaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.darkColorScheme
import org.emunix.insteadlauncher.presentation.about.AboutScreen
import org.emunix.insteadlauncher.presentation.launcher.AppArgumentViewModel
import org.emunix.insteadlauncher.presentation.unpackresources.UnpackResourcesScreen

@Composable
fun TvNavGraph(appArgumentViewModel: AppArgumentViewModel) {
    val navController = rememberNavController()
    val appColors = AppMaterialTheme.colorScheme

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = appColors.primary,
            onPrimary = appColors.onPrimary,
            background = appColors.background,
            onBackground = appColors.onBackground,
            surface = appColors.surface,
            onSurface = appColors.onSurface,
            surfaceVariant = appColors.surfaceVariant,
            onSurfaceVariant = appColors.onSurfaceVariant,
            secondary = appColors.secondary,
            onSecondary = appColors.onSecondary,
            tertiary = appColors.tertiary,
            onTertiary = appColors.onTertiary,
            error = appColors.error,
            onError = appColors.onError,
        ),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            colors = SurfaceDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
            ),
        ) {
            NavHost(
                navController = navController,
                startDestination = TvScreen.UnpackResourcesScreen,
            ) {
                composable<TvScreen.UnpackResourcesScreen> {
                    UnpackResourcesScreen(
                        navigateToInstalledGamesScreen = {
                            navController.navigate(TvScreen.GamesScreen) {
                                launchSingleTop = true
                                popUpTo(TvScreen.UnpackResourcesScreen) {
                                    inclusive = true
                                }
                            }
                        }
                    )
                }

                composable<TvScreen.GamesScreen> {
                    val uri = appArgumentViewModel.zipUri
                    appArgumentViewModel.zipUri = null
                    TvGamesScreen(
                        zipUri = uri,
                        onGameClick = { gameName ->
                            navController.navigate(TvScreen.GameInfoScreen(gameName)) { launchSingleTop = true }
                        },
                        onSearchClick = { navController.navigate(TvScreen.SearchScreen) { launchSingleTop = true } },
                        onSettingsClick = { navController.navigate(TvScreen.SettingsScreen) { launchSingleTop = true } },
                    )
                }

                composable<TvScreen.GameInfoScreen> { backStackEntry ->
                    val route = backStackEntry.toRoute<TvScreen.GameInfoScreen>()
                    TvGameInfoScreen(
                        gameName = route.gameName,
                        onBackClick = { navController.popBackStack() },
                    )
                }

                composable<TvScreen.SearchScreen> {
                    TvSearchScreen(
                        onBackClick = { navController.popBackStack() },
                        onGameClick = { gameName ->
                            navController.navigate(TvScreen.GameInfoScreen(gameName)) { launchSingleTop = true }
                        },
                    )
                }

                composable<TvScreen.SettingsScreen> {
                    TvSettingsScreen(
                        onBackClick = { navController.popBackStack() },
                        onAboutClick = { navController.navigate(TvScreen.AboutScreen) { launchSingleTop = true } },
                    )
                }

                composable<TvScreen.AboutScreen> {
                    AboutScreen(
                        onBackClick = { navController.popBackStack() },
                        showTopBar = false,
                    )
                }
            }
        }
    }
}
