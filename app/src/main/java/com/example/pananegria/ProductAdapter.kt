package com.example.pananegria

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load

class ProductAdapter(
    private val productos: List<Product>,
    private val onProductoClick:
        (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(view: View)
        : RecyclerView.ViewHolder(view) {

        val imageProducto: ImageView =
            view.findViewById(R.id.imageProducto)

        val textNombre: TextView =
            view.findViewById(R.id.textNombre)

        val textPrecio: TextView =
            view.findViewById(R.id.textPrecio)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_producto,
                parent,
                false
            )

        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(

        holder: ProductViewHolder,
        position: Int
    ) {

        val producto = productos[position]

        holder.setIsRecyclable(false)

        /*
=========================
RESET IMAGEVIEW
=========================
*/

        holder.imageProducto.setImageDrawable(
            null
        )

        holder.imageProducto.setImageBitmap(
            null
        )

        holder.textNombre.text =
            producto.nombre

        val unidad =

            if (

                producto.unidadMedida
                    .contains("kg", true)

            ) {

                "/kg"

            } else {

                "/u"
            }

        val textoCompleto =
            "$${producto.precio.toInt()}$unidad"

        val spannable =
            android.text.SpannableString(
                textoCompleto
            )

        /*
        =========================
        TAMAÑO UNIDAD
        =========================
        */

        spannable.setSpan(

            android.text.style.RelativeSizeSpan(
                0.55f
            ),

            textoCompleto.indexOf("/"),

            textoCompleto.length,

            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        holder.textPrecio.text =
            spannable

        holder.imageProducto.setImageDrawable(
            null
        )

        holder.imageProducto.setImageDrawable(
            null
        )

        if (

            !producto.imageUrl.startsWith(
                "http"
            )

        ) {

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
            )
        }

        holder.itemView.setOnClickListener {

            onProductoClick(producto)
        }
    }

    override fun getItemCount(): Int {

        return productos.size
    }
}