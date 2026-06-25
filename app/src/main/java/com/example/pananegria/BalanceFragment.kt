package com.example.pananegria

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.XAxis
import android.content.Intent
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream
import androidx.core.content.FileProvider

class BalanceFragment : Fragment() {

    private lateinit var db:
            FirebaseFirestore

    private lateinit var prefs:
            android.content.SharedPreferences

    private lateinit var chartBalance:
            LineChart

    private lateinit var textPromedio:
            TextView

    private lateinit var textMejorDia:
            TextView

    private lateinit var textPeorDia:
            TextView

    private lateinit var textTotalSemanal:
            TextView

    private var graficoActual =
        "semana"

    override fun onCreateView(

        inflater: LayoutInflater,

        container: ViewGroup?,

        savedInstanceState: Bundle?

    ): View {

        val view = inflater.inflate(

            R.layout.fragment_balance,

            container,

            false
        )

        db = FirebaseFirestore.getInstance()

        prefs = requireContext()

            .getSharedPreferences(

                "balance",

                android.content.Context.MODE_PRIVATE
            )

        /*
        =========================
        VISTAS
        =========================
        */

        val editMontoManana =
            view.findViewById<EditText>(
                R.id.editMontoManana
            )

        val editMontoTarde =
            view.findViewById<EditText>(
                R.id.editMontoTarde
            )

        val textBalanceTotal =
            view.findViewById<TextView>(
                R.id.textBalanceTotal
            )

        val textTituloBalance =
            view.findViewById<TextView>(
                R.id.textTituloBalance
            )

        val btnGuardarBalance =
            view.findViewById<Button>(
                R.id.btnGuardarBalance
            )

        val btnGraficoSemana =
            view.findViewById<Button>(
                R.id.btnGraficoSemana
            )

        val btnGraficoMes =
            view.findViewById<Button>(
                R.id.btnGraficoMes
            )

        chartBalance =
            view.findViewById<LineChart>(
                R.id.chartBalance
            )

        val btnCompartirGrafico =
            view.findViewById<ImageView>(
                R.id.btnCompartirGrafico
            )

        textPromedio =
            view.findViewById<TextView>(
                R.id.textPromedio
            )

        textMejorDia =
            view.findViewById<TextView>(
                R.id.textMejorDia
            )

        textPeorDia =
            view.findViewById<TextView>(
                R.id.textPeorDia
            )

        textTotalSemanal =
            view.findViewById<TextView>(
                R.id.textTotalSemanal
            )

        /*
        =========================
        FECHA
        =========================
        */

        val formatoFecha =

            java.text.SimpleDateFormat(

                "dd/MM/yy",

                java.util.Locale.getDefault()
            )

        val fechaHoy =
            formatoFecha.format(
                java.util.Date()
            )


        textTituloBalance.text =
            "BALANCE $fechaHoy"

        /*
        =========================
        RANGO DEL DIA
        =========================
        */

        val calendario =
            java.util.Calendar.getInstance()

        calendario.set(
            java.util.Calendar.HOUR_OF_DAY,
            0
        )

        calendario.set(
            java.util.Calendar.MINUTE,
            0
        )

        calendario.set(
            java.util.Calendar.SECOND,
            0
        )

        calendario.set(
            java.util.Calendar.MILLISECOND,
            0
        )

        val inicioDia =
            calendario.timeInMillis

        calendario.add(
            java.util.Calendar.DAY_OF_MONTH,
            1
        )

        val finDia =
            calendario.timeInMillis

        /*
        =========================
        ESTADO
        =========================
        */

        var balanceGuardado:
                Balance? = null

        /*
        =========================
        NUEVO DIA
        =========================
        */

        val fechaBorrador =

            prefs.getString(
                "fecha_borrador",
                ""
            )

        if (fechaBorrador != fechaHoy) {

            prefs.edit().clear().apply()
        }

        /*
=========================
INICIAR VACIO
=========================
*/

        editMontoManana.setText("")

        editMontoTarde.setText("")

        /*
        =========================
        RECALCULAR
        =========================
        */

        fun recalcularBalance() {

            val textoManana =

                editMontoManana.text
                    .toString()

            val textoTarde =

                editMontoTarde.text
                    .toString()

            /*
            =========================
            GUARDAR BORRADOR
            =========================
            */

            prefs.edit()

                .putString(
                    "monto_manana",
                    textoManana
                )

                .putString(
                    "monto_tarde",
                    textoTarde
                )

                .putString(
                    "fecha_borrador",
                    fechaHoy
                )

                .apply()

            /*
            =========================
            TOTAL
            =========================
            */

            val manana =

                textoManana
                    .toDoubleOrNull()

                    ?: 0.0

            val tarde =

                textoTarde
                    .toDoubleOrNull()

                    ?: 0.0

            val total =
                manana + tarde

            textBalanceTotal.text =
                "$ ${total.toInt()}"

            /*
            =========================
            VALIDAR INPUTS
            =========================
            */

            val inputsCompletos =

                textoManana.isNotEmpty()

                        &&

                        textoTarde.isNotEmpty()

            /*
            =========================
            DETECTAR CAMBIOS
            =========================
            */

            val hayCambios =

                balanceGuardado == null

                        ||

                        balanceGuardado?.montoManana != manana

                        ||

                        balanceGuardado?.montoTarde != tarde

            /*
            =========================
            ESTADO BOTON
            =========================
            */

            val habilitar =
                inputsCompletos && hayCambios

            btnGuardarBalance.isEnabled =
                habilitar

            if (habilitar) {

                btnGuardarBalance.backgroundTintList =

                    android.content.res.ColorStateList.valueOf(

                        android.graphics.Color.parseColor(
                            "#F2C95E"
                        )
                    )

                btnGuardarBalance.alpha =
                    1f

            } else {

                btnGuardarBalance.backgroundTintList =

                    android.content.res.ColorStateList.valueOf(

                        android.graphics.Color.parseColor(
                            "#D8D2CA"
                        )
                    )

                btnGuardarBalance.alpha =
                    0.55f
            }
        }



        /*
        =========================
        FIREBASE
        =========================
        */

        db.collection("balances")

            .whereGreaterThanOrEqualTo(
                "fecha",
                inicioDia
            )

            .whereLessThan(
                "fecha",
                finDia
            )

            .get(com.google.firebase.firestore.Source.SERVER)

            .addOnSuccessListener { result ->

                if (!result.isEmpty) {

                    val balance =
                        result.documents[0]
                            .toObject(
                                Balance::class.java
                            )

                            ?: return@addOnSuccessListener

                    balanceGuardado =
                        balance

                    editMontoManana.setText(
                        balance.montoManana.toString()
                    )

                    editMontoTarde.setText(
                        balance.montoTarde.toString()
                    )

                    recalcularBalance()
                }
            }

        /*
        =========================
        LISTENERS
        =========================
        */

        val watcher = object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {

                recalcularBalance()
            }

            override fun afterTextChanged(
                s: Editable?
            ) {
            }
        }

