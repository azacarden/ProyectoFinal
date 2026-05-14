package com.azahara.proyecto_final_azahara

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Configuramos el Header (Toolbar)
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        // 2. Conectamos la navegación
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 3. REQUISITO TÉCNICO: Lógica de visibilidad y flecha de retroceso
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Si estamos en Login o Registro -> Ocultamos el Header por completo
                // (Los IDs de Login y Dashboard marcarán error hasta que creemos el grafo en la próxima tarea)
                R.id.loginFragment -> {
                    toolbar.visibility = View.GONE
                }
                // Si estamos en el Panel Principal -> Mostramos Header pero SIN flecha de retroceso
                R.id.dashboardFragment -> {
                    toolbar.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                // Cualquier otra pantalla (Ajustes, Detalles...) -> Mostrar Header CON flecha
                else -> {
                    toolbar.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
            }
        }
    }

    // Gestiona qué pasa cuando el usuario pulsa la flecha de "Atrás" física o la del Header
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}