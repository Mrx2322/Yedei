package com.deiapp.yedei.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.deiapp.yedei.data.local.dao.MovimientoDao
import com.deiapp.yedei.data.local.entity.MovimientoEntity

@Database(
    entities = [
        MovimientoEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class YedeiDatabase : RoomDatabase() {

    abstract fun movimientoDao(): MovimientoDao

    companion object {

        @Volatile
        private var instancia: YedeiDatabase? = null

        fun obtenerInstancia(
            context: Context
        ): YedeiDatabase {

            return instancia ?: synchronized(this) {

                val nuevaInstancia =
                    Room.databaseBuilder(
                        context.applicationContext,
                        YedeiDatabase::class.java,
                        "yedei_database"
                    ).build()

                instancia = nuevaInstancia

                nuevaInstancia
            }
        }
    }
}