package edu.udb.kairostravel

import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class AddDestinoActivity : AppCompatActivity() {

    private var imageUri: Uri? = null
    private lateinit var ivPreview: ImageView

    // Instancias de Firebase
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            imageUri = uri
            ivPreview.setImageURI(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_destino)

        ivPreview = findViewById(R.id.ivPreview)
        val btnSelectImage = findViewById<Button>(R.id.btnSelectImage)
        val btnSaveDestino = findViewById<Button>(R.id.btnSaveDestino)

        val etNombre = findViewById<EditText>(R.id.etNombreDestino)
        val spinnerPais = findViewById<Spinner>(R.id.spinnerPais)
        val etPrecio = findViewById<EditText>(R.id.etPrecio)
        val etDescripcion = findViewById<EditText>(R.id.etDescripcion)

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

            if (imageUri == null) {
                Toast.makeText(this, "Debe seleccionar una imagen para el destino", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSaveDestino.isEnabled = false
            Toast.makeText(this, "Subiendo foto y guardando datos...", Toast.LENGTH_LONG).show()

            uploadImageAndSave(nombre, pais, precio, descripcion, btnSaveDestino)
        }
    }

    private fun uploadImageAndSave(nombre: String, pais: String, precio: Double, descripcion: String, btn: Button) {
        val fileName = UUID.randomUUID().toString() + ".jpg"
        val storageRef = storage.reference.child("destinos_imagenes/$fileName")

        storageRef.putFile(imageUri!!)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveToFirestore(nombre, pais, precio, descripcion, uri.toString(), btn)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir imagen: ${e.message}", Toast.LENGTH_SHORT).show()
                btn.isEnabled = true
            }
    }

    private fun saveToFirestore(nombre: String, pais: String, precio: Double, desc: String, imageUrl: String, btn: Button) {
        val destinoId = db.collection("destinos").document().id
        val destino = Destino(destinoId, nombre, pais, precio, desc, imageUrl)

        db.collection("destinos").document(destinoId).set(destino)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Destino guardado con éxito!", Toast.LENGTH_SHORT).show()
                ivPreview.setImageResource(android.R.drawable.ic_menu_gallery)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error en BD: ${e.message}", Toast.LENGTH_SHORT).show()
                btn.isEnabled = true
            }
    }
}