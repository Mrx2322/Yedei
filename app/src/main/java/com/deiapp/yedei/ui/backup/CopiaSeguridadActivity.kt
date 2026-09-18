package com.deiapp.yedei.ui.backup

import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.deiapp.yedei.R
import com.deiapp.yedei.data.backup.GestorCopiaSeguridad
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CopiaSeguridadActivity : AppCompatActivity() {

    private val gestor by lazy { GestorCopiaSeguridad(this) }
    private lateinit var botonExportar: MaterialButton
    private lateinit var botonImportar: MaterialButton

    private val crearDocumento = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) ejecutar {
            gestor.exportar(uri)
            avisar("Copia guardada correctamente.")
        }
    }

    private val abrirDocumento = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) ejecutar {
            val datos = gestor.revisar(uri)
            MaterialAlertDialogBuilder(this)
                .setTitle("Restaurar copia")
                .setMessage(
                    "La copia contiene ${datos.movimientos.size} movimientos y " +
                            "${datos.presupuestos.size} presupuestos. Se reemplazarán " +
                            "todos los datos actuales. ¿Deseas continuar?"
                )
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Restaurar") { _, _ ->
                    ejecutar {
                        gestor.restaurar(datos)
                        avisar("Copia restaurada correctamente.")
                        finish()
                    }
                }
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Copia de seguridad"

        val margen = (24 * resources.displayMetrics.density).toInt()
        val contenido = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(margen, margen, margen, margen)
        }

        contenido.addView(TextView(this).apply {
            setText(R.string.descripcion_copia_seguridad)
            textSize = 16f
        })

        botonExportar = MaterialButton(this).apply {
            setText(R.string.guardar_copia)

            setOnClickListener {
                val fecha = SimpleDateFormat(
                    "yyyy-MM-dd_HH-mm",
                    Locale.ROOT
                ).format(Date())

                crearDocumento.launch("yedei_copia_$fecha.json")
            }
        }
        contenido.addView(botonExportar)

        botonImportar = MaterialButton(this).apply {
            setText(R.string.restaurar_copia)

            setOnClickListener {
                abrirDocumento.launch(
                    arrayOf("application/json", "text/plain")
                )
            }
        }
        contenido.addView(botonImportar)

        setContentView(contenido)
    }

    private fun ejecutar(accion: suspend () -> Unit) {
        botonExportar.isEnabled = false
        botonImportar.isEnabled = false
        lifecycleScope.launch {
            try {
                accion()
            } catch (error: Exception) {
                avisar(error.message ?: "No se pudo completar la operación.")
            } finally {
                botonExportar.isEnabled = true
                botonImportar.isEnabled = true
            }
        }
    }

    private fun avisar(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }
}
