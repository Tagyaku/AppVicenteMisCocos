package com.example.appvicentemiscocos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

import com.example.appvicentemiscocos.databinding.ActivityMainBinding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        userDao = AppDatabase.getInstance(this).userDao()

        val hiddenView =binding.HiddenButton
        hiddenView.setOnLongClickListener {
            val intent = Intent(this, AdminActivity::class.java)
            startActivity(intent)
            true
        }
        binding.HiddenButton.isClickable = true
        binding.HiddenButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)

        binding.RegisterButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        binding.SignInButton.setBackgroundColor(android.graphics.Color.TRANSPARENT)


        binding.RegisterButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.SignInButton.setOnClickListener {
            binding.apply {
                textView.visibility = View.VISIBLE
                textView2.visibility = View.VISIBLE
                UsernameEditText.visibility = View.VISIBLE
                PasswordEditText.visibility = View.VISIBLE
                buttonNext.visibility = View.VISIBLE
            }
        }

        binding.buttonNext.setOnClickListener {
            val username = binding.UsernameEditText.text.toString()
            val password = binding.PasswordEditText.text.toString()

            GlobalScope.launch(Dispatchers.IO) {
                val userLiveData = userDao.findUserByUsername(username)

                launch(Dispatchers.Main) {
                    userLiveData.observe(this@MainActivity) { user ->
                        if (user != null) {
                            val encryptedPassword = passwordEncrypter(password)

                            if (encryptedPassword == user.password) {
                                val intent = Intent(this@MainActivity, SuccesfulRegister::class.java).apply {
                                    putExtra("USERNAME", user.username)
                                    putExtra("USER_AVATAR", user.picture)

                                }
                                startActivity(intent)
                            } else {
                                showToast("Incorrect password. Try again.")
                            }
                        } else {
                            showToast("User not found. Please register.")
                        }
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun passwordEncrypter(password: String): String {
        try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val passwordBytes = password.toByteArray()
            val hashBytes = messageDigest.digest(passwordBytes)

            val hexStringBuilder = StringBuilder()
            for (byte in hashBytes) {
                hexStringBuilder.append(String.format("%02x", byte))
            }

            return hexStringBuilder.toString()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            return "encryption error"
        }
    }
}
