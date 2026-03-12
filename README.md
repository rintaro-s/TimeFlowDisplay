# Mi Dashboard

A Jetpack Compose tablet dashboard that turns an Android device into a customizable productivity hub. Drag-and-drop widgets, keep critical info visible, and wire the UI to your local AI or automations.

## Highlights (English)

### Overview
- **Purpose**: Provide a flexible home dashboard for desks or wall-mounted tablets, mixing personal data, timers, notes, and automations in one always-on screen.
- **Main Screens**: Dashboard (widget canvas) and Settings (theme, AI, schedule control).

### Key Features
1. **Fully editable widget board**
   - Drag, resize, reorder via edit mode; wallpaper dimming and opacity controls keep widgets legible.
   - Preserves layouts in Room, so placements survive restarts.
2. **Widget catalog**
   - Clock (analog/digital), Calendar, Memo board with editor, Timer & daily alarms (with presets), Progress bar, Daily task checklist, AI chat (LMStudio backend), Webhook buttons, Image frame.
3. **Productivity workflows**
   - Memo editor dialog with pinning, quick daily task toggles, timer presets + silent mode, daily alarm countdown view.
4. **Automation & integrations**
   - AI widget talks to a self-hosted LMStudio server via Retrofit.
   - Webhook widget lets you send prepared payloads (e.g., Discord) with one tap.
   - WorkManager + scheduling dimmer to control brightness by time slots.
5. **Personalization**
   - Dynamic or custom themes, wallpaper picker with dim slider, keep-screen-on toggle, widget opacity/blur options.

### Architecture & Tech Stack
- **UI**: Jetpack Compose, Navigation Compose, Material 3, Coil for wallpaper/image previews.
- **State**: Kotlin coroutines + StateFlow, AndroidX ViewModel.
- **DI**: Hilt (including worker / navigation integrations).
- **Persistence**: Room database (memos, widgets, timers, AI logs, tasks, webhook buttons) + Preferences DataStore for settings.
- **Background**: WorkManager for scheduled dimming / alarms.
- **Networking**: Retrofit + OkHttp + Gson for LMStudio / webhook calls.

### Project Layout
- `app/src/main/java/com/nbks/mi/ui`: Composables, widgets, navigation, screens.
- `app/src/main/java/com/nbks/mi/ui/viewmodel`: ViewModels coordinating repositories and settings.
- `app/src/main/java/com/nbks/mi/data`: Room database, DAOs, repositories, DataStore wrappers.
- `app/src/main/java/com/nbks/mi/domain/model`: Domain models for widgets, tasks, settings, etc.
- `app/src/main/java/com/nbks/mi/di`: Hilt modules wiring Room, repositories, and workers.

### Getting Started
1. **Prerequisites**: Android Studio Ladybug+ (AGP 8 / Kotlin 2.0 toolchain), Android SDK 26–36, LMStudio (optional) if you want the AI widget.
2. **Clone & sync**: Open the root folder in Android Studio and let Gradle sync using `settings.gradle.kts`.
3. **Run**: Select the `app` configuration and deploy to a device (tablet recommended). From CLI you can also run `./gradlew :app:assembleDebug`.
4. **Configure**: Open the Settings screen to toggle dark mode, set wallpaper, connect to LMStudio (`http://<local-ip>:1234`), or define screen dimming schedules.

### Notes
- LMStudio / webhook credentials are stored only on-device via DataStore; no remote services are bundled.
- Target SDK 36, minimum SDK 26; core library desugaring is enabled for Java 11 APIs.

## ハイライト（日本語）

### 概要
- **目的**: デスクや壁掛けタブレットを、メモ・タイマー・自動化をまとめた常時表示ダッシュボードとして使えるようにすること。
- **主要画面**: ダッシュボード（ウィジェットキャンバス）と設定画面（テーマ、AI、スケジュール制御）。

### 主な機能
1. **フル編集可能なウィジェットボード**
   - 編集モードでドラッグ移動・リサイズ・前面化が可能。壁紙のディム量やウィジェットの不透明度も調整できます。
   - レイアウトは Room に保存され、再起動後も維持されます。
2. **多彩なウィジェット**
   - 時計（アナログ/デジタル）、カレンダー、メモ、タイマー＆デイリーアラーム、進捗バー、日課チェックリスト、AIチャット（LMStudio 連携）、Webhook ボタン、画像フレーム。
3. **生産性ワークフロー**
   - メモ編集ダイアログ（ピン留め対応）、日課タスクのトグル、タイマープリセット＋サイレントモード、デイリーアラーム残り時間表示。
4. **自動化・連携**
   - AI ウィジェットは Retrofit を介してローカルの LMStudio サーバーと通信。
   - Webhook ウィジェットで Discord など任意の Webhook にワンタップ送信。
   - WorkManager とスケジュール機能で時間帯ごとの画面暗度を制御。
5. **パーソナライズ**
   - テーマ（ダイナミックカラー対応）、壁紙とディム調整、画面常時点灯、ウィジェットの透明度・ブラー設定など。

### アーキテクチャ / 技術スタック
- **UI**: Jetpack Compose、Navigation Compose、Material 3、壁紙プレビューには Coil を使用。
- **状態管理**: Kotlin Coroutines + StateFlow、AndroidX ViewModel。
- **DI**: Hilt（Navigation / Worker 連携含む）。
- **永続化**: Room（メモ・ウィジェット・タイマー・AIログ・タスク・Webhook ボタン）＋ Preferences DataStore。
- **バックグラウンド**: WorkManager による暗転スケジューラや通知処理。
- **ネットワーク**: Retrofit + OkHttp + Gson（LMStudio / Webhook 通信）。

### プロジェクト構成
- `app/src/main/java/com/nbks/mi/ui`: Compose コンポーネント、ウィジェット、ナビゲーション、画面。
- `app/src/main/java/com/nbks/mi/ui/viewmodel`: ViewModel 群。リポジトリと設定を束ねる層。
- `app/src/main/java/com/nbks/mi/data`: Room データベース、DAO、リポジトリ、DataStore。
- `app/src/main/java/com/nbks/mi/domain/model`: ウィジェット・タスク・設定などのドメインモデル。
- `app/src/main/java/com/nbks/mi/di`: Hilt Module。Room や各種リポジトリを提供。

### セットアップ手順
1. **前提**: Android Studio Ladybug 以降（AGP 8 / Kotlin 2.0 系）、Android SDK 26–36、AI ウィジェットを使う場合は LMStudio を起動してください。
2. **取得 & 同期**: ルートフォルダを Android Studio で開き、Gradle 同期を実施。
3. **ビルド**: `app` を実行するか、CLI から `./gradlew :app:assembleDebug` を実行。
4. **設定**: アプリ内の「設定」からダークモード、壁紙、LMStudio 接続 (`http://<ローカルIP>:1234`)、画面暗転スケジュールなどを調整。

### 補足
- LMStudio / Webhook のクレデンシャルは DataStore にのみ保存され、外部送信は行われません。
- Target SDK 36 / Min SDK 26。Java 11 API を使うため coreLibraryDesugaring を有効化しています。
