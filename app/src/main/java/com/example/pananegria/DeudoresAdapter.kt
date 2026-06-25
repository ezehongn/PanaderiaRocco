package com.example.pananegria

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class DeudoresAdapter(

    private val listaClientes: List<Cliente>,

    private val onClick: (Cliente) -> Unit

) : RecyclerView.Adapter<DeudoresAdapter.ViewHolder>() {

    inner class ViewHolder(view: View)
        : RecyclerView.ViewHolder(view) {

        val card =
            view.findViewById<CardView>(
                R.id.cardDeudor
            )

        val textNombre =
            view.findViewById<TextView>(
                R.id.textNombreDeudor
            )

        val textMonto =
            view.findViewById<TextView>(
                R.id.textMontoDeuda
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(
            parent.context
        ).inflate(
            R.layout.item_deudor,
            parent,
            false
        )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val cliente =
            listaClientes[position]

        holder.textNombre.text =
            cliente.nombre

        holder.textMonto.text =
            "$${cliente.deudaTotal.toInt()}"

        holder.card.setOnClickListener {

            onClick(cliente)
        }
    }

    override fun getItemCount(): Int {

        return listaClientes.size
    }
}