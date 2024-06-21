package com.palacios.recetario.models

data class Recipe(
    val titulo: String = "",
    val categoria: String = "",
    val tipo: String = "",
    val ingredientes: String = "",
    val tiempoPreparacion: String = "",
    val modoPreparacion: String = "",
    val imagenUrl: String =""
)
