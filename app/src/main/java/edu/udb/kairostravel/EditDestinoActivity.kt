package edu.udb.kairostravel

import android.net.Uri
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class EditDestinoActivity : AppCompatActivity() {

    private var newImageUri: Uri? = null
    private lateinit var ivPreview: ImageView
    private var destinoId = ""
    private var currentImageUrl = ""

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            newImageUri = uri
            ivPreview.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // REUTILIZAMOS EL DISEÑO DE AGREGAR
        setContentView(R.layout.activity_add_destino)

        ivPreview = findViewById(R.id.ivPreview)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnSaveDestino = findViewById<Button>(R.id.btnSaveDestino)
        val etNombre = findViewById<EditText>(R.id.etNombreDestino)
        val spinnerPais = findViewById<Spinner>(R.id.spinnerPais)
        val etPrecio = findViewById<EditText>(R.id.etPrecio)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)

        // Cambiamos el texto del botón
        btnSaveDestino.text = "Actualizar Destino"

        // Recibir los datos enviados por el Adaptador
        destinoId = intent.getStringExtra("id") ?: ""
        etNombre.setText(intent.getStringExtra("nombre"))
        etPrecio.setText(intent.getDoubleExtra("precio", 0.0).toString())
        etDescripcion.setText(intent.getStringExtra("descripcion"))
        currentImageUrl = intent.getStringExtra("imageUrl") ?: ""

        // Cargar la imagen actual con Glide
        if (currentImageUrl.isNotEmpty()) {
            Glide.with(this).load(currentImageUrl).into(ivPreview)
        }

        // Seleccionar el país en el Spinner
        val paisSeleccionado = intent.getStringExtra("pais")
        val adapter = ArrayAdapter.createFromResource(this, R.array.countries_array, android.R.layout.simple_spinner_item)
        val position = adapter.getPosition(paisSeleccionado)
        spinnerPais.setSelection(position)

        btnSelectImage.setOnClickListener {
            selectImageLauncher.launch("image/*")
        }

        btnSaveDestino.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val pais = spinnerPais.selectedItem.toString()
            val precioStr = etPrecio.text.toString().trim()
            val descripcion = etDescripcion.text.toString().trim()

            if (nombre.isEmpty() || precioStr.isEmpty() || descripcion.isEmpty()) {
                Toast.makeText(this, "No se permiten campos vacíos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val precio = precioStr.toDoubleOrNull() ?: 0.0
            if (precio <= 0) {
                Toast.makeText(this, "El precio debe ser mayor a 0", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (descripcion.length < 20) {
                Toast.makeText(this, "La descripción debe tener al menos 20 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSaveDestino.isEnabled = false
            Toast.makeText(this, "Actualizando datos...", Toast.LENGTH_LONG).show()

            // Si seleccionó una nueva imagen, la subimos primero
            if (newImageUri != null) {
                uploadNewImageAndUpdate(nombre, pais, precio, descripcion, btnSaveDestino)
            } else {
                // Si no, actualizamos solo los textos manteniendo la imagen vieja
                updateFirestore(nombre, pais, precio, descripcion, currentImageUrl, btnSaveDestino)
            }
        }
    }

    private fun uploadNewImageAndUpdate(nombre: String, pais: String, precio: Double, descripcion: String, btn: Button) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val storageRef = storage.reference.child("destinos_imagenes/$fileName")

        storageRef.putFile(newImageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateFirestore(nombre, pais, precio, descripcion, uri.toString(), btn)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error subiendo nueva imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                btn.isEnabled = true
            }
    }

    private fun updateFirestore(nombre: String, pais: String, precio: Double, desc: String, imageUrl: String, btn: Button) {
        val destinoActualizado = mapOf(
            "nombre" to nombre,
            "pais" to pais,
            "precio" to precio,
            "descripcion" to desc,
            "imageUrl" to imageUrl
        )

        db.collection("destinos").document(destinoId).update(destinoActualizado)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Destino actualizado con éxito!", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al actualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                btn.isEnabled = true
            }
    }
}