package com.example.instagram.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.instagram.R
import com.example.instagram.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private val loginViewModel: LoginViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        val isLoggedIn = loginViewModel.isLoggedIn
        if (isLoggedIn){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        val hostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_login_host_fragment) as NavHostFragment?
                ?: return

        val navController = hostFragment.navController

    }
}