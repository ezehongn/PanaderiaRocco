package com.example.pananegria

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import androidx.activity.result.contract.ActivityResultContracts
import androidx.exifinterface.media.ExifInterface
import android.graphics.Matrix

class MainActivity : AppCompatActivity() {

    private var imagePreview: ImageView? = null

    private var textSeleccionarImagen: TextView? = null

    private var imageUri: String = ""

    private var imageBase64: String = ""

    private val seleccionarImagenLauncher =

        registerForActivityResult(

            ActivityResultContracts.GetContent()

        ) { uri ->

            if (uri != null) {

                /*
                =========================
                PREVIEW
                =========================
                */

                imagePreview?.setImageURI(uri)

                textSeleccionarImagen?.visibility =
                    View.GONE

                /*
                =========================
                BITMAP
                =========================
                */

                /*
=========================
INPUT STREAM
=========================
*/

                val inputStream =

                    contentResolver
                        .openInputStream(uri)

                /*
                =========================
                BITMAP ORIGINAL
                =========================
                */

                val bitmapOriginal =

                    BitmapFactory.decodeStream(
                        inputStream
                    )

                /*
                =========================
                EXIF
                =========================
                */

                val exifStream =

                    contentResolver
                        .openInputStream(uri)

                val exif =
                    ExifInterface(exifStream!!)

                val orientacion =
                    exif.getAttributeInt(

                        ExifInterface.TAG_ORIENTATION,

                        ExifInterface.ORIENTATION_NORMAL
                    )

                /*
                =========================
                ROTACION
                =========================
                */

                val matrix =
                    Matrix()

                when (orientacion) {

                    ExifInterface.ORIENTATION_ROTATE_90 -> {

                        matrix.postRotate(90f)
                    }

                    ExifInterface.ORIENTATION_ROTATE_180 -> {

                        matrix.postRotate(180f)
                    }

                    ExifInterface.ORIENTATION_ROTATE_270 -> {

                        matrix.postRotate(270f)
                    }
                }

                /*
                =========================
                BITMAP CORREGIDO
                =========================
                */

                val bitmap =

                    Bitmap.createBitmap(

                        bitmapOriginal,

                        0,

                        0,

                        bitmapOriginal.width,

                        bitmapOriginal.height,

                        matrix,

                        true
                    )

                /*
                =========================
                REDUCIR
                =========================
                */

                val reducido =

                    Bitmap.createScaledBitmap(

                        bitmap,

                        250,

                        250,

                        true
                    )

                /*
                =========================
                COMPRESION
                =========================
                */

                val outputStream =
                    ByteArrayOutputStream()

                reducido.compress(

                    Bitmap.CompressFormat.JPEG,

                    45,

                    outputStream
                )

                /*
                =========================
                BASE64
                =========================
                */

                val bytes =
                    outputStream.toByteArray()

                imageBase64 =

                    Base64.encodeToString(

                        bytes,

                        Base64.DEFAULT
                    )

                /*
                =========================
                LIMPIAR URL
                =========================
                */

                imageUri = ""
            }
        }

    private lateinit var db: FirebaseFirestore

    private lateinit var tabBusqueda: LinearLayout

    private lateinit var tabCuentas: LinearLayout

    private lateinit var tabBalance: LinearLayout

    private var esAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        tabBusqueda =
            findViewById(R.id.tabBusqueda)

        tabCuentas =
            findViewById(R.id.tabCuentas)

        tabBalance =
            findViewById(R.id.tabBalance)

        db = FirebaseFirestore.getInstance()

        /*
        =========================
        USUARIO ACTUAL
        =========================
        */

        val usuario =
            FirebaseAuth.getInstance().currentUser

        if (
            usuario != null &&
            usuario.email == "admin@rocco.com"
        ) {

            esAdmin = true
        }

        /*
=========================
INVITADO
=========================
*/

        if (usuario?.isAnonymous == true) {

            tabBalance.visibility =
                View.GONE
        }

        /*
        =========================
        FRAGMENT INICIAL
        =========================
        */

        supportFragmentManager.beginTransaction()

            .replace(
                R.id.fragmentContainer,
                BusquedaFragment()

            )

            .commit()


        /*
        =========================
        TAB BUSQUEDA
        =========================
        */

