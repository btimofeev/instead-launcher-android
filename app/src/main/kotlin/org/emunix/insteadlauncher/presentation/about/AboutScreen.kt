/*
 * Copyright (c) 2025-2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.about

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import org.emunix.insteadlauncher.BuildConfig
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.compose.LayoutType
import org.emunix.insteadlauncher.presentation.compose.parseLinks
import org.emunix.insteadlauncher.presentation.compose.rememberLayoutType
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@Composable
fun AboutScreen(
    onBackClick: () -> Unit
) {
    val viewModel: AboutViewModel = hiltViewModel()
    val appVersion by viewModel.appVersion.collectAsState()

    AboutScreenContent(
        insteadVersion = BuildConfig.INSTEAD_VERSION,
        insteadLauncherVersion = appVersion,
        onBackClick = onBackClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreenContent(
    insteadVersion: String,
    insteadLauncherVersion: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.about_activity_title))
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick.invoke() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
            )
        }
    ) { innerPadding ->
        val layoutType = rememberLayoutType()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter,
        ) {
            val isLandscape =
                layoutType is LayoutType.PhoneLandscape ||
                layoutType is LayoutType.TabletLandscape
            if (isLandscape) {
                AboutScreenLandscape(
                    layoutType = layoutType,
                    insteadVersion = insteadVersion,
                    insteadLauncherVersion = insteadLauncherVersion
                )
            } else {
                AboutScreenPortrait(insteadVersion, insteadLauncherVersion)
            }
        }
    }
}

@Composable
private fun AboutScreenPortrait(
    insteadVersion: String,
    insteadLauncherVersion: String
) {
    Column(
        modifier = Modifier.widthIn(max = 500.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(R.drawable.boy_and_cat),
            contentDescription = null,
            modifier = Modifier.size(300.dp),
            contentScale = ContentScale.Fit,
        )
        AboutScreenTextBlocks(insteadVersion, insteadLauncherVersion)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AboutScreenLandscape(
    layoutType: LayoutType,
    insteadVersion: String,
    insteadLauncherVersion: String
) {
    val isTablet = layoutType is LayoutType.TabletLandscape
    Row(
        modifier = Modifier
            .widthIn(max = if (isTablet) 900.dp else 700.dp)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(R.drawable.boy_and_cat),
            contentDescription = null,
            modifier = Modifier
                .weight(1f)
                .size(if (isTablet) 300.dp else 220.dp)
                .padding(top = 8.dp),
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AboutScreenTextBlocks(insteadVersion, insteadLauncherVersion)
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AboutScreenTextBlocks(
    insteadVersion: String,
    insteadLauncherVersion: String
) {
    TextBlock(stringResource(R.string.about_activity_about_instead, insteadVersion))
    TextBlock(
        stringResource(
            R.string.about_activity_about_instead_launcher,
            insteadLauncherVersion
        )
    )
    TextBlock(stringResource(R.string.about_activity_thanks))
    TextBlock(stringResource(R.string.about_activity_to_game_authors))
    TextBlock(stringResource(R.string.about_activity_discuss))
}

@Composable
private fun TextBlock(
    text: String,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val annotatedString = remember(text) { parseLinks(text, linkColor) }
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

@Preview(
    showBackground = true,
    widthDp = 400,
    uiMode = UI_MODE_NIGHT_YES,
    name = "Dark"
)
@Preview(showBackground = true, widthDp = 400)
@PreviewScreenSizes
@Composable
private fun AboutScreenContentPreview() {
    InsteadLauncherTheme {
        AboutScreenContent(
            insteadVersion = "3.5",
            insteadLauncherVersion = "0.9.1",
            onBackClick = {},
        )
    }
}