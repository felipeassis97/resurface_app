package com.resurface.resurface.data.episode

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.resurface.resurface.data.behavior.BehaviorEventDao
import com.resurface.resurface.data.behavior.BehaviorEventEntity
import com.resurface.resurface.data.outcome.AlertOutcomeDao
import com.resurface.resurface.data.outcome.AlertOutcomeEntity

/** Banco Room. v2=outcomes; v3=episódio único; v4=comportamento; v5=tom+fonte no outcome. */
@Database(
    entities = [EpisodeEntity::class, AlertOutcomeEntity::class, BehaviorEventEntity::class],
    version = 5,
)
abstract class ResurfaceDatabase : RoomDatabase() {

    /** DAO dos episódios. */
    abstract fun episodeDao(): EpisodeDao

    /** DAO dos outcomes de aviso. */
    abstract fun alertOutcomeDao(): AlertOutcomeDao

    /** DAO dos eventos de comportamento. */
    abstract fun behaviorEventDao(): BehaviorEventDao

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

        /** v3→v4: cria a tabela behavior_event (acessibilidade, additiva). */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `behavior_event` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`timestamp` INTEGER NOT NULL, " +
                        "`pkg` TEXT NOT NULL, " +
                        "`surface` TEXT NOT NULL, " +
                        "`hesitated` INTEGER NOT NULL)"
                )
            }
        }

        /** v4→v5: adiciona tom + fonte no outcome (H4, additiva). */
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `alert_outcome` ADD COLUMN `tone` TEXT")
                db.execSQL("ALTER TABLE `alert_outcome` ADD COLUMN `source` TEXT")
            }
        }
    }
}
