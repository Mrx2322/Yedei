package com.deiapp.yedei

import com.deiapp.yedei.ui.movimiento.HistorialMovimientosActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.content.Intent
import com.deiapp.yedei.ui.movimiento.AgregarMovimientoActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.deiapp.yedei.ui.movimiento.MovimientoViewModel
import com.deiapp.yedei.ui.movimiento.adapter.MovimientoAdapter
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val movimientoViewModel:
            MovimientoViewModel by viewModels()

    private lateinit var movimientoAdapter:
            MovimientoAdapter

    private lateinit var tvMesActual: TextView
    private lateinit var btnMesAnterior: TextView
    private lateinit var btnMesSiguiente: TextView

    private lateinit var tvSaldo: TextView
    private lateinit var tvIngresos: TextView
    private lateinit var tvGastos: TextView

    private lateinit var tvPorcentajePresupuesto: TextView
    private lateinit var tvDetallePresupuesto: TextView

    private lateinit var progresoPresupuesto:
            LinearProgressIndicator

    private lateinit var rvMovimientos: RecyclerView
    private lateinit var layoutSinMovimientos: View
    private lateinit var tvVerTodos: TextView

    private lateinit var fabAgregarMovimiento:
            ExtendedFloatingActionButton

    private val calendarioMes:
            Calendar = Calendar.getInstance()

    private var totalIngresosCentimos: Long = 0
    private var totalGastosCentimos: Long = 0

    private var movimientosPeriodoLiveData:
            LiveData<List<MovimientoEntity>>? = null

    private var ingresosPeriodoLiveData:
            LiveData<Long>? = null

    private var gastosPeriodoLiveData:
            LiveData<Long>? = null

    private val formatoMoneda =
        NumberFormat.getCurrencyInstance(
            Locale("es", "PE")
        ).apply {
            currency = Currency.getInstance("PEN")
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_main
        )

        configurarInsets()
        inicializarComponentes()
        configurarRecyclerView()
        configurarEventos()
        actualizarMesSeleccionado()
        configurarPresupuestoInicial()
    }

    private fun configurarInsets() {

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(R.id.main)
        ) { view, insets ->

            val systemBars =
                insets.getInsets(
                    WindowInsetsCompat.Type.systemBars()
                )

            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }
    }

    private fun inicializarComponentes() {

        tvMesActual =
            findViewById(R.id.tvMesActual)

        btnMesAnterior =
            findViewById(R.id.btnMesAnterior)

        btnMesSiguiente =
            findViewById(R.id.btnMesSiguiente)

        tvSaldo =
            findViewById(R.id.tvSaldo)

        tvIngresos =
            findViewById(R.id.tvIngresos)

        tvGastos =
            findViewById(R.id.tvGastos)

        tvPorcentajePresupuesto =
            findViewById(
                R.id.tvPorcentajePresupuesto
            )

        tvDetallePresupuesto =
            findViewById(
                R.id.tvDetallePresupuesto
            )

        progresoPresupuesto =
            findViewById(
                R.id.progresoPresupuesto
            )

        rvMovimientos =
            findViewById(R.id.rvMovimientos)

        layoutSinMovimientos =
            findViewById(
                R.id.layoutSinMovimientos
            )

        tvVerTodos =
            findViewById(R.id.tvVerTodos)

        fabAgregarMovimiento =
            findViewById(
                R.id.fabAgregarMovimiento
            )
    }

    private fun configurarRecyclerView() {

        movimientoAdapter =
            MovimientoAdapter { movimiento ->

                mostrarOpcionesMovimiento(
                    movimiento
                )
            }

        rvMovimientos.layoutManager =
            LinearLayoutManager(this)

        rvMovimientos.adapter =
            movimientoAdapter

        rvMovimientos.setHasFixedSize(
            false
        )
    }

    private fun configurarEventos() {

        btnMesAnterior.setOnClickListener {

            calendarioMes.add(
                Calendar.MONTH,
                -1
            )

            actualizarMesSeleccionado()
        }

        btnMesSiguiente.setOnClickListener {

            calendarioMes.add(
                Calendar.MONTH,
                1
            )

            actualizarMesSeleccionado()
        }

        tvVerTodos.setOnClickListener {

            val intent =
                Intent(
                    this,
                    HistorialMovimientosActivity::class.java
                )

            startActivity(intent)
        }

        fabAgregarMovimiento.setOnClickListener {

            val intent =
                Intent(
                    this,
                    AgregarMovimientoActivity::class.java
                )

            startActivity(intent)
        }
    }

    private fun actualizarMesSeleccionado() {

        mostrarNombreMes()

        val fechaInicio =
            obtenerInicioMes()

        val fechaFin =
            obtenerFinMes()

        observarMovimientosDelMes(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )

        observarIngresosDelMes(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )

        observarGastosDelMes(
            fechaInicio = fechaInicio,
            fechaFin = fechaFin
        )
    }

    private fun mostrarNombreMes() {

        val formatoMes =
            SimpleDateFormat(
                "MMMM yyyy",
                Locale("es", "PE")
            )

        val nombreMes =
            formatoMes
                .format(calendarioMes.time)
                .replaceFirstChar { caracter ->

                    if (caracter.isLowerCase()) {

                        caracter.titlecase(
                            Locale("es", "PE")
                        )

                    } else {

                        caracter.toString()
                    }
                }

        tvMesActual.text =
            nombreMes
    }

    private fun obtenerInicioMes(): Long {

        val calendarioInicio =
            calendarioMes.clone() as Calendar

        calendarioInicio.set(
            Calendar.DAY_OF_MONTH,
            1
        )

        calendarioInicio.set(
            Calendar.HOUR_OF_DAY,
            0
        )

        calendarioInicio.set(
            Calendar.MINUTE,
            0
        )

        calendarioInicio.set(
            Calendar.SECOND,
            0
        )

        calendarioInicio.set(
            Calendar.MILLISECOND,
            0
        )

        return calendarioInicio.timeInMillis
    }

    private fun obtenerFinMes(): Long {

        val calendarioFin =
            calendarioMes.clone() as Calendar

        calendarioFin.set(
            Calendar.DAY_OF_MONTH,
            1
        )

        calendarioFin.set(
            Calendar.HOUR_OF_DAY,
            0
        )

        calendarioFin.set(
            Calendar.MINUTE,
            0
        )

        calendarioFin.set(
            Calendar.SECOND,
            0
        )

        calendarioFin.set(
            Calendar.MILLISECOND,
            0
        )

        calendarioFin.add(
            Calendar.MONTH,
            1
        )

        calendarioFin.add(
            Calendar.MILLISECOND,
            -1
        )

        return calendarioFin.timeInMillis
    }

    private fun observarMovimientosDelMes(
        fechaInicio: Long,
        fechaFin: Long
    ) {

        movimientosPeriodoLiveData
            ?.removeObservers(this)

        movimientosPeriodoLiveData =
            movimientoViewModel
                .observarPorPeriodo(
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin
                )

        movimientosPeriodoLiveData
            ?.observe(this) { movimientos ->

                val movimientosRecientes =
                    movimientos.take(5)

                movimientoAdapter.submitList(
                    movimientosRecientes
                )

                actualizarEstadoLista(
                    hayMovimientos =
                        movimientos.isNotEmpty()
                )
            }
    }

    private fun actualizarEstadoLista(
        hayMovimientos: Boolean
    ) {

        rvMovimientos.visibility =
            if (hayMovimientos) {
                View.VISIBLE
            } else {
                View.GONE
            }

        layoutSinMovimientos.visibility =
            if (hayMovimientos) {
                View.GONE
            } else {
                View.VISIBLE
            }

        tvVerTodos.visibility =
            if (hayMovimientos) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun observarIngresosDelMes(
        fechaInicio: Long,
        fechaFin: Long
    ) {

        ingresosPeriodoLiveData
            ?.removeObservers(this)

        ingresosPeriodoLiveData =
            movimientoViewModel
                .observarTotalPorTipo(
                    tipo =
                        MovimientoViewModel
                            .TIPO_INGRESO,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin
                )

        ingresosPeriodoLiveData
            ?.observe(this) { total ->

                totalIngresosCentimos =
                    total ?: 0

                tvIngresos.text =
                    formatearMonto(
                        totalIngresosCentimos
                    )

                actualizarSaldo()
            }
    }

    private fun observarGastosDelMes(
        fechaInicio: Long,
        fechaFin: Long
    ) {

        gastosPeriodoLiveData
            ?.removeObservers(this)

        gastosPeriodoLiveData =
            movimientoViewModel
                .observarTotalPorTipo(
                    tipo =
                        MovimientoViewModel
                            .TIPO_GASTO,
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin
                )

        gastosPeriodoLiveData
            ?.observe(this) { total ->

                totalGastosCentimos =
                    total ?: 0

                tvGastos.text =
                    formatearMonto(
                        totalGastosCentimos
                    )

                actualizarSaldo()
            }
    }

    private fun actualizarSaldo() {

        val saldoCentimos =
            totalIngresosCentimos -
                    totalGastosCentimos

        tvSaldo.text =
            formatearMonto(
                saldoCentimos
            )
    }

    private fun configurarPresupuestoInicial() {

        progresoPresupuesto.setProgressCompat(
            0,
            false
        )

        tvPorcentajePresupuesto.text =
            getString(
                R.string.percentage_format,
                0
            )

        tvDetallePresupuesto.text =
            "Configura tu presupuesto mensual"
    }

    private fun mostrarOpcionesMovimiento(
        movimiento: MovimientoEntity
    ) {

        val opciones =
            arrayOf(
                getString(R.string.edit),
                getString(R.string.delete)
            )

        MaterialAlertDialogBuilder(this)
            .setTitle(
                R.string.movement_options
            )
            .setItems(opciones) {
                    _,
                    posicion ->

                when (posicion) {

                    0 -> {

                        abrirEdicionMovimiento(
                            movimiento
                        )
                    }

                    1 -> {

                        confirmarEliminacion(
                            movimiento
                        )
                    }
                }
            }
            .setNegativeButton(
                R.string.cancel,
                null
            )
            .show()
    }

    private fun abrirEdicionMovimiento(
        movimiento: MovimientoEntity
    ) {

        val intent =
            Intent(
                this,
                AgregarMovimientoActivity::class.java
            ).apply {

                putExtra(
                    AgregarMovimientoActivity
                        .EXTRA_MOVIMIENTO_ID,
                    movimiento.id
                )

                putExtra(
                    AgregarMovimientoActivity
                        .EXTRA_TIPO,
                    movimiento.tipo
                )

                putExtra(
                    AgregarMovimientoActivity
                        .EXTRA_MONTO_CENTIMOS,
                    movimiento.montoCentimos
                )

                putExtra(
                    AgregarMovimientoActivity
                        .EXTRA_CATEGORIA,
                    movimiento.categoria
                )

                putExtra(
                    AgregarMovimientoActivity
                        .EXTRA_DESCRIPCION,
                    movimiento.descripcion
                )

                putExtra(
                    AgregarMovimientoActivity
                        .EXTRA_FECHA,
                    movimiento.fecha
                )

                putExtra(
                    AgregarMovimientoActivity
                        .EXTRA_CREADO_EN,
                    movimiento.creadoEn
                )
            }

        startActivity(intent)
    }

    private fun confirmarEliminacion(
        movimiento: MovimientoEntity
    ) {

        MaterialAlertDialogBuilder(this)
            .setTitle(
                R.string.delete_movement_question
            )
            .setMessage(
                R.string.delete_movement_explanation
            )
            .setNegativeButton(
                R.string.cancel,
                null
            )
            .setPositiveButton(
                R.string.delete
            ) { dialog, _ ->

                eliminarMovimiento(
                    movimiento
                )

                dialog.dismiss()
            }
            .show()
    }

    private fun eliminarMovimiento(
        movimiento: MovimientoEntity
    ) {

        movimientoViewModel.eliminar(
            movimiento = movimiento,

            onCompletado = {

                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@eliminar
                }

                Toast.makeText(
                    this,
                    getString(
                        R.string.movement_deleted
                    ),
                    Toast.LENGTH_SHORT
                ).show()
            },

            onError = { error ->

                if (
                    !isFinishing &&
                    !isDestroyed
                ) {

                    Toast.makeText(
                        this,
                        getString(
                            R.string.movement_delete_error
                        ),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun formatearMonto(
        montoCentimos: Long
    ): String {

        val monto =
            montoCentimos / 100.0

        return formatoMoneda.format(
            monto
        )
    }
}