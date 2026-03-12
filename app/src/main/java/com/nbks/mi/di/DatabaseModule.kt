package com.nbks.mi.di

import android.content.Context
import androidx.room.Room
import com.nbks.mi.data.local.db.MiDatabase
import com.nbks.mi.data.local.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MiDatabase =
        Room.databaseBuilder(context, MiDatabase::class.java, MiDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideMemoDao(db: MiDatabase): MemoDao = db.memoDao()
    @Provides fun provideWidgetConfigDao(db: MiDatabase): WidgetConfigDao = db.widgetConfigDao()
    @Provides fun provideTimerPresetDao(db: MiDatabase): TimerPresetDao = db.timerPresetDao()
    @Provides fun provideAiMessageDao(db: MiDatabase): AiMessageDao = db.aiMessageDao()
    @Provides fun provideDiscordNotificationDao(db: MiDatabase): DiscordNotificationDao = db.discordNotificationDao()
    @Provides fun provideScreenScheduleDao(db: MiDatabase): ScreenScheduleDao = db.screenScheduleDao()
    @Provides fun provideDailyTaskDao(db: MiDatabase): DailyTaskDao = db.dailyTaskDao()
    @Provides fun provideWebhookButtonDao(db: MiDatabase): WebhookButtonDao = db.webhookButtonDao()
}
