/*
 * Copyright (c) 2025 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.about

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.theme.InsteadLauncherTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    insteadVersion: String,
    insteadLauncherVersion: String,
    onBackClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
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
        Column(
            modifier = Modifier.padding(innerPadding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.boy_and_cat),
                contentDescription = null,
                modifier = Modifier.size(300.dp),
                contentScale = ContentScale.Fit,
            )
            TextBlock(stringResource(R.string.about_activity_about_instead, insteadVersion))
            TextBlock(stringResource(R.string.about_activity_about_instead_launcher, insteadLauncherVersion))
            TextBlock(stringResource(R.string.about_activity_thanks))
            TextBlock(stringResource(R.string.about_activity_to_game_authors))
            TextBlock(stringResource(R.string.about_activity_discuss))
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun TextBlock(
    text: String,
) {
    Text(
        text = text,
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
@Composable
private fun AboutScreenPreview() {
    InsteadLauncherTheme {
        AboutScreen(
            insteadVersion = "3.5",
            insteadLauncherVersion = "0.9.1",
            onBackClick = {},
        )
    }
}