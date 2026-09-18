package com.deiapp.yedei.data.backup

import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.deiapp.yedei.data.local.entity.PresupuestoEntity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Formato de intercambio para copias de seguridad de Yedei (versión 1). */
object CopiaSeguridadJson {

    data class Datos(
        val movimientos: List<MovimientoEntity>,
        val presupuestos: List<PresupuestoEntity>
    )

    fun crear(
        movimientos: List<MovimientoEntity>,
        presupuestos: List<PresupuestoEntity>
    ): String {
        val movimientosJson = JSONArray()
        movimientos.forEach { movimiento ->
            movimientosJson.put(
                JSONObject().apply {
                    put("id", movimiento.id)
                    put("tipo", movimiento.tipo)
                    put("montoCentimos", movimiento.montoCentimos)
                    put("categoria", movimiento.categoria)
                    put("descripcion", movimiento.descripcion)
                    put("fecha", movimiento.fecha)
                    put("creadoEn", movimiento.creadoEn)
                    put("actualizadoEn", movimiento.actualizadoEn)
                }
            )
        }

        val presupuestosJson = JSONArray()
        presupuestos.forEach { presupuesto ->
            presupuestosJson.put(
                JSONObject().apply {
                    put("periodo", presupuesto.periodo)
                    put("montoCentimos", presupuesto.montoCentimos)
                    put("actualizadoEn", presupuesto.actualizadoEn)
                }
            )
        }

        return JSONObject().apply {
            put("formato", "yedei-copia")
            put("version", 1)
            put("movimientos", movimientosJson)
            put("presupuestos", presupuestosJson)
        }.toString()
    }

    /** Valida la copia completa antes de que se modifique la base de datos. */
    @Throws(JSONException::class, IllegalArgumentException::class)
    fun leer(texto: String): Datos {
        val raiz = JSONObject(texto)
        require(raiz.optString("formato") == "yedei-copia") {
            "El archivo no es una copia de Yedei."
        }
        require(raiz.entero("version") == 1L) {
            "La versión de esta copia no es compatible."
        }

        val movimientosJson = raiz.getJSONArray("movimientos")
        val presupuestosJson = raiz.getJSONArray("presupuestos")
        val ids = mutableSetOf<Long>()
        val periodos = mutableSetOf<String>()

        val movimientos = (0 until movimientosJson.length()).map { indice ->
            val item = movimientosJson.getJSONObject(indice)
            val id = item.entero("id")
            val tipo = item.cadena("tipo")
            val monto = item.entero("montoCentimos")
            val categoria = item.cadena("categoria")
            val fecha = item.entero("fecha")
            val creadoEn = item.entero("creadoEn")
            val actualizadoEn = item.entero("actualizadoEn")

            require(id > 0 && ids.add(id)) { "La copia contiene IDs de movimientos inválidos o repetidos." }
            require(tipo == "INGRESO" || tipo == "GASTO") { "La copia contiene un tipo de movimiento inválido." }
            require(monto > 0) { "La copia contiene un monto de movimiento inválido." }
            require(categoria.isNotBlank()) { "La copia contiene una categoría vacía." }
            require(fecha >= 0 && creadoEn >= 0 && actualizadoEn >= 0) {
                "La copia contiene fechas inválidas."
            }

            MovimientoEntity(
                id = id,
                tipo = tipo,
                montoCentimos = monto,
                categoria = categoria,
                descripcion = item.cadena("descripcion"),
                fecha = fecha,
                creadoEn = creadoEn,
                actualizadoEn = actualizadoEn
            )
        }

        val presupuestos = (0 until presupuestosJson.length()).map { indice ->
            val item = presupuestosJson.getJSONObject(indice)
            val periodo = item.cadena("periodo")
            val monto = item.entero("montoCentimos")
            val actualizadoEn = item.entero("actualizadoEn")

            require(periodo.matches(Regex("[0-9]{4}-(0[1-9]|1[0-2])")) && periodos.add(periodo)) {
                "La copia contiene períodos inválidos o repetidos."
            }
            require(monto >= 0 && actualizadoEn >= 0) {
                "La copia contiene presupuestos inválidos."
            }

            PresupuestoEntity(
                periodo = periodo,
                montoCentimos = monto,
                actualizadoEn = actualizadoEn
            )
        }

        return Datos(movimientos, presupuestos)
    }

    private fun JSONObject.entero(clave: String): Long {
        val valor = get(clave)
        require(valor is Number && valor.toString().matches(Regex("-?[0-9]+"))) {
            "El campo $clave debe ser un número entero."
        }
        return valor.toString().toLongOrNull()
            ?: throw IllegalArgumentException("El campo $clave está fuera de rango.")
    }

    private fun JSONObject.cadena(clave: String): String {
        val valor = get(clave)
        require(valor is String) { "El campo $clave debe ser texto." }
        return valor
    }
}
