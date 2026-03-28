package edu.udb.kairostravel

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore

class DestinoAdapter(private val destinosList: MutableList<Destino>) :
    RecyclerView.Adapter<DestinoAdapter.DestinoViewHolder>() {

    class DestinoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivImagen: ImageView = itemView.findViewById(R.id.ivItemDestino)
        val tvNombre: TextView = itemView.findViewById(R.id.tvItemNombre)
        val tvPrecio: TextView = itemView.findViewById(R.id.tvItemPrecio)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tvItemDescripcion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_destino, parent, false)
        return DestinoViewHolder(view)
    }

    override fun onBindViewHolder(holder: DestinoViewHolder, position: Int) {
        val destino = destinosList[position]
        holder.tvNombre.text = destino.nombre
        holder.tvPrecio.text = "Precio: $" + destino.precio
        holder.tvDescripcion.text = destino.descripcion

        Glide.with(holder.itemView.context)
            .load(destino.imageUrl)
            .into(holder.ivImagen)

        // CLIC NORMAL: Ir a la pantalla de Editar
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, EditDestinoActivity::class.java)
            intent.putExtra("id", destino.id)
            intent.putExtra("nombre", destino.nombre)
            intent.putExtra("pais", destino.pais)
            intent.putExtra("precio", destino.precio)
            intent.putExtra("descripcion", destino.descripcion)
            intent.putExtra("imageUrl", destino.imageUrl)
            holder.itemView.context.startActivity(intent)
        }

        // CLIC LARGO: Confirmar y Eliminar (Delete)
        holder.itemView.setOnLongClickListener {
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Eliminar Destino")
            builder.setMessage("¿Estás seguro de que deseas eliminar '${destino.nombre}'? Esta acción no se puede deshacer.")

            builder.setPositiveButton("Sí, eliminar") { _, _ ->
                val db = FirebaseFirestore.getInstance()
                db.collection("destinos").document(destino.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(holder.itemView.context, "Destino eliminado", Toast.LENGTH_SHORT).show()

                        // SOLUCIÓN: Buscamos el ítem exacto por su ID y lo borramos de forma segura
                        val index = destinosList.indexOfFirst { it.id == destino.id }
                        if (index != -1) {
                            destinosList.removeAt(index)
                            notifyItemRemoved(index)
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(holder.itemView.context, "Error al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return destinosList.size
    }
}