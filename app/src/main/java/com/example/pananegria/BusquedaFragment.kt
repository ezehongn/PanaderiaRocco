package com.example.pananegria

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ImageView

import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import java.text.Normalizer
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

import coil.load
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager


class BusquedaFragment : Fragment() {

    private lateinit var db: FirebaseFirestore

    private lateinit var recyclerProductos: RecyclerView

    private lateinit var adapter: ProductAdapter

    private var productoEditandoImagen: Product? = null

    private var imageDetalleActual: ImageView? = null

    private val seleccionarImagenLauncher =

        registerForActivityResult(

            androidx.activity.result.contract.ActivityResultContracts.GetContent()

        ) { uri ->

            if (uri == null) return@registerForActivityResult

            val producto =
                productoEditandoImagen
                    ?: return@registerForActivityResult

            val inputStream =
                requireContext()
                    .contentResolver
                    .openInputStream(uri)

            val bitmap =

                BitmapFactory.decodeStream(
                    inputStream
                )

            val reducido =

                Bitmap.createScaledBitmap(

                    bitmap,

                    250,

                    250,

                    true
                )

            val outputStream =
                java.io.ByteArrayOutputStream()

            reducido.compress(

                Bitmap.CompressFormat.JPEG,

                45,

                outputStream
            )

            val bytes =
                outputStream.toByteArray()

            val imageBase64 =

                Base64.encodeToString(

                    bytes,

                    Base64.DEFAULT
                )

            db.collection("productos")

                .document(producto.id)

                .update(
                    "imageUrl",
                    imageBase64
                )

                .addOnSuccessListener {

                    val bytes =

                        Base64.decode(

                            imageBase64,

                            Base64.DEFAULT
                        )

                    val bitmapActualizado =

                        BitmapFactory.decodeByteArray(

                            bytes,

                            0,

                            bytes.size
                        )

                    imageDetalleActual?.setImageBitmap(
                        bitmapActualizado
                    )

                    cargarProductos()

                    Toast.makeText(

                        requireContext(),

                        "Imagen actualizada",

                        Toast.LENGTH_SHORT

                    ).show()
                }
        }

    private val listaProductos =
        mutableListOf<Product>()

    private val listaProductosOriginal =
        mutableListOf<Product>()

    override fun onCreateView(

        inflater: LayoutInflater,

        container: ViewGroup?,

        savedInstanceState: Bundle?

    ): View {

        return inflater.inflate(

            R.layout.fragment_busqueda,

            container,

            false
        )

    }

    override fun onViewCreated(

        view: View,

        savedInstanceState: Bundle?

    ) {

        super.onViewCreated(
            view,
            savedInstanceState
        )

        val btnOpciones =
            view.findViewById<FloatingActionButton>(
                R.id.btnOpciones
            )

        btnOpciones.setOnClickListener {

            (activity as MainActivity)
                .mostrarMenuOpciones()
        }

        db = FirebaseFirestore.getInstance()

        val editBuscar =
            view.findViewById<EditText>(
                R.id.editBuscar
            )

        recyclerProductos =
            view.findViewById(
                R.id.recyclerProductos
            )

        adapter =
            ProductAdapter(listaProductos) {

                    producto ->

                mostrarDetalleProducto(producto)
            }

        recyclerProductos.adapter =
            adapter

        recyclerProductos.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        recyclerProductos.addItemDecoration(

            GridSpacingItemDecoration(
                2,
                5,
                true
            )
        )

        cargarProductos()

        editBuscar.addTextChangedListener {

            val textoBusqueda =
                it.toString()
                    .trim()
                    .lowercase()

            listaProductos.clear()

            if (textoBusqueda.isEmpty()) {

                listaProductos.addAll(
                    listaProductosOriginal
                )

            } else {

                val textoBusquedaNormalizado =

                    normalizarTexto(textoBusqueda)

                val productosFiltrados =

                    listaProductosOriginal.filter { producto ->

                        normalizarTexto(
                            producto.nombre
                        ).contains(
                            textoBusquedaNormalizado
                        )

                                ||

                                producto.categorias.any { categoria ->

                                    normalizarTexto(
                                        categoria
                                    ).contains(
                                        textoBusquedaNormalizado
                                    )
                                }
                    }

                listaProductos.addAll(
                    productosFiltrados
                )
            }

            adapter.notifyDataSetChanged()
        }
    }

     fun cargarProductos() {

        db.collection("productos")

            .whereEqualTo(
                "activo",
                true
            )

            .get()

            .addOnSuccessListener { result ->

                listaProductos.clear()

                listaProductosOriginal.clear()

                for (document in result) {

                    val producto =
                        document.toObject(
                            Product::class.java
                        )

                    listaProductos.add(producto)

                    listaProductosOriginal.add(producto)
                }

                adapter.notifyDataSetChanged()
            }

            .addOnFailureListener {

                Toast.makeText(

                    requireContext(),

                    "Error cargando productos",

                    Toast.LENGTH_SHORT

                ).show()
            }
    }

