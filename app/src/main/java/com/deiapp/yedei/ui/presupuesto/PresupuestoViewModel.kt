package com.deiapp.yedei.ui.presupuesto

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.deiapp.yedei.data.local.database.YedeiDatabase
import com.deiapp.yedei.data.local.entity.PresupuestoEntity
import com.deiapp.yedei.data.repository.PresupuestoRepository
import kotlinx.coroutines.launch

class PresupuestoViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository:
            PresupuestoRepository

    init {
        val presupuestoDao =
            YedeiDatabase
                .obtenerInstancia(application)
                .presupuestoDao()

        repository =
            PresupuestoRepository(
                presupuestoDao
            )
    }

    fun observarPorPeriodo(
        periodo: String
    ): LiveData<PresupuestoEntity?> {

        return repository
            .observarPorPeriodo(
                periodo
            )
            .asLiveData()
    }

    fun guardar(
        periodo: String,
        montoCentimos: Long,
        onCompletado: () -> Unit,
        onError: (Throwable) -> Unit
    ) {

        val presupuesto =
            PresupuestoEntity(
                periodo = periodo,
                montoCentimos = montoCentimos,
                actualizadoEn =
                    System.currentTimeMillis()
            )

        viewModelScope.launch {

            try {

                repository.guardar(
                    presupuesto
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
        periodo: String,
        onCompletado: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {

        viewModelScope.launch {

            try {

                repository.eliminarPorPeriodo(
                    periodo
                )

                onCompletado()

            } catch (error: Throwable) {

                onError(
                    error
                )
            }
        }
    }
}