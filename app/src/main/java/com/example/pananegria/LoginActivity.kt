package com.example.pananegria

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        /*
        =========================
        SI YA HAY SESION
        =========================
        */

        /*
=========================
PREFERENCIAS
=========================
*/

        val prefs =

            getSharedPreferences(

                "login",

                MODE_PRIVATE
            )

        val recordarme =

            prefs.getBoolean(

                "recordarme",

                false
            )

        /*
        =========================
        SESION
        =========================
        */

        if (

            auth.currentUser != null

            &&

            recordarme

        ) {

            abrirMainActivity()

            return
        }

        /*
        =========================
        CERRAR SI NO RECORDAR
        =========================
        */

        if (

            auth.currentUser != null

            &&

            !recordarme

        ) {

            auth.signOut()
        }

        val editEmail =
            findViewById<TextInputEditText>(
                R.id.editEmail
            )

        val editPassword =
            findViewById<TextInputEditText>(
                R.id.editPassword
            )

        val btnIngresar =
            findViewById<Button>(
                R.id.btnIngresar
            )

        val btnInvitado =
            findViewById<Button>(
                R.id.btnInvitado
            )

        val checkRecordar =
            findViewById<CheckBox>(
                R.id.checkRecordar
            )

        /*
        =========================
        LOGIN
        =========================
        */

        btnIngresar.setOnClickListener {

            val email =
                editEmail.text.toString().trim()

            val password =
                editPassword.text.toString().trim()

            /*
            =========================
            VALIDACIONES
            =========================
            */

            if (
                email.isEmpty() ||
                password.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Completá todos los campos",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            /*
            =========================
            LOGIN FIREBASE
            =========================
            */

            auth.signInWithEmailAndPassword(
                email,
                password
            )

                .addOnSuccessListener {

                    /*
=========================
PREFERENCIAS
=========================
*/

                    val prefs =

                        getSharedPreferences(

                            "login",

                            MODE_PRIVATE
                        )

                    prefs.edit()

                        .putBoolean(

                            "recordarme",

                            checkRecordar.isChecked
                        )

                        .apply()

                    abrirMainActivity()
                }

                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        "Correo o contraseña incorrectos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        /*
        =========================
        INVITADO
        =========================
        */

        btnInvitado.setOnClickListener {

            auth.signInAnonymously()

                .addOnSuccessListener {

                    abrirMainActivity()
                }

                .addOnFailureListener {

                    Toast.makeText(

                        this,

                        "Error al ingresar como invitado",

                        Toast.LENGTH_SHORT

                    ).show()
                }
        }
    }

    /*
    =========================
    ABRIR MAIN
    =========================
    */

    private fun abrirMainActivity() {

        startActivity(
            Intent(
                this,
                MainActivity::class.java
            )
        )

        finish()
    }
}