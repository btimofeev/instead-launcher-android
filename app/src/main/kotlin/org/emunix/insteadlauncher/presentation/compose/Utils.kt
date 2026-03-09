/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.presentation.compose

import android.util.Patterns
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

fun parseLinks(text: String, linkColor: Color): AnnotatedString =
    buildAnnotatedString {
        append(text)
        val matcher = Patterns.WEB_URL.matcher(text)

        while (matcher.find()) {
            val url = matcher.group()

            addLink(
                url = LinkAnnotation.Url(
                    url = url,
                    styles = TextLinkStyles(
                        style = SpanStyle(
                            color = linkColor,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                ),
                start = matcher.start(),
                end = matcher.end()
            )
        }
    }

@Composable
fun rememberListImageModifier(): Modifier {
    val layoutType = rememberLayoutType()
    return remember(layoutType) {
        when (layoutType) {
            LayoutType.PhoneLandscape -> Modifier
                .padding(top = 12.dp, bottom = 12.dp, start = 12.dp)
                .width(100.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(12.dp))

            LayoutType.PhonePortrait -> Modifier
                .padding(vertical = 12.dp)
                .width(100.dp)
                .height(64.dp)
                .clip(RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp))

            LayoutType.TabletLandscape -> Modifier
                .padding(20.dp)
                .width(200.dp)
                .height(128.dp)
                .clip(RoundedCornerShape(24.dp))

            LayoutType.TabletPortrait -> Modifier
                .padding(16.dp)
                .width(150.dp)
                .height(96.dp)
                .clip(RoundedCornerShape(24.dp))
        }
    }
}