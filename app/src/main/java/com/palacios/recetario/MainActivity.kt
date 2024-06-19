package com.palacios.recetario

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val registro: Button = findViewById(R.id.regsitrarmain)
        registro.setOnClickListener{
            val irRegistro = Intent(this,RegisterActivity::class.java)
            startActivity(irRegistro)
        }
    }
}