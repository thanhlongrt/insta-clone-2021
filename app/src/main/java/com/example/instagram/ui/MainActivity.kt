package com.example.instagram.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
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
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.instagram.R
import com.example.instagram.databinding.ActivityMainBinding
import com.example.instagram.setupWithNavController
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

    override fun onStart() {
        super.onStart()
        useLightStatusBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mainViewModel.getCurrentUserData()

        setSupportActionBar(binding.toolBar)

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }

    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_nav_view)

        val navGraphIds = listOf(
            R.navigation.nav_home,
            R.navigation.nav_explore,
            R.navigation.nav_reel,
            R.navigation.nav_notification,
            R.navigation.nav_profile,
        )

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_fragment,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, { navController ->
            setupActionBarWithNavController(navController)
            navController.addOnDestinationChangedListener { navController, destination, _ ->
                Log.e(
                    TAG,
                    "setupBottomNavigationBar: ${resources.getResourceName(destination.id)}",
                )
                configUi(destination, navController)

            }
        })
        currentNavigationController = controller

    }

    private fun configUi(
        destination: NavDestination,
        navController: NavController
    ) {
        binding.toolBar.title = when (destination.id) {
            R.id.homeFragment -> "Instagram"
            R.id.notificationFragment -> "Notifications"
            R.id.userPostsFragment -> "Posts"
            R.id.editProfileFragment -> "Edit Profile"
            R.id.createNewPostFragment -> "New post"
            else -> ""
        }

        if (destination.id == R.id.storyFragment) {
            hideUIs()
        } else {
            showUIs()
        }

        binding.searchViewHolder.setOnClickListener {
            navController.navigate(R.id.action_exploreFragment_to_searchFragment)
        }
        if (destination.id == R.id.exploreFragment) {
            binding.searchViewHolder.visibility = View.VISIBLE

        } else {
            binding.searchViewHolder.visibility = View.GONE

        }

        val searchEditText =
            binding.searchView.findViewById(androidx.appcompat.R.id.search_src_text) as EditText
        searchEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        if (destination.id == R.id.searchFragment) {
            binding.searchView.visibility = View.VISIBLE
            searchEditText.requestFocus()
            showKeyboard(searchEditText)
        } else {
            binding.searchView.visibility = View.GONE
        }

        binding.bottomNavView.visibility =
            if (destination.id == R.id.commentFragment) View.GONE else View.VISIBLE
    }

    private fun showKeyboard(searchEditText: EditText) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun showUIs() {
        binding.toolBar.visibility = View.VISIBLE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            binding.root.windowInsetsController?.show(WindowInsets.Type.statusBars())
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
        useLightStatusBar()

        binding.bottomNavView.visibility = View.VISIBLE
    }

    private fun hideUIs() {
        binding.toolBar.visibility = View.GONE

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