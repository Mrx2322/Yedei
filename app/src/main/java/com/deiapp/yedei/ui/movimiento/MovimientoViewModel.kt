package com.deiapp.yedei.ui.movimiento

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.deiapp.yedei.data.local.database.YedeiDatabase
import com.deiapp.yedei.data.local.entity.MovimientoEntity
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
            MovimientoRepository(movimientoDao)

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

    fun insertar(
        movimiento: MovimientoEntity
    ) {
        viewModelScope.launch {
            repository.insertar(
                movimiento
            )
        }
    }

    fun actualizar(
        movimiento: MovimientoEntity
    ) {
        viewModelScope.launch {

            val movimientoActualizado =
                movimiento.copy(
                    actualizadoEn =
                        System.currentTimeMillis()
                )

            repository.actualizar(
                movimientoActualizado
            )
        }
    }

    fun eliminar(
        movimiento: MovimientoEntity
    ) {
        viewModelScope.launch {
            repository.eliminar(
                movimiento
            )
        }
    }

    fun eliminarTodos() {
        viewModelScope.launch {
            repository.eliminarTodos()
        }
    }

    companion object {

        const val TIPO_INGRESO =
            "INGRESO"

        const val TIPO_GASTO =
            "GASTO"
    }
}