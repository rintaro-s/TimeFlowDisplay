package com.nbks.mi.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.nbks.mi.domain.model.AppLanguage
import com.nbks.mi.domain.model.AppSettings
import com.nbks.mi.domain.model.ClockStyle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "mi_settings")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val dataStore = context.dataStore

    companion object Keys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val WALLPAPER_URI = stringPreferencesKey("wallpaper_uri")
        val WALLPAPER_DIM_ALPHA = floatPreferencesKey("wallpaper_dim_alpha")
        val LM_STUDIO_URL = stringPreferencesKey("lm_studio_url")
        val LM_STUDIO_MODEL = stringPreferencesKey("lm_studio_model")
        val LM_STUDIO_MAX_TOKENS = intPreferencesKey("lm_studio_max_tokens")
        val DISCORD_BOT_TOKEN = stringPreferencesKey("discord_bot_token")
        val DISCORD_GUILD_ID = stringPreferencesKey("discord_guild_id")
        val DISCORD_CHANNEL_IDS = stringPreferencesKey("discord_channel_ids")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SCHEDULE_SCREEN_DIM = booleanPreferencesKey("schedule_screen_dim")
        val PRIMARY_COLOR_SEED = longPreferencesKey("primary_color_seed")
        val WIDGET_OPACITY = floatPreferencesKey("widget_opacity")
    }

    val appSettings: Flow<AppSettings> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs ->
            AppSettings(
                isDarkMode = prefs[IS_DARK_MODE] ?: true,
                useDynamicColor = prefs[USE_DYNAMIC_COLOR] ?: false,
                appLanguage = prefs[APP_LANGUAGE]?.let {
                    runCatching { AppLanguage.valueOf(it) }.getOrDefault(AppLanguage.SYSTEM)
                } ?: AppLanguage.SYSTEM,
                clockStyle = prefs[CLOCK_STYLE]?.let {
                    runCatching { ClockStyle.valueOf(it) }.getOrDefault(ClockStyle.DIGITAL)
                } ?: ClockStyle.DIGITAL,
                wallpaperUri = prefs[WALLPAPER_URI] ?: "",
                wallpaperDimAlpha = prefs[WALLPAPER_DIM_ALPHA] ?: 0.3f,
                lmStudioBaseUrl = prefs[LM_STUDIO_URL] ?: "http://localhost:1234",
                lmStudioModel = prefs[LM_STUDIO_MODEL] ?: "local-model",
                lmStudioMaxTokens = prefs[LM_STUDIO_MAX_TOKENS] ?: 2048,
                discordBotToken = prefs[DISCORD_BOT_TOKEN] ?: "",
                discordGuildId = prefs[DISCORD_GUILD_ID] ?: "",
                discordChannelIds = prefs[DISCORD_CHANNEL_IDS] ?: "",
                keepScreenOn = prefs[KEEP_SCREEN_ON] ?: false,
                scheduleScreenDim = prefs[SCHEDULE_SCREEN_DIM] ?: false,
                primaryColorSeed = prefs[PRIMARY_COLOR_SEED] ?: 0xFF6650A4,
                widgetOpacity = prefs[WIDGET_OPACITY] ?: 0.85f,
            )
        }

    suspend fun updateSettings(block: suspend MutablePreferences.() -> Unit) {
        dataStore.edit { block(it) }
    }

    suspend fun setDarkMode(value: Boolean) = updateSettings { this[IS_DARK_MODE] = value }
    suspend fun setDynamicColor(value: Boolean) = updateSettings { this[USE_DYNAMIC_COLOR] = value }
    suspend fun setAppLanguage(value: AppLanguage) = updateSettings { this[APP_LANGUAGE] = value.name }
    suspend fun setClockStyle(value: ClockStyle) = updateSettings { this[CLOCK_STYLE] = value.name }
    suspend fun setWallpaperUri(uri: String) = updateSettings { this[WALLPAPER_URI] = uri }
    suspend fun setWallpaperDimAlpha(v: Float) = updateSettings { this[WALLPAPER_DIM_ALPHA] = v }
    suspend fun setLmStudioUrl(url: String) = updateSettings { this[LM_STUDIO_URL] = url }
    suspend fun setLmStudioModel(m: String) = updateSettings { this[LM_STUDIO_MODEL] = m }
    suspend fun setLmStudioMaxTokens(n: Int) = updateSettings { this[LM_STUDIO_MAX_TOKENS] = n }
    suspend fun setDiscordBotToken(token: String) = updateSettings { this[DISCORD_BOT_TOKEN] = token }
    suspend fun setDiscordGuildId(id: String) = updateSettings { this[DISCORD_GUILD_ID] = id }
    suspend fun setDiscordChannelIds(ids: String) = updateSettings { this[DISCORD_CHANNEL_IDS] = ids }
    suspend fun setKeepScreenOn(v: Boolean) = updateSettings { this[KEEP_SCREEN_ON] = v }
    suspend fun setScheduleScreenDim(v: Boolean) = updateSettings { this[SCHEDULE_SCREEN_DIM] = v }
    suspend fun setPrimaryColorSeed(seed: Long) = updateSettings { this[PRIMARY_COLOR_SEED] = seed }
    suspend fun setWidgetOpacity(v: Float) = updateSettings { this[WIDGET_OPACITY] = v }
}
