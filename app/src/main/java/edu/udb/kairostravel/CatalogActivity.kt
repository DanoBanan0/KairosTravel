package edu.udb.kairostravel

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class CatalogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DestinoAdapter
    private val destinosList = mutableListOf<Destino>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        recyclerView = findViewById(R.id.recyclerViewDestinos)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = DestinoAdapter(destinosList)
        recyclerView.adapter = adapter

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddDestino)
        fabAdd.setOnClickListener {
            // Mandamos al usuario a la pantalla de crear
            startActivity(Intent(this, AddDestinoActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        cargarDestinos() // Lo ponemos aquí para que la lista se recargue si agregamos un destino nuevo
    }

    private fun cargarDestinos() {
        db.collection("destinos").get()
            .addOnSuccessListener { result ->
                destinosList.clear()
                for (document in result) {
                    val destino = document.toObject(Destino::class.java)
                    destino.id = document.id
                    destinosList.add(destino)
                }
                adapter.notifyDataSetChanged() // Avisamos al adaptador que hay datos nuevos
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al cargar datos", Toast.LENGTH_SHORT).show()
            }
    }
}