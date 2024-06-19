package com.palacios.recetario

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.palacios.recetario.R
import java.util.*
import java.util.Locale.Category

class RecipeActivity : AppCompatActivity() {
    private lateinit var titleEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var ingredientsEditText: EditText
    private lateinit var timeEditText: EditText
    private lateinit var instructionsEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var salirRecipeButton: Button
    private lateinit var recipeImageView: ImageView
    private var imageUri: Uri? = null

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val PICK_IMAGE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_edit_recipe)

        titleEditText = findViewById(R.id.recipeTitle)
        categorySpinner = findViewById(R.id.recipeCategory)
        typeSpinner = findViewById(R.id.recipeType)
        ingredientsEditText = findViewById(R.id.recipeIngredients)
        timeEditText = findViewById(R.id.recipeTime)
        instructionsEditText = findViewById(R.id.recipeInstructions)
        saveButton = findViewById(R.id.saveRecipeButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        recipeImageView = findViewById(R.id.recipeImageView)
        salirRecipeButton = findViewById(R.id.salirRecipes)

        selectImageButton.setOnClickListener {
            openFileChooser()
        }

        saveButton.setOnClickListener {
            saveRecipe()
        }

        salirRecipeButton.setOnClickListener {
            val intent = Intent(this, SeleccionActivity::class.java)
            startActivity(intent)
        }

        setupSpinner(categorySpinner, R.array.categories, "Seleccione una categoría")
        setupSpinner(typeSpinner, R.array.types, "Seleccione un tipo")
    }
    private fun setupSpinner(spinner: Spinner, arrayResId: Int, hint: String){
        val items = resources.getStringArray(arrayResId).toMutableList()
        items.add(0, hint)

        val adapter = HintSpinnerAdapter(this, android.R.layout.simple_spinner_item, items.toTypedArray())
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(0, false)
    }

    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK &&
            data != null && data.data != null) {
            imageUri = data.data
            recipeImageView.setImageURI(imageUri)
        }
    }

    private fun saveRecipe() {
        val title = titleEditText.text.toString()
        val category = categorySpinner.selectedItem.toString()
        //val type = typeSpinner.selectedItem.toString()
        val ingredients = ingredientsEditText.text.toString()
        val time = timeEditText.text.toString()
        val instructions = instructionsEditText.text.toString()
        val selectedCategory = categorySpinner.selectedItem.toString()
        val selectedType = typeSpinner.selectedItem.toString()

        val type = if (selectedType != "Seleccione un tipo") selectedType else ""

        if (title.isEmpty()) {
            Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategory == "Seleccione una categoría") {
            Toast.makeText(this, "Por favor, seleccione una categoría válida.", Toast.LENGTH_SHORT).show()
            return
        }

        if (ingredients.isEmpty()) {
            Toast.makeText(this, "Ingredientes no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (time.isEmpty()) {
            Toast.makeText(this, "El tiempo de preparación no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (instructions.isEmpty()) {
            Toast.makeText(this, "El modo de prparación no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        if (imageUri != null) {
            val fileName = UUID.randomUUID().toString()
            val storageRef = storage.reference.child("recetas/$fileName")

            storageRef.putFile(imageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val recipe = hashMapOf(
                            "titulo" to title,
                            "categoria" to category,
                            "tipo" to type,
                            "ingredientes" to ingredients,
                            "tiempoPreparacion" to time,
                            "modoPreparacion" to instructions,
                            "imagenUrl" to uri.toString()
                        )

                        db.collection("recetas")
                            .document(title)
                            .set(recipe)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Error guardando receta", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error subiendo imagen", Toast.LENGTH_SHORT).show()
                }
        } else {
            val recipe = hashMapOf(
                "titulo" to title,
                "categoria" to category,
                "tipo" to type,
                "ingredientes" to ingredients,
                "tiempoPreparacion" to time,
                "modoPreparacion" to instructions
            )

            db.collection("recetas")
                .document(title)
                .set(recipe)
                .addOnSuccessListener {
                    Toast.makeText(this, "Receta guardada", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error guardando receta", Toast.LENGTH_SHORT).show()
                }
        }
    }

    class HintSpinnerAdapter(context: Context, textViewResourceId: Int, private val objects: Array<String>) :
        ArrayAdapter<String>(context, textViewResourceId, objects) {

        override fun isEnabled(position: Int): Boolean {
            return position !=0
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            val textView = view as TextView
            if(position == 0){
                textView.setTextColor(Color.GRAY)
            } else{
                textView.setTextColor(Color.BLACK)
            }
            return view
        }
    }
}
