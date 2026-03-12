package com.nbks.mi.ui.theme

import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────
// Mi アプリ カラーパレット
// ─────────────────────────────────────────────

// プライマリ (深いインディゴ/パープル)
object MiColors {
    // ダークテーマ
    val Primary = Color(0xFF9C82F0)
    val OnPrimary = Color(0xFF1A0A5E)
    val PrimaryContainer = Color(0xFF2D1F7A)
    val OnPrimaryContainer = Color(0xFFE4DCFF)

    val Secondary = Color(0xFF62D0C0)
    val OnSecondary = Color(0xFF003731)
    val SecondaryContainer = Color(0xFF004E47)
    val OnSecondaryContainer = Color(0xFF7EEDE0)

    val Tertiary = Color(0xFFFFB86C)
    val OnTertiary = Color(0xFF3B1E00)
    val TertiaryContainer = Color(0xFF542D00)
    val OnTertiaryContainer = Color(0xFFFFDCC0)

    val Error = Color(0xFFCF6679)
    val OnError = Color(0xFF5C0A21)
    val ErrorContainer = Color(0xFF7D1530)
    val OnErrorContainer = Color(0xFFFFB3BB)

    val Background = Color(0xFF0F0F17)
    val OnBackground = Color(0xFFE5E1F0)
    val Surface = Color(0xFF18182A)
    val OnSurface = Color(0xFFE5E1F0)
    val SurfaceVariant = Color(0xFF272742)
    val OnSurfaceVariant = Color(0xFFC8C3DC)
    val SurfaceTint = Color(0xFF9C82F0)
    val Outline = Color(0xFF5E5A74)
    val OutlineVariant = Color(0xFF3A364F)
    val Scrim = Color(0xFF000000)
    val InverseSurface = Color(0xFFE5E1F0)
    val InverseOnSurface = Color(0xFF2E2B3D)
    val InversePrimary = Color(0xFF5B3FC2)

    // ライトテーマ
    val PrimaryLight = Color(0xFF5B3FC2)
    val OnPrimaryLight = Color(0xFFFFFFFF)
    val PrimaryContainerLight = Color(0xFFE4DCFF)
    val OnPrimaryContainerLight = Color(0xFF1A004E)

    val SecondaryLight = Color(0xFF006962)
    val OnSecondaryLight = Color(0xFFFFFFFF)
    val SecondaryContainerLight = Color(0xFF7EEDE0)
    val OnSecondaryContainerLight = Color(0xFF00201D)

    val TertiaryLight = Color(0xFF874400)
    val OnTertiaryLight = Color(0xFFFFFFFF)
    val TertiaryContainerLight = Color(0xFFFFDCC0)
    val OnTertiaryContainerLight = Color(0xFF2C1400)

    val BackgroundLight = Color(0xFFFBF8FF)
    val OnBackgroundLight = Color(0xFF1C1A27)
    val SurfaceLight = Color(0xFFFBF8FF)
    val OnSurfaceLight = Color(0xFF1C1A27)
    val SurfaceVariantLight = Color(0xFFE7E0F4)
    val OnSurfaceVariantLight = Color(0xFF4A4561)
    val OutlineLight = Color(0xFF7B7593)
    val OutlineVariantLight = Color(0xFFCBC4DF)

    // ウィジェット背景色
    val WidgetBgDark = Color(0xCC1E1E30)
    val WidgetBgLight = Color(0xCCF3F0FF)
    val WidgetBorderDark = Color(0x335E5A74)
    val WidgetBorderLight = Color(0x33635B78)

    // メモカラー
    val MemoColors = listOf(
        Color(0xFF2D2D4A), // デフォルト (ダーク)
        Color(0xFF2A3D2A), // グリーン
        Color(0xFF3D2A2A), // レッド
        Color(0xFF2A2A3D), // ブルー
        Color(0xFF3D3A2A), // イエロー
        Color(0xFF2A3D3D), // ティール
        Color(0xFF3D2A3D), // パープル
    )
    val MemoColorsLight = listOf(
        Color(0xFFF1EFFC),
        Color(0xFFE6F4E6),
        Color(0xFFF9EAEA),
        Color(0xFFEAEAF9),
        Color(0xFFF9F5E6),
        Color(0xFFE6F4F4),
        Color(0xFFF4E6F4),
    )
    val MemoColorLabels = listOf("デフォルト", "グリーン", "レッド", "ブルー", "イエロー", "ティール", "パープル")

    // グラデーション
    val GradientStart = Color(0xFF1A0A5E)
    val GradientEnd = Color(0xFF0F0F17)
}