package com.resurface.resurface.di

import android.content.Context
import androidx.room.Room
import com.resurface.resurface.data.episode.EpisodeDao
import com.resurface.resurface.data.episode.ResurfaceDatabase
import com.resurface.resurface.data.outcome.AlertOutcomeDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /** Fornece o banco Room único do app, com a migração v1→v2 (D-6). */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ResurfaceDatabase =
        Room.databaseBuilder(context, ResurfaceDatabase::class.java, "resurface.db")
            .addMigrations(ResurfaceDatabase.MIGRATION_1_2, ResurfaceDatabase.MIGRATION_2_3)
            .build()

    /** Fornece o DAO dos episódios a partir do banco. */
    @Provides
    fun provideEpisodeDao(database: ResurfaceDatabase): EpisodeDao = database.episodeDao()

    /** Fornece o DAO dos outcomes a partir do banco. */
    @Provides
    fun provideAlertOutcomeDao(database: ResurfaceDatabase): AlertOutcomeDao = database.alertOutcomeDao()
}
