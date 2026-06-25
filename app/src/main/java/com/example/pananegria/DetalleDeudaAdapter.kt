package com.example.pananegria

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class DetalleDeudaAdapter(

    private val lista: MutableList<DetalleDeuda>,

    private val onCheckChanged:
        (DetalleDeuda, Boolean) -> Unit

) : RecyclerView.Adapter<
        DetalleDeudaAdapter.DetalleViewHolder>() {

    var mostrarPagadas = false

    inner class DetalleViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val textNombre =
            view.findViewById<TextView>(
                R.id.textNombreProductoDetalle
            )

        val textSubtotal =
            view.findViewById<TextView>(
                R.id.textSubtotalDetalle
            )

        val checkPagado =
            view.findViewById<CheckBox>(
                R.id.checkPagado
            )

        val textFecha =
            view.findViewById<TextView>(
                R.id.textFechaDetalle
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetalleViewHolder {

        val view = LayoutInflater.from(
            parent.context
        )

            .inflate(
                R.layout.item_detalle_deuda,
                parent,
                false
            )

        return DetalleViewHolder(view)
    }

    override fun getItemCount(): Int {

        return if (mostrarPagadas) {

            lista.size

        } else {

            lista.count {
                !it.pagado
            }
        }
    }

    override fun onBindViewHolder(
        holder: DetalleViewHolder,
        position: Int
    ) {

        val listaVisible =

            if (mostrarPagadas) {

                lista

            } else {

                lista.filter {
                    !it.pagado
                }
            }

        val detalle =
            listaVisible[position]

        val formato =
            SimpleDateFormat(
                "dd MMM yyyy",
                Locale.getDefault()
            )

        val fechaActual =
            formato.format(
                Date(detalle.fecha)
            )

        val fechaAnterior =

            if (position > 0) {

                formato.format(

                    Date(
                        listaVisible[position - 1].fecha
                    )
                )

            } else {

                ""
            }

        val mostrarFecha =
            fechaActual != fechaAnterior

        if (mostrarFecha) {

            holder.textFecha.visibility =
                View.VISIBLE

            holder.textFecha.text =
                fechaActual

        } else {

            holder.textFecha.visibility =
                View.GONE
        }

        holder.textNombre.text =
            detalle.nombreProducto

        holder.textSubtotal.text =
            "$${detalle.subtotal.toInt()}"

        holder.checkPagado.setOnCheckedChangeListener(null)

        holder.checkPagado.isChecked =
            detalle.pagado

        /*
        =========================
        SI YA ESTA PAGADO
        =========================
        */

        if (detalle.pagado) {

            holder.checkPagado.isEnabled =
                false

            holder.textNombre.alpha =
                0.5f

            holder.textSubtotal.alpha =
                0.5f

        } else {

            holder.checkPagado.isEnabled =
                true

            holder.textNombre.alpha =
                1f

            holder.textSubtotal.alpha =
                1f
        }

        holder.checkPagado.setOnCheckedChangeListener {

                _, isChecked ->

            onCheckChanged(
                detalle,
                isChecked
            )
        }
    }

}