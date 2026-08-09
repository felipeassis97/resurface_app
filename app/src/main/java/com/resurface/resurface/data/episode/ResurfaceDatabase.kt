package com.resurface.resurface.data.episode

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.resurface.resurface.data.outcome.AlertOutcomeDao
import com.resurface.resurface.data.outcome.AlertOutcomeEntity

/** Banco Room do app. v2 adiciona outcomes; v3 torna o episódio único por `startedAt`. */
@Database(entities = [EpisodeEntity::class, AlertOutcomeEntity::class], version = 3)
abstract class ResurfaceDatabase : RoomDatabase() {

    /** DAO dos episódios. */
    abstract fun episodeDao(): EpisodeDao

    /** DAO dos outcomes de aviso. */
    abstract fun alertOutcomeDao(): AlertOutcomeDao

    companion object {
        /** v1→v2: cria a tabela alert_outcome sem tocar em episode (sem perda). */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `alert_outcome` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`firedAt` INTEGER NOT NULL, " +
                        "`appLabel` TEXT NOT NULL, " +
                        "`response` TEXT, " +
                        "`respondedAt` INTEGER)"
                )
            }
        }

        /** v2→v3: remove episódios duplicados e cria o índice único por `startedAt`. */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Mantém a menor id por startedAt; apaga as duplicatas geradas pelo replay.
                db.execSQL(
                    "DELETE FROM `episode` WHERE `id` NOT IN " +
                        "(SELECT MIN(`id`) FROM `episode` GROUP BY `startedAt`)"
                )
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS `index_episode_startedAt` " +
                        "ON `episode` (`startedAt`)"
                )
            }
        }
    }
}
