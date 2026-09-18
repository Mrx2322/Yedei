package com.deiapp.yedei

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deiapp.yedei.data.local.model.ResumenCategoria
import com.deiapp.yedei.ui.movimiento.MovimientoViewModel
import com.deiapp.yedei.ui.movimiento.adapter.ResumenCategoriaAdapter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Currency
import java.util.Locale
import kotlin.math.abs

class EstadisticasActivity : AppCompatActivity() {

    private val movimientoViewModel: MovimientoViewModel by viewModels()
    private val mesSeleccionado: Calendar = Calendar.getInstance()
    private val locale = Locale.forLanguageTag("es-PE")

    private val formatoMoneda = NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance("PEN")
    }

    private lateinit var tvMesActual: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvMensajeBalance: TextView
    private lateinit var tvIngresos: TextView
    private lateinit var tvGastos: TextView
    private lateinit var tvComparacion: TextView
    private lateinit var tvCategoriaMayor: TextView
    private lateinit var tvMontoCategoriaMayor: TextView
    private lateinit var cardCategoriaMayor: View
    private lateinit var rvCategoriasEstadisticas: RecyclerView
    private lateinit var layoutSinEstadisticas: View
    private lateinit var categoriasAdapter: ResumenCategoriaAdapter

    private var ingresosActuales = 0L
    private var gastosActuales = 0L
    private var gastosAnteriores = 0L
    private var gastosActualesCargados = false
    private var gastosAnterioresCargados = false

    private var ingresosLiveData: LiveData<Long>? = null
    private var gastosLiveData: LiveData<Long>? = null
    private var gastosAnterioresLiveData: LiveData<Long>? = null
    private var categoriasLiveData: LiveData<List<ResumenCategoria>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_estadisticas)

        savedInstanceState?.getLong(CLAVE_MES)?.let(mesSeleccionado::setTimeInMillis)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(barras.left, barras.top, barras.right, barras.bottom)
            insets
        }

        tvMesActual = findViewById(R.id.tvMesActual)
        tvBalance = findViewById(R.id.tvBalance)
        tvMensajeBalance = findViewById(R.id.tvMensajeBalance)
        tvIngresos = findViewById(R.id.tvIngresos)
        tvGastos = findViewById(R.id.tvGastos)
        tvComparacion = findViewById(R.id.tvComparacion)
        tvCategoriaMayor = findViewById(R.id.tvCategoriaMayor)
        tvMontoCategoriaMayor = findViewById(R.id.tvMontoCategoriaMayor)
        cardCategoriaMayor = findViewById(R.id.cardCategoriaMayor)
        rvCategoriasEstadisticas = findViewById(R.id.rvCategoriasEstadisticas)
        layoutSinEstadisticas = findViewById(R.id.layoutSinEstadisticas)

        categoriasAdapter = ResumenCategoriaAdapter()
        rvCategoriasEstadisticas.layoutManager = LinearLayoutManager(this)
        rvCategoriasEstadisticas.adapter = categoriasAdapter

        findViewById<ImageButton>(R.id.btnVolver).setOnClickListener { finish() }
        findViewById<ImageButton>(R.id.btnMesAnterior).setOnClickListener {
            mesSeleccionado.add(Calendar.MONTH, -1)
            cargarMes()
        }
        findViewById<ImageButton>(R.id.btnMesSiguiente).setOnClickListener {
            mesSeleccionado.add(Calendar.MONTH, 1)
            cargarMes()
        }

        cargarMes()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(CLAVE_MES, mesSeleccionado.timeInMillis)
        super.onSaveInstanceState(outState)
    }

    private fun cargarMes() {
        ingresosLiveData?.removeObservers(this)
        gastosLiveData?.removeObservers(this)
        gastosAnterioresLiveData?.removeObservers(this)
        categoriasLiveData?.removeObservers(this)

        ingresosActuales = 0L
        gastosActuales = 0L
        gastosAnteriores = 0L
        gastosActualesCargados = false
        gastosAnterioresCargados = false

        tvMesActual.text = SimpleDateFormat("MMMM yyyy", locale)
            .format(mesSeleccionado.time)
            .replaceFirstChar { it.titlecase(locale) }

        actualizarTotales()
        tvComparacion.setText(R.string.statistics_loading)
        tvCategoriaMayor.setText(R.string.no_category_expenses)
        tvMontoCategoriaMayor.text = formatearMonto(0L)
        cardCategoriaMayor.visibility = View.GONE
        categoriasAdapter.submitList(emptyList())
        rvCategoriasEstadisticas.visibility = View.GONE
        layoutSinEstadisticas.visibility = View.VISIBLE

        val (inicio, fin) = limitesDelMes(mesSeleccionado)
        val mesAnterior = mesSeleccionado.clone() as Calendar
        mesAnterior.add(Calendar.MONTH, -1)
        val (inicioAnterior, finAnterior) = limitesDelMes(mesAnterior)

        ingresosLiveData = movimientoViewModel.observarTotalPorTipo(
            MovimientoViewModel.TIPO_INGRESO, inicio, fin
        ).also { datos ->
            datos.observe(this) { total ->
                ingresosActuales = total ?: 0L
                actualizarTotales()
            }
        }

        gastosLiveData = movimientoViewModel.observarTotalPorTipo(
            MovimientoViewModel.TIPO_GASTO, inicio, fin
        ).also { datos ->
            datos.observe(this) { total ->
                gastosActuales = total ?: 0L
                gastosActualesCargados = true
                actualizarTotales()
                actualizarComparacion()
            }
        }

        gastosAnterioresLiveData = movimientoViewModel.observarTotalPorTipo(
            MovimientoViewModel.TIPO_GASTO, inicioAnterior, finAnterior
        ).also { datos ->
            datos.observe(this) { total ->
                gastosAnteriores = total ?: 0L
                gastosAnterioresCargados = true
                actualizarComparacion()
            }
        }

        categoriasLiveData = movimientoViewModel.observarGastosPorCategoria(
            inicio, fin
        ).also { datos ->
            datos.observe(this) { resumen ->
                val categorias = resumen.orEmpty()
                categoriasAdapter.submitList(categorias)

                val hayCategorias = categorias.isNotEmpty()
                rvCategoriasEstadisticas.visibility = if (hayCategorias) View.VISIBLE else View.GONE
                layoutSinEstadisticas.visibility = if (hayCategorias) View.GONE else View.VISIBLE
                cardCategoriaMayor.visibility = if (hayCategorias) View.VISIBLE else View.GONE

                categorias.firstOrNull()?.let { mayor ->
                    tvCategoriaMayor.text = mayor.categoria.ifBlank {
                        getString(R.string.categoria_sin_nombre)
                    }
                    tvMontoCategoriaMayor.text = formatearMonto(mayor.totalCentimos)
                }
            }
        }
    }

    private fun limitesDelMes(mes: Calendar): Pair<Long, Long> {
        val inicio = (mes.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val fin = (inicio.clone() as Calendar).apply {
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }
        return inicio.timeInMillis to fin.timeInMillis
    }

    private fun actualizarTotales() {
        tvIngresos.text = formatearMonto(ingresosActuales)
        tvGastos.text = formatearMonto(gastosActuales)
        tvBalance.text = formatearMonto(ingresosActuales - gastosActuales)

        tvMensajeBalance.setText(
            when {
                ingresosActuales == 0L && gastosActuales == 0L -> R.string.statistics_no_movements
                ingresosActuales > gastosActuales -> R.string.statistics_positive_balance
                ingresosActuales < gastosActuales -> R.string.statistics_negative_balance
                else -> R.string.statistics_even_balance
            }
        )
    }

    private fun actualizarComparacion() {
        if (!gastosActualesCargados || !gastosAnterioresCargados) return

        tvComparacion.text = when {
            gastosAnteriores == 0L && gastosActuales == 0L ->
                getString(R.string.statistics_no_expenses_comparison)

            gastosAnteriores == 0L ->
                getString(R.string.statistics_no_previous_expenses, formatearMonto(gastosActuales))

            gastosActuales == gastosAnteriores ->
                getString(R.string.statistics_same_expenses)

            else -> {
                val diferencia = gastosActuales.toDouble() - gastosAnteriores.toDouble()
                val porcentaje = abs(diferencia) / gastosAnteriores.toDouble() * 100.0
                val porcentajeTexto = String.format(locale, "%.1f", porcentaje)
                if (diferencia > 0) {
                    getString(R.string.statistics_spent_more, porcentajeTexto)
                } else {
                    getString(R.string.statistics_spent_less, porcentajeTexto)
                }
            }
        }
    }

    private fun formatearMonto(centimos: Long): String =
        formatoMoneda.format(centimos / 100.0)

    companion object {
        private const val CLAVE_MES = "mes_seleccionado"
    }
}
