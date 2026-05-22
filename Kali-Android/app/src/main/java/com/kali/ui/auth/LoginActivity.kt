package com.kali.ui.auth

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kali.databinding.ActivityLoginBinding
import com.kali.databinding.DialogCustomGoogleInputBinding
import com.kali.databinding.DialogGooglePickerBinding
import com.kali.databinding.ItemGoogleAccountBinding
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
                    Toast.makeText(this, "Failed to retrieve Google email. Falling back to sandbox...", Toast.LENGTH_SHORT).show()
                    showMockGooglePicker()
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In failed: ${e.statusCode}. Opening sandbox picker...", Toast.LENGTH_SHORT).show()
                showMockGooglePicker()
            }
        } else {
            Toast.makeText(this, "Google Sign-In cancelled. Opening sandbox picker...", Toast.LENGTH_SHORT).show()
            showMockGooglePicker()
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

        // Initialize server settings
        RetrofitClient.initialize(this)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email == "decoy@kali.com" || password == "9999") {
                startActivity(Intent(this, DecoyActivity::class.java))
                finish()
                return@setOnClickListener
            }
            lifecycleScope.launch { doLogin(email, password) }
        }

        binding.btnGoogleSignIn.setOnClickListener {
            try {
                // Launch the native, system-level Google account picker sheet
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            } catch (e: Exception) {
                showMockGooglePicker()
            }
        }

        binding.btnServerSettings.setOnClickListener {
            showServerSettingsDialog()
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
        val result = runCatching { RetrofitClient.api.login(LoginRequest(email, password)) }
        binding.btnLogin.isEnabled = true
        
        result.onSuccess { resp ->
            if (resp.isSuccessful) {
                val body = resp.body()!!
                session.saveSession(body.token, body.role, body.name)
                routeByRole(body.role)
            } else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }.onFailure { t ->
            t.printStackTrace()
            Toast.makeText(this, "Connection failed: ${t.localizedMessage}\nCheck Server IP in settings!", Toast.LENGTH_LONG).show()
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
            val result = runCatching { RetrofitClient.api.googleLogin(req) }
            
            binding.btnGoogleSignIn.isEnabled = true
            binding.btnGoogleSignIn.text = "Continue with Google"

            result.onSuccess { resp ->
                if (resp.isSuccessful) {
                    val body = resp.body()!!
                    session.saveSession(body.token, body.role, body.name)
                    Toast.makeText(this@LoginActivity, "Welcome back, ${body.name}!", Toast.LENGTH_LONG).show()
                    routeByRole(body.role)
                } else {
                    Toast.makeText(this@LoginActivity, "Google Sign-In failed: Server rejected request", Toast.LENGTH_LONG).show()
                }
            }.onFailure { t ->
                t.printStackTrace()
                Toast.makeText(this@LoginActivity, "Google Sign-In connection failed: ${t.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun showMockGooglePicker() {
        val dialog = BottomSheetDialog(this)
        val dialogBinding = DialogGooglePickerBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        val accounts = listOf(
            Pair("Priyansh Patel", "priyansh.patel@gmail.com"),
            Pair("KALI Security", "security@kali.com"),
            Pair("Test Guardian", "guardian.test@gmail.com")
        )

        val container = dialogBinding.llAccountsContainer
        container.removeAllViews()

        // Populate accounts
        accounts.forEach { account ->
            val itemBinding = ItemGoogleAccountBinding.inflate(layoutInflater, container, false)
            itemBinding.tvName.text = account.first
            itemBinding.tvEmail.text = account.second
            
            // Set beautiful avatar
            val firstLetter = account.first.take(1).uppercase()
            itemBinding.tvAvatar.text = firstLetter
            val gd = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                val colors = intArrayOf(
                    Color.parseColor("#E91E8C"),
                    Color.parseColor("#00F2FE"),
                    Color.parseColor("#4CAF50")
                )
                val colorIndex = Math.abs(account.second.hashCode()) % colors.size
                setColor(colors[colorIndex])
            }
            itemBinding.tvAvatar.background = gd

            itemBinding.root.setOnClickListener {
                dialog.dismiss()
                performGoogleLogin(account.second, account.first, "mock_google_" + System.currentTimeMillis())
            }
            container.addView(itemBinding.root)
        }

        // Add a button at the bottom for custom account input
        val customItemBinding = ItemGoogleAccountBinding.inflate(layoutInflater, container, false)
        customItemBinding.tvName.text = "Add another account"
        customItemBinding.tvEmail.text = "Use a custom mock Google account"
        customItemBinding.tvAvatar.text = "+"
        val customGd = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.parseColor("#33FFFFFF"))
        }
        customItemBinding.tvAvatar.background = customGd
        customItemBinding.root.setOnClickListener {
            dialog.dismiss()
            showCustomGoogleInput()
        }
        container.addView(customItemBinding.root)

        dialog.show()
    }

    private fun showCustomGoogleInput() {
        val inputBinding = DialogCustomGoogleInputBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(this)
            .setView(inputBinding.root)
            .setPositiveButton("Sign In") { dialog, _ ->
                val name = inputBinding.etCustomName.text.toString().trim()
                val email = inputBinding.etCustomEmail.text.toString().trim()
                if (name.isNotEmpty() && email.isNotEmpty()) {
                    dialog.dismiss()
                    performGoogleLogin(email, name, "mock_google_" + System.currentTimeMillis())
                } else {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        
        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#E91E8C"))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY)
        }
        alertDialog.show()
    }

    private fun showServerSettingsDialog() {
        val prefs = getSharedPreferences("server_settings", MODE_PRIVATE)
        val currentIp = prefs.getString("server_ip", "192.168.0.151") ?: "192.168.0.151"
        val currentPort = prefs.getString("server_port", "3000") ?: "3000"

        // Build a beautiful custom layout programmatically
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 48, 64, 48)
            setBackgroundColor(Color.parseColor("#0F0F1A"))
        }

        val titleTv = TextView(this).apply {
            text = "KALI Server Configuration"
            setTextColor(Color.WHITE)
            textSize = 20f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            setPadding(0, 0, 0, 32)
        }
        container.addView(titleTv)

        val ipInput = com.google.android.material.textfield.TextInputLayout(this).apply {
            hint = "Server Host IP Address"
            boxBackgroundMode = com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxBackgroundColor(Color.parseColor("#16213E"))
            boxStrokeColor = Color.parseColor("#E91E8C")
            hintTextColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E8C"))
            defaultHintTextColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#AAAAAA"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
        }
        val ipEditText = com.google.android.material.textfield.TextInputEditText(this).apply {
            setText(currentIp)
            setTextColor(Color.WHITE)
            inputType = android.text.InputType.TYPE_CLASS_TEXT
        }
        ipInput.addView(ipEditText)
        container.addView(ipInput)

        val portInput = com.google.android.material.textfield.TextInputLayout(this).apply {
            hint = "Server Port"
            boxBackgroundMode = com.google.android.material.textfield.TextInputLayout.BOX_BACKGROUND_OUTLINE
            setBoxBackgroundColor(Color.parseColor("#16213E"))
            boxStrokeColor = Color.parseColor("#E91E8C")
            hintTextColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#E91E8C"))
            defaultHintTextColor = android.content.res.ColorStateList.valueOf(Color.parseColor("#AAAAAA"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, 24) }
        }
        val portEditText = com.google.android.material.textfield.TextInputEditText(this).apply {
            setText(currentPort)
            setTextColor(Color.WHITE)
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        portInput.addView(portEditText)
        container.addView(portInput)

        val builder = AlertDialog.Builder(this)
            .setView(container)
            .setPositiveButton("Save Settings") { dialog, _ ->
                val newIp = ipEditText.text.toString().trim()
                val newPort = portEditText.text.toString().trim()
                if (newIp.isNotEmpty() && newPort.isNotEmpty()) {
                    prefs.edit().apply {
                        putString("server_ip", newIp)
                        putString("server_port", newPort)
                        apply()
                    }
                    RetrofitClient.updateBaseUrl(newIp, newPort)
                    Toast.makeText(this, "Base URL updated: http://$newIp:$newPort/api/", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Reset to Default") { dialog, _ ->
                prefs.edit().apply {
                    putString("server_ip", "192.168.0.151")
                    putString("server_port", "3000")
                    apply()
                }
                RetrofitClient.updateBaseUrl("192.168.0.151", "3000")
                Toast.makeText(this, "Reset to default: 192.168.0.151:3000", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }

        val alertDialog = builder.create()
        alertDialog.setOnShowListener {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.parseColor("#E91E8C"))
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.GRAY)
        }
        alertDialog.show()
    }
}
