package com.deiapp.yedei

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.deiapp.yedei.data.local.entity.PresupuestoEntity
import com.deiapp.yedei.data.local.model.ResumenCategoria
import com.deiapp.yedei.ui.movimiento.AgregarMovimientoActivity
import com.deiapp.yedei.ui.movimiento.HistorialMovimientosActivity
import com.deiapp.yedei.ui.movimiento.MovimientoViewModel
import com.deiapp.yedei.ui.movimiento.adapter.MovimientoAdapter
import com.deiapp.yedei.ui.movimiento.adapter.ResumenCategoriaAdapter
import com.deiapp.yedei.ui.presupuesto.PresupuestoViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private val movimientoViewModel:
            MovimientoViewModel by viewModels()

    private val presupuestoViewModel:
            PresupuestoViewModel by viewModels()

    private lateinit var movimientoAdapter:
            MovimientoAdapter

    private lateinit var resumenCategoriaAdapter:
            ResumenCategoriaAdapter

    private lateinit var tvMesActual: TextView
    private lateinit var btnMesAnterior: TextView
    private lateinit var btnMesSiguiente: TextView

    private lateinit var tvSaldo: TextView
    private lateinit var tvIngresos: TextView
    private lateinit var tvGastos: TextView

    private lateinit var cardPresupuesto:
            MaterialCardView

    private lateinit var tvPorcentajePresupuesto:
            TextView

    private lateinit var tvDetallePresupuesto:
            TextView

    private lateinit var progresoPresupuesto:
            LinearProgressIndicator

    private lateinit var rvMovimientos:
            RecyclerView

    private lateinit var layoutSinMovimientos:
            View

    private lateinit var rvResumenCategorias:
            RecyclerView

    private lateinit var layoutSinResumenCategorias:
            View

    private lateinit var tvVerTodos:
            TextView

    private lateinit var btnVerEstadisticas:
            TextView

    private lateinit var fabAgregarMovimiento:
            ExtendedFloatingActionButton

    private val calendarioMes:
            Calendar = Calendar.getInstance()

    private var totalIngresosCentimos:
            Long = 0

    private var totalGastosCentimos:
            Long = 0

    private var presupuestoCentimos:
            Long = 0

    private var movimientosPeriodoLiveData:
            LiveData<List<MovimientoEntity>>? = null

    private var ingresosPeriodoLiveData:
            LiveData<Long>? = null

    private var gastosPeriodoLiveData:
            LiveData<Long>? = null

    private var gastosCategoriaPeriodoLiveData:
            LiveData<List<ResumenCategoria>>? = null

    private var presupuestoPeriodoLiveData:
            LiveData<PresupuestoEntity?>? = null

    private val localePeru = Locale.forLanguageTag("es-PE")

    private val formatoMoneda =
        NumberFormat.getCurrencyInstance(
            localePeru
        ).apply {
            currency =
                Currency.getInstance("PEN")
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        configurarInsets()
        inicializarComponentes()
        configurarRecyclerView()
        configurarEventos()
        actualizarMesSeleccionado()
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

        cardPresupuesto =
            findViewById(R.id.cardPresupuesto)

        tvPorcentajePresupuesto =
            findViewById(R.id.tvPorcentajePresupuesto)

        tvDetallePresupuesto =
            findViewById(R.id.tvDetallePresupuesto)

        progresoPresupuesto =
            findViewById(R.id.progresoPresupuesto)

        rvMovimientos =
            findViewById(R.id.rvMovimientos)

        layoutSinMovimientos =
            findViewById(R.id.layoutSinMovimientos)

        rvResumenCategorias =
            findViewById(R.id.rvResumenCategorias)

        layoutSinResumenCategorias =
            findViewById(R.id.layoutSinResumenCategorias)

        tvVerTodos =
            findViewById(R.id.tvVerTodos)

        btnVerEstadisticas =
            findViewById(R.id.btnVerEstadisticas)

        fabAgregarMovimiento =
            findViewById(R.id.fabAgregarMovimiento)
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

        rvMovimientos.setHasFixedSize(false)

        resumenCategoriaAdapter =
            ResumenCategoriaAdapter()

        rvResumenCategorias.layoutManager =
            LinearLayoutManager(this)

        rvResumenCategorias.adapter =
            resumenCategoriaAdapter

        rvResumenCategorias.setHasFixedSize(false)
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

        cardPresupuesto.setOnClickListener {
            mostrarDialogoPresupuesto()
        }

        btnVerEstadisticas.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    EstadisticasActivity::class.java
                )
            )
        }

        tvVerTodos.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    HistorialMovimientosActivity::class.java
                )
            )
        }

        fabAgregarMovimiento.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    AgregarMovimientoActivity::class.java
                )
            )
        }
    }

    private fun actualizarMesSeleccionado() {
        mostrarNombreMes()

        totalIngresosCentimos = 0
        totalGastosCentimos = 0
        presupuestoCentimos = 0

        actualizarTotalesVisuales()
        actualizarPresupuestoVisual()

        val fechaInicio =
            obtenerInicioMes()

        val fechaFin =
            obtenerFinMes()

        observarMovimientosDelMes(
            fechaInicio,
            fechaFin
        )

        observarIngresosDelMes(
            fechaInicio,
            fechaFin
        )

        observarGastosDelMes(
            fechaInicio,
            fechaFin
        )

        observarGastosPorCategoria(
            fechaInicio,
            fechaFin
        )

        observarPresupuestoDelMes(
            obtenerPeriodoActual()
        )
    }

    private fun mostrarNombreMes() {
        val formatoMes =
            SimpleDateFormat(
                "MMMM yyyy",
                localePeru
            )

        val nombreMes =
            formatoMes
                .format(calendarioMes.time)
                .replaceFirstChar { caracter ->
                    if (caracter.isLowerCase()) {
                        caracter.titlecase(
                            localePeru
                        )
                    } else {
                        caracter.toString()
                    }
                }

        tvMesActual.text = nombreMes
    }

    private fun obtenerPeriodoActual(): String {
        return SimpleDateFormat(
            "yyyy-MM",
            Locale.US
        ).format(
            calendarioMes.time
        )
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
                    fechaInicio,
                    fechaFin
                )

        movimientosPeriodoLiveData
            ?.observe(this) { movimientos ->

                val lista =
                    movimientos.orEmpty()

                movimientoAdapter.submitList(
                    lista.take(5)
                )

                actualizarEstadoLista(
                    lista.isNotEmpty()
                )
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
                    MovimientoViewModel.TIPO_INGRESO,
                    fechaInicio,
                    fechaFin
                )

        ingresosPeriodoLiveData
            ?.observe(this) { total ->
                totalIngresosCentimos =
                    total ?: 0

                actualizarTotalesVisuales()
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
                    MovimientoViewModel.TIPO_GASTO,
                    fechaInicio,
                    fechaFin
                )

        gastosPeriodoLiveData
            ?.observe(this) { total ->
                totalGastosCentimos =
                    total ?: 0

                actualizarTotalesVisuales()
                actualizarPresupuestoVisual()
            }
    }

    private fun observarPresupuestoDelMes(
        periodo: String
    ) {
        presupuestoPeriodoLiveData
            ?.removeObservers(this)

        presupuestoPeriodoLiveData =
            presupuestoViewModel
                .observarPorPeriodo(periodo)

        presupuestoPeriodoLiveData
            ?.observe(this) { presupuesto ->
                presupuestoCentimos =
                    presupuesto?.montoCentimos ?: 0

                actualizarPresupuestoVisual()
            }
    }

    private fun observarGastosPorCategoria(
        fechaInicio: Long,
        fechaFin: Long
    ) {
        gastosCategoriaPeriodoLiveData
            ?.removeObservers(this)

        gastosCategoriaPeriodoLiveData =
            movimientoViewModel
                .observarGastosPorCategoria(
                    fechaInicio = fechaInicio,
                    fechaFin = fechaFin
                )

        gastosCategoriaPeriodoLiveData
            ?.observe(this) { resumenCategorias ->
                val lista =
                    resumenCategorias.orEmpty()

                resumenCategoriaAdapter.submitList(
                    lista
                )

                actualizarEstadoResumenCategorias(
                    lista.isNotEmpty()
                )
            }
    }

    private fun actualizarTotalesVisuales() {
        tvIngresos.text =
            formatearMonto(
                totalIngresosCentimos
            )

        tvGastos.text =
            formatearMonto(
                totalGastosCentimos
            )

        val saldoCentimos =
            totalIngresosCentimos -
                    totalGastosCentimos

        tvSaldo.text =
            formatearMonto(
                saldoCentimos
            )
    }

    private fun actualizarPresupuestoVisual() {
        if (presupuestoCentimos <= 0) {
            progresoPresupuesto.setProgressCompat(
                0,
                true
            )

            progresoPresupuesto.setIndicatorColor(
                ContextCompat.getColor(
                    this,
                    R.color.yedei_primary
                )
            )

            tvPorcentajePresupuesto.text =
                getString(
                    R.string.percentage_format,
                    0
                )

            tvDetallePresupuesto.setText(
                R.string.budget_not_configured
            )

            return
        }

        val porcentajeReal =
            (
                    totalGastosCentimos.toDouble() /
                            presupuestoCentimos.toDouble() *
                            100.0
                    ).roundToInt()

        val progreso =
            porcentajeReal.coerceIn(
                0,
                100
            )

        progresoPresupuesto.setProgressCompat(
            progreso,
            true
        )

        val colorIndicador =
            if (
                totalGastosCentimos >
                presupuestoCentimos
            ) {
                R.color.yedei_expense
            } else {
                R.color.yedei_primary
            }

        progresoPresupuesto.setIndicatorColor(
            ContextCompat.getColor(
                this,
                colorIndicador
            )
        )

        tvPorcentajePresupuesto.text =
            getString(
                R.string.percentage_format,
                porcentajeReal.coerceAtLeast(0)
            )

        val disponibleCentimos =
            presupuestoCentimos -
                    totalGastosCentimos

        tvDetallePresupuesto.text =
            getString(
                R.string.budget_available_format,
                formatearMonto(
                    disponibleCentimos
                ),
                formatearMonto(
                    presupuestoCentimos
                )
            )
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

    private fun actualizarEstadoResumenCategorias(
        hayCategorias: Boolean
    ) {
        rvResumenCategorias.visibility =
            if (hayCategorias) {
                View.VISIBLE
            } else {
                View.GONE
            }

        layoutSinResumenCategorias.visibility =
            if (hayCategorias) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    private fun mostrarDialogoPresupuesto() {
        val dialogView =
            LayoutInflater
                .from(this)
                .inflate(
                    R.layout.dialog_presupuesto,
                    null
                )

        val tvTituloPresupuesto =
            dialogView.findViewById<TextView>(
                R.id.tvTituloPresupuesto
            )

        val layoutMontoPresupuesto =
            dialogView.findViewById<TextInputLayout>(
                R.id.layoutMontoPresupuesto
            )

        val etMontoPresupuesto =
            dialogView.findViewById<TextInputEditText>(
                R.id.etMontoPresupuesto
            )

        val btnCancelarPresupuesto =
            dialogView.findViewById<MaterialButton>(
                R.id.btnCancelarPresupuesto
            )

        val btnGuardarPresupuesto =
            dialogView.findViewById<MaterialButton>(
                R.id.btnGuardarPresupuesto
            )

        if (presupuestoCentimos > 0) {
            tvTituloPresupuesto.setText(
                R.string.edit_budget
            )

            etMontoPresupuesto.setText(
                convertirCentimosATexto(
                    presupuestoCentimos
                )
            )
        }

        val dialog =
            MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .create()

        dialog.show()

        etMontoPresupuesto.doAfterTextChanged {
            layoutMontoPresupuesto.error = null
        }

        btnCancelarPresupuesto.setOnClickListener {
            dialog.dismiss()
        }

        btnGuardarPresupuesto.setOnClickListener {
            val montoCentimos =
                obtenerCentimos(
                    etMontoPresupuesto.text
                        ?.toString()
                        .orEmpty()
                )

            if (
                montoCentimos == null ||
                montoCentimos <= 0
            ) {
                layoutMontoPresupuesto.error =
                    getString(
                        R.string.invalid_budget
                    )

                etMontoPresupuesto.requestFocus()

                return@setOnClickListener
            }

            btnGuardarPresupuesto.isEnabled =
                false

            val periodo =
                obtenerPeriodoActual()

            presupuestoViewModel.guardar(
                periodo = periodo,
                montoCentimos = montoCentimos,

                onCompletado = {
                    if (
                        isFinishing ||
                        isDestroyed
                    ) {
                        return@guardar
                    }

                    Toast.makeText(
                        this,
                        getString(
                            R.string.budget_saved
                        ),
                        Toast.LENGTH_SHORT
                    ).show()

                    dialog.dismiss()
                },

                onError = {
                    if (
                        !isFinishing &&
                        !isDestroyed
                    ) {
                        btnGuardarPresupuesto.isEnabled =
                            true

                        Toast.makeText(
                            this,
                            getString(
                                R.string.budget_save_error
                            ),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }
    }

    private fun obtenerCentimos(
        texto: String
    ): Long? {
        val valorNormalizado =
            texto
                .trim()
                .replace(",", ".")

        val decimal =
            valorNormalizado
                .toBigDecimalOrNull()
                ?: return null

        if (decimal.signum() <= 0) {
            return null
        }

        return try {
            decimal
                .setScale(
                    2,
                    RoundingMode.HALF_UP
                )
                .movePointRight(2)
                .longValueExact()
        } catch (_: ArithmeticException) {
            null
        }
    }

    private fun convertirCentimosATexto(
        montoCentimos: Long
    ): String {
        return BigDecimal
            .valueOf(montoCentimos)
            .movePointLeft(2)
            .stripTrailingZeros()
            .toPlainString()
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
            .setTitle(R.string.movement_options)
            .setItems(opciones) { _, posicion ->
                when (posicion) {
                    0 ->
                        abrirEdicionMovimiento(
                            movimiento
                        )

                    1 ->
                        confirmarEliminacion(
                            movimiento
                        )
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
                    AgregarMovimientoActivity.EXTRA_MOVIMIENTO_ID,
                    movimiento.id
                )

                putExtra(
                    AgregarMovimientoActivity.EXTRA_TIPO,
                    movimiento.tipo
                )

                putExtra(
                    AgregarMovimientoActivity.EXTRA_MONTO_CENTIMOS,
                    movimiento.montoCentimos
                )

                putExtra(
                    AgregarMovimientoActivity.EXTRA_CATEGORIA,
                    movimiento.categoria
                )

                putExtra(
                    AgregarMovimientoActivity.EXTRA_DESCRIPCION,
                    movimiento.descripcion
                )

                putExtra(
                    AgregarMovimientoActivity.EXTRA_FECHA,
                    movimiento.fecha
                )

                putExtra(
                    AgregarMovimientoActivity.EXTRA_CREADO_EN,
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

            onError = {
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
        return formatoMoneda.format(
            montoCentimos / 100.0
        )
    }
}
