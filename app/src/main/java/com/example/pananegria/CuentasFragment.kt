package com.example.pananegria

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.bottomsheet.BottomSheetDialog
import android.widget.TextView
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.AutoCompleteTextView
import android.widget.Button
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.widget.addTextChangedListener
import com.google.firebase.auth.FirebaseAuth

class CuentasFragment : Fragment() {
    private lateinit var db: FirebaseFirestore
    private lateinit var recyclerDeudores: RecyclerView

    private lateinit var adapter: DeudoresAdapter

    private val listaClientes =
        mutableListOf<Cliente>()

    private val listaClientesOriginal =
        mutableListOf<Cliente>()

    private var mostrarSaldadas = false

    override fun onCreateView(

        inflater: LayoutInflater,

        container: ViewGroup?,

        savedInstanceState: Bundle?


    ): View {

        val view = inflater.inflate(

            R.layout.fragment_cuentas,

            container,

            false
        )

        db = FirebaseFirestore.getInstance()

        recyclerDeudores =
            view.findViewById(R.id.recyclerDeudores)

        adapter =
            DeudoresAdapter(listaClientes) {

                    cliente ->

                mostrarDetalleDeudor(cliente)
            }

        recyclerDeudores.adapter =
            adapter

        recyclerDeudores.layoutManager =
            LinearLayoutManager(requireContext())

        cargarDeudores()

        val btnOpcionesCuentas =
            view.findViewById<FloatingActionButton>(
                R.id.btnOpcionesCuentas
            )

        val editBuscarDeudor =
            view.findViewById<EditText>(
                R.id.editBuscarDeudor
            )
        editBuscarDeudor.addTextChangedListener {

            val texto =
                it.toString()
                    .trim()
                    .lowercase()

            listaClientes.clear()

            /*
            =========================
            VACIO
            =========================
            */

            if (texto.isEmpty()) {

                listaClientes.addAll(
                    listaClientesOriginal
                )
            }

            /*
            =========================
            FILTRAR
            =========================
            */

            else {

                val filtrados =

                    listaClientesOriginal.filter { cliente ->

                        cliente.nombre
                            .lowercase()
                            .contains(texto)

                                ||

                                cliente.apodos.any { apodo ->

                                    apodo
                                        .lowercase()
                                        .contains(texto)
                                }
                    }

                listaClientes.addAll(
                    filtrados
                )
            }

            adapter.notifyDataSetChanged()
        }

        btnOpcionesCuentas.setOnClickListener {

            mostrarMenuDeudas()
        }

        return view
    }

    private fun mostrarMenuDeudas() {

        val dialog = BottomSheetDialog(requireContext())

        val view = layoutInflater.inflate(
            R.layout.bottomsheet_menu_deudas,
            null
        )

        dialog.setContentView(view)

        val btnRegistrar = view.findViewById<View>(R.id.btnRegistrarDeuda)

        btnRegistrar.setOnClickListener {
            dialog.dismiss()
            mostrarRegistrarDeuda()
        }

        val btnVerEstadisticas =
            view.findViewById<Button>(
                R.id.btnVerEstadisticas
            )

        btnVerEstadisticas.setOnClickListener {

            mostrarBottomSheetEstadisticas()
        }

        val btnCerrarSesion =
            view.findViewById<Button>(
                R.id.btnCerrarSesion
            )

        btnCerrarSesion.setOnClickListener {

            AlertDialog.Builder(requireContext())

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
                            requireContext(),
                            LoginActivity::class.java
                        )
                    )

