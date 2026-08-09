package com.resurface.resurface.di

import com.resurface.resurface.data.alarm.AlarmScheduler
import com.resurface.resurface.data.alarm.AlarmSchedulerImpl
import com.resurface.resurface.data.notification.Notifier
import com.resurface.resurface.data.notification.NotifierImpl
import com.resurface.resurface.data.usage.UsageStatsReader
import com.resurface.resurface.data.usage.UsageStatsReaderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /** Liga a interface do reader ao impl real (G5). */
    @Binds
    @Singleton
    abstract fun bindUsageStatsReader(impl: UsageStatsReaderImpl): UsageStatsReader

    /** Liga o agendador de alarme ao impl real. */
    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(impl: AlarmSchedulerImpl): AlarmScheduler

    /** Liga o notifier ao impl real. */
    @Binds
    @Singleton
    abstract fun bindNotifier(impl: NotifierImpl): Notifier
}
