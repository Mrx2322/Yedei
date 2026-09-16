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
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AgregarMovimientoActivity :
    AppCompatActivity() {

    private val movimientoViewModel:
            MovimientoViewModel by viewModels()

    private lateinit var btnCancelarSuperior:
            TextView

    private lateinit var toggleTipoMovimiento:
            MaterialButtonToggleGroup

    private lateinit var layoutMonto:
            TextInputLayout

    private lateinit var etMonto:
            TextInputEditText

    private lateinit var layoutCategoria:
            TextInputLayout

    private lateinit var actvCategoria:
            MaterialAutoCompleteTextView

    private lateinit var layoutFecha:
            TextInputLayout

    private lateinit var etFecha:
            TextInputEditText

    private lateinit var etDescripcion:
            TextInputEditText

    private lateinit var btnGuardarMovimiento:
            MaterialButton

    private val fechaMovimiento:
            Calendar = Calendar.getInstance()

    private val formatoFecha =
        SimpleDateFormat(
            "dd/MM/yyyy",
            Locale("es", "PE")
        )

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_agregar_movimiento
        )

        configurarInsets()
        inicializarComponentes()
        configurarFormulario()
        mostrarFechaSeleccionada()
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

        btnCancelarSuperior =
            findViewById(
                R.id.btnCancelarSuperior
            )

        toggleTipoMovimiento =
            findViewById(
                R.id.toggleTipoMovimiento
            )

        layoutMonto =
            findViewById(R.id.layoutMonto)

        etMonto =
            findViewById(R.id.etMonto)

        layoutCategoria =
            findViewById(
                R.id.layoutCategoria
            )

        actvCategoria =
            findViewById(
                R.id.actvCategoria
            )

        layoutFecha =
            findViewById(R.id.layoutFecha)

        etFecha =
            findViewById(R.id.etFecha)

        etDescripcion =
            findViewById(
                R.id.etDescripcion
            )

        btnGuardarMovimiento =
            findViewById(
                R.id.btnGuardarMovimiento
            )
    }

    private fun configurarFormulario() {

        configurarCategorias(
            tipo =
                MovimientoViewModel
                    .TIPO_GASTO
        )

        btnCancelarSuperior.setOnClickListener {
            finish()
        }

        toggleTipoMovimiento
            .addOnButtonCheckedListener {
                    _,
                    checkedId,
                    isChecked ->

                if (!isChecked) {
                    return@addOnButtonCheckedListener
                }

                val tipo =
                    if (
                        checkedId ==
                        R.id.btnIngreso
                    ) {
                        MovimientoViewModel
                            .TIPO_INGRESO
                    } else {
                        MovimientoViewModel
                            .TIPO_GASTO
                    }

                actvCategoria.setText(
                    "",
                    false
                )

                layoutCategoria.error =
                    null

                configurarCategorias(
                    tipo = tipo
                )
            }

        etMonto.doAfterTextChanged {
            layoutMonto.error = null
        }

        actvCategoria.setOnItemClickListener {
                _,
                _,
                _,
                _ ->

            layoutCategoria.error =
                null
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

    private fun configurarCategorias(
        tipo: String
    ) {

        val categorias =
            if (
                tipo ==
                MovimientoViewModel
                    .TIPO_INGRESO
            ) {

                listOf(
                    getString(
                        R.string.category_salary
                    ),
                    getString(
                        R.string.category_sales
                    ),
                    getString(
                        R.string.category_extra_income
                    ),
                    getString(
                        R.string.category_other
                    )
                )

            } else {

                listOf(
                    getString(
                        R.string.category_food
                    ),
                    getString(
                        R.string.category_transport
                    ),
                    getString(
                        R.string.category_home
                    ),
                    getString(
                        R.string.category_services
                    ),
                    getString(
                        R.string.category_health
                    ),
                    getString(
                        R.string.category_entertainment
                    ),
                    getString(
                        R.string.category_education
                    ),
                    getString(
                        R.string.category_other
                    )
                )
            }

        val categoriaAdapter =
            ArrayAdapter(
                this,
                android.R.layout
                    .simple_dropdown_item_1line,
                categorias
            )

        actvCategoria.setAdapter(
            categoriaAdapter
        )
    }

    private fun mostrarSelectorFecha() {

        DatePickerDialog(
            this,
            {
                    _,
                    anio,
                    mes,
                    dia ->

                fechaMovimiento.set(
                    Calendar.YEAR,
                    anio
                )

                fechaMovimiento.set(
                    Calendar.MONTH,
                    mes
                )

                fechaMovimiento.set(
                    Calendar.DAY_OF_MONTH,
                    dia
                )

                fechaMovimiento.set(
                    Calendar.HOUR_OF_DAY,
                    12
                )

                fechaMovimiento.set(
                    Calendar.MINUTE,
                    0
                )

                fechaMovimiento.set(
                    Calendar.SECOND,
                    0
                )

                fechaMovimiento.set(
                    Calendar.MILLISECOND,
                    0
                )

                mostrarFechaSeleccionada()
            },
            fechaMovimiento.get(
                Calendar.YEAR
            ),
            fechaMovimiento.get(
                Calendar.MONTH
            ),
            fechaMovimiento.get(
                Calendar.DAY_OF_MONTH
            )
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

        val tipoSeleccionado =
            if (
                toggleTipoMovimiento
                    .checkedButtonId ==
                R.id.btnIngreso
            ) {
                MovimientoViewModel
                    .TIPO_INGRESO
            } else {
                MovimientoViewModel
                    .TIPO_GASTO
            }

        val descripcion =
            etDescripcion.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val movimiento =
            MovimientoEntity(
                tipo = tipoSeleccionado,
                montoCentimos = montoCentimos,
                categoria = categoria,
                descripcion = descripcion,
                fecha =
                    fechaMovimiento.timeInMillis
            )

        btnGuardarMovimiento.isEnabled =
            false

        movimientoViewModel.insertar(
            movimiento = movimiento,

            onCompletado = {

                if (
                    isFinishing ||
                    isDestroyed
                ) {
                    return@insertar
                }

                Toast.makeText(
                    this,
                    getString(
                        R.string.movement_saved
                    ),
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            },

            onError = { error ->

                Log.e(
                    "YEDEI_MOVIMIENTO",
                    "No se pudo guardar el movimiento",
                    error
                )

                if (
                    !isFinishing &&
                    !isDestroyed
                ) {

                    btnGuardarMovimiento.isEnabled =
                        true

                    Toast.makeText(
                        this,
                        "No se pudo guardar el movimiento",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun obtenerMontoCentimos():
            Long? {

        val textoMonto =
            etMonto.text
                ?.toString()
                ?.trim()
                ?.replace(",", ".")
                .orEmpty()

        if (textoMonto.isEmpty()) {

            mostrarErrorMonto()

            return null
        }

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

        } catch (error: ArithmeticException) {

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
}