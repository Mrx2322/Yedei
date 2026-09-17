package com.deiapp.yedei.ui.movimiento.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deiapp.yedei.R
import com.deiapp.yedei.data.local.model.ResumenCategoria
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.util.Locale
import kotlin.math.roundToInt

class ResumenCategoriaAdapter :
    ListAdapter<
            ResumenCategoria,
            ResumenCategoriaAdapter.ResumenCategoriaViewHolder
            >(ResumenCategoriaDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ResumenCategoriaViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_resumen_categoria,
                    parent,
                    false
                )

        return ResumenCategoriaViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ResumenCategoriaViewHolder,
        position: Int
    ) {
        holder.render(
            resumenCategoria = getItem(position),
            totalGastosCentimos = calcularTotalGastos()
        )
    }

    private fun calcularTotalGastos(): Long {
        return currentList.sumOf { resumen ->
            resumen.totalCentimos
        }
    }

    class ResumenCategoriaViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView) {

        private val tvInicialCategoria: TextView =
            itemView.findViewById(
                R.id.tvInicialCategoria
            )

        private val tvNombreCategoria: TextView =
            itemView.findViewById(
                R.id.tvNombreCategoria
            )

        private val tvMontoCategoria: TextView =
            itemView.findViewById(
                R.id.tvMontoCategoria
            )

        private val tvPorcentajeCategoria: TextView =
            itemView.findViewById(
                R.id.tvPorcentajeCategoria
            )

        private val progresoCategoria: LinearProgressIndicator =
            itemView.findViewById(
                R.id.progresoCategoria
            )

        fun render(
            resumenCategoria: ResumenCategoria,
            totalGastosCentimos: Long
        ) {
            val categoria =
                resumenCategoria.categoria.ifBlank {
                    itemView.context.getString(
                        R.string.categoria_sin_nombre
                    )
                }

            val inicial =
                categoria
                    .trim()
                    .firstOrNull()
                    ?.uppercaseChar()
                    ?.toString()
                    ?: "?"

            val porcentaje =
                calcularPorcentaje(
                    montoCategoria = resumenCategoria.totalCentimos,
                    totalGastos = totalGastosCentimos
                )

            tvInicialCategoria.text =
                inicial

            tvNombreCategoria.text =
                categoria

            tvMontoCategoria.text =
                formatearMonto(
                    resumenCategoria.totalCentimos
                )

            tvPorcentajeCategoria.text =
                itemView.context.getString(
                    R.string.porcentaje_gastos_categoria,
                    porcentaje
                )

            progresoCategoria.setProgressCompat(
                porcentaje,
                true
            )
        }

        private fun calcularPorcentaje(
            montoCategoria: Long,
            totalGastos: Long
        ): Int {

            if (totalGastos <= 0L) {
                return 0
            }

            return (
                    montoCategoria.toDouble() /
                            totalGastos.toDouble() *
                            100.0
                    )
                .roundToInt()
                .coerceIn(0, 100)
        }

        private fun formatearMonto(
            montoCentimos: Long
        ): String {

            val monto =
                montoCentimos / 100.0

            return String.format(
                Locale.US,
                "S/ %.2f",
                monto
            )
        }
    }

    private class ResumenCategoriaDiffCallback :
        DiffUtil.ItemCallback<ResumenCategoria>() {

        override fun areItemsTheSame(
            oldItem: ResumenCategoria,
            newItem: ResumenCategoria
        ): Boolean {

            return oldItem.categoria ==
                    newItem.categoria
        }

        override fun areContentsTheSame(
            oldItem: ResumenCategoria,
            newItem: ResumenCategoria
        ): Boolean {

            return oldItem == newItem
        }
    }
}