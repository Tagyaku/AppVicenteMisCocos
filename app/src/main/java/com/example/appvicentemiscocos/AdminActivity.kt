
package com.example.appvicentemiscocos


import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.coroutines.resume

class AdminActivity : AppCompatActivity() {

    private lateinit var userDao: UserDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        userDao = AppDatabase.getInstance(this).userDao()
        val userListButton = findViewById<Button>(R.id.userListButton)
        val changePasswordButton = findViewById<Button>(R.id.changePasswordButton)
        val deleteAllUsersButton = findViewById<Button>(R.id.deleteAllUsersButton)
        val backButton = findViewById<Button>(R.id.backButton)

        userListButton.setOnClickListener {
            val inflater = layoutInflater
            val view = inflater.inflate(R.layout.user_list, null)

            val dialog = AlertDialog.Builder(this)
                .setView(view)
                .create()

            val recyclerView = view.findViewById<RecyclerView>(R.id.usersRecyclerView)
            recyclerView.layoutManager = LinearLayoutManager(this)

            userDao.getAllUsers().observe(this, Observer { users ->
                recyclerView.adapter = UserAdapter(users)
            })

            view.findViewById<Button>(R.id.closeUserListButton).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }


        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }


        deleteAllUsersButton.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun showDeleteConfirmationDialog() {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.confirm_delete, null)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        view.findViewById<Button>(R.id.confirmDeleteButton).setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                userDao.deleteAllUsers()

                withContext(Dispatchers.Main) {
                    showToast("Todos los usuarios han sido eliminados.")
                }

                dialog.dismiss()
            }
        }

        view.findViewById<Button>(R.id.cancelDeleteButton).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showChangePasswordDialog() {
        val inflater = layoutInflater
        val view = inflater.inflate(R.layout.change_password, null)

        val usernameEditText = view.findViewById<EditText>(R.id.usernameEditText)
        val currentPasswordEditText = view.findViewById<EditText>(R.id.currentPasswordEditText)
        val newPasswordEditText = view.findViewById<EditText>(R.id.newPasswordEditText)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        view.findViewById<Button>(R.id.confirmChangePasswordButton).setOnClickListener {
            val username = usernameEditText.text.toString()
            val currentPassword = currentPasswordEditText.text.toString()
            val newPassword = newPasswordEditText.text.toString()

            if (username.isNotBlank() && currentPassword.isNotBlank() && newPassword.isNotBlank()) {
                changeUserPassword(username, currentPassword, newPassword)
            } else {
                showToast("Por favor, rellene todos los campos.")
            }

            dialog.dismiss()
        }

        dialog.show()
    }
    private fun changeUserPassword(username: String, currentPassword: String, newPassword: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = getUserByUsername(username)

                if (user != null && user.password == passwordEncrypter(currentPassword)) {
                    userDao.updatePasswordByUsername(username, passwordEncrypter(newPassword))
                    withContext(Dispatchers.Main) {
                        showToast("Contraseña actualizada correctamente.")
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showToast("Nombre de usuario o contraseña incorrecta.")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error: ${e.message}")
                }
            }
        }
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
    private suspend fun getUserByUsername(username: String): User? = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { continuation ->
            userDao.findUserByUsername(username).observeOnce(this@AdminActivity) { user ->
                continuation.resume(user)
            }
        }
    }



    fun <T> LiveData<T>.observeOnce(lifecycleOwner: LifecycleOwner, observer: (T?) -> Unit) {
        val wrappedObserver = object : Observer<T> {

            override fun onChanged(value: T) {
                observer(value)
                removeObserver(this)            }
        }
        observe(lifecycleOwner, wrappedObserver)
    }




    }





