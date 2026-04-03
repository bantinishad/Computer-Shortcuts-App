package com.banti.computershortcuts


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText
    private lateinit var osSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var favoritesButton: Button

    private var allShortcuts = listOf<Shortcut>()
    private var filteredShortcuts = listOf<Shortcut>()
    private lateinit var adapter: ShortcutAdapter

    private var selectedOS = "windows"
    private var selectedCategory = "All"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        loadShortcutsFromJSON()
        setupSpinners()
        setupRecyclerView()
        setupSearch()
        setupFavoritesButton()
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recyclerView)
        searchEditText = findViewById(R.id.searchEditText)
        osSpinner = findViewById(R.id.osSpinner)
        categorySpinner = findViewById(R.id.categorySpinner)
        favoritesButton = findViewById(R.id.favoritesButton)
    }

    private fun loadShortcutsFromJSON() {
        try {
            val jsonString = assets.open("shortcuts.json").bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Shortcut>>() {}.type
            allShortcuts = Gson().fromJson(jsonString, type)
            filteredShortcuts = allShortcuts
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading shortcuts", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinners() {
        // OS Spinner
        val osOptions = arrayOf("Windows", "Mac", "Linux")
        val osAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, osOptions)
        osAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        osSpinner.adapter = osAdapter

        osSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedOS = osOptions[position].lowercase()
                filterShortcuts()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Category Spinner
        val categories = listOf("All") + allShortcuts.map { it.category }.distinct().sorted()
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = categoryAdapter

        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                filterShortcuts()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupRecyclerView() {
        adapter = ShortcutAdapter(filteredShortcuts, selectedOS) { shortcut ->
            // Open detail activity
            val intent = Intent(this, ActivityShortcutDetail::class.java)
            intent.putExtra("shortcut", shortcut)
            intent.putExtra("os", selectedOS)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearch() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterShortcuts()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupFavoritesButton() {
        favoritesButton.setOnClickListener {
            val intent = Intent(this, ActivityFavorites::class.java)
            intent.putExtra("os", selectedOS)
            startActivity(intent)
        }
    }

    private fun filterShortcuts() {
        val searchQuery = searchEditText.text.toString().lowercase()

        filteredShortcuts = allShortcuts.filter { shortcut ->
            val matchesCategory = selectedCategory == "All" || shortcut.category == selectedCategory
            val matchesSearch = searchQuery.isEmpty() ||
                    shortcut.action.lowercase().contains(searchQuery) ||
                    shortcut.description.lowercase().contains(searchQuery) ||
                    shortcut.getShortcutForOS(selectedOS).lowercase().contains(searchQuery)

            matchesCategory && matchesSearch
        }

        adapter.updateData(filteredShortcuts, selectedOS)
    }
}