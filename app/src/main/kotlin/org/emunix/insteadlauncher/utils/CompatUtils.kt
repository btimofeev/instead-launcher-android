/*
 * Copyright (c) 2024 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.insteadlauncher.utils

import android.app.Notification
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.work.ForegroundInfo

internal fun createForegroundInfoCompat(
    notificationId: Int,
    notification: Notification,
    foregroundServiceType: Int,
): ForegroundInfo =
    if (VERSION.SDK_INT >= VERSION_CODES.Q) {
        ForegroundInfo(
            notificationId,
            notification,
            foregroundServiceType
        )
    } else {
        ForegroundInfo(
            notificationId,
            notification
        )
    }