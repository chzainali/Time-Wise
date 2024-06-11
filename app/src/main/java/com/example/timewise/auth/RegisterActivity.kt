package com.example.timewise.auth

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.timewise.database.DatabaseHelper
import com.example.timewise.databinding.ActivityRegisterBinding
import com.example.timewise.model.UsersModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    var name: String = ""
    var email: String = ""
    var phone: String = ""
    var password: String = ""
    lateinit var databaseHelper: DatabaseHelper

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)
        binding.rlTop.tvLabel.text = "Register"

        binding.rlTop.ivBack.visibility = View.VISIBLE
        binding.rlTop.ivBack.setOnClickListener { finish() }
        binding.llBottom.setOnClickListener {
            finish()
        }

        binding.btnRegister.setOnClickListener {
            if (isValidated()) {
                val users = UsersModel(0, name, email, phone, password)
                databaseHelper.register(users)
                showMessage("Successfully Registered")
                finish()
            }
        }

    }

    private fun isValidated(): Boolean {
        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        phone = binding.phoneEt.text.toString().trim()
        password = binding.passET.text.toString().trim()
        if (name.isEmpty()) {
            showMessage("Please enter name")
            return false
        }
        if (email.isEmpty()) {
            showMessage("Please enter email")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Please enter email in correct format")
            return false
        }
        if (phone.isEmpty() ) {
            showMessage("Please enter phone number")
            return false
        }
        if (password.isEmpty()) {
            showMessage("Please enter password")
            return false
        }
        return true
    }

    // Show a toast message
    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}