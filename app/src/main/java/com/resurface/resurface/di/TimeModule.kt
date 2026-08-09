package com.resurface.resurface.di

import com.resurface.resurface.data.config.MidnightClock
import com.resurface.resurface.data.config.TimeProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object TimeModule {

    /** Relógio real de produção. */
    @Provides
    fun provideTimeProvider(): TimeProvider = TimeProvider { System.currentTimeMillis() }

    /** Calculadora de meia-noite no fuso do dispositivo. */
    @Provides
    fun provideMidnightClock(): MidnightClock = MidnightClock()
}