        tabBusqueda.setOnClickListener {

            supportFragmentManager.beginTransaction()

                .replace(
                    R.id.fragmentContainer,
                    BusquedaFragment()
                )

                .commit()

            actualizarTabs("busqueda")
        }

        /*
        =========================
        TAB CUENTAS
        =========================
        */

        tabCuentas.setOnClickListener {

            supportFragmentManager.beginTransaction()

                .replace(
                    R.id.fragmentContainer,
                    CuentasFragment()
                )

                .commit()

            actualizarTabs("cuentas")
        }

        tabBalance.setOnClickListener {

            supportFragmentManager.beginTransaction()

                .replace(

                    R.id.fragmentContainer,

                    BalanceFragment()
                )

                .commit()

            actualizarTabs("balance")
        }
    }

    private fun actualizarTabs(

        pantalla: String

    ) {

        /*
        =========================
        RESET
        =========================
        */

        tabBusqueda.setBackgroundColor(
            getColor(android.R.color.transparent)
        )

        tabCuentas.setBackgroundColor(
            getColor(android.R.color.transparent)
        )

        tabBalance.setBackgroundColor(
            getColor(android.R.color.transparent)
        )

        /*
        =========================
        TAB ACTIVA
        =========================
        */

        when (pantalla) {

            "busqueda" -> {

                tabBusqueda.setBackgroundColor(
                    getColor(R.color.tab_activa)
                )
            }

            "cuentas" -> {

                tabCuentas.setBackgroundColor(
                    getColor(R.color.tab_activa)
                )
            }

            "balance" -> {

                tabBalance.setBackgroundColor(
                    getColor(R.color.tab_activa)
                )
            }
        }
    }

    /*
    =========================
    MENU OPCIONES
    =========================
    */

     fun mostrarMenuOpciones() {

        val dialog = BottomSheetDialog(this)

        val view = LayoutInflater.from(this)
            .inflate(R.layout.bottomsheet_menu, null)

        dialog.setContentView(view)

        val btnAgregarProducto =
            view.findViewById<Button>(
                R.id.btnAgregarProducto
            )

        val btnEliminarProducto =
            view.findViewById<Button>(
                R.id.btnEliminarProducto
            )

        val btnModificarPrecios =
            view.findViewById<Button>(
                R.id.btnModificarPrecios
            )

        val btnCerrarSesion =
            view.findViewById<Button>(
                R.id.btnCerrarSesion
            )

        /*
        =========================
        AGREGAR PRODUCTO
        =========================
        */

        btnAgregarProducto.setOnClickListener {

            dialog.dismiss()

            mostrarAgregarProducto()
        }

        /*
        =========================
        ELIMINAR PRODUCTO
        =========================
        */

        btnEliminarProducto.setOnClickListener {

            dialog.dismiss()

            mostrarEliminarProducto()
        }

        /*
        =========================
        MODIFICAR PRECIOS
        =========================
        */

        btnModificarPrecios.setOnClickListener {

            dialog.dismiss()

            mostrarModificarPrecios()
        }

        /*
        =========================
        CERRAR SESION
        =========================
        */

        btnCerrarSesion.setOnClickListener {

            AlertDialog.Builder(this)

                .setTitle("Cerrar sesión")

                .setMessage(
                    "¿Querés cerrar la sesión?"
                )

                .setPositiveButton("Cerrar") { _, _ ->

                    FirebaseAuth
                        .getInstance()
                        .signOut()

                    startActivity(

                        Intent(
                            this,
                            LoginActivity::class.java
                        )
                    )

                    finish()
                }

                .setNegativeButton(
                    "Cancelar",
                    null
                )

                .show()
        }

        /*
        =========================
        RESTRICCIONES
        =========================
        */

        if (!esAdmin) {

            btnEliminarProducto.visibility =
                View.GONE

            btnModificarPrecios.visibility =
                View.GONE
        }

        dialog.show()
    }

    private fun actualizarBusquedaFragment() {

        val fragment =
            supportFragmentManager
                .findFragmentById(R.id.fragmentContainer)

        if (fragment is BusquedaFragment) {

            fragment.cargarProductos()
        }
    }

    /*
    =========================
    AGREGAR PRODUCTO
    =========================
    */

    private fun mostrarAgregarProducto() {

        val dialog = BottomSheetDialog(this)

        val view = LayoutInflater.from(this)

            .inflate(
                R.layout.bottomsheet_agregar_producto,
                null
            )

        dialog.setContentView(view)

        val spinnerUnidad =
            view.findViewById<Spinner>(
                R.id.spinnerUnidad
            )

        val layoutSeleccionarImagen =
            view.findViewById<FrameLayout>(
                R.id.layoutSeleccionarImagen
            )

        imagePreview =
            view.findViewById(
                R.id.imagePreview
            )

        textSeleccionarImagen =
            view.findViewById(
                R.id.textSeleccionarImagen
            )

        val chipGroup =
            view.findViewById<ChipGroup>(
                R.id.chipGroupCategorias
            )

        val editCategoria =
            view.findViewById<EditText>(
                R.id.editCategoria
            )

        val editNombre =
            view.findViewById<EditText>(
                R.id.editNombre
            )

        val editPrecio =
            view.findViewById<EditText>(
                R.id.editPrecio
            )

        val btnGuardar =
            view.findViewById<Button>(
                R.id.btnGuardarProducto
            )

        /*
        =========================
        SPINNER
        =========================
        */

        val opcionesUnidad = listOf(
            "Por unidad",
            "Por kg"
        )

        val adapter = ArrayAdapter(

            this,

            android.R.layout
                .simple_spinner_dropdown_item,

            opcionesUnidad
        )

        spinnerUnidad.adapter = adapter

        /*
        =========================
        IMAGEN
        =========================
        */

        layoutSeleccionarImagen.setOnClickListener {

            mostrarOpcionesImagen()
        }

        /*
        =========================
        CATEGORIAS
        =========================
        */

        editCategoria.setOnEditorActionListener {

                _, actionId, _ ->

            if (
                actionId ==
                EditorInfo.IME_ACTION_DONE
            ) {

                val texto =
                    editCategoria.text
                        .toString()
                        .trim()

                if (texto.isNotEmpty()) {

                    val categoria =
                        texto.lowercase()

                    val chip = Chip(this)

                    chip.text =
                        "#$categoria"

                    chip.isCloseIconVisible =
                        true

                    chip.setOnCloseIconClickListener {

                        chipGroup.removeView(chip)
                    }

                    chipGroup.addView(chip)

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

            val nombre =
                editNombre.text
                    .toString()
                    .trim()

            val precioTexto =
                editPrecio.text
                    .toString()
                    .trim()

            val unidad =
                spinnerUnidad
                    .selectedItem
                    .toString()

            if (nombre.isEmpty()) {

                editNombre.error =
                    "Ingresá un nombre"

                return@setOnClickListener
            }

            val precio =
                precioTexto.toDoubleOrNull()

            if (precio == null) {

                editPrecio.error =
                    "Precio inválido"

                return@setOnClickListener
            }

            if (

                imageUri.isEmpty()

                &&

                imageBase64.isEmpty()

            ) {

                Toast.makeText(

                    this,

                    "Seleccioná una imagen",

                    Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }

            val categorias =
                mutableListOf<String>()

            for (i in 0 until chipGroup.childCount) {

                val chip =
                    chipGroup.getChildAt(i)
                            as Chip

                categorias.add(

                    chip.text.toString()
                        .replace("#", "")
                )
            }

            val docRef =
                db.collection("productos")
                    .document()

            val producto = Product(

                id = docRef.id,

                nombre = nombre,

                precio = precio,

                unidadMedida = unidad,

                categorias = categorias,

                imageUrl =

                    if (imageUri.isNotEmpty()) {

                        imageUri

                    } else {

                        imageBase64
                    },

                activo = true
            )

            docRef.set(producto)

                .addOnSuccessListener {

                    Toast.makeText(

                        this,

                        "Producto guardado",

                        Toast.LENGTH_SHORT

                    ).show()

                    actualizarBusquedaFragment()

                    dialog.dismiss()
                }
        }


        imageUri = ""

        imageBase64 = ""

        dialog.show()
    }

    /*
    =========================
    ELIMINAR PRODUCTO
    =========================
    */

    private fun mostrarEliminarProducto() {

        val dialog = BottomSheetDialog(this)

        val view = LayoutInflater.from(this)
            .inflate(
                R.layout.bottomsheet_eliminar_producto,
                null
            )

        dialog.setContentView(view)

        val editBuscar =
            view.findViewById<AutoCompleteTextView>(
                R.id.editBuscarProducto
            )

        val imageProducto =
            view.findViewById<ImageView>(
                R.id.imageProductoEliminar
            )

        val textNombre =
            view.findViewById<TextView>(
                R.id.textNombreEliminar
            )

        val textPrecio =
            view.findViewById<TextView>(
                R.id.textPrecioEliminar
            )

        val btnEliminar =
            view.findViewById<Button>(
                R.id.btnConfirmarEliminar
            )

        var productoSeleccionado: Product? = null

        val listaProductosBusqueda =
            mutableListOf<Product>()

        val nombresProductos =
            mutableListOf<String>()

        db.collection("productos")
            .whereEqualTo("activo", true)
            .get()

            .addOnSuccessListener { result ->

                listaProductosBusqueda.clear()

                nombresProductos.clear()

                for (document in result) {

                    val producto =
                        document.toObject(Product::class.java)

                    listaProductosBusqueda.add(producto)

                    nombresProductos.add(producto.nombre)
                }

                val adapterBusqueda =
                    ArrayAdapter(

                        this,

                        android.R.layout
                            .simple_dropdown_item_1line,

                        nombresProductos
                    )

                editBuscar.setAdapter(adapterBusqueda)

                editBuscar.threshold = 1
            }

        editBuscar.setOnItemClickListener { _, _, _, _ ->

            val textoSeleccionado =
                editBuscar.text.toString().trim()

            val producto =
                listaProductosBusqueda.find {

                    it.nombre.equals(
                        textoSeleccionado,
                        true
                    )
                }

            producto?.let {

                productoSeleccionado = it

                if (

                    it.imageUrl.startsWith(
                        "http"
                    )

                ) {

                    imageProducto.load(
                        it.imageUrl
                    )

                } else {

                    val bytes =

                        Base64.decode(

                            it.imageUrl,

                            Base64.DEFAULT
                        )

                    val bitmap =

                        BitmapFactory.decodeByteArray(

                            bytes,

                            0,

                            bytes.size
                        )

                    imageProducto.setImageBitmap(
                        bitmap
                    )
                }

                textNombre.text =
                    it.nombre

                textPrecio.text =
                    "$${it.precio.toInt()}"
            }
        }

        btnEliminar.setOnClickListener {

            val producto =
                productoSeleccionado

            if (producto == null) {

                Toast.makeText(

                    this,

                    "Seleccioná un producto",

                    Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }

            AlertDialog.Builder(this)

                .setTitle("Eliminar producto")

                .setMessage(
                    "¿Estás seguro que querés eliminar el producto?"
                )

                .setPositiveButton("Eliminar") { _, _ ->

                    db.collection("productos")
                        .document(producto.id)

                        .update(
                            "activo",
                            false
                        )

                        .addOnSuccessListener {

                            Toast.makeText(

                                this,

                                "Producto eliminado",

                                Toast.LENGTH_SHORT

                            ).show()

                            actualizarBusquedaFragment()

                            dialog.dismiss()
                        }
                }

                .setNegativeButton(
                    "Cancelar",
                    null
                )

                .show()
        }

        dialog.show()
    }

    /*
    =========================
    MODIFICAR PRECIOS
    =========================
    */

    private fun mostrarModificarPrecios() {

        val dialog = BottomSheetDialog(this)

        val view = LayoutInflater.from(this)
            .inflate(
                R.layout.bottomsheet_modificar_precios,
                null
            )

        dialog.setContentView(view)

        val editBuscar =
            view.findViewById<AutoCompleteTextView>(
                R.id.editBuscarModificar
            )

        val layoutProductos =
            view.findViewById<LinearLayout>(
                R.id.layoutProductosSeleccionados
            )

        val radioAumento =
            view.findViewById<RadioButton>(
                R.id.radioAumento
            )

        val radioDescuento =
            view.findViewById<RadioButton>(
                R.id.radioDescuento
            )

        val editPorcentaje =
            view.findViewById<EditText>(
                R.id.editPorcentaje
            )

        val btnCancelar =
            view.findViewById<Button>(
                R.id.btnCancelarCambios
            )

        val btnConfirmar =
            view.findViewById<Button>(
                R.id.btnConfirmarCambios
            )

        val productosSeleccionados =
            mutableListOf<Product>()

        val listaProductosBusqueda =
            mutableListOf<Product>()

        val opcionesBusqueda =
            mutableListOf<String>()

        /*
        =========================
        CARGAR PRODUCTOS
        =========================
        */

        db.collection("productos")
            .whereEqualTo("activo", true)
            .get()

            .addOnSuccessListener { result ->

                for (document in result) {

                    val producto =
                        document.toObject(Product::class.java)

                    listaProductosBusqueda.add(producto)

                    opcionesBusqueda.add(producto.nombre)

                    producto.categorias.forEach {

                        if (!opcionesBusqueda.contains(it)) {

                            opcionesBusqueda.add(it)
                        }
                    }
                }

                val adapterBusqueda =
                    ArrayAdapter(

                        this,

                        android.R.layout
                            .simple_dropdown_item_1line,

                        opcionesBusqueda
                    )

                editBuscar.setAdapter(adapterBusqueda)

                editBuscar.threshold = 1
            }

        /*
        =========================
        SELECCIONAR PRODUCTOS
        =========================
        */

        editBuscar.setOnItemClickListener { _, _, _, _ ->

            val textoSeleccionado =
                editBuscar.text.toString().trim()

            val productosEncontrados =

                listaProductosBusqueda.filter {

                    it.nombre.equals(
                        textoSeleccionado,
                        true
                    )

                            ||

                            it.categorias.any { categoria ->

                                categoria.equals(
                                    textoSeleccionado,
                                    true
                                )
                            }
                }

            productosEncontrados.forEach { producto ->

                if (
                    productosSeleccionados.any {
                        it.id == producto.id
                    }
                ) {
                    return@forEach
                }

                productosSeleccionados.add(producto)

                val itemLayout =
                    LinearLayout(this)

                itemLayout.orientation =
                    LinearLayout.HORIZONTAL

                itemLayout.gravity =
                    Gravity.CENTER_VERTICAL

                itemLayout.setPadding(
                    0,
                    20,
                    0,
                    20
                )

                /*
                =========================
                BOTON ELIMINAR
                =========================
                */

                val btnEliminarItem =
                    ImageView(this)

                btnEliminarItem.layoutParams =
                    LinearLayout.LayoutParams(
                        70,
                        70
                    )

                btnEliminarItem.setImageResource(
                    android.R.drawable
                        .ic_menu_close_clear_cancel
                )

                btnEliminarItem.setOnClickListener {

                    productosSeleccionados.removeAll {

                        it.id == producto.id
                    }

                    layoutProductos.removeView(itemLayout)
                }

                /*
                =========================
                NOMBRE
                =========================
                */

                val textNombre =
                    TextView(this)

                textNombre.layoutParams =
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )

                textNombre.text =
                    producto.nombre

                textNombre.textSize = 18f

                textNombre.setPadding(
                    24,
                    0,
                    0,
                    0
                )

                /*
                =========================
                PRECIO ORIGINAL
                =========================
                */

                val textPrecioOriginal =
                    TextView(this)

                textPrecioOriginal.layoutParams =
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                textPrecioOriginal.text =
                    "$${producto.precio.toInt()}"

                textPrecioOriginal.textSize =
                    18f

                textPrecioOriginal.setPadding(
                    0,
                    0,
                    40,
                    0
                )

                /*
                =========================
                NUEVO PRECIO
                =========================
                */

                val textNuevoPrecio =
                    TextView(this)

                textNuevoPrecio.tag =
                    producto.id

                textNuevoPrecio.textSize =
                    18f

                /*
                =========================
                AGREGAR VISTAS
                =========================
                */

                itemLayout.addView(btnEliminarItem)

                itemLayout.addView(textNombre)

                itemLayout.addView(textPrecioOriginal)

                itemLayout.addView(textNuevoPrecio)

                layoutProductos.addView(itemLayout)
            }
        }

        /*
        =========================
        ACTUALIZAR PREVIEW
        =========================
        */

        fun actualizarPreview() {

            val porcentajeTexto =
                editPorcentaje.text.toString()

            if (porcentajeTexto.isEmpty()) {

                return
            }

            val porcentaje =
                porcentajeTexto.toDoubleOrNull()

            if (porcentaje == null) {

                return
            }

            for (i in 0 until layoutProductos.childCount) {

                val itemLayout =
                    layoutProductos.getChildAt(i)
                            as LinearLayout

                val textNuevoPrecio =
                    itemLayout.getChildAt(3)
                            as TextView

                val productoId =
                    textNuevoPrecio.tag.toString()

                val producto =
                    productosSeleccionados.find {

                        it.id == productoId
                    }

                producto?.let {

                    var nuevoPrecio =
                        it.precio

                    /*
                    =========================
                    AUMENTO
                    =========================
                    */

                    if (radioAumento.isChecked) {

                        nuevoPrecio +=
                            (
                                    it.precio *
                                            porcentaje / 100
                                    )

                        textNuevoPrecio.setTextColor(
                            getColor(
                                android.R.color
                                    .holo_red_dark
                            )
                        )
                    }

                    /*
                    =========================
                    DESCUENTO
                    =========================
                    */

                    else if (radioDescuento.isChecked) {

                        nuevoPrecio -=
                            (
                                    it.precio *
                                            porcentaje / 100
                                    )

                        textNuevoPrecio.setTextColor(
                            getColor(
                                android.R.color
                                    .holo_green_dark
                            )
                        )
                    }

                    textNuevoPrecio.text =
                        "→  $${nuevoPrecio.toInt()}"
                }
            }
        }

        /*
        =========================
        LISTENERS
        =========================
        */

        editPorcentaje.addTextChangedListener {

            actualizarPreview()
        }

        radioAumento.setOnCheckedChangeListener { _, _ ->

            actualizarPreview()
        }

        radioDescuento.setOnCheckedChangeListener { _, _ ->

            actualizarPreview()
        }

        /*
        =========================
        CANCELAR
        =========================
        */

        btnCancelar.setOnClickListener {

            dialog.dismiss()
        }

        /*
        =========================
        CONFIRMAR
        =========================
        */

        btnConfirmar.setOnClickListener {

            val porcentajeTexto =
                editPorcentaje.text.toString()

            if (
                productosSeleccionados.isEmpty()
            ) {

                Toast.makeText(

                    this,

                    "Seleccioná productos",

                    Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }

            if (porcentajeTexto.isEmpty()) {

                Toast.makeText(

                    this,

                    "Ingresá un porcentaje",

                    Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }

            val porcentaje =
                porcentajeTexto.toDouble()

            for (producto in productosSeleccionados) {

                var nuevoPrecio =
                    producto.precio

                if (radioAumento.isChecked) {

                    nuevoPrecio +=
                        (
                                producto.precio *
                                        porcentaje / 100
                                )

                } else {

                    nuevoPrecio -=
                        (
                                producto.precio *
                                        porcentaje / 100
                                )
                }

                db.collection("productos")
                    .document(producto.id)

                    .update(
                        "precio",
                        nuevoPrecio
                    )
            }

            Toast.makeText(

                this,

                "Precios actualizados",

                Toast.LENGTH_SHORT

            ).show()

            actualizarBusquedaFragment()

            dialog.dismiss()
        }

        dialog.show()
    }

    /*
    =========================
    IMAGEN
    =========================
    */

    private fun mostrarOpcionesImagen() {

        val opciones = arrayOf(

            "Cargar desde URL",

            "Seleccionar de galería"
        )

        AlertDialog.Builder(this)

            .setTitle(
                "Seleccionar imagen"
            )

            .setItems(opciones) { _, which ->

                when (which) {

                    0 -> {

                        mostrarDialogoUrl()
                    }

                    1 -> {

                        seleccionarImagenLauncher.launch(
                            "image/*"
                        )
                    }
                }
            }

            .show()
    }

    private fun mostrarDialogoUrl() {

        val editText = EditText(this)

        editText.hint = "https://..."

        AlertDialog.Builder(this)

            .setTitle("Cargar imagen desde URL")

            .setView(editText)

            .setPositiveButton("Cargar") { _, _ ->

                val url =
                    editText.text.toString().trim()

                if (url.isEmpty()) {

                    Toast.makeText(

                        this,

                        "Ingresá una URL",

                        Toast.LENGTH_SHORT

                    ).show()

                    return@setPositiveButton
                }

                imageUri = url

                imageBase64 = ""

                imagePreview?.load(url) {

                    crossfade(true)

                    listener(

                        onSuccess = { _, _ ->

                            textSeleccionarImagen?.visibility =
                                View.GONE
                        },

                        onError = { _, _ ->

                            Toast.makeText(

                                this@MainActivity,

                                "Error cargando imagen",

                                Toast.LENGTH_SHORT

                            ).show()
                        }
                    )
                }
            }

            .setNegativeButton(
                "Cancelar",
                null
            )

            .show()
    }
}