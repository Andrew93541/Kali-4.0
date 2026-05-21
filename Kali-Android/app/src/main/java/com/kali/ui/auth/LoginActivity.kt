package com.kali.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.kali.databinding.ActivityLoginBinding
import com.kali.network.GoogleLoginRequest
import com.kali.network.LoginRequest
import com.kali.network.RetrofitClient
import com.kali.ui.sos.SosActivity
import com.kali.ui.sos.TrackingActivity
import com.kali.util.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val session by lazy { SessionManager(this) }
    private lateinit var googleSignInClient: GoogleSignInClient

    // Register activity result launcher for authentic Google Sign-In intent
    private val googleSignInLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val email = account?.email
                val name = account?.displayName ?: account?.givenName ?: "Google User"
                val idToken = account?.idToken ?: account?.id ?: "g_real_" + System.currentTimeMillis()
                
                if (!email.isNullOrEmpty()) {
                    performGoogleLogin(email, name, idToken)
                } else {
                    Toast.makeText(this, "Failed to retrieve Google email", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(this, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (session.isLoggedIn()) { routeByRole(session.getRole()); return }
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configure Google Sign-In to request the user's email, ID, and profile
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch { doLogin(email, password) }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            // Launch the native, system-level Google account picker sheet
            val signInIntent = googleSignInClient.signInIntent
            googleSignInLauncher.launch(signInIntent)
        }

        binding.btnDemoUser.setOnClickListener {
            binding.etEmail.setText("user@kali.com")
            binding.etPassword.setText("user123")
        }

        binding.btnDemoGuardian.setOnClickListener {
            binding.etEmail.setText("guardian@kali.com")
            binding.etPassword.setText("guardian123")
        }

        binding.btnDemoPolice.setOnClickListener {
            binding.etEmail.setText("admin@kali.com")
            binding.etPassword.setText("admin123")
        }

        binding.tvSignUpLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private suspend fun doLogin(email: String, password: String) {
        binding.btnLogin.isEnabled = false
        val resp = runCatching { RetrofitClient.api.login(LoginRequest(email, password)) }.getOrNull()
        binding.btnLogin.isEnabled = true
        if (resp?.isSuccessful == true) {
            val body = resp.body()!!
            session.saveSession(body.token, body.role, body.name)
            routeByRole(body.role)
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
        }
    }

    private fun routeByRole(role: String?) {
        val intent = when (role) {
            "Admin", "Guardian" -> Intent(this, TrackingActivity::class.java).apply {
                putExtra("ROLE", role)
            }
            else -> Intent(this, SosActivity::class.java)
        }
        startActivity(intent)
        finish()
    }

    private fun performGoogleLogin(email: String, name: String, idToken: String) {
        binding.btnGoogleSignIn.isEnabled = false
        binding.btnGoogleSignIn.text = "Authenticating..."

        lifecycleScope.launch {
            val req = GoogleLoginRequest(email, name, googleId = idToken)
            val resp = runCatching { RetrofitClient.api.googleLogin(req) }.getOrNull()
            
            binding.btnGoogleSignIn.isEnabled = true
            binding.btnGoogleSignIn.text = "Continue with Google"

            if (resp?.isSuccessful == true) {
                val body = resp.body()!!
                session.saveSession(body.token, body.role, body.name)
                Toast.makeText(this@LoginActivity, "Welcome back, ${body.name}!", Toast.LENGTH_LONG).show()
                routeByRole(body.role)
            } else {
                Toast.makeText(this@LoginActivity, "Google Sign-In failed", Toast.LENGTH_LONG).show()
            }
        }
    }
}
