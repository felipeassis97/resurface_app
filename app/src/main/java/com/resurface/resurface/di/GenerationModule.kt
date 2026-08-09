package com.resurface.resurface.di

import com.resurface.resurface.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object GenerationModule {

    /** Fornece a chave do Gemini lida do BuildConfig (do local.properties). Vazia = fallback total. */
    @Provides
    @GeminiApiKey
    fun provideGeminiApiKey(): String = BuildConfig.GEMINI_API_KEY
}
