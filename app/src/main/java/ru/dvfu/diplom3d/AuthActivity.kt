package ru.dvfu.diplom3d

import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentActivity
import ru.dvfu.diplom3d.databinding.ActivityAuthBinding

class AuthActivity : FragmentActivity() {
    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Показываем форму входа, скрываем регистрацию
        showLoginForm()

        binding.buttonShowRegister.setOnClickListener {
            showRegisterForm()
        }
        binding.buttonShowLogin.setOnClickListener {
            showLoginForm()
        }

        // Здесь добавьте обработку кнопок входа и регистрации
    }

    private fun showLoginForm() {
        binding.loginForm.visibility = View.VISIBLE
        binding.registerForm.visibility = View.GONE
    }

    private fun showRegisterForm() {
        binding.loginForm.visibility = View.GONE
        binding.registerForm.visibility = View.VISIBLE
    }
} 