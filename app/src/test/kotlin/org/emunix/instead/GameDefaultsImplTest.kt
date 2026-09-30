/*
 * Copyright (c) 2026 Boris Timofeev <btimofeev@emunix.org>
 * Distributed under the MIT License (license terms are at http://opensource.org/licenses/MIT).
 */

package org.emunix.instead

import android.app.UiModeManager
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.content.res.Resources
import io.mockk.every
import io.mockk.mockk
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.DEFAULT_INSTEAD_TEXT_SIZE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.INSTEAD_THEME_DEFAULT
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.INSTEAD_THEME_MOBILE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.INSTEAD_THEME_WIDE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.LARGE_SCREEN_INSTEAD_TEXT_SIZE
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_DEFAULT_THEME_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProvider.Companion.PREF_TEXT_SIZE_KEY
import org.emunix.instead.core_preferences.preferences_provider.PreferencesProviderImpl
import org.emunix.instead.core_storage_api.data.Storage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path

class GameDefaultsImplTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var context: Context
    private lateinit var configuration: Configuration
    private lateinit var resources: Resources
    private var uiModeManager: UiModeManager? = null
    private lateinit var preferences: SharedPreferences
    private lateinit var preferencesProvider: PreferencesProvider
    private lateinit var storage: Storage
    private lateinit var themesDir: File
    private lateinit var userThemesDir: File

    @BeforeEach
    fun setUp() {
        themesDir = File(tempDir.toFile(), "themes")
        userThemesDir = File(tempDir.toFile(), "user-themes")
        installTheme(themesDir, INSTEAD_THEME_MOBILE)
        installTheme(themesDir, INSTEAD_THEME_WIDE)

        preferences = mockPreferences()
        preferencesProvider = PreferencesProviderImpl(preferences)

        storage = mockk()
        every { storage.getThemesDirectory() } returns themesDir
        every { storage.getUserThemesDirectory() } returns userThemesDir

        configuration = mockk()
        configuration.smallestScreenWidthDp = PHONE_SMALLEST_WIDTH_DP
        resources = mockk()
        every { resources.configuration } returns configuration
        uiModeManager = null
        context = mockk()
        every { context.resources } returns resources
        every { context.getSystemService(Context.UI_MODE_SERVICE) } answers { uiModeManager }
    }

    @Test
    fun `picks the mobile theme and stores it on first run on a phone`() {
        assertEquals(INSTEAD_THEME_MOBILE, defaults().resolveTheme())
        assertEquals(INSTEAD_THEME_MOBILE, preferences.getString(PREF_DEFAULT_THEME_KEY, null))
    }

    @Test
    fun `picks the wide theme and stores it on first run on a tablet`() {
        setSmallestWidthDp(TABLET_SMALLEST_WIDTH_DP)

        assertEquals(INSTEAD_THEME_WIDE, defaults().resolveTheme())
        assertEquals(INSTEAD_THEME_WIDE, preferences.getString(PREF_DEFAULT_THEME_KEY, null))
    }

    @Test
    fun `picks the wide theme on a television`() {
        setUiModeType(Configuration.UI_MODE_TYPE_TELEVISION)

        assertEquals(INSTEAD_THEME_WIDE, defaults().resolveTheme())
    }

    @Test
    fun `picks the wide theme on an appliance`() {
        setUiModeType(Configuration.UI_MODE_TYPE_APPLIANCE)

        assertEquals(INSTEAD_THEME_WIDE, defaults().resolveTheme())
        assertEquals(LARGE_SCREEN_INSTEAD_TEXT_SIZE, defaults().resolveTextScale())
    }

    @Test
    fun `treats a watch, a headset, a desk and a car as small screens`() {
        listOf(
            Configuration.UI_MODE_TYPE_WATCH,
            Configuration.UI_MODE_TYPE_VR_HEADSET,
            Configuration.UI_MODE_TYPE_DESK,
            Configuration.UI_MODE_TYPE_CAR,
        ).forEach { uiModeType ->
            setUiModeType(uiModeType)

            assertEquals(INSTEAD_THEME_MOBILE, defaults().resolveTheme(), "uiModeType $uiModeType")
            assertEquals(DEFAULT_INSTEAD_TEXT_SIZE, defaults().resolveTextScale(), "uiModeType $uiModeType")
        }
    }

    @Test
    fun `the device type alone is enough on a television with a narrow screen`() {
        setUiModeType(Configuration.UI_MODE_TYPE_TELEVISION)
        setSmallestWidthDp(NARROW_SMALLEST_WIDTH_DP)

        assertEquals(INSTEAD_THEME_WIDE, defaults().resolveTheme())
        assertEquals(LARGE_SCREEN_INSTEAD_TEXT_SIZE, defaults().resolveTextScale())
    }

    @Test
    fun `picks the mobile theme in the normal ui mode`() {
        setUiModeType(Configuration.UI_MODE_TYPE_NORMAL)

        assertEquals(INSTEAD_THEME_MOBILE, defaults().resolveTheme())
    }

    @Test
    fun `keeps a theme the user picked`() {
        preferencesProvider.defaultInsteadTheme = INSTEAD_THEME_WIDE
        setSmallestWidthDp(PHONE_SMALLEST_WIDTH_DP)

        assertEquals(INSTEAD_THEME_WIDE, defaults().resolveTheme())
        assertEquals(INSTEAD_THEME_WIDE, preferences.getString(PREF_DEFAULT_THEME_KEY, null))
    }

    @Test
    fun `falls back to the other theme when the preferred one is not installed`() {
        File(themesDir, INSTEAD_THEME_WIDE).deleteRecursively()
        setSmallestWidthDp(TABLET_SMALLEST_WIDTH_DP)

        assertEquals(INSTEAD_THEME_MOBILE, defaults().resolveTheme())
    }

    @Test
    fun `falls back to the default theme when neither theme is installed`() {
        File(themesDir, INSTEAD_THEME_WIDE).deleteRecursively()
        File(themesDir, INSTEAD_THEME_MOBILE).deleteRecursively()
        setSmallestWidthDp(TABLET_SMALLEST_WIDTH_DP)

        assertEquals(INSTEAD_THEME_DEFAULT, defaults().resolveTheme())
    }

    @Test
    fun `a theme directory without theme ini does not count as installed`() {
        File(themesDir, INSTEAD_THEME_MOBILE).deleteRecursively()
        File(themesDir, INSTEAD_THEME_MOBILE).mkdirs()

        assertEquals(INSTEAD_THEME_WIDE, defaults().resolveTheme())
    }

    @Test
    fun `picks a theme that is only installed in the user themes dir`() {
        File(themesDir, INSTEAD_THEME_WIDE).deleteRecursively()
        installTheme(userThemesDir, INSTEAD_THEME_WIDE)
        setSmallestWidthDp(TABLET_SMALLEST_WIDTH_DP)

        assertEquals(INSTEAD_THEME_WIDE, defaults().resolveTheme())
    }

    @Test
    fun `keeps the enlarged text size a phone needs`() {
        assertEquals(DEFAULT_INSTEAD_TEXT_SIZE, defaults().resolveTextScale())
        assertEquals(DEFAULT_INSTEAD_TEXT_SIZE, preferences.getString(PREF_TEXT_SIZE_KEY, null))
    }

    @Test
    fun `picks the plain text size on a tablet and stores it`() {
        setSmallestWidthDp(TABLET_SMALLEST_WIDTH_DP)

        assertEquals(LARGE_SCREEN_INSTEAD_TEXT_SIZE, defaults().resolveTextScale())
        assertEquals(LARGE_SCREEN_INSTEAD_TEXT_SIZE, preferences.getString(PREF_TEXT_SIZE_KEY, null))
    }

    @Test
    fun `picks the plain text size on a television`() {
        setUiModeType(Configuration.UI_MODE_TYPE_TELEVISION)

        assertEquals(LARGE_SCREEN_INSTEAD_TEXT_SIZE, defaults().resolveTextScale())
    }

    @Test
    fun `keeps a text size the user picked`() {
        preferencesProvider.defaultInsteadTextSize = DEFAULT_INSTEAD_TEXT_SIZE
        setSmallestWidthDp(TABLET_SMALLEST_WIDTH_DP)

        assertEquals(DEFAULT_INSTEAD_TEXT_SIZE, defaults().resolveTextScale())
        assertEquals(DEFAULT_INSTEAD_TEXT_SIZE, preferences.getString(PREF_TEXT_SIZE_KEY, null))
    }

    @Test
    fun `picking the theme does not mark the text size as set`() {
        setSmallestWidthDp(TABLET_SMALLEST_WIDTH_DP)

        defaults().resolveTheme()

        assertEquals(LARGE_SCREEN_INSTEAD_TEXT_SIZE, defaults().resolveTextScale())
    }

    @Test
    fun `the plain preference getters report the resolved defaults, not the static ones`() {
        setSmallestWidthDp(TABLET_SMALLEST_WIDTH_DP)

        defaults().resolveTheme()
        defaults().resolveTextScale()

        assertEquals(INSTEAD_THEME_WIDE, preferencesProvider.defaultInsteadTheme)
        assertEquals(LARGE_SCREEN_INSTEAD_TEXT_SIZE, preferencesProvider.defaultInsteadTextSize)
    }

    @Test
    fun `resolving is idempotent`() {
        setSmallestWidthDp(TABLET_SMALLEST_WIDTH_DP)

        assertEquals(defaults().resolveTheme(), defaults().resolveTheme())
        assertEquals(defaults().resolveTextScale(), defaults().resolveTextScale())
    }

    private fun defaults() = GameDefaultsImpl(context, preferencesProvider, storage)

    private fun setUiModeType(type: Int) {
        uiModeManager = mockk<UiModeManager>().also { every { it.currentModeType } returns type }
    }

    private fun setSmallestWidthDp(value: Int) {
        configuration.smallestScreenWidthDp = value
    }

    private fun installTheme(dir: File, name: String) {
        File(dir, name).mkdirs()
        File(File(dir, name), "theme.ini").writeText("scr.w = 800\nscr.h = 600\n")
    }

    private fun mockPreferences(): SharedPreferences {
        val values = mutableMapOf<String, String>()
        return mockk<SharedPreferences>().also { preferences ->
            every { preferences.getString(any(), any()) } answers {
                values[firstArg()] ?: secondArg()
            }
            every { preferences.contains(any()) } answers { values.containsKey(firstArg()) }
            every { preferences.edit() } answers {
                mockk<SharedPreferences.Editor>().also { editor ->
                    every { editor.putString(any(), any()) } answers {
                        values[firstArg()] = secondArg()
                        editor
                    }
                    every { editor.apply() } returns Unit
                }
            }
        }
    }

    companion object {
        private const val PHONE_SMALLEST_WIDTH_DP = 411
        private const val TABLET_SMALLEST_WIDTH_DP = 800
        private const val NARROW_SMALLEST_WIDTH_DP = 480
    }
}
