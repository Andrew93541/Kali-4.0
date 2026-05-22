package com.kali.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.kali.R
import com.kali.databinding.ActivityProfileBinding
import com.kali.network.*
import com.kali.util.SessionManager
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var session: SessionManager
    private var userRole: String = "User"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        session = SessionManager(this)
        userRole = session.getRole() ?: "User"

        setupUIForRole()
        loadProfileData()

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnUpdateProfile.setOnClickListener {
            updateProfileDetails()
        }

        if (userRole == "Guardian") {
            binding.btnLinkGuardian.setOnClickListener {
                linkToUser()
            }
            binding.btnUnlinkGuardian.setOnClickListener {
                unlinkFromUser()
            }
        }

        if (userRole == "User") {
            binding.btnAddContact.setOnClickListener {
                addEmergencyContact()
            }
        }
    }

    // ─── Setup UI based on user role ──────────────────────────────────────────

    private fun setupUIForRole() {
        binding.tvRole.text = userRole

        when (userRole) {
            "User" -> {
                binding.cardGuardian.visibility = View.VISIBLE
                binding.tvGuardianTitle.text = "GUARDIAN SECURE LINK"
                binding.llLinkGuardianForm.visibility = View.GONE // Users cannot form links, guardians must do it
                binding.btnUnlinkGuardian.visibility = View.GONE
                
                binding.cardContacts.visibility = View.VISIBLE
            }
            "Guardian" -> {
                binding.cardGuardian.visibility = View.VISIBLE
                binding.tvGuardianTitle.text = "PROTECTED USER CONNECTION"
                binding.llLinkGuardianForm.visibility = View.VISIBLE
                binding.cardContacts.visibility = View.GONE
            }
            else -> { // Police / Admin
                binding.cardGuardian.visibility = View.GONE
                binding.cardContacts.visibility = View.GONE
            }
        }
    }

    // ─── Load profile data from API ────────────────────────────────────────────

    private fun loadProfileData() {
        val token = session.getToken()
        if (token == null) {
            Toast.makeText(this, "Session expired. Please log in again.", Toast.LENGTH_LONG).show()
            logoutUser()
            return
        }

        val authHeader = "Bearer $token"

        lifecycleScope.launch {
            // Load user profile details
            val profileRes = runCatching { RetrofitClient.api.getProfile(authHeader) }.getOrNull()
            if (profileRes?.isSuccessful == true) {
                val profile = profileRes.body()
                if (profile != null) {
                    binding.tvEmail.text = profile.email
                    binding.etName.setText(profile.name)
                    binding.etPhone.setText(profile.phone ?: "")

                    if (userRole == "User") {
                        if (profile.guardian_id != null) {
                            binding.tvGuardianStatus.text = "Status: Linked to Guardian ID #${profile.guardian_id}\n\nNote: Your Guardian has active tracking permissions."
                            binding.tvGuardianStatus.setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.kali_green))
                        } else {
                            binding.tvGuardianStatus.text = "Status: Not Linked to a Guardian\n\nTo establish a secure connection, tell your Guardian to search for your phone number (${profile.phone ?: "unregistered"}) or email address in their dashboard."
                            binding.tvGuardianStatus.setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.kali_pink))
                        }
                    }
                }
            } else {
                Toast.makeText(this@ProfileActivity, "Failed to load profile data", Toast.LENGTH_SHORT).show()
            }

            // Load guardian relation if role is Guardian
            if (userRole == "Guardian") {
                loadGuardianUserRelation(authHeader)
            }

            // Load emergency contacts if role is User
            if (userRole == "User") {
                loadEmergencyContacts(authHeader)
            }
        }
    }

    // ─── Update profile details ───────────────────────────────────────────────

    private fun updateProfileDetails() {
        val token = session.getToken() ?: return
        val authHeader = "Bearer $token"
        val newName = binding.etName.text.toString().trim()
        val newPhone = binding.etPhone.text.toString().trim()

        if (newName.isEmpty()) {
            binding.etName.error = "Name cannot be empty"
            return
        }

        binding.btnUpdateProfile.isEnabled = false
        binding.btnUpdateProfile.text = "UPDATING..."

        lifecycleScope.launch {
            val response = runCatching {
                RetrofitClient.api.updateProfile(authHeader, ProfileUpdateRequest(name = newName, phone = newPhone))
            }.getOrNull()

            binding.btnUpdateProfile.isEnabled = true
            binding.btnUpdateProfile.text = "UPDATE PROFILE DETAILS"

            if (response?.isSuccessful == true) {
                val updated = response.body()
                if (updated != null) {
                    session.saveSession(token, updated.role, updated.name)
                    Toast.makeText(this@ProfileActivity, "Profile details updated successfully", Toast.LENGTH_SHORT).show()
                    loadProfileData()
                }
            } else {
                Toast.makeText(this@ProfileActivity, "Update failed: ${response?.message() ?: "Server error"}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── Guardian management (Guardian Role Only) ───────────────────────────

    private suspend fun loadGuardianUserRelation(authHeader: String) {
        val response = runCatching { RetrofitClient.api.getMyUser(authHeader) }.getOrNull()
        if (response?.isSuccessful == true) {
            val linked = response.body()?.linkedUser
            if (linked != null) {
                binding.tvGuardianStatus.text = "Status: SECURE LINK ACTIVE\n\nConnected to User:\nName: ${linked.name}\nPhone: ${linked.phone ?: "N/A"}\nEmail: ${linked.email}"
                binding.tvGuardianStatus.setTextColor(ContextCompat.getColor(this, R.color.kali_green))
                
                binding.llLinkGuardianForm.visibility = View.GONE
                binding.btnUnlinkGuardian.visibility = View.VISIBLE
            } else {
                binding.tvGuardianStatus.text = "Status: NO ACTIVE USER LINK\n\nUse the form below to search and connect to a User's terminal. This grants you real-time SOS telemetry tracking permissions."
                binding.tvGuardianStatus.setTextColor(ContextCompat.getColor(this, R.color.kali_pink))
                
                binding.llLinkGuardianForm.visibility = View.VISIBLE
                binding.btnUnlinkGuardian.visibility = View.GONE
            }
        }
    }

    private fun linkToUser() {
        val token = session.getToken() ?: return
        val authHeader = "Bearer $token"
        val email = binding.etGuardianEmail.text.toString().trim()
        val phone = binding.etGuardianPhone.text.toString().trim()

        if (email.isEmpty() && phone.isEmpty()) {
            Toast.makeText(this, "Enter user's email or phone number", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnLinkGuardian.isEnabled = false
        binding.btnLinkGuardian.text = "ESTABLISHING SECURE LINK..."

        lifecycleScope.launch {
            val req = GuardianLinkRequest(
                userEmail = if (email.isNotEmpty()) email else null,
                userPhone = if (phone.isNotEmpty()) phone else null
            )
            val res = runCatching { RetrofitClient.api.linkGuardian(authHeader, req) }.getOrNull()
            binding.btnLinkGuardian.isEnabled = true
            binding.btnLinkGuardian.text = "ESTABLISH GUARDIAN SECURE LINK"

            if (res?.isSuccessful == true && res.body()?.success == true) {
                Toast.makeText(this@ProfileActivity, "Secure connection established!", Toast.LENGTH_SHORT).show()
                binding.etGuardianEmail.setText("")
                binding.etGuardianPhone.setText("")
                loadProfileData()
            } else {
                Toast.makeText(this@ProfileActivity, "Connection failed: User not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun unlinkFromUser() {
        val token = session.getToken() ?: return
        val authHeader = "Bearer $token"

        binding.btnUnlinkGuardian.isEnabled = false
        binding.btnUnlinkGuardian.text = "SEVERING LINK..."

        lifecycleScope.launch {
            val res = runCatching { RetrofitClient.api.unlinkGuardian(authHeader) }.getOrNull()
            binding.btnUnlinkGuardian.isEnabled = true
            binding.btnUnlinkGuardian.text = "SEVER GUARDIAN LINK (UNLINK)"

            if (res?.isSuccessful == true) {
                Toast.makeText(this@ProfileActivity, "Link severed successfully", Toast.LENGTH_SHORT).show()
                loadProfileData()
            } else {
                Toast.makeText(this@ProfileActivity, "Unlinking failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ─── Emergency Contacts (User Role Only) ──────────────────────────────────

    private suspend fun loadEmergencyContacts(authHeader: String) {
        val response = runCatching { RetrofitClient.api.getContacts(authHeader) }.getOrNull()
        if (response?.isSuccessful == true) {
            val contacts = response.body() ?: emptyList()
            populateContactsUI(contacts)
        }
    }

    private fun populateContactsUI(contacts: List<EmergencyContact>) {
        binding.llContactsContainer.removeAllViews()
        if (contacts.isEmpty()) {
            val emptyView = TextView(this).apply {
                text = "No emergency contacts registered. Please add contacts below to ensure rapid notifications."
                setTextColor(0x59557D.or(-0x1000000))
                textSize = 12f
                setPadding(0, 10, 0, 10)
            }
            binding.llContactsContainer.addView(emptyView)
            return
        }

        contacts.forEach { contact ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.bottomMargin = (12 * resources.displayMetrics.density).toInt()
                layoutParams = params
            }

            val contactInfo = TextView(this).apply {
                val p = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                layoutParams = p
                text = "${contact.name} (${contact.relation ?: "Friend"})" + 
                       if (contact.is_primary == 1) " [PRIMARY]" else ""
                append("\n${contact.phone}")
                setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.kali_text))
                textSize = 13f
            }

            val deleteBtn = MaterialButton(this).apply {
                val p = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    (36 * resources.displayMetrics.density).toInt()
                )
                layoutParams = p
                text = "Delete"
                textSize = 10f
                setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.white))
                backgroundTintList = ContextCompat.getColorStateList(this@ProfileActivity, R.color.kali_red)
                cornerRadius = (6 * resources.displayMetrics.density).toInt()
                insetTop = 0
                insetBottom = 0
                setOnClickListener {
                    deleteEmergencyContact(contact.id)
                }
            }

            row.addView(contactInfo)
            row.addView(deleteBtn)
            binding.llContactsContainer.addView(row)
        }
    }

    private fun addEmergencyContact() {
        val token = session.getToken() ?: return
        val authHeader = "Bearer $token"
        val name = binding.etContactName.text.toString().trim()
        val phone = binding.etContactPhone.text.toString().trim()
        val relation = binding.etContactRelation.text.toString().trim()
        val isPrimary = binding.cbIsPrimary.isChecked

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Name and Phone number are required", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnAddContact.isEnabled = false
        binding.btnAddContact.text = "ADDING..."

        lifecycleScope.launch {
            val req = EmergencyContactRequest(
                name = name,
                phone = phone,
                relation = if (relation.isNotEmpty()) relation else null,
                is_primary = isPrimary
            )
            val res = runCatching { RetrofitClient.api.addContact(authHeader, req) }.getOrNull()
            binding.btnAddContact.isEnabled = true
            binding.btnAddContact.text = "ADD EMERGENCY CONTACT"

            if (res?.isSuccessful == true) {
                Toast.makeText(this@ProfileActivity, "Emergency contact added!", Toast.LENGTH_SHORT).show()
                binding.etContactName.setText("")
                binding.etContactPhone.setText("")
                binding.etContactRelation.setText("")
                binding.cbIsPrimary.isChecked = false
                loadProfileData()
            } else {
                Toast.makeText(this@ProfileActivity, "Failed to add contact", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun deleteEmergencyContact(contactId: Int) {
        val token = session.getToken() ?: return
        val authHeader = "Bearer $token"

        lifecycleScope.launch {
            val res = runCatching { RetrofitClient.api.deleteContact(authHeader, contactId) }.getOrNull()
            if (res?.isSuccessful == true) {
                Toast.makeText(this@ProfileActivity, "Contact removed", Toast.LENGTH_SHORT).show()
                loadProfileData()
            } else {
                Toast.makeText(this@ProfileActivity, "Failed to remove contact", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun logoutUser() {
        session.clear()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
