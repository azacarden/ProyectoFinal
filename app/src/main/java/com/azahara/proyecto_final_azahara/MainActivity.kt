package com.azahara.proyecto_final_azahara

import android.os.Bundle
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.azahara.proyecto_final_azahara.utils.PreferencesManager
import com.google.android.material.appbar.MaterialToolbar

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController

    // Sobrescribe el contexto para aplicar el tamaño de letra gigante globalmente
    override fun attachBaseContext(newBase: Context) {
        val pm = PreferencesManager(newBase)
        val config = android.content.res.Configuration(newBase.resources.configuration)
        config.fontScale = if (pm.textoGrande) 1.3f else 1.0f // Escalado del 30% más grande
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Carga la preferencia de Alto Contraste antes de pintar la actividad
        val pm = PreferencesManager(this)
        if (pm.altoContraste) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Permisos de notificación
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
            }
        }

        // Configuración para el Header (Toolbar)
        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)
        setSupportActionBar(toolbar)

        // Conecta la navegación
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Lógica de visibilidad y flecha de retroceso
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment, R.id.registroFragment -> {
                    toolbar.visibility = View.GONE
                }
                R.id.dashboardFragment -> {
                    toolbar.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                else -> {
                    toolbar.visibility = View.VISIBLE
                    supportActionBar?.setDisplayHomeAsUpEnabled(true)
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_perfil_menu -> {
                if (navController.currentDestination?.id != R.id.profileFragment) {
                    navController.navigate(R.id.profileFragment)
                }
                true
            }
            R.id.action_ajustes_menu -> {
                if (navController.currentDestination?.id != R.id.settingsFragment) {
                    navController.navigate(R.id.settingsFragment)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun dispatchTouchEvent(event: android.view.MotionEvent): Boolean {
        if (event.action == android.view.MotionEvent.ACTION_DOWN) {
            val viewActual = currentFocus
            if (viewActual is android.widget.EditText) {
                val rectanguloFuera = android.graphics.Rect()
                viewActual.getGlobalVisibleRect(rectanguloFuera)

                if (!rectanguloFuera.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    viewActual.clearFocus()
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(viewActual.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}