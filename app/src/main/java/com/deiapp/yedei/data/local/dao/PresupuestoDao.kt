package com.deiapp.yedei.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.deiapp.yedei.data.local.entity.PresupuestoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PresupuestoDao {

    @Upsert
    suspend fun guardar(
        presupuesto: PresupuestoEntity
    )

    @Query("SELECT * FROM presupuestos ORDER BY periodo ASC")
    suspend fun obtenerTodosParaCopia(): List<PresupuestoEntity>

    @Query(
        """
        SELECT * FROM presupuestos
        WHERE periodo = :periodo
        LIMIT 1
        """
    )
    fun observarPorPeriodo(
        periodo: String
    ): Flow<PresupuestoEntity?>

    @Query(
        """
        SELECT * FROM presupuestos
        WHERE periodo = :periodo
        LIMIT 1
        """
    )
    suspend fun obtenerPorPeriodo(
        periodo: String
    ): PresupuestoEntity?

    @Query(
        """
        DELETE FROM presupuestos
        WHERE periodo = :periodo
        """
    )
    suspend fun eliminarPorPeriodo(
        periodo: String
    )

    @Query("DELETE FROM presupuestos")
    suspend fun eliminarTodos()
}
