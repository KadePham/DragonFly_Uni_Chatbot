package com.example.chatgptapi.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.chatgptapi.R

class SettingsNavAdapter(
    private val items: List<NavItem>,
    private val onClick: (NavItem) -> Unit
) : RecyclerView.Adapter<SettingsNavAdapter.VH>() {

    private var selectedPos = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_settings_nav, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]              // <-- rename it -> item
        holder.title.text = item.title
        holder.icon.setImageResource(item.iconRes)
        holder.itemView.isSelected = (position == selectedPos)
        holder.itemView.setOnClickListener { v ->
            val old = selectedPos
            selectedPos = position
            notifyItemChanged(old)
            notifyItemChanged(selectedPos)
            onClick(item)                       // <-- pass NavItem
        }
    }


    override fun getItemCount(): Int = items.size

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.icon)
        val title: TextView = view.findViewById(R.id.title)
    }
}

data class NavItem(val id: String, val title: String, val iconRes: Int)
