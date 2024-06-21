package com.palacios.recetario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class SeleccionActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seleccion)

        val buttonAgregar: Button = findViewById(R.id.agregarRecipe)
        val buttonModificar: Button = findViewById(R.id.modificaRecipe)
        val buttonEliminar: Button = findViewById(R.id.borraRecipe)
        val buttonVer: Button = findViewById(R.id.verRecipe)
        val buttonCerrar: Button = findViewById(R.id.salirApp)

        buttonAgregar.setOnClickListener{
            val intent = Intent(this, RecipeActivity::class.java)
            startActivity(intent)
        }

        buttonModificar.setOnClickListener {
            val intent = Intent( this,ModifyRecipeActivity::class.java)
            startActivity(intent)
        }

        buttonEliminar.setOnClickListener {
            val intent = Intent( this,DeleteRecipeActivity::class.java)
            startActivity(intent)
        }

        buttonVer.setOnClickListener {
            val intent = Intent( this,ViewRecipeActivity::class.java)
            startActivity(intent)
        }

        buttonCerrar.setOnClickListener {
            closeApp()
        }
    }
    private fun closeApp(){
        finishAffinity()
    }
}