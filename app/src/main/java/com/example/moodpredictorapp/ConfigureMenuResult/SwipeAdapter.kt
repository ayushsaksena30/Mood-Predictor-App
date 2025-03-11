package com.example.moodpredictorapp.ConfigureMenuResult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.moodpredictorapp.R

class SwipeAdapter(private val categories: List<String>, private val images: List<Int>) : RecyclerView.Adapter<SwipeAdapter.SwipeViewHolder>() {

    class SwipeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.iv_cardImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SwipeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_swipe_card, parent, false)
        return SwipeViewHolder(view)
    }

    override fun onBindViewHolder(holder: SwipeViewHolder, position: Int) {
        holder.image.setImageResource(images[position])
    }

    override fun getItemCount(): Int = categories.size
}
