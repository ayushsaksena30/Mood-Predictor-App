package com.example.moodpredictorapp.ConfigureMenu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moodpredictorapp.R

class MyAdapter(
    private val itemList: List<Item>,
    private val onClick: (String) -> Unit // Pass the item name instead of view and position
) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_layout, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]
        holder.textView.text = item.text
        holder.imageView.setImageResource(item.imageResId)

        holder.itemView.setOnClickListener {
            onClick(item.text) // Pass the item name to the callback
        }
    }

    override fun getItemCount(): Int = itemList.size
}