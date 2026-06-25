package com.example.pananegria

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class MovimientoAdapter(

    private val lista: List<Movimiento>

) : RecyclerView.Adapter<MovimientoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View)
        : RecyclerView.ViewHolder(view) {

        val textDescripcion =
            view.findViewById<TextView>(
                R.id.textDescripcionMovimiento
            )

        val textMonto =
            view.findViewById<TextView>(
                R.id.textMontoMovimiento
            )

        val textUsuario =
            view.findViewById<TextView>(
                R.id.textUsuarioMovimiento
            )

        val textFecha =
            view.findViewById<TextView>(
                R.id.textFechaMovimiento
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(
            parent.context
        ).inflate(
            R.layout.item_movimiento,
            parent,
            false
        )

        return ViewHolder(view)
    }

    override fun getItemCount() =
        lista.size

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val movimiento =
            lista[position]

        holder.textDescripcion.text =
            movimiento.descripcion

        holder.textMonto.text =
            "$${movimiento.monto.toInt()}"

        holder.textUsuario.text =
            "Usuario: ${movimiento.usuario}"

        val formato =
            SimpleDateFormat(
                "dd/MM/yyyy HH:mm",
                Locale.getDefault()
            )

        holder.textFecha.text =
            formato.format(
                Date(movimiento.fecha)
            )
    }
}