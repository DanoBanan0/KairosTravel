package edu.udb.kairostravel

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class DestinoAdapter(private val destinosList: List<Destino>) :
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

        // Aquí usamos Glide para descargar y mostrar la imagen desde la URL de Firebase
        Glide.with(holder.itemView.context)
            .load(destino.imageUrl)
            .into(holder.ivImagen)
    }

    override fun getItemCount(): Int {
        return destinosList.size
    }
}