        editMontoManana.addTextChangedListener(
            watcher
        )

        editMontoTarde.addTextChangedListener(
            watcher
        )

        recalcularBalance()

        /*
        =========================
        GUARDAR
        =========================
        */

        btnGuardarBalance.setOnClickListener {

            if (!btnGuardarBalance.isEnabled) {

                return@setOnClickListener
            }

            val montoManana =

                editMontoManana.text
                    .toString()
                    .toDoubleOrNull()

                    ?: 0.0

            val montoTarde =

                editMontoTarde.text
                    .toString()
                    .toDoubleOrNull()

                    ?: 0.0

            val total =
                montoManana + montoTarde

            /*
            =========================
            CONFIRMAR SOBREESCRITURA
            =========================
            */

            if (balanceGuardado != null) {

                AlertDialog.Builder(
                    requireContext()
                )

                    .setTitle("Actualizar balance")

                    .setMessage(

                        "¿Seguro que deseas cambiar:\n\n" +

                                "Mañana: $" +
                                balanceGuardado!!.montoManana.toInt()

                                +

                                "\nTarde: $" +
                                balanceGuardado!!.montoTarde.toInt()

                                +

                                "\n\npor:\n\n" +

                                "Mañana: $" +
                                montoManana.toInt()

                                +

                                "\nTarde: $" +
                                montoTarde.toInt()

                    )

                    .setPositiveButton("Guardar") {

                            _, _ ->

                        guardarBalance(

                            balanceGuardado!!.id,

                            montoManana,

                            montoTarde,

                            total,

                            inicioDia,

                            editMontoManana,

                            editMontoTarde,

                            btnGuardarBalance,

                            textBalanceTotal,

                            fechaHoy

                        ) {

                            balanceGuardado = it

                            recalcularBalance()
                        }
                    }

                    .setNegativeButton(
                        "Cancelar",
                        null
                    )

                    .show()

            } else {

                val docBalance =

                    db.collection("balances")
                        .document()

                val balance = Balance(

                    id = docBalance.id,

                    montoManana = montoManana,

                    montoTarde = montoTarde,

                    total = total,

                    fecha = inicioDia
                )

                docBalance.set(balance)

                    .addOnSuccessListener {

                        balanceGuardado =
                            balance

                        recalcularBalance()

                        if (graficoActual == "semana") {

                            cargarGraficoSemanal()

                        } else {

                            cargarGraficoMensual()
                        }

                        cargarDatosRapidos()

                        Toast.makeText(

                            requireContext(),

                            "Balance guardado",

                            Toast.LENGTH_SHORT

                        ).show()
                    }
            }
        }

