package com.palacios.recetario

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.palacios.recetario.models.Recipe
import java.util.*

class ModifyRecipeActivity : AppCompatActivity() {

    private lateinit var recipeSpinner: Spinner
    private lateinit var categorySpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var ingredientsEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var instructionsEditText: EditText
    private lateinit var selectImageButton: Button
    private lateinit var recipeImageView: ImageView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null
    private var recipes: List<Pair<String, Recipe>> = listOf()
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_recipe)

        recipeSpinner = findViewById(R.id.recipeSpinner)
        categorySpinner = findViewById(R.id.modifyRecipeCategory)
        typeSpinner = findViewById(R.id.modifyRecipeType)
        ingredientsEditText = findViewById(R.id.modifyRecipeIngredients)
        timeEditText = findViewById(R.id.modifyRecipeTime)
        instructionsEditText = findViewById(R.id.modifyRecipeInstructions)
        selectImageButton = findViewById(R.id.selectImageButton)
        recipeImageView = findViewById(R.id.recipeImageView)
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        setupSpinner(categorySpinner, R.array.categories, "Seleccione una categoría")
        setupSpinner(typeSpinner, R.array.types, "Seleccione un tipo")

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        val modifyButton: Button = findViewById(R.id.modifyRecipeButton)
        modifyButton.setOnClickListener {
            modifyRecipe()
        }

        val salirModifyRecipeButton: Button = findViewById(R.id.modifySalirRecipeButton)
        salirModifyRecipeButton.setOnClickListener {
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

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            Glide.with(this).load(imageUri).into(recipeImageView)
        }
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
        findViewById<EditText>(R.id.modifyRecipeTitle).setText(recipe.titulo)
        categorySpinner.setSelection(resources.getStringArray(R.array.categories).indexOf(recipe.categoria))
        typeSpinner.setSelection(resources.getStringArray(R.array.types).indexOf(recipe.tipo))
        ingredientsEditText.setText(recipe.ingredientes)
        timeEditText.setText(recipe.tiempoPreparacion)
        instructionsEditText.setText(recipe.modoPreparacion)

        if (recipe.imagenUrl.isNotEmpty()) {
            Glide.with(this)
                .load(recipe.imagenUrl)
                .placeholder(R.drawable.ic_placeholder_image)
                .into(recipeImageView)
        } else {
            recipeImageView.setImageResource(R.drawable.ic_placeholder_image)
        }
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

        val title = findViewById<EditText>(R.id.modifyRecipeTitle).text.toString()

        val recipe = Recipe(
            titulo = title,
            categoria = selectedCategory,
            tipo = selectedType,
            ingredientes = ingredients,
            tiempoPreparacion = time,
            modoPreparacion = instructions,
            imagenUrl = ""
        )

        val recipeId = recipes.find { it.second.titulo == recipe.titulo }?.first ?: return

        if (imageUri != null) {
            uploadImageAndSaveRecipe(recipe, imageUri!!, recipeId)
        } else {
            updateRecipeInDatabase(recipe, recipeId)
        }
    }

    private fun uploadImageAndSaveRecipe(recipe: Recipe, imageUri: Uri, recipeId: String) {
        val storageRef = storage.reference.child("images/${UUID.randomUUID()}.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    recipe.imagenUrl = uri.toString()
                    updateRecipeInDatabase(recipe, recipeId)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al cargar la imagen: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateRecipeInDatabase(recipe: Recipe, recipeId: String) {
        firestore.collection("recetas").document(recipeId)
            .set(recipe)
            .addOnSuccessListener {
                Toast.makeText(this, "Receta actualizada correctamente.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error al actualizar receta: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
