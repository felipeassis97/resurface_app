package com.resurface.resurface.di

import javax.inject.Qualifier

/** Marca a chave da API do Gemini (do BuildConfig), pra injetar sem ambiguidade. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiApiKey
