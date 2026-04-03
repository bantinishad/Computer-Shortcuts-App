package com.banti.computershortcuts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ActivityFavorites : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyTextView: TextView
    private lateinit var backButton: Button

    private var favoriteShortcuts = listOf<Shortcut>()
    private lateinit var adapter: ShortcutAdapter
    private lateinit var selectedOS: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        selectedOS = intent.getStringExtra("os") ?: "windows"

        recyclerView = findViewById(R.id.favoritesRecyclerView)
        emptyTextView = findViewById(R.id.emptyTextView)
        backButton = findViewById(R.id.backButton)

        loadFavorites()
        setupRecyclerView()

        backButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
        updateUI()
    }

    private fun loadFavorites() {
        val prefs = getSharedPreferences("favorites", MODE_PRIVATE)
        val favoriteIds = prefs.getStringSet("favorite_ids", mutableSetOf()) ?: mutableSetOf()

        // Load all shortcuts
        val jsonString = assets.open("shortcuts.json").bufferedReader().use { it.readText() }
        val type = object : TypeToken<List<Shortcut>>() {}.type
        val allShortcuts: List<Shortcut> = Gson().fromJson(jsonString, type)

        favoriteShortcuts = allShortcuts.filter { favoriteIds.contains(it.id.toString()) }
    }

    private fun setupRecyclerView() {
        adapter = ShortcutAdapter(favoriteShortcuts, selectedOS) { shortcut ->
            val intent = Intent(this, ActivityShortcutDetail::class.java)
            intent.putExtra("shortcut", shortcut)
            intent.putExtra("os", selectedOS)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun updateUI() {
        if (favoriteShortcuts.isEmpty()) {
            recyclerView.visibility = View.GONE
            emptyTextView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            emptyTextView.visibility = View.GONE
            adapter.updateData(favoriteShortcuts, selectedOS)
        }
    }
}