        /*
        =========================
        SWITCH GRAFICOS
        =========================
        */

        btnGraficoSemana.setOnClickListener {

            btnGraficoSemana.backgroundTintList =

                android.content.res.ColorStateList.valueOf(

                    android.graphics.Color.parseColor(
                        "#F2C95E"
                    )
                )

            btnGraficoSemana.setTextColor(
                android.graphics.Color.BLACK
            )

            btnGraficoMes.backgroundTintList =

                android.content.res.ColorStateList.valueOf(

                    android.graphics.Color.TRANSPARENT
                )

            btnGraficoMes.setTextColor(
                android.graphics.Color.parseColor(
                    "#8B7B6B"
                )
            )

            graficoActual =
                "semana"

            cargarGraficoSemanal()
        }

        btnGraficoMes.setOnClickListener {

            btnGraficoMes.backgroundTintList =

                android.content.res.ColorStateList.valueOf(

                    android.graphics.Color.parseColor(
                        "#F2C95E"
                    )
                )

            btnGraficoMes.setTextColor(
                android.graphics.Color.BLACK
            )

            btnGraficoSemana.backgroundTintList =

                android.content.res.ColorStateList.valueOf(

                    android.graphics.Color.TRANSPARENT
                )

            btnGraficoSemana.setTextColor(
                android.graphics.Color.parseColor(
                    "#8B7B6B"
                )
            )

            graficoActual =
                "mes"

            cargarGraficoMensual()
        }

        btnGraficoSemana.performClick()

        btnCompartirGrafico.setOnClickListener {

            /*
            =========================
            BITMAP
            =========================
            */


            val bitmap =
                chartBalance.chartBitmap

            /*
            =========================
            ARCHIVO
            =========================
            */

            val file = File(

                requireContext()
                    .cacheDir,

                "grafico_balance.png"
            )

            val output =
                FileOutputStream(file)

            bitmap.compress(

                Bitmap.CompressFormat.PNG,

                100,

                output
            )

            output.flush()

            output.close()

            /*
            =========================
            URI
            =========================
            */

            val uri = FileProvider.getUriForFile(

                requireContext(),

                requireContext().packageName +
                        ".provider",

                file
            )

            /*
            =========================
            SHARE
            =========================
            */

            val intent =
                Intent(Intent.ACTION_SEND)

            intent.type =
                "image/png"

            intent.putExtra(
                Intent.EXTRA_STREAM,
                uri
            )

            intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            startActivity(

                Intent.createChooser(

                    intent,

                    "Compartir gráfico"
                )
            )
        }

