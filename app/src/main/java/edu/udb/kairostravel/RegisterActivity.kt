package edu.udb.kairostravel

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    // Variable para Firebase Auth
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Inicializamos Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Enlazamos las vistas del XML con variables en Kotlin
        val etEmail = findViewById<EditText>(R.id.etRegEmail)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirm = findViewById<EditText>(R.id.etRegPasswordConfirm)
        val btnRegister = findViewById<Button>(R.id.btnRegisterSubmit)
        val tvBackLogin = findViewById<TextView>(R.id.tvBackToLogin)

        // Botón para regresar al Login
        tvBackLogin.setOnClickListener {
            finish() // Cierra esta pantalla y vuelve a la anterior (Login)
        }

        // Acción al presionar el botón de Registrarse
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()

            // Validación Obligatoria 1: No campos vacíos
            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, getString(R.string.error_empty_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar que las contraseñas sean iguales
            if (password != confirm) {
                Toast.makeText(this, getString(R.string.error_password_match), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validar longitud mínima de contraseña (Firebase pide al menos 6 caracteres)
            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear el usuario en Firebase
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Usuario creado exitosamente", Toast.LENGTH_SHORT).show()
                        finish() // Si todo sale bien, lo regresamos al login para que entre
                    } else {
                        // Si falla (ej. correo ya existe), mostramos el error en pantalla
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}