/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.tv

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardBorder
import androidx.tv.material3.CardColors
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Precision
import org.emunix.insteadlauncher.R
import org.emunix.insteadlauncher.presentation.theme.onSurfaceDark
import org.emunix.insteadlauncher.presentation.theme.surfaceContainerHighDark
import org.emunix.insteadlauncher.presentation.theme.surfaceContainerHighestDark

@Composable
internal fun tvCardColors(): CardColors = CardDefaults.colors(
    containerColor = surfaceContainerHighDark,
    contentColor = onSurfaceDark,
    pressedContainerColor = surfaceContainerHighestDark,
    pressedContentColor = onSurfaceDark,
    focusedContainerColor = surfaceContainerHighestDark,
    focusedContentColor = onSurfaceDark,
)

@Composable
internal fun tvCardFocusBorder(): CardBorder = CardDefaults.border(
    focusedBorder = Border(
        BorderStroke(width = 3.dp, color = MaterialTheme.colorScheme.primary),
    ),
)

internal fun gameImageRequest(context: Context, imageUrl: String): ImageRequest =
    ImageRequest.Builder(context)
        .data(imageUrl)
        .size(CARD_IMAGE_WIDTH_PX, CARD_IMAGE_HEIGHT_PX)
        .precision(Precision.INEXACT)
        .build()

@Composable
internal fun TvGameCard(
    item: TvGameItem,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
) {
    val context = LocalContext.current
    val imageRequest = remember(item.imageUrl) { gameImageRequest(context, item.imageUrl) }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (focusRequester != null) {
                    Modifier.focusRequester(focusRequester)
                } else {
                    Modifier
                },
            ),
        colors = tvCardColors(),
        scale = CardDefaults.scale(focusedScale = 1.02f, pressedScale = 1.02f),
        border = tvCardFocusBorder(),
    ) {
        Column {
            AsyncImage(
                model = imageRequest,
                placeholder = painterResource(R.drawable.walking_cat),
                error = painterResource(R.drawable.sleeping_cat),
                contentDescription = item.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
            Text(
                text = item.title,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

private const val CARD_IMAGE_WIDTH_PX = 512
private const val CARD_IMAGE_HEIGHT_PX = 288
