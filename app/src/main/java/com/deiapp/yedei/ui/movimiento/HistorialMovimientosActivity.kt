package com.deiapp.yedei.ui.movimiento

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.deiapp.yedei.R
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.deiapp.yedei.ui.movimiento.adapter.MovimientoAdapter
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HistorialMovimientosActivity :
    AppCompatActivity() {

    private val movimientoViewModel:
            MovimientoViewModel by viewModels()

    private lateinit var btnVolver:
            ImageButton

    private lateinit var toggleFiltroMovimiento:
            MaterialButtonToggleGroup

    private lateinit var rvHistorialMovimientos:
            RecyclerView

    private lateinit var layoutHistorialVacio:
            View

    private lateinit var movimientoAdapter:
            MovimientoAdapter

    private var listaCompleta:
            List<MovimientoEntity> = emptyList()

    private var filtroActual:
            String = FILTRO_TODOS

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContentView(
            R.layout.activity_historial_movimientos
        )

        configurarInsets()
        inicializarComponentes()
        configurarRecyclerView()
        configurarEventos()
        observarMovimientos()
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

        btnVolver =
            findViewById(R.id.btnVolver)

        toggleFiltroMovimiento =
            findViewById(
                R.id.toggleFiltroMovimiento
            )

        rvHistorialMovimientos =
            findViewById(
                R.id.rvHistorialMovimientos
            )

        layoutHistorialVacio =
            findViewById(
                R.id.layoutHistorialVacio
            )
    }

    private fun configurarRecyclerView() {

        movimientoAdapter =
            MovimientoAdapter { movimiento ->

                mostrarOpcionesMovimiento(
                    movimiento
                )
            }

        rvHistorialMovimientos.layoutManager =
            LinearLayoutManager(this)

        rvHistorialMovimientos.adapter =
            movimientoAdapter

        rvHistorialMovimientos.setHasFixedSize(
            false
        )
    }

    private fun configurarEventos() {

        btnVolver.setOnClickListener {
            finish()
        }

        toggleFiltroMovimiento
            .addOnButtonCheckedListener {
                    _,
                    checkedId,
                    isChecked ->

                if (!isChecked) {
                    return@addOnButtonCheckedListener
                }

                filtroActual =
                    when (checkedId) {

                        R.id.btnFiltroIngresos ->
                            MovimientoViewModel
                                .TIPO_INGRESO

                        R.id.btnFiltroGastos ->
                            MovimientoViewModel
                                .TIPO_GASTO

                        else ->
                            FILTRO_TODOS
                    }

                aplicarFiltro()
            }
    }

    private fun observarMovimientos() {

        movimientoViewModel
            .movimientos
            .observe(this) { movimientos ->

                listaCompleta =
                    movimientos.orEmpty()

                aplicarFiltro()
            }
    }

    private fun aplicarFiltro() {

        val listaFiltrada =
            if (
                filtroActual ==
                FILTRO_TODOS
            ) {

                listaCompleta

            } else {

                listaCompleta.filter {
                        movimiento ->

                    movimiento.tipo ==
                            filtroActual
                }
            }

        movimientoAdapter.submitList(
            listaFiltrada
        )

        actualizarEstadoVacio(
            listaFiltrada.isEmpty()
        )
    }

    private fun actualizarEstadoVacio(
        estaVacio: Boolean
    ) {

        rvHistorialMovimientos.visibility =
            if (estaVacio) {
                View.GONE
            } else {
                View.VISIBLE
            }

        layoutHistorialVacio.visibility =
            if (estaVacio) {
                View.VISIBLE
            } else {
                View.GONE
            }
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

    companion object {

        private const val FILTRO_TODOS =
            "TODOS"
    }
}