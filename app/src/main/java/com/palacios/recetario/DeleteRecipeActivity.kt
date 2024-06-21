package com.palacios.recetario

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.palacios.recetario.models.Recipe

class DeleteRecipeActivity : AppCompatActivity() {

    private lateinit var recipeSpinner: Spinner
    private lateinit var firestore: FirebaseFirestore
    private var recipes: List<Pair<String, Recipe>> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_recipe)

        recipeSpinner = findViewById(R.id.recipeSpinner)
        firestore = FirebaseFirestore.getInstance()

        val deleteButton: Button = findViewById(R.id.deleteRecipeButton)
        deleteButton.setOnClickListener {
            deleteRecipe()
        }
        val salirRecipeButton: Button = findViewById(R.id.salirDeleteRecipeButton)
        salirRecipeButton.setOnClickListener {
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
                        modoPreparacion = document.getString("modoPreparacion") ?: ""
                    )
                    Pair(document.id, recipe)
                }.sortedBy { it.second.titulo }

                val recipeTitles = recipes.map { it.second.titulo }
                val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recipeTitles)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                recipeSpinner.adapter = adapter

                recipeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        // No es necesario hacer nada aquí por ahora
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

    private fun deleteRecipe() {
        val selectedPosition = recipeSpinner.selectedItemPosition
        if (selectedPosition >= 0) {
            val recipeId = recipes[selectedPosition].first
            firestore.collection("recetas").document(recipeId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(this, "Receta eliminada correctamente.", Toast.LENGTH_SHORT).show()
                    loadRecipesIntoSpinner() // Recargar el spinner después de eliminar
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Error al eliminar receta: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Por favor, seleccione una receta para eliminar.", Toast.LENGTH_SHORT).show()
        }
    }
}
