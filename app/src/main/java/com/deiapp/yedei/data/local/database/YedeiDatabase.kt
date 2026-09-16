package com.deiapp.yedei.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.deiapp.yedei.data.local.dao.MovimientoDao
import com.deiapp.yedei.data.local.dao.PresupuestoDao
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.deiapp.yedei.data.local.entity.PresupuestoEntity

@Database(
    entities = [
        MovimientoEntity::class,
        PresupuestoEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class YedeiDatabase :
    RoomDatabase() {

    abstract fun movimientoDao():
            MovimientoDao

    abstract fun presupuestoDao():
            PresupuestoDao

    companion object {

        @Volatile
        private var instancia:
                YedeiDatabase? = null

        private val MIGRACION_1_2 =
            object : Migration(
                startVersion = 1,
                endVersion = 2
            ) {

                override fun migrate(
                    database: SupportSQLiteDatabase
                ) {

                    database.execSQL(
                        """
                        CREATE TABLE IF NOT EXISTS presupuestos (
                            periodo TEXT NOT NULL,
                            montoCentimos INTEGER NOT NULL,
                            actualizadoEn INTEGER NOT NULL,
                            PRIMARY KEY(periodo)
                        )
                        """.trimIndent()
                    )
                }
            }

        fun obtenerInstancia(
            context: Context
        ): YedeiDatabase {

            return instancia
                ?: synchronized(this) {

                    val nuevaInstancia =
                        Room.databaseBuilder(
                            context.applicationContext,
                            YedeiDatabase::class.java,
                            "yedei_database"
                        )
                            .addMigrations(
                                MIGRACION_1_2
                            )
                            .build()

                    instancia =
                        nuevaInstancia

                    nuevaInstancia
                }
        }
    }
}