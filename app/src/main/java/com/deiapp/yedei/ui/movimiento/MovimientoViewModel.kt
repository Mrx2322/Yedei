package com.deiapp.yedei.ui.movimiento

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.deiapp.yedei.data.local.database.YedeiDatabase
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.deiapp.yedei.data.local.model.ResumenCategoria
import com.deiapp.yedei.data.repository.MovimientoRepository
import kotlinx.coroutines.launch

class MovimientoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: MovimientoRepository

    val movimientos: LiveData<List<MovimientoEntity>>

    init {
        val movimientoDao =
            YedeiDatabase
                .obtenerInstancia(application)
                .movimientoDao()

        repository =
            MovimientoRepository(
                movimientoDao
            )

        movimientos =
            repository
                .observarTodos()
                .asLiveData()
    }

    fun observarPorPeriodo(
        fechaInicio: Long,
        fechaFin: Long
    ): LiveData<List<MovimientoEntity>> {

        return repository
            .observarPorPeriodo(
                fechaInicio = fechaInicio,
                fechaFin = fechaFin
            )
            .asLiveData()
    }

    fun observarTotalPorTipo(
        tipo: String,
        fechaInicio: Long,
        fechaFin: Long
    ): LiveData<Long> {

        return repository
            .observarTotalPorTipo(
                tipo = tipo,
                fechaInicio = fechaInicio,
                fechaFin = fechaFin
            )
            .asLiveData()
    }

    fun observarGastosPorCategoria(
        fechaInicio: Long,
        fechaFin: Long
    ): LiveData<List<ResumenCategoria>> {

        return repository
            .observarGastosPorCategoria(
                fechaInicio = fechaInicio,
                fechaFin = fechaFin
            )
            .asLiveData()
    }

    fun insertar(
        movimiento: MovimientoEntity,
        onCompletado: (Long) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val movimientoId =
                    repository.insertar(
                        movimiento
                    )

                onCompletado(
                    movimientoId
                )

            } catch (error: Throwable) {
                onError(
                    error
                )
            }
        }
    }

    fun actualizar(
        movimiento: MovimientoEntity,
        onCompletado: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                val movimientoActualizado =
                    movimiento.copy(
                        actualizadoEn =
                            System.currentTimeMillis()
                    )

                repository.actualizar(
                    movimientoActualizado
                )

                onCompletado()

            } catch (error: Throwable) {
                onError(
                    error
                )
            }
        }
    }

    fun eliminar(
        movimiento: MovimientoEntity,
        onCompletado: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.eliminar(
                    movimiento
                )

                onCompletado()

            } catch (error: Throwable) {
                onError(
                    error
                )
            }
        }
    }

    fun eliminarTodos(
        onCompletado: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        viewModelScope.launch {
            try {
                repository.eliminarTodos()

                onCompletado()

            } catch (error: Throwable) {
                onError(
                    error
                )
            }
        }
    }

    companion object {

        const val TIPO_INGRESO =
            "INGRESO"

        const val TIPO_GASTO =
            "GASTO"
    }
}