package com.banti.computershortcuts

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ActivityShortcutDetail : AppCompatActivity() {

    private lateinit var shortcut: Shortcut
    private lateinit var selectedOS: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shortcut_detail)

        shortcut = intent.getParcelableExtra("shortcut")!!
        selectedOS = intent.getStringExtra("os") ?: "windows"

        setupViews()
    }

    private fun setupViews() {
        val titleTextView: TextView = findViewById(R.id.titleTextView)
        val categoryTextView: TextView = findViewById(R.id.categoryTextView)
        val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
        val windowsShortcut: TextView = findViewById(R.id.windowsShortcut)
        val macShortcut: TextView = findViewById(R.id.macShortcut)
        val linuxShortcut: TextView = findViewById(R.id.linuxShortcut)
        val copyButton: Button = findViewById(R.id.copyButton)
        val favoriteButton: Button = findViewById(R.id.favoriteButton)
        val backButton: Button = findViewById(R.id.backButton)

        titleTextView.text = shortcut.action
        categoryTextView.text = shortcut.category
        descriptionTextView.text = shortcut.description
        windowsShortcut.text = "Windows: ${shortcut.windows}"
        macShortcut.text = "Mac: ${shortcut.mac}"
        linuxShortcut.text = "Linux: ${shortcut.linux}"

        // Highlight selected OS
        when(selectedOS) {
            "windows" -> windowsShortcut.setBackgroundColor(getColor(android.R.color.holo_blue_light))
            "mac" -> macShortcut.setBackgroundColor(getColor(android.R.color.holo_blue_light))
            "linux" -> linuxShortcut.setBackgroundColor(getColor(android.R.color.holo_blue_light))
        }

        copyButton.setOnClickListener {
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = android.content.ClipData.newPlainText("shortcut", shortcut.getShortcutForOS(selectedOS))
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
        }

        updateFavoriteButton(favoriteButton)

        favoriteButton.setOnClickListener {
            toggleFavorite()
            updateFavoriteButton(favoriteButton)
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun toggleFavorite() {
        val prefs = getSharedPreferences("favorites", MODE_PRIVATE)
        val favorites = prefs.getStringSet("favorite_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        if (favorites.contains(shortcut.id.toString())) {
            favorites.remove(shortcut.id.toString())
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show()
        } else {
            favorites.add(shortcut.id.toString())
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show()
        }

        prefs.edit().putStringSet("favorite_ids", favorites).apply()
    }

    private fun updateFavoriteButton(button: Button) {
        val prefs = getSharedPreferences("favorites", MODE_PRIVATE)
        val favorites = prefs.getStringSet("favorite_ids", mutableSetOf()) ?: mutableSetOf()

        if (favorites.contains(shortcut.id.toString())) {
            button.text = "★ Remove from Favorites"
        } else {
            button.text = "☆ Add to Favorites"
        }
    }
}

// ===== 3. FavoritesActivity.kt =====
class FavoritesActivity : AppCompatActivity() {

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