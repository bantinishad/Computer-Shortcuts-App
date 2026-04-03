package com.banti.computershortcuts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class ShortcutAdapter(
    private var shortcuts: List<Shortcut>,
    private var selectedOS: String,
    private val onItemClick: (Shortcut) -> Unit
) : RecyclerView.Adapter<ShortcutAdapter.ShortcutViewHolder>() {

    class ShortcutViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val actionTextView: TextView = view.findViewById(R.id.actionTextView)
        val categoryTextView: TextView = view.findViewById(R.id.categoryTextView)
        val shortcutTextView: TextView = view.findViewById(R.id.shortcutTextView)
        val descriptionTextView: TextView = view.findViewById(R.id.descriptionTextView)
        val cardView: androidx.cardview.widget.CardView = view.findViewById(R.id.cardView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shortcut, parent, false)
        return ShortcutViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShortcutViewHolder, position: Int) {
        val shortcut = shortcuts[position]
        holder.actionTextView.text = shortcut.action
        holder.categoryTextView.text = shortcut.category
        holder.shortcutTextView.text = shortcut.getShortcutForOS(selectedOS)
        holder.descriptionTextView.text = shortcut.description

        holder.cardView.setOnClickListener {
            onItemClick(shortcut)
        }
    }

    override fun getItemCount() = shortcuts.size

    fun updateData(newShortcuts: List<Shortcut>, newOS: String) {
        shortcuts = newShortcuts
        selectedOS = newOS
        notifyDataSetChanged()
    }
}