package com.deiapp.yedei

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
import androidx.recyclerview.widget.RecyclerView
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.deiapp.yedei.ui.movimiento.MovimientoViewModel
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

        fabAgregarMovimiento =
            findViewById(
                R.id.fabAgregarMovimiento
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

        fabAgregarMovimiento.setOnClickListener {

            Toast.makeText(
                this,
                "Próximamente agregaremos el formulario",
                Toast.LENGTH_SHORT
            ).show()
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

        tvMesActual.text = nombreMes
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

                val hayMovimientos =
                    movimientos.isNotEmpty()

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