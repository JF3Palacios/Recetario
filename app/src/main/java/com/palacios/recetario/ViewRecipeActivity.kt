package com.palacios.recetario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.palacios.recetario.models.Recipe

class ViewRecipeActivity : AppCompatActivity() {

    private lateinit var recipeSpinner: Spinner
    private lateinit var recipeImageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var typeTextView: TextView
    private lateinit var ingredientsTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var instructionsTextView: TextView
    private lateinit var firestore: FirebaseFirestore
    private var recipes: List<Pair<String, Recipe>> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_recipe)

        recipeSpinner = findViewById(R.id.recipeSpinner)
        recipeImageView = findViewById(R.id.recipeImageView)
        titleTextView = findViewById(R.id.titleTextView)
        categoryTextView = findViewById(R.id.categoryTextView)
        typeTextView = findViewById(R.id.typeTextView)
        ingredientsTextView = findViewById(R.id.ingredientsTextView)
        timeTextView = findViewById(R.id.timeTextView)
        instructionsTextView = findViewById(R.id.instructionsTextView)
        firestore = FirebaseFirestore.getInstance()

        val salirVerButton: Button = findViewById(R.id.salirVerRecipes)
        salirVerButton.setOnClickListener {
            val intent = Intent(this, SeleccionActivity::class.java)
            startActivity(intent)
        }

        loadRecipesIntoSpinner()
    }

    private fun loadRecipesIntoSpinner() {
        firestore.collection("recetas")
            .get()
            .addOnSuccessListener { result ->
                recipes = result.map { document ->
                    val recipe = Recipe(
                        titulo = document.getString("titulo") ?: "",
                        categoria = document.getString("categoria") ?: "",
                        tipo = document.getString("tipo") ?: "",
                        ingredientes = document.getString("ingredientes") ?: "",
                        tiempoPreparacion = document.getString("tiempoPreparacion") ?: "",
                        modoPreparacion = document.getString("modoPreparacion") ?: "",
                        imagenUrl = document.getString("imagenUrl") ?: ""
                    )
                    Pair(document.id, recipe)
                }.sortedBy { it.second.titulo }

                val recipeTitles = recipes.map { it.second.titulo }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTitles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                recipeSpinner.adapter = adapter

                recipeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        val selectedRecipe = recipes[position].second
                        displayRecipeDetails(selectedRecipe)
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        // No hacer nada
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar recetas: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayRecipeDetails(recipe: Recipe) {
        titleTextView.text = recipe.titulo
        categoryTextView.text = "Categoría: ${recipe.categoria}"
        typeTextView.text = "Tipo: ${recipe.tipo}"
        ingredientsTextView.text = "Ingredientes:\n${recipe.ingredientes}"
        timeTextView.text = "Tiempo de preparación: ${recipe.tiempoPreparacion}"
        instructionsTextView.text = "Modo de preparación:\n${recipe.modoPreparacion}"

        if (recipe.imagenUrl.isNotEmpty()) {
            Glide.with(this)
                .load(recipe.imagenUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .into(recipeImageView)
        } else {
            recipeImageView.setImageResource(R.drawable.ic_placeholder_image)
        }
    }
}
