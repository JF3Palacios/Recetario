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
        val buttonCerrar: Button = findViewById(R.id.salirApp)

        buttonAgregar.setOnClickListener{
            val intent = Intent(this, RecipeActivity::class.java)
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