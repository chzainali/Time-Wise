package com.example.timewise.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.timewise.R
import com.example.timewise.dashboard.DashboardActivity
import com.example.timewise.database.DatabaseHelper
import com.example.timewise.databinding.ActivityLoginBinding
import com.example.timewise.model.HelperClass
import com.example.timewise.model.UsersModel

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    var email: String = ""
    var password: String = ""
    lateinit var databaseHelper: DatabaseHelper
    var checkDetails = false
    var list: List<UsersModel> = ArrayList()
    private val PERMISSION_REQUEST_CODE = 100

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!checkPermissions()){
            requestPermissions()
        }

        databaseHelper = DatabaseHelper(this)
        binding.rlTop.tvLabel.text = "Login"
        binding.llRegister.setOnClickListener {
            startActivity(
                Intent(
                    this@LoginActivity,
                    RegisterActivity::class.java
                )
            )
        }

        binding.btnLogin.setOnClickListener {
            // Validate user input
            if (isValidated()) {
                list = databaseHelper.allUsers
                for (users in list) {
                    if (email == users.email && password == users.password) {
                        checkDetails = true
                        showMessage("Successfully Login")
                        HelperClass.users = users
                        startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        finish()
                        break
                    }
                }
                if (!checkDetails) {
                    showMessage("Wrong Credentials...\nPlease check email or password")
                }
            }
        }
    }

    // Validate user input for email and password
    private fun isValidated(): Boolean {
        email = binding.emailEt.text.toString().trim()
        password = binding.passET.text.toString().trim()
        if (email.isEmpty()) {
            showMessage("Please enter email")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showMessage("Please enter email in correct format")
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

    @RequiresApi(Build.VERSION_CODES.R)
    private fun checkPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val externalStoragePermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val writeStoragePermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val manageStoragePermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.MANAGE_EXTERNAL_STORAGE
        )
        val notificationPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.POST_NOTIFICATIONS
        )
        val readCalenderPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_CALENDAR
        )
        val writeCalenderPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_CALENDAR
        )
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                externalStoragePermission == PackageManager.PERMISSION_GRANTED &&
                writeStoragePermission == PackageManager.PERMISSION_GRANTED &&
                manageStoragePermission == PackageManager.PERMISSION_GRANTED &&
                notificationPermission == PackageManager.PERMISSION_GRANTED &&
                readCalenderPermission == PackageManager.PERMISSION_GRANTED &&
                writeCalenderPermission == PackageManager.PERMISSION_GRANTED
    }

    // Function to request permissions
    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.MANAGE_EXTERNAL_STORAGE,
                android.Manifest.permission.POST_NOTIFICATIONS,
                android.Manifest.permission.READ_CALENDAR,
                android.Manifest.permission.WRITE_CALENDAR
            ),
            PERMISSION_REQUEST_CODE
        )
    }

}