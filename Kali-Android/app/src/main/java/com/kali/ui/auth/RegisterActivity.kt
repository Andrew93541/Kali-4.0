package com.kali.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kali.databinding.ActivityRegisterBinding
import com.kali.network.RegisterRequest
import com.kali.network.RetrofitClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the Role dropdown selection
        val roles = arrayOf("User", "Guardian", "Police")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, roles)
        binding.actvRole.setAdapter(adapter)
        // Default select the first item
        binding.actvRole.setText(roles[0], false)

        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        binding.tvSignInLink.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun validateAndRegister() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val role = binding.actvRole.text.toString()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            binding.etName.requestFocus()
            return
        }
        if (phone.isEmpty()) {
            binding.etPhone.error = "Phone number is required"
            binding.etPhone.requestFocus()
            return
        }
        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.error = "Enter a valid email address"
            binding.etEmail.requestFocus()
            return
        }
        if (password.isEmpty() || password.length < 6) {
            binding.etPassword.error = "Password must be at least 6 characters"
            binding.etPassword.requestFocus()
            return
        }
        if (role.isEmpty()) {
            binding.actvRole.error = "Please select a role"
            return
        }

        lifecycleScope.launch {
            registerUser(RegisterRequest(name, phone, email, password, role))
        }
    }

    private suspend fun registerUser(request: RegisterRequest) {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating Account..."

        val result = runCatching {
            RetrofitClient.api.register(request)
        }

        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = "Sign Up"

        val response = result.getOrNull()
        if (response != null) {
            if (response.isSuccessful) {
                Toast.makeText(this, "Registration Successful! Please Sign In.", Toast.LENGTH_LONG).show()
                navigateToLogin()
            } else {
                val errorMsg = when (response.code()) {
                    409 -> "Email is already registered"
                    else -> "Registration failed. Please try again."
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Network connection error. Check your server.", Toast.LENGTH_LONG).show()
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
