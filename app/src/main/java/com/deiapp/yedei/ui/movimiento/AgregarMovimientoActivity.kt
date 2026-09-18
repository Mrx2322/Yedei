package com.deiapp.yedei.ui.movimiento

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged
import com.deiapp.yedei.R
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgregarMovimientoActivity : AppCompatActivity() {

    private val movimientoViewModel:
            MovimientoViewModel by viewModels()

    private lateinit var tvTituloFormulario: TextView
    private lateinit var btnCancelarSuperior: TextView
    private lateinit var toggleTipoMovimiento: MaterialButtonToggleGroup
    private lateinit var layoutMonto: TextInputLayout
    private lateinit var etMonto: TextInputEditText
    private lateinit var layoutCategoria: TextInputLayout
    private lateinit var actvCategoria: MaterialAutoCompleteTextView
    private lateinit var layoutFecha: TextInputLayout
    private lateinit var etFecha: TextInputEditText
    private lateinit var etDescripcion: TextInputEditText
    private lateinit var btnGuardarMovimiento: MaterialButton

    private val fechaMovimiento = Calendar.getInstance()

    private val formatoFecha =
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale.forLanguageTag("es-PE")
        )

    private var movimientoId: Long = 0
    private var creadoEnOriginal: Long =
        System.currentTimeMillis()

    private var esEdicion = false

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_agregar_movimiento)

        configurarInsets()
        inicializarComponentes()
        configurarFormulario()
        cargarDatosRecibidos()
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
        tvTituloFormulario =
            findViewById(R.id.tvTituloFormulario)

        btnCancelarSuperior =
            findViewById(R.id.btnCancelarSuperior)

        toggleTipoMovimiento =
            findViewById(R.id.toggleTipoMovimiento)

        layoutMonto =
            findViewById(R.id.layoutMonto)

        etMonto =
            findViewById(R.id.etMonto)

        layoutCategoria =
            findViewById(R.id.layoutCategoria)

        actvCategoria =
            findViewById(R.id.actvCategoria)

        layoutFecha =
            findViewById(R.id.layoutFecha)

        etFecha =
            findViewById(R.id.etFecha)

        etDescripcion =
            findViewById(R.id.etDescripcion)

        btnGuardarMovimiento =
            findViewById(R.id.btnGuardarMovimiento)
    }

    private fun configurarFormulario() {
        configurarCategorias(
            MovimientoViewModel.TIPO_GASTO
        )

        btnCancelarSuperior.setOnClickListener {
            finish()
        }

        toggleTipoMovimiento.addOnButtonCheckedListener {
                _,
                checkedId,
                isChecked ->

            if (!isChecked) {
                return@addOnButtonCheckedListener
            }

            val tipo =
                if (checkedId == R.id.btnIngreso) {
                    MovimientoViewModel.TIPO_INGRESO
                } else {
                    MovimientoViewModel.TIPO_GASTO
                }

            actvCategoria.setText("", false)
            layoutCategoria.error = null
            configurarCategorias(tipo)
        }

        etMonto.doAfterTextChanged {
            layoutMonto.error = null
        }

        actvCategoria.setOnItemClickListener {
                _,
                _,
                _,
                _ ->

            layoutCategoria.error = null
        }

        etFecha.setOnClickListener {
            mostrarSelectorFecha()
        }

        layoutFecha.setEndIconOnClickListener {
            mostrarSelectorFecha()
        }

        btnGuardarMovimiento.setOnClickListener {
            guardarMovimiento()
        }
    }

    private fun cargarDatosRecibidos() {
        movimientoId =
            intent.getLongExtra(
                EXTRA_MOVIMIENTO_ID,
                0L
            )

        esEdicion = movimientoId > 0

        if (!esEdicion) {
            mostrarFechaSeleccionada()
            return
        }

        tvTituloFormulario.setText(
            R.string.edit_movement
        )

        btnGuardarMovimiento.setText(
            R.string.update_movement
        )

        val tipo =
            intent.getStringExtra(
                EXTRA_TIPO
            ) ?: MovimientoViewModel.TIPO_GASTO

        val montoCentimos =
            intent.getLongExtra(
                EXTRA_MONTO_CENTIMOS,
                0L
            )

        val categoria =
            intent.getStringExtra(
                EXTRA_CATEGORIA
            ).orEmpty()

        val descripcion =
            intent.getStringExtra(
                EXTRA_DESCRIPCION
            ).orEmpty()

        val fecha =
            intent.getLongExtra(
                EXTRA_FECHA,
                System.currentTimeMillis()
            )

        creadoEnOriginal =
            intent.getLongExtra(
                EXTRA_CREADO_EN,
                System.currentTimeMillis()
            )

        val botonTipo =
            if (
                tipo ==
                MovimientoViewModel.TIPO_INGRESO
            ) {
                R.id.btnIngreso
            } else {
                R.id.btnGasto
            }

        toggleTipoMovimiento.check(
            botonTipo
        )

        configurarCategorias(tipo)

        etMonto.setText(
            convertirCentimosATexto(
                montoCentimos
            )
        )

        actvCategoria.setText(
            categoria,
            false
        )

        etDescripcion.setText(
            descripcion
        )

        fechaMovimiento.timeInMillis =
            fecha

        mostrarFechaSeleccionada()
    }

    private fun configurarCategorias(
        tipo: String
    ) {
        val categorias =
            if (
                tipo ==
                MovimientoViewModel.TIPO_INGRESO
            ) {
                listOf(
                    getString(R.string.category_salary),
                    getString(R.string.category_sales),
                    getString(R.string.category_extra_income),
                    getString(R.string.category_other)
                )
            } else {
                listOf(
                    getString(R.string.category_food),
                    getString(R.string.category_transport),
                    getString(R.string.category_home),
                    getString(R.string.category_services),
                    getString(R.string.category_health),
                    getString(R.string.category_entertainment),
                    getString(R.string.category_education),
                    getString(R.string.category_other)
                )
            }

        actvCategoria.setAdapter(
            ArrayAdapter(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categorias
            )
        )
    }

    private fun mostrarSelectorFecha() {
        DatePickerDialog(
            this,
            { _, anio, mes, dia ->

                fechaMovimiento.set(
                    anio,
                    mes,
                    dia,
                    12,
                    0,
                    0
                )

                fechaMovimiento.set(
                    Calendar.MILLISECOND,
                    0
                )

                mostrarFechaSeleccionada()
            },
            fechaMovimiento.get(Calendar.YEAR),
            fechaMovimiento.get(Calendar.MONTH),
            fechaMovimiento.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun mostrarFechaSeleccionada() {
        etFecha.setText(
            formatoFecha.format(
                fechaMovimiento.time
            )
        )

        layoutFecha.error = null
    }

    private fun guardarMovimiento() {
        val montoCentimos =
            obtenerMontoCentimos()
                ?: return

        val categoria =
            actvCategoria.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (categoria.isEmpty()) {
            layoutCategoria.error =
                getString(
                    R.string.select_category
                )

            actvCategoria.requestFocus()
            return
        }

        val tipo =
            if (
                toggleTipoMovimiento.checkedButtonId ==
                R.id.btnIngreso
            ) {
                MovimientoViewModel.TIPO_INGRESO
            } else {
                MovimientoViewModel.TIPO_GASTO
            }

        val movimiento =
            MovimientoEntity(
                id = movimientoId,
                tipo = tipo,
                montoCentimos = montoCentimos,
                categoria = categoria,
                descripcion =
                    etDescripcion.text
                        ?.toString()
                        ?.trim()
                        .orEmpty(),
                fecha = fechaMovimiento.timeInMillis,
                creadoEn = creadoEnOriginal,
                actualizadoEn =
                    System.currentTimeMillis()
            )

        btnGuardarMovimiento.isEnabled =
            false

        if (esEdicion) {
            actualizarMovimiento(movimiento)
        } else {
            insertarMovimiento(movimiento)
        }
    }

    private fun insertarMovimiento(
        movimiento: MovimientoEntity
    ) {
        movimientoViewModel.insertar(
            movimiento = movimiento,

            onCompletado = {
                finalizarGuardado(
                    R.string.movement_saved
                )
            },

            onError = { error ->
                manejarError(
                    error,
                    R.string.movement_update_error
                )
            }
        )
    }

    private fun actualizarMovimiento(
        movimiento: MovimientoEntity
    ) {
        movimientoViewModel.actualizar(
            movimiento = movimiento,

            onCompletado = {
                finalizarGuardado(
                    R.string.movement_updated
                )
            },

            onError = { error ->
                manejarError(
                    error,
                    R.string.movement_update_error
                )
            }
        )
    }

    private fun finalizarGuardado(
        mensajeId: Int
    ) {
        if (isFinishing || isDestroyed) {
            return
        }

        Toast.makeText(
            this,
            getString(mensajeId),
            Toast.LENGTH_SHORT
        ).show()

        finish()
    }

    private fun manejarError(
        error: Throwable,
        mensajeId: Int
    ) {
        Log.e(
            "YEDEI_MOVIMIENTO",
            "Error guardando movimiento",
            error
        )

        if (!isFinishing && !isDestroyed) {
            btnGuardarMovimiento.isEnabled =
                true

            Toast.makeText(
                this,
                getString(mensajeId),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun obtenerMontoCentimos(): Long? {
        val textoMonto =
            etMonto.text
                ?.toString()
                ?.trim()
                ?.replace(",", ".")
                .orEmpty()

        val montoDecimal =
            textoMonto.toBigDecimalOrNull()

        if (
            montoDecimal == null ||
            montoDecimal.signum() <= 0
        ) {
            mostrarErrorMonto()
            return null
        }

        return try {
            montoDecimal
                .setScale(
                    2,
                    RoundingMode.HALF_UP
                )
                .movePointRight(2)
                .longValueExact()
        } catch (_: ArithmeticException) {
            mostrarErrorMonto()
            null
        }
    }

    private fun mostrarErrorMonto() {
        layoutMonto.error =
            getString(
                R.string.invalid_amount
            )

        etMonto.requestFocus()
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

    companion object {
        const val EXTRA_MOVIMIENTO_ID =
            "extra_movimiento_id"

        const val EXTRA_TIPO =
            "extra_tipo"

        const val EXTRA_MONTO_CENTIMOS =
            "extra_monto_centimos"

        const val EXTRA_CATEGORIA =
            "extra_categoria"

        const val EXTRA_DESCRIPCION =
            "extra_descripcion"

        const val EXTRA_FECHA =
            "extra_fecha"

        const val EXTRA_CREADO_EN =
            "extra_creado_en"
    }
}
