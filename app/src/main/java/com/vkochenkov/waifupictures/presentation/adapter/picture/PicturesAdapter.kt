package com.vkochenkov.waifupictures.presentation.adapter.picture

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.vkochenkov.waifupictures.R
import com.vkochenkov.waifupictures.data.model.PictureItem
import com.vkochenkov.waifupictures.presentation.adapter.ItemClickListener

class PicturesAdapter(private val pictureItemClickListener: ItemClickListener<PictureViewHolder, PictureItem>): RecyclerView.Adapter<PictureViewHolder>() {

    private var itemsList: List<PictureItem> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PictureViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.image_item, parent, false)
        return PictureViewHolder(view)
    }

    override fun onBindViewHolder(holder: PictureViewHolder, position: Int) {
        val pictureItem = itemsList[position]
        holder.bind(pictureItem)
        holder.itemView.setOnClickListener {
            val animationWhenPressed = AnimationUtils.loadAnimation(it.context, R.anim.decreases_when_pressed)
            holder.itemView.startAnimation(animationWhenPressed)
            pictureItemClickListener.onItemCLick(holder, pictureItem)
        }
    }

    override fun getItemCount(): Int {
        return itemsList.size
    }

    fun setItemsList(itemsList: List<PictureItem>) {
        this.itemsList = itemsList
    }
}