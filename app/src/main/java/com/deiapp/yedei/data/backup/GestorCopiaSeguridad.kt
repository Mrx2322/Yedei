package com.deiapp.yedei.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.deiapp.yedei.data.local.database.YedeiDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Lee y escribe las copias elegidas con el selector de documentos de Android. */
class GestorCopiaSeguridad(context: Context) {

    private val contexto = context.applicationContext
    private val baseDeDatos = YedeiDatabase.obtenerInstancia(contexto)

    /** Escribe una copia completa en el documento creado por el usuario. */
    suspend fun exportar(destino: Uri) = withContext(Dispatchers.IO) {
        val datos = baseDeDatos.withTransaction {
            CopiaSeguridadJson.Datos(
                movimientos = baseDeDatos.movimientoDao().obtenerTodosParaCopia(),
                presupuestos = baseDeDatos.presupuestoDao().obtenerTodosParaCopia()
            )
        }

        val contenido = CopiaSeguridadJson.crear(
            datos.movimientos,
            datos.presupuestos
        )

        val salida = contexto.contentResolver.openOutputStream(destino, "wt")
            ?: throw IOException("No se pudo abrir el archivo para guardar la copia.")

        salida.bufferedWriter(Charsets.UTF_8).use { escritor ->
            escritor.write(contenido)
        }
    }

    /** Lee y valida un archivo, sin cambiar la información guardada. */
    suspend fun revisar(origen: Uri): CopiaSeguridadJson.Datos =
        withContext(Dispatchers.IO) {
            val entrada = contexto.contentResolver.openInputStream(origen)
                ?: throw IOException("No se pudo abrir la copia seleccionada.")

            // Evita leer archivos enormes o ajenos a la aplicación en memoria.
            val bytes = entrada.use { flujo ->
                val limite = 50 * 1024 * 1024
                val contenido = flujo.readNBytesCompat(limite + 1)
                require(contenido.size <= limite) { "La copia supera los 50 MB." }
                contenido
            }

            CopiaSeguridadJson.leer(bytes.toString(Charsets.UTF_8))
        }

    /** Llamar únicamente después de mostrar y aceptar la confirmación del usuario. */
    suspend fun restaurar(datos: CopiaSeguridadJson.Datos) =
        withContext(Dispatchers.IO) {
            baseDeDatos.withTransaction {
                val movimientosDao = baseDeDatos.movimientoDao()
                val presupuestosDao = baseDeDatos.presupuestoDao()

                movimientosDao.eliminarTodos()
                presupuestosDao.eliminarTodos()

                datos.movimientos.forEach { movimientosDao.insertar(it) }
                datos.presupuestos.forEach { presupuestosDao.guardar(it) }
            }
        }

    private fun java.io.InputStream.readNBytesCompat(maximo: Int): ByteArray {
        val salida = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(8192)
        while (salida.size() < maximo) {
            val leidos = read(buffer, 0, minOf(buffer.size, maximo - salida.size()))
            if (leidos < 0) break
            salida.write(buffer, 0, leidos)
        }
        return salida.toByteArray()
    }
}
