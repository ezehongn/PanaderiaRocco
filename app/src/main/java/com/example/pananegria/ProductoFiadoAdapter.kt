package com.example.pananegria

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import android.graphics.BitmapFactory
import android.util.Base64
import coil.dispose

import coil.load

class ProductoFiadoAdapter(

    private val listaProductos: MutableList<Product>,

    private val onTotalChanged: (Double) -> Unit

) : RecyclerView.Adapter<ProductoFiadoAdapter.ProductoViewHolder>() {

    /*
    =========================
    CANTIDADES
    =========================
    */

    private val cantidadesKg =
        mutableMapOf<String, Double>()
    private val cantidades =
        mutableMapOf<String, Double>()

    inner class ProductoViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val imageProducto =
            view.findViewById<ImageView>(
                R.id.imageProductoFiado
            )

        val textNombre =
            view.findViewById<TextView>(
                R.id.textNombreProductoFiado
            )

        val textPrecio =
            view.findViewById<TextView>(
                R.id.textPrecioProductoFiado
            )

        val textCantidad =
            view.findViewById<TextView>(
                R.id.textCantidad
            )

        val textSubtotal =
            view.findViewById<TextView>(
                R.id.textSubtotal
            )

        val btnMas =
            view.findViewById<TextView>(
                R.id.btnMas
            )

        val btnMenos =
            view.findViewById<TextView>(
                R.id.btnMenos
            )

        val btnEliminar =
            view.findViewById<ImageView>(
                R.id.btnEliminarProducto
            )

        val editCantidadKg =
            view.findViewById<EditText>(
                R.id.editCantidadKg
            )

        val layoutCantidadKg =
            view.findViewById<LinearLayout>(
                R.id.layoutCantidadKg
            )
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductoViewHolder {

        val view = LayoutInflater.from(
            parent.context
        )

            .inflate(
                R.layout.item_producto_fiado,
                parent,
                false
            )

        return ProductoViewHolder(view)
    }

    override fun getItemCount(): Int {

        return listaProductos.size
    }

    override fun onBindViewHolder(
        holder: ProductoViewHolder,
        position: Int
    ) {

        val producto =
            listaProductos[position]

        /*
=========================
RESET
=========================
*/

        holder.imageProducto.setImageDrawable(
            null
        )

        holder.imageProducto.setImageBitmap(
            null
        )

        /*
        =========================
        BASE64
        =========================
        */

        if (

            !producto.imageUrl.startsWith(
                "http"
            )

        ) {

            holder.imageProducto.dispose()

            try {

                val bytes =

                    Base64.decode(

                        producto.imageUrl,

                        Base64.DEFAULT
                    )

                val bitmap =

                    BitmapFactory.decodeByteArray(

                        bytes,

                        0,

                        bytes.size
                    )

                if (bitmap != null) {

                    holder.imageProducto.setImageBitmap(
                        bitmap
                    )
                }

            } catch (e: Exception) {

                holder.imageProducto.setImageResource(
                    android.R.color.transparent
                )
            }
        }

        /*
        =========================
        URL
        =========================
        */

        else {

            holder.imageProducto.load(
                producto.imageUrl
            ) {

                crossfade(true)
            }
        }

        holder.textNombre.text =
            producto.nombre

        holder.textPrecio.text =
            "$${producto.precio.toInt()}"

        /*
        =========================
        CANTIDAD ACTUAL
        =========================
        */

        val cantidadActual =
            cantidades[producto.id] ?: 0.0

        /*
        =========================
        MOSTRAR CANTIDAD
        =========================
        */

        if (
            producto.unidadMedida == "Por kg"
        ) {

            holder.textCantidad.text =
                "${cantidadActual}kg"

        } else {

            holder.textCantidad.text =
                cantidadActual.toInt().toString()
        }

        /*
        =========================
        SUBTOTAL
        =========================
        */

        val subtotal =
            producto.precio * cantidadActual

        holder.textSubtotal.text =
            "$${subtotal.toInt()}"

        /*
        =========================
        BOTON +
        =========================
        */

        holder.btnMas.setOnClickListener {

            var nuevaCantidad =
                cantidades[producto.id] ?: 0.0

            if (
                producto.unidadMedida == "Por kg"
            ) {

                nuevaCantidad += 0.100

            } else {

                nuevaCantidad += 1
            }

            cantidades[producto.id] =
                nuevaCantidad

            notifyItemChanged(position)

            actualizarTotal()
        }

        /*
        =========================
        BOTON -
        =========================
        */

        holder.btnMenos.setOnClickListener {

            var nuevaCantidad =
                cantidades[producto.id] ?: 0.0

            if (
                producto.unidadMedida == "Por kg"
            ) {

                nuevaCantidad -= 0.100

            } else {

                nuevaCantidad -= 1
            }

            if (nuevaCantidad < 0) {

                nuevaCantidad = 0.0
            }

            cantidades[producto.id] =
                nuevaCantidad

            notifyItemChanged(position)

            actualizarTotal()
        }

        holder.btnEliminar.setOnClickListener {

            listaProductos.removeAt(position)

            notifyItemRemoved(position)

            actualizarTotal()
        }

        if (producto.unidadMedida == "Por kg") {

            holder.btnMas.visibility =
                View.GONE

            holder.btnMenos.visibility =
                View.GONE

            holder.textCantidad.visibility =
                View.GONE

            holder.layoutCantidadKg.visibility =
                View.VISIBLE

        } else {

            holder.btnMas.visibility =
                View.VISIBLE

            holder.btnMenos.visibility =
                View.VISIBLE

            holder.textCantidad.visibility =
                View.VISIBLE

            holder.layoutCantidadKg.visibility =
                View.GONE
        }

        holder.editCantidadKg.addTextChangedListener {

            val kg =
                it.toString()
                    .toDoubleOrNull() ?: 0.0

            /*
            =========================
            GUARDAR KG
            =========================
            */

            cantidadesKg[producto.id] =
                kg

            /*
            =========================
            SUBTOTAL
            =========================
            */

            val subtotal =
                producto.precio * kg

            holder.textSubtotal.text =
                "$${subtotal.toInt()}"

            actualizarTotal()
        }

    }



    /*
    =========================
    TOTAL GENERAL
    =========================
    */

    private fun actualizarTotal() {

        var total = 0.0

        listaProductos.forEach { producto ->

            /*
            =========================
            PRODUCTOS POR KG
            =========================
            */

            if (
                producto.unidadMedida == "Por kg"
            ) {

                val kg =
                    cantidadesKg[producto.id]
                        ?: 0.0

                total +=
                    producto.precio * kg
            }

            /*
            =========================
            PRODUCTOS POR UNIDAD
            =========================
            */

            else {

                val cantidad =
                    cantidades[producto.id]
                        ?: 0.0

                total +=
                    producto.precio * cantidad
            }
        }

        onTotalChanged(total)
    }

    fun obtenerTotal(): Double {

        var total = 0.0

        listaProductos.forEach { producto ->

            if (
                producto.unidadMedida == "Por kg"
            ) {

                val kg =
                    cantidadesKg[producto.id]
                        ?: 0.0

                total +=
                    producto.precio * kg

            } else {

                val cantidad =
                    cantidades[producto.id]
                        ?: 0.0

                total +=
                    producto.precio * cantidad
            }
        }

        return total
    }

    fun obtenerCantidadProducto(
        productoId: String
    ): Double {

        return cantidades[productoId] ?: 0.0
    }

    fun obtenerCantidadKg(
        productoId: String
    ): Double {

        return cantidadesKg[productoId] ?: 0.0
    }
}