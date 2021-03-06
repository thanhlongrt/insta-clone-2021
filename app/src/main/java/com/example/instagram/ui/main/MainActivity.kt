package com.example.instagram.ui.main

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.instagram.R
import com.example.instagram.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var currentNavigationController: LiveData<NavController>? = null

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var navController: NavController

    override fun onStart() {
        super.onStart()
        useLightStatusBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.getCurrentUserData()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { navController, destination, _ ->
            Log.e(
                TAG,
                "setupBottomNavigationBar: ${resources.getResourceName(destination.id)}",
            )
            configUi(destination, navController)
        }

    }

    private fun configUi(
        destination: NavDestination,
        navController: NavController
    ) {
        if (destination.id == R.id.storyFragment) {
            hideUIs()
        } else {
            showUIs()
        }
    }

    private fun showKeyboard(searchEditText: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showUIs() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.root.windowInsetsController?.show(WindowInsets.Type.statusBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        binding.bottomNavView.visibility = View.VISIBLE
        useLightStatusBar()
    }

    private fun hideUIs() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.root.windowInsetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        binding.bottomNavView.visibility = View.GONE
    }

    private fun useLightStatusBar() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
//                window.statusBarColor = Color.WHITE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavigationController?.value?.navigateUp() ?: false
    }
}