                    requireActivity().finish()
                }

                .setNegativeButton(
                    "Cancelar",
                    null
                )

                .show()
        }

        val btnToggleSaldadas =
            view.findViewById<Button>(
                R.id.btnToggleSaldadas
            )

        btnToggleSaldadas.text =

            if (mostrarSaldadas) {

                "Ocultar cuentas saldadas"

            } else {

                "Mostrar cuentas saldadas"
            }

        btnToggleSaldadas.setOnClickListener {

            mostrarSaldadas =
                !mostrarSaldadas

            if (mostrarSaldadas) {

                btnToggleSaldadas.text =
                    "Ocultar cuentas saldadas"

            } else {

                btnToggleSaldadas.text =
                    "Mostrar cuentas saldadas"
            }

            /*
            =========================
            RECARGAR LISTA
            =========================
            */

            cargarDeudores()
        }

        dialog.show()
    }

    private fun cargarDeudores() {

        db.collection("clientes")

            .get()

            .addOnSuccessListener { clientesResult ->

                listaClientes.clear()

                listaClientesOriginal.clear()

                val nuevaLista =
                    mutableListOf<Cliente>()

                var procesados = 0

                for (clienteDoc in clientesResult) {

                    val cliente =
                        clienteDoc.toObject(
                            Cliente::class.java
                        )

                    db.collection("detalleDeuda")

                        .whereEqualTo(
                            "clienteId",
                            cliente.id
                        )

                        .get()

                        .addOnSuccessListener { detallesResult ->

                            var totalPendiente = 0.0

                            for (detalleDoc in detallesResult) {

                                val detalle =
                                    detalleDoc.toObject(
                                        DetalleDeuda::class.java
                                    )

                                if (!detalle.pagado) {

                                    totalPendiente +=
                                        detalle.subtotal
                                }
                            }

                            cliente.deudaTotal =
                                totalPendiente

                            /*
                            =========================
                            SOLO SI TIENE DEUDA
                            =========================
                            */

                            if (mostrarSaldadas) {

                                nuevaLista.add(cliente)

                            } else {

                                if (totalPendiente > 0) {

                                    nuevaLista.add(cliente)
                                }
                            }

                            /*
                            =========================
                            REFRESH FINAL
                            =========================
                            */

                            procesados++

                            if (
                                procesados ==
                                clientesResult.size()
                            ) {

                                /*
                                =========================
                                LIMPIAR
                                =========================
                                */

                                listaClientes.clear()

                                listaClientesOriginal.clear()

                                /*
                                =========================
                                MOSTRAR / OCULTAR SALDADAS
                                =========================
                                */

                                if (mostrarSaldadas) {

                                    listaClientes.addAll(
                                        nuevaLista
                                    )

                                    listaClientesOriginal.addAll(
                                        nuevaLista
                                    )

                                } else {

                                    val pendientes =

                                        nuevaLista.filter {

                                            it.deudaTotal > 0
                                        }

                                    listaClientes.addAll(
                                        pendientes
                                    )

                                    listaClientesOriginal.addAll(
                                        pendientes
                                    )
                                }

                                /*
                                =========================
                                REFRESH
                                =========================
                                */

                                adapter.notifyDataSetChanged()
                            }
                        }
                }
            }
    }

    private fun mostrarBottomSheetEstadisticas() {

        val dialog =

            BottomSheetDialog(
                requireContext()
            )

        val view = layoutInflater.inflate(

            R.layout.bottomsheet_estadisticas_deuda,

            null
        )

        dialog.setContentView(view)

        /*
        =========================
        TEXTS
        =========================
        */

        val textDeudaTotal =
            view.findViewById<TextView>(
                R.id.textDeudaTotal
            )

        val textCuentasActivas =
            view.findViewById<TextView>(
                R.id.textCuentasActivas
            )

        val textCuentaMasVieja =
            view.findViewById<TextView>(
                R.id.textCuentaMasVieja
            )

        val textClienteMasViejo =
            view.findViewById<TextView>(
                R.id.textClienteMasViejo
            )

        val textPromedioDeuda =
            view.findViewById<TextView>(
                R.id.textPromedioDeuda
            )

        val textTop1 =
            view.findViewById<TextView>(
                R.id.textTop1
            )

        val textTop2 =
            view.findViewById<TextView>(
                R.id.textTop2
            )

        val textTop3 =
            view.findViewById<TextView>(
                R.id.textTop3
            )

        /*
        =========================
        CLIENTES
        =========================
        */

        db.collection("clientes")

            .get()

            .addOnSuccessListener { result ->

                val lista =
                    mutableListOf<Cliente>()

                var procesados = 0

                /*
                =========================
                RECORRER CLIENTES
                =========================
                */

                for (document in result) {

                    val cliente =
                        document.toObject(
                            Cliente::class.java
                        )

                    db.collection("detalleDeuda")

                        .whereEqualTo(
                            "clienteId",
                            cliente.id
                        )

                        .get()

                        .addOnSuccessListener { detalles ->

                            var totalPendiente = 0.0

                            for (detalleDoc in detalles) {

                                val detalle =
                                    detalleDoc.toObject(
                                        DetalleDeuda::class.java
                                    )

                                if (!detalle.pagado) {

                                    totalPendiente +=
                                        detalle.subtotal
                                }
                            }

                            cliente.deudaTotal =
                                totalPendiente

                            /*
                            =========================
                            SOLO ACTIVOS
                            =========================
                            */

                            if (totalPendiente > 0) {

                                lista.add(cliente)
                            }

                            procesados++

                            /*
                            =========================
                            FINAL
                            =========================
                            */

                            if (
                                procesados ==
                                result.size()
                            ) {

                                /*
                                =========================
                                TOTAL GENERAL
                                =========================
                                */

                                val deudaTotal =

                                    lista.sumOf {
                                        it.deudaTotal
                                    }

                                textDeudaTotal.text =

                                    "$${deudaTotal.toInt()}"

                                /*
                                =========================
                                CUENTAS ACTIVAS
                                =========================
                                */

                                textCuentasActivas.text =

                                    lista.size.toString()

                                /*
                                =========================
                                PROMEDIO
                                =========================
                                */

                                val promedio =

                                    if (lista.isNotEmpty()) {

                                        deudaTotal / lista.size

                                    } else {

                                        0.0
                                    }

                                textPromedioDeuda.text =

                                    "$${promedio.toInt()}"

                                /*
                                =========================
                                CUENTA MAS VIEJA
                                =========================
                                */

                                val masViejo =

                                    lista.minByOrNull {
                                        it.fechaRegistro
                                    }

                                if (masViejo != null) {

                                    val diferencia =

                                        System.currentTimeMillis() -
                                                masViejo.fechaRegistro

                                    val dias =

                                        diferencia /
                                                (1000 * 60 * 60 * 24)

                                    textCuentaMasVieja.text =

                                        "$dias días"

                                    textClienteMasViejo.text =

                                        masViejo.nombre
                                }

                                /*
                                =========================
                                TOP DEUDORES
                                =========================
                                */

                                val top =

                                    lista.sortedByDescending {
                                        it.deudaTotal
                                    }

                                /*
=========================
TOP 1
=========================
*/

                                if (top.isNotEmpty()) {

                                    val texto =

                                        "1. ${top[0].nombre} - $${top[0].deudaTotal.toInt()}"

                                    val spannable =

                                        android.text.SpannableString(
                                            texto
                                        )

                                    val inicioMonto =
                                        texto.indexOf("$")

                                    spannable.setSpan(

                                        android.text.style.ForegroundColorSpan(
                                            android.graphics.Color.parseColor(
                                                "#D94B3D"
                                            )
                                        ),

                                        inicioMonto,

                                        texto.length,

                                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )

                                    textTop1.text =
                                        spannable

                                } else {

                                    textTop1.text = ""
                                }

                                /*
                                =========================
                                TOP 2
                                =========================
                                */

                                if (top.size >= 2) {

                                    val texto =

                                        "2. ${top[1].nombre} - $${top[1].deudaTotal.toInt()}"

                                    val spannable =

                                        android.text.SpannableString(
                                            texto
                                        )

                                    val inicioMonto =
                                        texto.indexOf("$")

                                    spannable.setSpan(

                                        android.text.style.ForegroundColorSpan(
                                            android.graphics.Color.parseColor(
                                                "#D94B3D"
                                            )
                                        ),

                                        inicioMonto,

                                        texto.length,

                                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )

                                    textTop2.text =
                                        spannable

                                } else {

                                    textTop2.text = ""
                                }

                                /*
                                =========================
                                TOP 3
                                =========================
                                */

                                if (top.size >= 3) {

                                    val texto =

                                        "3. ${top[2].nombre} - $${top[2].deudaTotal.toInt()}"

                                    val spannable =

                                        android.text.SpannableString(
                                            texto
                                        )

                                    val inicioMonto =
                                        texto.indexOf("$")

                                    spannable.setSpan(

                                        android.text.style.ForegroundColorSpan(
                                            android.graphics.Color.parseColor(
                                                "#D94B3D"
                                            )
                                        ),

                                        inicioMonto,

                                        texto.length,

                                        android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )

                                    textTop3.text =
                                        spannable

                                } else {

                                    textTop3.text = ""
                                }
                            }
                        }
                }
            }

        dialog.show()
    }
    private fun mostrarRegistrarDeuda() {

        val dialog =
            BottomSheetDialog(requireContext())

        val view = LayoutInflater.from(
            requireContext()
        )

            .inflate(
                R.layout.bottomsheet_registrar_deuda,
                null
            )

        dialog.setContentView(view)

        /*
        =========================
        VISTAS
        =========================
        */

        val editBuscarProducto =
            view.findViewById<AutoCompleteTextView>(
                R.id.editBuscarProducto
            )

        val recyclerProductos =
            view.findViewById<RecyclerView>(
                R.id.recyclerProductosSeleccionados
            )

        val textTotal =
            view.findViewById<TextView>(
                R.id.textTotalFiado
            )

        val btnRegistrarFiado =
            view.findViewById<Button>(
                R.id.btnRegistrarFiado
            )

        val editCliente =
            view.findViewById<AutoCompleteTextView>(
                R.id.editCliente
            )

        val listaClientesFirebase =
            mutableListOf<Cliente>()

        /*
        =========================
        LISTAS
        =========================
        */

        val listaProductosFirebase =
            mutableListOf<Product>()

        val listaProductosSeleccionados =
            mutableListOf<Product>()

        /*
        =========================
        ADAPTER
        =========================
        */

        val adapterFiado =
            ProductoFiadoAdapter(
                listaProductosSeleccionados
            ) { total ->

                textTotal.text =
                    "$${total.toInt()}"
            }

        btnRegistrarFiado.setOnClickListener {

            val nombreCliente =
                editCliente.text.toString()
                    .trim()

            /*
            =========================
            VALIDAR CLIENTE
            =========================
            */

            if (nombreCliente.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Ingresá un cliente",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            /*
            =========================
            VALIDAR PRODUCTOS
            =========================
            */

            if (listaProductosSeleccionados.isEmpty()) {

                Toast.makeText(
                    requireContext(),
                    "Agregá productos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            /*
            =========================
            BUSCAR CLIENTE
            =========================
            */

            val clienteExistente =

                listaClientesFirebase.find {

                    it.nombre.equals(
                        nombreCliente,
                        true
                    )

                            ||

                            it.apodos.any { apodo ->

                                apodo.equals(
                                    nombreCliente,
                                    true
                                )
                            }
                }

            /*
            =========================
            CLIENTE EXISTE
            =========================
            */

            if (clienteExistente != null) {

                registrarDeuda(

                    clienteId =
                        clienteExistente.id,

                    clienteNombre =
                        clienteExistente.nombre,

                    adapterFiado =
                        adapterFiado,

                    listaProductosSeleccionados =
                        listaProductosSeleccionados,

                    dialog =
                        dialog
                )
            }

            /*
            =========================
            CREAR CLIENTE
            =========================
            */

            else {

                val docCliente =
                    db.collection("clientes")
                        .document()

                val cliente = Cliente(

                    id = docCliente.id,

                    nombre = nombreCliente,

                    fechaRegistro =
                        System.currentTimeMillis()
                )

                docCliente.set(cliente)

                    .addOnSuccessListener {

                        registrarDeuda(

                            clienteId =
                                docCliente.id,

                            clienteNombre =
                                nombreCliente,

                            adapterFiado =
                                adapterFiado,

                            listaProductosSeleccionados =
                                listaProductosSeleccionados,

                            dialog =
                                dialog
                        )
                    }
            }
        }


        recyclerProductos.adapter =
            adapterFiado

        recyclerProductos.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        /*
        =========================
        CARGAR PRODUCTOS FIREBASE
        =========================
        */

        db.collection("productos")

            .whereEqualTo(
                "activo",
                true
            )

            .get()

            .addOnSuccessListener { result ->

                val nombresProductos =
                    mutableListOf<String>()

                listaProductosFirebase.clear()

                for (document in result) {

                    val producto =
                        document.toObject(
                            Product::class.java
                        )

                    listaProductosFirebase.add(producto)

                    nombresProductos.add(
                        producto.nombre
                    )
                }

                /*
                =========================
                AUTOCOMPLETE
                =========================
                */

                val adapterBusqueda =
                    ArrayAdapter(

                        requireContext(),

                        android.R.layout
                            .simple_dropdown_item_1line,

                        nombresProductos
                    )

                editBuscarProducto.setAdapter(
                    adapterBusqueda
                )

                editBuscarProducto.threshold = 1
            }

        /*
=========================
CARGAR CLIENTES
=========================
*/

        db.collection("clientes")

            .get()

            .addOnSuccessListener { result ->

                listaClientesFirebase.clear()

                val sugerencias =
                    mutableListOf<String>()

                for (document in result) {

                    val cliente =
                        document.toObject(
                            Cliente::class.java
                        )

                    listaClientesFirebase.add(
                        cliente
                    )

                    /*
                    =========================
                    NOMBRE
                    =========================
                    */

                    sugerencias.add(
                        cliente.nombre
                    )

                    /*
                    =========================
                    APODOS
                    =========================
                    */

                    cliente.apodos.forEach { apodo ->

                        sugerencias.add(
                            "$apodo (${cliente.nombre})"
                        )
                    }
                }

                /*
                =========================
                AUTOCOMPLETE CLIENTES
                =========================
                */

                val adapterClientes =
                    ArrayAdapter(

                        requireContext(),

                        android.R.layout
                            .simple_dropdown_item_1line,

                        sugerencias.distinct()
                    )

                editCliente.setAdapter(
                    adapterClientes
                )

                editCliente.threshold = 1

                editCliente.setOnItemClickListener {

                        _, _, position, _ ->

                    val seleccionado =

                        adapterClientes.getItem(position)
                            .toString()

                    val textoReal =

                        if (seleccionado.contains("(")) {

                            seleccionado
                                .substringBefore("(")
                                .trim()

                        } else {

                            seleccionado
                        }

                    editCliente.setText(textoReal)

                    editCliente.setSelection(
                        textoReal.length
                    )
                }
            }

        /*
        =========================
        SELECCIONAR PRODUCTO
        =========================
        */

        editBuscarProducto.setOnItemClickListener {

                _, _, _, _ ->

            val nombreSeleccionado =
                editBuscarProducto.text
                    .toString()

            val producto =
                listaProductosFirebase.find {

                    it.nombre == nombreSeleccionado
                }

            producto?.let {

                /*
                =========================
                EVITAR DUPLICADOS
                =========================
                */

                val yaExiste =
                    listaProductosSeleccionados.any {

                        it.id == producto.id
                    }

                if (!yaExiste) {

                    listaProductosSeleccionados.add(
                        producto
                    )

                    adapterFiado.notifyItemInserted(
                        listaProductosSeleccionados.size - 1
                    )
                }

                editBuscarProducto.text.clear()
            }
        }

        dialog.show()
    }

    private fun mostrarDetalleDeudor(
        cliente: Cliente
    ) {

        val dialog =
            BottomSheetDialog(requireContext())

        val view = layoutInflater.inflate(
            R.layout.bottomsheet_detalle_deudor,
            null
        )

        dialog.setContentView(view)

        /*
        =========================
        VISTAS
        =========================
        */


        val btnTogglePagadas =
            view.findViewById<Button>(
                R.id.btnTogglePagadas
            )

        val btnPagarTodo =
            view.findViewById<Button>(
                R.id.btnPagarTodo
            )

        val btnEditarNombre =
            view.findViewById<ImageView>(
                R.id.btnEditarNombre
            )

        val chipGroup =
            view.findViewById<ChipGroup>(
                R.id.chipGroupApodos
            )

        val editApodo =
            view.findViewById<EditText>(
                R.id.editApodo
            )

        val btnAgregarApodo =
            view.findViewById<ImageView>(
                R.id.btnAgregarApodo
            )

        val textNombre =
            view.findViewById<TextView>(
                R.id.textNombreClienteDetalle
            )

        val textFooter =
            view.findViewById<TextView>(
                R.id.textFooterPendiente
            )

        val recycler =
            view.findViewById<RecyclerView>(
                R.id.recyclerDetalleDeuda
            )

        /*
        =========================
        NOMBRE
        =========================
        */

        textNombre.text =
            cliente.nombre

        btnEditarNombre.setOnClickListener {

            val editNombre =
                EditText(requireContext())

            editNombre.setText(
                cliente.nombre
            )

            androidx.appcompat.app.AlertDialog.Builder(
                requireContext()
            )

                .setTitle("Editar nombre")

                .setView(editNombre)

                .setPositiveButton("Guardar") {

                        _, _ ->

                    val nuevoNombre =
                        editNombre.text.toString()
                            .trim()

                    if (nuevoNombre.isEmpty()) {

                        return@setPositiveButton
                    }

                    /*
                    =========================
                    FIREBASE
                    =========================
                    */

                    db.collection("clientes")

                        .document(cliente.id)

                        .update(
                            "nombre",
                            nuevoNombre
                        )

                        .addOnSuccessListener {

                            /*
                            =========================
                            ACTUALIZAR DEUDAS
                            =========================
                            */

                            db.collection("deudas")

                                .whereEqualTo(
                                    "clienteId",
                                    cliente.id
                                )

                                .get()

                                .addOnSuccessListener { result ->

                                    for (document in result) {

                                        db.collection("deudas")

                                            .document(document.id)

                                            .update(
                                                "clienteNombre",
                                                nuevoNombre
                                            )
                                    }

                                    textNombre.text =
                                        nuevoNombre

                                    cargarDeudores()
                                }
                        }
                }

                .setNegativeButton(
                    "Cancelar",
                    null
                )

                .show()
        }

        val listaApodos =
            cliente.apodos.toMutableList()

        listaApodos.forEach { apodo ->

            crearChipApodo(

                apodo,

                chipGroup,

                listaApodos,

                cliente.id
            )
        }

        btnAgregarApodo.setOnClickListener {

            val apodo =
                editApodo.text.toString()
                    .trim()

            if (apodo.isEmpty()) {

                return@setOnClickListener
            }

            /*
            =========================
            EVITAR DUPLICADOS
            =========================
            */

            if (
                listaApodos.contains(apodo)
            ) {

                return@setOnClickListener
            }

            /*
            =========================
            AGREGAR LOCAL
            =========================
            */

            listaApodos.add(apodo)

            /*
            =========================
            FIREBASE
            =========================
            */

            db.collection("clientes")

                .document(cliente.id)

                .update(
                    "apodos",
                    listaApodos
                )

                .addOnSuccessListener {

                    crearChipApodo(

                        apodo,

                        chipGroup,

                        listaApodos,

                        cliente.id
                    )

                    editApodo.text.clear()

                    cargarDeudores()
                }
        }

        /*
        =========================
        LISTA
        =========================
        */

        val listaDetalles =
            mutableListOf<DetalleDeuda>()

        var ocultarPagadas = true

        btnPagarTodo.setOnClickListener {

            val totalPendiente =

                listaDetalles
                    .filter { !it.pagado }
                    .sumOf { it.subtotal }

            /*
            =========================
            VALIDAR
            =========================
            */

            if (totalPendiente <= 0) {

                Toast.makeText(

                    requireContext(),

                    "No hay deuda pendiente",

                    Toast.LENGTH_SHORT

                ).show()

                return@setOnClickListener
            }

            /*
            =========================
            CONFIRMACION
            =========================
            */

            androidx.appcompat.app.AlertDialog.Builder(
                requireContext()
            )

                .setTitle("Pagar deuda")

                .setMessage(
                    "¿Seguro que deseas pagar toda la deuda de ${cliente.nombre} por $${totalPendiente.toInt()}?"
                )

                .setPositiveButton("Pagar") {

                        _, _ ->

                    /*
                    =========================
                    MARCAR TODO PAGADO
                    =========================
                    */

                    listaDetalles.forEach { detalle ->

                        if (!detalle.pagado) {

                            detalle.pagado = true

                            db.collection("detalleDeuda")

                                .document(detalle.id)

                                .update(
                                    "pagado",
                                    true
                                )
                        }
                    }

                    /*
                    =========================
                    ACTUALIZAR UI
                    =========================
                    */

                    recycler.adapter =
                        DetalleDeudaAdapter(
                            mutableListOf()
                        ) { _, _ -> }

                    recalcularPendiente(

                        listaDetalles,
                        textFooter,
                        cliente.id
                    )

                    actualizarTotalCliente(
                        cliente.id
                    )

                    Toast.makeText(

                        requireContext(),

                        "Deuda saldada",

                        Toast.LENGTH_SHORT

                    ).show()
                }

                .setNegativeButton(
                    "Cancelar",
                    null
                )

                .show()
        }

        /*
=========================
LISTA
=========================
*/

        /*
        =========================
        ADAPTER
        =========================
        */

        lateinit var adapterDetalle: DetalleDeudaAdapter

        adapterDetalle =
            DetalleDeudaAdapter(
                listaDetalles
            ) {

                    detalle,
                    pagado ->

                /*
                =========================
                UPDATE FIREBASE
                =========================
                */

                db.collection("detalleDeuda")

                    .document(detalle.id)

                    .update(
                        "pagado",
                        pagado
                    )

                    .addOnSuccessListener {

                        /*
                        =========================
                        ACTUALIZAR LOCAL
                        =========================
                        */

                        detalle.pagado =
                            pagado

                        /*
                        =========================
                        RECALCULAR FOOTER
                        =========================
                        */

                        recalcularPendiente(

                            listaDetalles,
                            textFooter,
                            cliente.id
                        )

                        /*
                        =========================
                        REFRESH
                        =========================
                        */

                        adapterDetalle.notifyDataSetChanged()

                        /*
                        =========================
                        ACTUALIZAR TOTAL CLIENTE
                        =========================
                        */

                        actualizarTotalCliente(
                            cliente.id
                        )
                    }
            }

        recycler.adapter =
            adapterDetalle

        recycler.layoutManager =
            LinearLayoutManager(
                requireContext()
            )

        /*
        =========================
        TOGGLE PAGADAS
        =========================
        */

        btnTogglePagadas.text =
            "MOSTRAR PAGADAS"

        btnTogglePagadas.setOnClickListener {

            ocultarPagadas =
                !ocultarPagadas

            adapterDetalle.mostrarPagadas =
                !ocultarPagadas

            adapterDetalle.notifyDataSetChanged()

            if (ocultarPagadas) {

                btnTogglePagadas.text =
                    "MOSTRAR PAGADAS"

            } else {

                btnTogglePagadas.text =
                    "OCULTAR PAGADAS"
            }
        }




        /*
        =========================
        CARGAR DETALLES
        =========================
        */

        db.collection("detalleDeuda")

            .whereEqualTo(
                "clienteId",
                cliente.id
            )

            .get()

            .addOnSuccessListener { result ->

                listaDetalles.clear()

                for (document in result) {

                    val detalle =
                        document.toObject(
                            DetalleDeuda::class.java
                        )

                    listaDetalles.add(detalle)
                }

                /*
                =========================
                ORDENAR
                =========================
                */

                listaDetalles.sortBy {
                    it.fecha
                }

                /*
                =========================
                REFRESH
                =========================
                */

                adapterDetalle.notifyDataSetChanged()

                /*
                =========================
                FOOTER
                =========================
                */

                recalcularPendiente(

                    listaDetalles,
                    textFooter,
                    cliente.id
                )
            }

        dialog.show()
    }

    private fun recalcularPendiente(

        lista: List<DetalleDeuda>,

        textFooter: TextView,

        clienteId: String? = null

    ) {

        var total = 0.0

        lista.forEach { detalle ->

            if (!detalle.pagado) {

                total += detalle.subtotal
            }
        }

        val texto =
            "$${total.toInt()}"

        /*
        =========================
        FOOTER
        =========================
        */

        textFooter.text =
            texto

        /*
        =========================
        ITEM DEUDOR
        =========================
        */

        if (clienteId != null) {

            val clienteLocal =
                listaClientes.find {

                    it.id == clienteId
                }

            clienteLocal?.deudaTotal =
                total

            adapter.notifyDataSetChanged()
        }
    }

    private fun actualizarTotalCliente(
        clienteId: String
    ) {

        db.collection("detalleDeuda")

            .whereEqualTo(
                "clienteId",
                clienteId
            )

            .get()

            .addOnSuccessListener { result ->

                var nuevoTotal = 0.0

                for (document in result) {

                    val detalle =
                        document.toObject(
                            DetalleDeuda::class.java
                        )

                    if (!detalle.pagado) {

                        nuevoTotal +=
                            detalle.subtotal
                    }
                }

                val clienteLocal =

                    listaClientes.find {

                        it.id == clienteId
                    }

                clienteLocal?.deudaTotal =
                    nuevoTotal

                adapter.notifyDataSetChanged()

                cargarDeudores()
            }
    }
    private fun registrarDeuda(

        clienteId: String,

        clienteNombre: String,

        adapterFiado: ProductoFiadoAdapter,

        listaProductosSeleccionados: MutableList<Product>,

        dialog: BottomSheetDialog

    ) {

        /*
        =========================
        TOTAL
        =========================
        */

        val total =
            adapterFiado.obtenerTotal()

        /*
        =========================
        CREAR DEUDA
        =========================
        */

        val docDeuda =
            db.collection("deudas")
                .document()

        val deuda = Deuda(

            id = docDeuda.id,

            clienteId = clienteId,

            clienteNombre = clienteNombre,

            fecha = System.currentTimeMillis(),

            total = total,

            estado = "pendiente"
        )

        docDeuda.set(deuda)

            .addOnSuccessListener {

                /*
                =========================
                GUARDAR DETALLES
                =========================
                */

                listaProductosSeleccionados.forEach { producto ->

                    /*
                    =========================
                    CANTIDAD
                    =========================
                    */

                    val cantidad =

                        if (
                            producto.unidadMedida == "Por kg"
                        ) {

                            adapterFiado
                                .obtenerCantidadKg(
                                    producto.id
                                )

                        } else {

                            adapterFiado
                                .obtenerCantidadProducto(
                                    producto.id
                                )
                        }

                    /*
                    =========================
                    SUBTOTAL
                    =========================
                    */

                    val subtotal =
                        producto.precio * cantidad

                    /*
                    =========================
                    DOCUMENTO
                    =========================
                    */

                    val docDetalle =
                        db.collection("detalleDeuda")
                            .document()

                    val detalle = DetalleDeuda(

                        id = docDetalle.id,

                        deudaId = docDeuda.id,

                        clienteId = clienteId,

                        productoId = producto.id,

                        nombreProducto = producto.nombre,

                        cantidad = cantidad,

                        precioUnitario = producto.precio,

                        subtotal = subtotal,

                        unidadMedida =
                            producto.unidadMedida,

                        fecha = System.currentTimeMillis()
                    )

                    docDetalle.set(detalle)
                }

                Toast.makeText(

                    requireContext(),

                    "Fiado registrado",

                    Toast.LENGTH_SHORT

                ).show()

                /*
=========================
CERRAR BOTTOMSHEET
=========================
*/

                dialog.dismiss()

                /*
                =========================
                RECARGAR LISTA
                =========================
                */

                cargarDeudores()
            }
    }

    private fun crearChipApodo(

        apodo: String,

        chipGroup: ChipGroup,

        listaApodos: MutableList<String>,

        clienteId: String

    ) {

        val chip =
            Chip(requireContext())

        chip.text =
            apodo

        chip.isCloseIconVisible =
            true

        chip.setOnCloseIconClickListener {

            /*
            =========================
            REMOVER LOCAL
            =========================
            */

            listaApodos.remove(apodo)

            /*
            =========================
            REMOVER VISUAL
            =========================
            */

            chipGroup.removeView(chip)

            /*
            =========================
            FIREBASE
            =========================
            */

            db.collection("clientes")

                .document(clienteId)

                .update(
                    "apodos",
                    listaApodos
                )

                .addOnSuccessListener {

                    cargarDeudores()
                }
        }

        chipGroup.addView(chip)
    }
}