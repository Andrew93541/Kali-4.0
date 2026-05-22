package com.kali.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.kali.R
import com.kali.databinding.ActivityRegisterBinding
import com.kali.network.RegisterRequest
import com.kali.network.RetrofitClient
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private var selectedRole: String = "User"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRoleChips()

        binding.btnRegister.setOnClickListener {
            validateAndRegister()
        }

        binding.tvSignInLink.setOnClickListener {
            navigateToLogin()
        }
    }

    // ─── Role chip selection ──────────────────────────────────────────────────

    private fun setupRoleChips() {
        selectRoleChip("User")

        binding.chipUser.setOnClickListener { selectRoleChip("User") }
        binding.chipGuardian.setOnClickListener { selectRoleChip("Guardian") }
        binding.chipPolice.setOnClickListener { selectRoleChip("Police") }
    }

    private fun selectRoleChip(role: String) {
        selectedRole = role
        binding.tvSelectedRole.text = "Role: $role"

        val activeColor = ContextCompat.getColor(this, R.color.kali_pink)
        val inactiveColor = 0x1E1A33.or(-0x1000000) // #FF1E1A33 — same as app:backgroundTint in XML

        val chips = listOf(
            binding.chipUser to "User",
            binding.chipGuardian to "Guardian",
            binding.chipPolice to "Police"
        )

        chips.forEach { (chip, chipRole) ->
            if (chipRole == role) {
                chip.setBackgroundColor(activeColor)
                chip.setTextColor(ContextCompat.getColor(this, R.color.white))
            } else {
                chip.setBackgroundColor(inactiveColor)
                chip.setTextColor(0x88FFFFFF.toInt())
            }
        }
    }

    // ─── Validation ───────────────────────────────────────────────────────────

    private fun validateAndRegister() {
        val name = binding.etName.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

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
        if (phone.length < 7) {
            binding.etPhone.error = "Enter a valid phone number"
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

        lifecycleScope.launch {
            registerUser(RegisterRequest(name, phone, email, password, selectedRole))
        }
    }

    // ─── Network call ─────────────────────────────────────────────────────────

    private suspend fun registerUser(request: RegisterRequest) {
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "Creating Account..."

        val result = runCatching {
            RetrofitClient.api.register(request)
        }

        binding.btnRegister.isEnabled = true
        binding.btnRegister.text = "CREATE ACCOUNT"

        val response = result.getOrNull()
        if (response != null) {
            if (response.isSuccessful) {
                Toast.makeText(
                    this,
                    "Account created! Please sign in.",
                    Toast.LENGTH_LONG
                ).show()
                navigateToLogin()
            } else {
                val errorMsg = when (response.code()) {
                    409 -> "Email is already registered"
                    400 -> "Invalid registration details"
                    else -> "Registration failed (${response.code()}). Try again."
                }
                Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            }
        } else {
            val throwable = result.exceptionOrNull()
            Toast.makeText(
                this,
                "Network error: ${throwable?.message ?: "Check your server connection"}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }
}
