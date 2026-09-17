package com.deiapp.yedei.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.deiapp.yedei.data.local.model.ResumenCategoria
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(
        movimiento: MovimientoEntity
    ): Long

    @Update
    suspend fun actualizar(
        movimiento: MovimientoEntity
    )

    @Delete
    suspend fun eliminar(
        movimiento: MovimientoEntity
    )

    @Query(
        """
        SELECT * FROM movimientos
        ORDER BY fecha DESC, id DESC
        """
    )
    fun observarTodos(): Flow<List<MovimientoEntity>>

    @Query(
        """
        SELECT * FROM movimientos
        WHERE fecha BETWEEN :fechaInicio AND :fechaFin
        ORDER BY fecha DESC, id DESC
        """
    )
    fun observarPorPeriodo(
        fechaInicio: Long,
        fechaFin: Long
    ): Flow<List<MovimientoEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(montoCentimos), 0)
        FROM movimientos
        WHERE tipo = :tipo
        AND fecha BETWEEN :fechaInicio AND :fechaFin
        """
    )
    fun observarTotalPorTipo(
        tipo: String,
        fechaInicio: Long,
        fechaFin: Long
    ): Flow<Long>

    @Query(
        """
        SELECT
            categoria AS categoria,
            COALESCE(SUM(montoCentimos), 0) AS totalCentimos
        FROM movimientos
        WHERE tipo = 'GASTO'
        AND fecha BETWEEN :fechaInicio AND :fechaFin
        GROUP BY categoria
        ORDER BY totalCentimos DESC
        """
    )
    fun observarGastosPorCategoria(
        fechaInicio: Long,
        fechaFin: Long
    ): Flow<List<ResumenCategoria>>

    @Query(
        """
        SELECT * FROM movimientos
        WHERE id = :movimientoId
        LIMIT 1
        """
    )
    suspend fun obtenerPorId(
        movimientoId: Long
    ): MovimientoEntity?

    @Query("DELETE FROM movimientos")
    suspend fun eliminarTodos()
}