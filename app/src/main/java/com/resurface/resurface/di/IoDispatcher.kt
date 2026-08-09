package com.resurface.resurface.di

import javax.inject.Qualifier

/** Marca o `CoroutineDispatcher` de IO, pra ser injetado (G9) em vez de hard-coded. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher
