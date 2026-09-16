package com.deiapp.yedei.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "presupuestos"
)
data class PresupuestoEntity(

    @PrimaryKey
    val periodo: String,

    val montoCentimos: Long,

    val actualizadoEn: Long =
        System.currentTimeMillis()
)