        cargarGraficoSemanal()

        cargarDatosRapidos()


        return view
    }

    /*
    =========================
    UPDATE FIREBASE
    =========================
    */

    private fun guardarBalance(

        balanceId: String,

        montoManana: Double,

        montoTarde: Double,

        total: Double,

        fecha: Long,

        editMontoManana: EditText,

        editMontoTarde: EditText,

        btnGuardarBalance: Button,

        textBalanceTotal: TextView,

        fechaHoy: String,

        onFinish: (Balance) -> Unit

    ) {

        val balance = Balance(

            id = balanceId,

            montoManana = montoManana,

            montoTarde = montoTarde,

            total = total,

            fecha = fecha
        )

        db.collection("balances")

            .document(balanceId)

            .set(balance)

            .addOnSuccessListener {

                Toast.makeText(

                    requireContext(),

                    "Balance actualizado",

                    Toast.LENGTH_SHORT

                ).show()

                onFinish(balance)

                if (graficoActual == "semana") {

                    cargarGraficoSemanal()

                } else {

                    cargarGraficoMensual()
                }

                cargarDatosRapidos()
            }

    }

    fun cargarGraficoSemanal() {

        /*
=========================
INICIO SEMANA
=========================
*/

        val inicioSemana =
            java.util.Calendar.getInstance()

        inicioSemana.firstDayOfWeek =
            java.util.Calendar.MONDAY

        while (

            inicioSemana.get(
                java.util.Calendar.DAY_OF_WEEK
            )

            !=

            java.util.Calendar.MONDAY

        ) {

            inicioSemana.add(
                java.util.Calendar.DAY_OF_MONTH,
                -1
            )
        }

        inicioSemana.set(
            java.util.Calendar.HOUR_OF_DAY,
            0
        )

        inicioSemana.set(
            java.util.Calendar.MINUTE,
            0
        )

        inicioSemana.set(
            java.util.Calendar.SECOND,
            0
        )

        inicioSemana.set(
            java.util.Calendar.MILLISECOND,
            0
        )

        /*
        =========================
        FIN SEMANA
        =========================
        */

        val finSemana =
            inicioSemana.clone()

                    as java.util.Calendar

        finSemana.add(
            java.util.Calendar.DAY_OF_MONTH,
            7
        )

        /*
        =========================
        FIREBASE
        =========================
        */

        db.collection("balances")

            .get()

            .addOnSuccessListener { result ->

                /*
                =========================
                MAPA DIAS
                =========================
                */

                val mapa =
                    mutableMapOf<Int, Double>()

                for (document in result) {

                    val balance =
                        document.toObject(
                            Balance::class.java
                        )

                    val calBalance =
                        java.util.Calendar.getInstance()

                    calBalance.timeInMillis =
                        balance.fecha

                    /*
                    =========================
                    FILTRAR SEMANA ACTUAL
                    =========================
                    */

                    if (

                        balance.fecha >=
                        inicioSemana.timeInMillis

                        &&

                        balance.fecha <
                        finSemana.timeInMillis

                    ) {

                        /*
                        =========================
                        DIA
                        =========================
                        */

                        val dia =

                            when (

                                calBalance.get(
                                    java.util.Calendar.DAY_OF_WEEK
                                )

                            ) {

                                java.util.Calendar.MONDAY -> 0

                                java.util.Calendar.TUESDAY -> 1

                                java.util.Calendar.WEDNESDAY -> 2

                                java.util.Calendar.THURSDAY -> 3

                                java.util.Calendar.FRIDAY -> 4

                                java.util.Calendar.SATURDAY -> 5

                                java.util.Calendar.SUNDAY -> 6

                                else -> -1
                            }

                        if (dia != -1) {

                            mapa[dia] =
                                balance.total
                        }
                    }
                }

                /*
                =========================
                ENTRIES
                =========================
                */

                val entries =
                    mutableListOf<Entry>()

                for (i in 0..6) {

                    val valor =
                        mapa[i] ?: 0.0

                    entries.add(

                        Entry(

                            i.toFloat(),

                            valor.toFloat()
                        )
                    )
                }

                /*
                =========================
                LABELS
                =========================
                */

                val labels = listOf(

                    "L",

                    "M",

                    "M",

                    "J",

                    "V",

                    "S",

                    "D"
                )

                /*
                =========================
                DATASET
                =========================
                */

                val dataSet =
                    LineDataSet(
                        entries,
                        "Semana"
                    )

                dataSet.lineWidth =
                    3f

                dataSet.circleRadius =
                    5f

                dataSet.valueTextSize =
                    11f

                dataSet.color =

                    android.graphics.Color.parseColor(
                        "#F2C95E"
                    )

                dataSet.setCircleColor(

                    android.graphics.Color.parseColor(
                        "#2D1B10"
                    )
                )

                /*
                =========================
                CHART
                =========================
                */

                val lineData =
                    LineData(dataSet)

                chartBalance.data =
                    lineData

                chartBalance.description.isEnabled =
                    false

                chartBalance.legend.isEnabled =
                    false

                chartBalance.axisRight.isEnabled =
                    false

                chartBalance.animateX(700)

                /*
                =========================
                X AXIS
                =========================
                */

                val xAxis =
                    chartBalance.xAxis

                xAxis.position =
                    XAxis.XAxisPosition.BOTTOM

                xAxis.valueFormatter =

                    IndexAxisValueFormatter(
                        labels
                    )

                xAxis.granularity =
                    1f

                xAxis.setDrawGridLines(
                    false
                )

                chartBalance.invalidate()
            }
    }
     fun cargarGraficoMensual() {

        db.collection("balances")

            .get()

            .addOnSuccessListener { result ->

                /*
                =========================
                AGRUPAR POR MES
                =========================
                */

                val mapaMeses =
                    mutableMapOf<Int, Double>()

                for (document in result) {

                    val balance =
                        document.toObject(
                            Balance::class.java
                        )

                    val calendario =
                        java.util.Calendar.getInstance()

                    calendario.timeInMillis =
                        balance.fecha

                    val mes =

                        calendario.get(
                            java.util.Calendar.MONTH
                        )

                    val totalActual =
                        mapaMeses[mes] ?: 0.0

                    mapaMeses[mes] =
                        totalActual + balance.total
                }

                /*
                =========================
                LABELS
                =========================
                */

                val labels = listOf(

                    "E",

                    "F",

                    "M",

                    "A",

                    "M",

                    "J",

                    "J",

                    "A",

                    "S",

                    "O",

                    "N",

                    "D"
                )

                /*
                =========================
                ENTRIES
                =========================
                */

                val entries =
                    mutableListOf<Entry>()

                for (mes in 0..11) {

                    val valor =
                        mapaMeses[mes] ?: 0.0

                    entries.add(

                        Entry(

                            mes.toFloat(),

                            valor.toFloat()
                        )
                    )
                }

                /*
                =========================
                DATASET
                =========================
                */

                val dataSet =
                    LineDataSet(
                        entries,
                        "Meses"
                    )

                dataSet.lineWidth =
                    3f

                dataSet.circleRadius =
                    5f

                dataSet.valueTextSize =
                    11f

                dataSet.color =

                    android.graphics.Color.parseColor(
                        "#F2C95E"
                    )

                dataSet.setCircleColor(

                    android.graphics.Color.parseColor(
                        "#2D1B10"
                    )
                )

                val lineData =
                    LineData(dataSet)

                /*
                =========================
                CHART
                =========================
                */

                chartBalance.data =
                    lineData

                chartBalance.description.isEnabled =
                    false

                chartBalance.legend.isEnabled =
                    false

                chartBalance.axisRight.isEnabled =
                    false

                chartBalance.animateX(700)

                /*
                =========================
                X AXIS
                =========================
                */

                val xAxis =
                    chartBalance.xAxis

                xAxis.position =
                    XAxis.XAxisPosition.BOTTOM

                xAxis.valueFormatter =

                    IndexAxisValueFormatter(
                        labels
                    )

                xAxis.granularity =
                    1f

                xAxis.setDrawGridLines(
                    false
                )

                chartBalance.invalidate()
            }
    }

    fun cargarDatosRapidos() {

        /*
        =========================
        CALENDARIO
        =========================
        */

        val calendario =
            java.util.Calendar.getInstance()

        calendario.firstDayOfWeek =
            java.util.Calendar.MONDAY

        calendario.minimalDaysInFirstWeek =
            4

        /*
        =========================
        RETROCEDER HASTA LUNES
        =========================
        */

        while (

            calendario.get(
                java.util.Calendar.DAY_OF_WEEK
            )

            !=

            java.util.Calendar.MONDAY

        ) {

            calendario.add(
                java.util.Calendar.DAY_OF_MONTH,
                -1
            )
        }

        /*
        =========================
        INICIO SEMANA
        =========================
        */

        calendario.set(
            java.util.Calendar.HOUR_OF_DAY,
            0
        )

        calendario.set(
            java.util.Calendar.MINUTE,
            0
        )

        calendario.set(
            java.util.Calendar.SECOND,
            0
        )

        calendario.set(
            java.util.Calendar.MILLISECOND,
            0
        )

        val inicioSemana =
            calendario.timeInMillis

        /*
        =========================
        FIN SEMANA
        =========================
        */

        calendario.add(
            java.util.Calendar.DAY_OF_MONTH,
            7
        )

        val finSemana =
            calendario.timeInMillis

        /*
        =========================
        FIREBASE
        =========================
        */

        db.collection("balances")

            .get()

            .addOnSuccessListener { result ->

                val semanaActual =
                    mutableListOf<Balance>()

                /*
                =========================
                FILTRAR SEMANA
                =========================
                */

                for (document in result) {

                    val balance =
                        document.toObject(
                            Balance::class.java
                        )

                    if (

                        balance.fecha >= inicioSemana

                        &&

                        balance.fecha < finSemana

                    ) {

                        semanaActual.add(balance)
                    }
                }

                /*
                =========================
                VACIO
                =========================
                */

                if (semanaActual.isEmpty()) {

                    textPromedio.text =
                        "$0"

                    textTotalSemanal.text =
                        "$0"

                    textMejorDia.text =
                        "-"

                    textPeorDia.text =
                        "-"

                    return@addOnSuccessListener
                }

                /*
                =========================
                TOTAL
                =========================
                */

                val totalSemana =

                    semanaActual.sumOf {
                        it.total
                    }

                /*
                =========================
                PROMEDIO
                =========================
                */

                val promedio =

                    totalSemana /
                            semanaActual.size

                /*
                =========================
                MEJOR DIA
                =========================
                */

                val mejor =

                    semanaActual.maxByOrNull {
                        it.total
                    }

                /*
                =========================
                PEOR DIA
                =========================
                */

                val peor =

                    semanaActual.minByOrNull {
                        it.total
                    }

                /*
                =========================
                FORMATO
                =========================
                */

                val formatoDia =

                    java.text.SimpleDateFormat(

                        "EEEE",

                        java.util.Locale("es")
                    )

                /*
                =========================
                UI
                =========================
                */

                textPromedio.text =

                    "$${promedio.toInt()}"

                textTotalSemanal.text =

                    "$${totalSemana.toInt()}"

                textMejorDia.text =

                    formatoDia.format(

                        java.util.Date(
                            mejor!!.fecha
                        )
                    )

                textPeorDia.text =

                    formatoDia.format(

                        java.util.Date(
                            peor!!.fecha
                        )
                    )
            }
    }


}