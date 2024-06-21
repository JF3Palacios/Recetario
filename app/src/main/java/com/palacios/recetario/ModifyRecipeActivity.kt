package com.palacios.recetario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.palacios.recetario.models.Recipe

class ModifyRecipeActivity : AppCompatActivity() {

    private lateinit var recipeSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var ingredientsEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var instructionsEditText: EditText
    private lateinit var firestore: FirebaseFirestore
    private var recipes: List<Recipe> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_recipe)

        recipeSpinner = findViewById(R.id.recipeSpinner)
        categorySpinner = findViewById(R.id.modifyRecipeCategory)
        typeSpinner = findViewById(R.id.modifyRecipeType)
        ingredientsEditText = findViewById(R.id.modifyRecipeIngredients)
        timeEditText = findViewById(R.id.modifyRecipeTime)
        instructionsEditText = findViewById(R.id.modifyRecipeInstructions)
        firestore = FirebaseFirestore.getInstance()

        setupSpinner(categorySpinner, R.array.categories, "Seleccione una categoría")
        setupSpinner(typeSpinner, R.array.types, "Seleccione un tipo")

        val modifyButton: Button = findViewById(R.id.modifyRecipeButton)
        modifyButton.setOnClickListener {
            modifyRecipe()
        }
        val salirModificarRecipeButton: Button = findViewById(R.id.salirModifyRecipeButton)
        salirModificarRecipeButton.setOnClickListener {
            val intent = Intent(this, SeleccionActivity::class.java)
            startActivity(intent)
        }

        loadRecipesIntoSpinner()
    }

    private fun setupSpinner(spinner: Spinner, arrayResId: Int, hint: String) {
        val items = resources.getStringArray(arrayResId).toMutableList()
        items.add(0, hint) // Añadir el hint como el primer elemento

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0, false)
    }

    private fun loadRecipesIntoSpinner() {
        firestore.collection("recetas")
            .get()
            .addOnSuccessListener { result ->
                recipes = result.map { document ->
                    Recipe(
                        titulo = document.getString("titulo") ?: "",
                        categoria = document.getString("categoria") ?: "",
                        tipo = document.getString("tipo") ?: "",
                        ingredientes = document.getString("ingredientes") ?: "",
                        tiempoPreparacion = document.getString("tiempoPreparacion") ?: "",
                        modoPreparacion = document.getString("modoPreparacion") ?: ""
                    )
                }.sortedBy { it.titulo }

                val recipeTitles = recipes.map { it.titulo }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTitles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                recipeSpinner.adapter = adapter

                recipeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        if (position >= 0) { // Evitar la selección del hint
                            val selectedRecipe = recipes.find { it.titulo == recipeTitles[position] }
                            selectedRecipe?.let { populateRecipeDetails(it) }
                        }
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

    private fun populateRecipeDetails(recipe: Recipe) {
        findViewById<EditText>(R.id.modifyRecipeTitle).setText(recipe.titulo)
        categorySpinner.setSelection(resources.getStringArray(R.array.categories).indexOf(recipe.categoria))
        typeSpinner.setSelection(resources.getStringArray(R.array.types).indexOf(recipe.tipo))
        ingredientsEditText.setText(recipe.ingredientes)
        timeEditText.setText(recipe.tiempoPreparacion)
        instructionsEditText.setText(recipe.modoPreparacion)
    }

    private fun modifyRecipe() {
        val selectedCategory = categorySpinner.selectedItem.toString()
        val selectedType = typeSpinner.selectedItem.toString()
        val ingredients = ingredientsEditText.text.toString().trim()
        val time = timeEditText.text.toString().trim()
        val instructions = instructionsEditText.text.toString().trim()

        if (selectedCategory == "Seleccione una categoría") {
            Toast.makeText(this, "Por favor, seleccione una categoría válida.", Toast.LENGTH_SHORT).show()
            return
        }

        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese los ingredientes.", Toast.LENGTH_SHORT).show()
            return
        }

        if (time.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el tiempo de preparación.", Toast.LENGTH_SHORT).show()
            return
        }

        if (instructions.isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el modo de preparación.", Toast.LENGTH_SHORT).show()
            return
        }

        val recipe = Recipe(
            titulo = findViewById<EditText>(R.id.modifyRecipeTitle).text.toString(),
            categoria = selectedCategory,
            tipo = selectedType,
            ingredientes = ingredients,
            tiempoPreparacion = time,
            modoPreparacion = instructions
        )

        // Implementa la lógica para actualizar la receta en la base de datos
        updateRecipeInDatabase(recipe)
        Toast.makeText(this, "Receta modificada correctamente.", Toast.LENGTH_SHORT).show()
    }

    private fun updateRecipeInDatabase(recipe: Recipe) {
        // Implementa la lógica para actualizar la receta en la base de datos
        firestore.collection("recetas").document(recipe.titulo)
            .set(recipe)
            .addOnSuccessListener {
                Toast.makeText(this, "Receta actualizada correctamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al actualizar receta: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
