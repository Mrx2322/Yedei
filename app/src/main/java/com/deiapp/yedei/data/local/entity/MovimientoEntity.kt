package com.deiapp.yedei.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimientos",
    indices = [
        Index(value = ["fecha"])
    ]
)
data class MovimientoEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val tipo: String,

    val montoCentimos: Long,

    val categoria: String,

    val descripcion: String = "",

    val fecha: Long,

    val creadoEn: Long = System.currentTimeMillis(),

    val actualizadoEn: Long = System.currentTimeMillis()
)