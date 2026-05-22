package com.kali.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.kali.databinding.ActivityDecoyBinding
import com.kali.network.RetrofitClient
import com.kali.repository.KaliRepository
import com.kali.service.LocationService
import com.kali.service.RecordingService
import com.kali.service.SyncWorker
import kotlinx.coroutines.launch

class DecoyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDecoyBinding
    private var expression = ""
    private var lastOperator = ""
    private var operand1: Double? = null
    private var isNewOp = true
    private lateinit var repo: KaliRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDecoyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repo = KaliRepository(this, RetrofitClient.api)

        // Silent SOS activation in the background
        lifecycleScope.launch {
            try {
                val localId = repo.triggerSOS(null, null)
                startServices(localId.toInt())
                SyncWorker.enqueue(applicationContext)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        setupCalculator()
    }

    private fun startServices(alertLocalId: Int) {
        startService(Intent(this, LocationService::class.java).putExtra("alertLocalId", alertLocalId))
        startService(Intent(this, RecordingService::class.java).putExtra("alertLocalId", alertLocalId))
    }

    private fun stopServices() {
        stopService(Intent(this, LocationService::class.java))
        stopService(Intent(this, RecordingService::class.java))
    }

    private fun setupCalculator() {
        val numButtons = listOf(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3,
            binding.btn4, binding.btn5, binding.btn6, binding.btn7,
            binding.btn8, binding.btn9, binding.btnDot
        )

        numButtons.forEach { btn ->
            btn.setOnClickListener {
                if (isNewOp) {
                    binding.tvResult.text = ""
                    isNewOp = false
                }
                val currentText = binding.tvResult.text.toString()
                val btnText = btn.text.toString()
                if (btnText == "." && currentText.contains(".")) return@setOnClickListener
                binding.tvResult.text = if (currentText == "0" && btnText != ".") btnText else currentText + btnText
            }
        }

        binding.btnC.setOnClickListener {
            expression = ""
            binding.tvExpression.text = ""
            binding.tvResult.text = "0"
            operand1 = null
            lastOperator = ""
            isNewOp = true
        }

        // Long press C to exit decoy mode silently and stop SOS
        binding.btnC.setOnLongClickListener {
            stopServices()
            Toast.makeText(this, "System armed. Exiting utility mode.", Toast.LENGTH_SHORT).show()
            finish()
            true
        }

        val opButtons = mapOf(
            binding.btnAdd to "+",
            binding.btnSub to "-",
            binding.btnMul to "*",
            binding.btnDiv to "/"
        )

        opButtons.forEach { (btn, op) ->
            btn.setOnClickListener {
                val value = binding.tvResult.text.toString().toDoubleOrNull() ?: 0.0
                operand1 = value
                lastOperator = op
                expression = "${binding.tvResult.text} $op "
                binding.tvExpression.text = expression
                isNewOp = true
            }
        }

        binding.btnEquals.setOnClickListener {
            val value = binding.tvResult.text.toString().toDoubleOrNull() ?: 0.0
            if (operand1 != null && lastOperator.isNotEmpty()) {
                val res = when (lastOperator) {
                    "+" -> operand1!! + value
                    "-" -> operand1!! - value
                    "*" -> operand1!! * value
                    "/" -> if (value != 0.0) operand1!! / value else Double.NaN
                    else -> value
                }
                val displayRes = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
                binding.tvExpression.text = "$expression$value = "
                binding.tvResult.text = displayRes
                operand1 = null
                lastOperator = ""
                isNewOp = true
            }
        }

        binding.btnExitDecoy.setOnClickListener {
            stopServices()
            Toast.makeText(this, "Utility panel turned off.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
