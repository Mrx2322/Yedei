package com.deiapp.yedei.data.repository

import com.deiapp.yedei.data.local.dao.PresupuestoDao
import com.deiapp.yedei.data.local.entity.PresupuestoEntity
import kotlinx.coroutines.flow.Flow

class PresupuestoRepository(
    private val presupuestoDao:
    PresupuestoDao
) {

    fun observarPorPeriodo(
        periodo: String
    ): Flow<PresupuestoEntity?> {

        return presupuestoDao
            .observarPorPeriodo(
                periodo
            )
    }

    suspend fun obtenerPorPeriodo(
        periodo: String
    ): PresupuestoEntity? {

        return presupuestoDao
            .obtenerPorPeriodo(
                periodo
            )
    }

    suspend fun guardar(
        presupuesto: PresupuestoEntity
    ) {

        presupuestoDao.guardar(
            presupuesto
        )
    }

    suspend fun eliminarPorPeriodo(
        periodo: String
    ) {

        presupuestoDao
            .eliminarPorPeriodo(
                periodo
            )
    }
}