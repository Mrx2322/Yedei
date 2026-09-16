package com.deiapp.yedei.data.repository

import com.deiapp.yedei.data.local.dao.MovimientoDao
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import kotlinx.coroutines.flow.Flow

class MovimientoRepository(
    private val movimientoDao: MovimientoDao
) {

    fun observarTodos(): Flow<List<MovimientoEntity>> {
        return movimientoDao.observarTodos()
    }

    fun observarPorPeriodo(
        fechaInicio: Long,
        fechaFin: Long
    ): Flow<List<MovimientoEntity>> {

        return movimientoDao.observarPorPeriodo(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )
    }

    fun observarTotalPorTipo(
        tipo: String,
        fechaInicio: Long,
        fechaFin: Long
    ): Flow<Long> {

        return movimientoDao.observarTotalPorTipo(
            tipo = tipo,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )
    }

    suspend fun obtenerPorId(
        movimientoId: Long
    ): MovimientoEntity? {

        return movimientoDao.obtenerPorId(
            movimientoId
        )
    }

    suspend fun insertar(
        movimiento: MovimientoEntity
    ): Long {

        return movimientoDao.insertar(
            movimiento
        )
    }

    suspend fun actualizar(
        movimiento: MovimientoEntity
    ) {
        movimientoDao.actualizar(
            movimiento
        )
    }

    suspend fun eliminar(
        movimiento: MovimientoEntity
    ) {
        movimientoDao.eliminar(
            movimiento
        )
    }

    suspend fun eliminarTodos() {
        movimientoDao.eliminarTodos()
    }
}