    private fun mostrarDetalleProducto(
        producto: Product
    ) {

        val dialog =
            BottomSheetDialog(requireContext())

        val view = LayoutInflater.from(
            requireContext()
        )

            .inflate(
                R.layout.bottomsheet_detalle_producto,
                null
            )

        dialog.setContentView(view)



        val imageProducto =
            view.findViewById<ImageView>(
                R.id.imageDetalleProducto
            )

        imageDetalleActual = imageProducto

        val textNombre =
            view.findViewById<TextView>(
                R.id.textDetalleNombre
            )

        val textPrecio =
            view.findViewById<TextView>(
                R.id.textDetallePrecio
            )

        val textUnidad =
            view.findViewById<TextView>(
                R.id.textDetalleUnidad
            )

        val textCategorias =
            view.findViewById<TextView>(
                R.id.textDetalleCategorias
            )

        val btnEditarImagen =
            view.findViewById<ImageButton>(
                R.id.btnEditarImagen
            )

        val btnEditarNombre =
            view.findViewById<ImageButton>(
                R.id.btnEditarNombre
            )

        val btnEditarPrecio =
            view.findViewById<ImageButton>(
                R.id.btnEditarPrecio
            )

        val btnEditarUnidad =
            view.findViewById<ImageButton>(
                R.id.btnEditarUnidad
            )

        val btnEditarCategorias =
            view.findViewById<ImageButton>(
                R.id.btnEditarCategorias
            )

        /*
=========================
RESET
=========================
*/

        imageProducto.setImageDrawable(
            null
        )

        imageProducto.setImageBitmap(
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

                    imageProducto.setImageBitmap(
                        bitmap
                    )
                }

            } catch (e: Exception) {

                imageProducto.setImageResource(
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

            imageProducto.load(
                producto.imageUrl
            )
        }

        textNombre.text =
            producto.nombre

        textPrecio.text =
            "$${producto.precio.toInt()}"

        textUnidad.text =
            producto.unidadMedida

        textCategorias.text =
            producto.categorias.joinToString(
                separator = " "
            ) {
                "#$it"
            }

        btnEditarImagen.setOnClickListener {

            mostrarOpcionesImagen(
                producto
            )
        }

        /*
=========================
EDITAR NOMBRE
=========================
*/

        btnEditarNombre.setOnClickListener {

            val editText =
                EditText(requireContext())

            editText.setText(
                producto.nombre
            )

            android.app.AlertDialog
                .Builder(requireContext())

                .setTitle(
                    "Editar nombre"
                )

                .setView(editText)

                .setPositiveButton(
                    "Guardar"
                ) { _, _ ->

                    val nuevoNombre =
                        editText.text
                            .toString()
                            .trim()

                    if (
                        nuevoNombre.isEmpty()
                    ) {
                        return@setPositiveButton
                    }

                    db.collection("productos")
                        .document(producto.id)

                        .update(
                            "nombre",
                            nuevoNombre
                        )

                        .addOnSuccessListener {

                            textNombre.text =
                                nuevoNombre

                            cargarProductos()

                            Toast.makeText(

                                requireContext(),

                                "Nombre actualizado",

                                Toast.LENGTH_SHORT

                            ).show()
                        }
                }

                .setNegativeButton(
                    "Cancelar",
                    null
                )

                .show()
        }

        btnEditarPrecio.setOnClickListener {

            val editText =
                EditText(requireContext())

            editText.inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

            editText.setText(
                producto.precio.toString()
            )

            android.app.AlertDialog
                .Builder(requireContext())

                .setTitle("Editar precio")

                .setView(editText)

                .setPositiveButton("Guardar") { _, _ ->

                    val nuevoPrecio =

                        editText.text
                            .toString()
                            .toDoubleOrNull()

                    if (nuevoPrecio == null) {

                        Toast.makeText(

                            requireContext(),

                            "Precio inválido",

                            Toast.LENGTH_SHORT

                        ).show()

                        return@setPositiveButton
                    }

                    db.collection("productos")
                        .document(producto.id)

                        .update(
                            "precio",
                            nuevoPrecio
                        )

                        .addOnSuccessListener {

                            textPrecio.text =
                                "$${nuevoPrecio.toInt()}"

                            cargarProductos()

                            Toast.makeText(

                                requireContext(),

                                "Precio actualizado",

                                Toast.LENGTH_SHORT

                            ).show()
                        }
                }

                .setNegativeButton(
                    "Cancelar",
                    null
                )

                .show()
        }

        btnEditarUnidad.setOnClickListener {

            val opciones = arrayOf(

                "Por unidad",

                "Por kg"
            )

            android.app.AlertDialog
                .Builder(requireContext())

                .setTitle("Unidad de medida")

                .setItems(opciones) { _, which ->

                    val nuevaUnidad =
                        opciones[which]

                    db.collection("productos")
                        .document(producto.id)

                        .update(
                            "unidadMedida",
                            nuevaUnidad
                        )

                        .addOnSuccessListener {

                            textUnidad.text =
                                nuevaUnidad

                            cargarProductos()

                            Toast.makeText(

                                requireContext(),

                                "Unidad actualizada",

                                Toast.LENGTH_SHORT

                            ).show()
                        }
                }

                .show()
        }

        btnEditarCategorias.setOnClickListener {

            val dialogCategorias =

                BottomSheetDialog(
                    requireContext()
                )

            val viewCategorias =

                layoutInflater.inflate(

                    R.layout.bottomsheet_editar_categorias,

                    null
                )

            dialogCategorias.setContentView(
                viewCategorias
            )

            val chipGroup =

                viewCategorias.findViewById<com.google.android.material.chip.ChipGroup>(

                R.id.chipGroupCategoriasEditar
                )

            val editCategoria =

                viewCategorias.findViewById<EditText>(

                    R.id.editCategoriaNueva
                )

            val btnGuardar =

                viewCategorias.findViewById<Button>(

                    R.id.btnGuardarCategorias
                )

            /*
            =========================
            CARGAR CATEGORIAS
            =========================
            */

            producto.categorias.forEach {

                    categoria ->

                val chip =
                    com.google.android.material.chip.Chip(
                        requireContext()
                    )

                chip.text =
                    "#$categoria"

                chip.isCloseIconVisible =
                    true

                chip.setOnCloseIconClickListener {

                    chipGroup.removeView(
                        chip
                    )
                }

                chipGroup.addView(
                    chip
                )
            }

            /*
            =========================
            AGREGAR NUEVA
            =========================
            */

            editCategoria.setOnEditorActionListener {

                    _, actionId, _ ->

                if (

                    actionId ==
                    android.view.inputmethod.EditorInfo.IME_ACTION_DONE

                ) {

                    val texto =

                        editCategoria.text
                            .toString()
                            .trim()

                    if (

                        texto.isNotEmpty()

                    ) {

                        val chip =
                            com.google.android.material.chip.Chip(
                                requireContext()
                            )

                        chip.text =
                            "#$texto"

                        chip.isCloseIconVisible =
                            true

                        chip.setOnCloseIconClickListener {

                            chipGroup.removeView(
                                chip
                            )
                        }

                        chipGroup.addView(
                            chip
                        )

                        editCategoria.text.clear()
                    }

                    true

                } else {

                    false
                }
            }

            /*
            =========================
            GUARDAR
            =========================
            */

            btnGuardar.setOnClickListener {

                val categorias =
                    mutableListOf<String>()

                for (

                i in 0 until chipGroup.childCount

                ) {

                    val chip =

                        chipGroup.getChildAt(i)

                                as com.google.android.material.chip.Chip

                    categorias.add(

                        chip.text.toString()

                            .replace("#", "")
                    )
                }

                db.collection("productos")

                    .document(
                        producto.id
                    )

                    .update(

                        "categorias",

                        categorias
                    )

                    .addOnSuccessListener {

                        textCategorias.text =

                            categorias.joinToString(
                                separator = " "
                            ) {
                                "#$it"
                            }

                        cargarProductos()

                        dialogCategorias.dismiss()

                        Toast.makeText(

                            requireContext(),

                            "Categorías actualizadas",

                            Toast.LENGTH_SHORT

                        ).show()
                    }
            }

            dialogCategorias.show()
        }
        dialog.show()
    }
    private fun mostrarOpcionesImagen(

        producto: Product

    ) {

        val opciones = arrayOf(

            "Cargar desde URL",

            "Seleccionar de galería"
        )

        android.app.AlertDialog
            .Builder(requireContext())

            .setTitle(
                "Cambiar imagen"
            )

            .setItems(opciones) { _, which ->

                when (which) {

                    0 -> {

                        mostrarDialogoUrl(
                            producto
                        )
                    }

                    1 -> {

                        productoEditandoImagen =
                            producto

                        seleccionarImagenLauncher.launch(
                            "image/*"
                        )
                    }
                }
            }

            .show()
    }
    private fun mostrarDialogoUrl(

        producto: Product

    ){

        val editText =
            EditText(requireContext())

        editText.hint =
            "https://..."

        android.app.AlertDialog
            .Builder(requireContext())

            .setTitle(
                "URL de imagen"
            )

            .setView(editText)

            .setPositiveButton(
                "Guardar"
            ) { _, _ ->

                val url =

                    editText.text
                        .toString()
                        .trim()

                if (url.isEmpty()) {

                    return@setPositiveButton
                }

                db.collection("productos")

                    .document(producto.id)

                    .update(
                        "imageUrl",
                        url
                    )

                    .addOnSuccessListener {

                        imageDetalleActual?.load(
                            url
                        )

                        cargarProductos()

                        Toast.makeText(

                            requireContext(),

                            "Imagen actualizada",

                            Toast.LENGTH_SHORT

                        ).show()
                    }
            }

            .setNegativeButton(
                "Cancelar",
                null
            )

            .show()
    }

    private fun normalizarTexto(
        texto: String
    ): String {

        return Normalizer.normalize(
            texto.lowercase(),
            Normalizer.Form.NFD
        )
            .replace(
                "\\p{InCombiningDiacriticalMarks}+".toRegex(),
                ""
            )
    }

}