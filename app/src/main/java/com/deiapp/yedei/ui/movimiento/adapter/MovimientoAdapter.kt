package com.deiapp.yedei.ui.movimiento.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.deiapp.yedei.R
import com.deiapp.yedei.data.local.entity.MovimientoEntity
import com.deiapp.yedei.ui.movimiento.MovimientoViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Currency
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class MovimientoAdapter(
    private val onMovimientoClick:
        (MovimientoEntity) -> Unit
) : ListAdapter<
        MovimientoEntity,
        MovimientoAdapter.MovimientoViewHolder
        >(MovimientoDiffCallback()) {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MovimientoViewHolder {

        val view =
            LayoutInflater
                .from(parent.context)
                .inflate(
                    R.layout.item_movimiento,
                    parent,
                    false
                )

        return MovimientoViewHolder(
            view = view,
            onMovimientoClick = onMovimientoClick
        )
    }

    override fun onBindViewHolder(
        holder: MovimientoViewHolder,
        position: Int
    ) {
        holder.render(
            movimiento = getItem(position)
        )
    }

    class MovimientoViewHolder(
        view: View,
        private val onMovimientoClick:
            (MovimientoEntity) -> Unit
    ) : RecyclerView.ViewHolder(view) {

        private val tvIconoMovimiento:
                TextView =
            view.findViewById(
                R.id.tvIconoMovimiento
            )

        private val tvCategoriaMovimiento:
                TextView =
            view.findViewById(
                R.id.tvCategoriaMovimiento
            )

        private val tvDescripcionMovimiento:
                TextView =
            view.findViewById(
                R.id.tvDescripcionMovimiento
            )

        private val tvFechaMovimiento:
                TextView =
            view.findViewById(
                R.id.tvFechaMovimiento
            )

        private val tvMontoMovimiento:
                TextView =
            view.findViewById(
                R.id.tvMontoMovimiento
            )

        private val localePeru = Locale.forLanguageTag("es-PE")

        private val formatoFecha =
            SimpleDateFormat(
                "dd 'de' MMMM",
                localePeru
            )

        private val formatoMoneda =
            NumberFormat.getCurrencyInstance(
                localePeru
            ).apply {
                currency =
                    Currency.getInstance("PEN")
            }

        fun render(
            movimiento: MovimientoEntity
        ) {

            tvCategoriaMovimiento.text =
                movimiento.categoria

            configurarDescripcion(
                movimiento.descripcion
            )

            tvFechaMovimiento.text =
                formatoFecha.format(
                    Date(movimiento.fecha)
                )

            configurarTipoMovimiento(
                movimiento
            )

            itemView.setOnClickListener {
                onMovimientoClick(
                    movimiento
                )
            }
        }

        private fun configurarDescripcion(
            descripcion: String
        ) {

            if (descripcion.isBlank()) {

                tvDescripcionMovimiento.visibility =
                    View.GONE

            } else {

                tvDescripcionMovimiento.visibility =
                    View.VISIBLE

                tvDescripcionMovimiento.text =
                    descripcion
            }
        }

        private fun configurarTipoMovimiento(
            movimiento: MovimientoEntity
        ) {

            val esIngreso =
                movimiento.tipo ==
                        MovimientoViewModel
                            .TIPO_INGRESO

            val colorTexto =
                if (esIngreso) {
                    R.color.yedei_income
                } else {
                    R.color.yedei_expense
                }

            val colorFondo =
                if (esIngreso) {
                    R.color.yedei_income_background
                } else {
                    R.color.yedei_expense_background
                }

            tvIconoMovimiento.text =
                if (esIngreso) {
                    "I"
                } else {
                    "G"
                }

            tvIconoMovimiento.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    colorTexto
                )
            )

            tvIconoMovimiento.background =
                crearFondoIndicador(
                    colorFondo = colorFondo
                )

            tvMontoMovimiento.setTextColor(
                ContextCompat.getColor(
                    itemView.context,
                    colorTexto
                )
            )

            val prefijo =
                if (esIngreso) {
                    "+"
                } else {
                    "-"
                }

            val montoFormateado =
                formatoMoneda.format(
                    abs(
                        movimiento.montoCentimos
                    ) / 100.0
                )

            tvMontoMovimiento.text = itemView.context.getString(
                R.string.movement_amount_with_sign,
                prefijo,
                montoFormateado
            )
        }

        private fun crearFondoIndicador(
            colorFondo: Int
        ): GradientDrawable {

            val radio =
                14f *
                        itemView.resources
                            .displayMetrics
                            .density

            return GradientDrawable().apply {

                shape =
                    GradientDrawable.RECTANGLE

                cornerRadius =
                    radio

                setColor(
                    ContextCompat.getColor(
                        itemView.context,
                        colorFondo
                    )
                )
            }
        }
    }

    private class MovimientoDiffCallback :
        DiffUtil.ItemCallback<MovimientoEntity>() {

        override fun areItemsTheSame(
            oldItem: MovimientoEntity,
            newItem: MovimientoEntity
        ): Boolean {

            return oldItem.id ==
                    newItem.id
        }

        override fun areContentsTheSame(
            oldItem: MovimientoEntity,
            newItem: MovimientoEntity
        ): Boolean {

            return oldItem ==
                    newItem
        }
    }
}
