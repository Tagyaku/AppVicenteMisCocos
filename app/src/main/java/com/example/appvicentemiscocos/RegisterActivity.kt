package com.example.appvicentemiscocos

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.appvicentemiscocos.databinding.ActivityRegisterBinding
import kotlinx.coroutines.DelicateCoroutinesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var capturedImageBtn: ImageButton

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://randomuser.me/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private lateinit var userDao: UserDao

    private val randomUserService = retrofit.create(ApiService::class.java)
    private val REQUEST_IMAGE_CAPTURE = 1

    private lateinit var cameraImageView: ImageView
    private var photoFile: File? = null

    private val CAMERA_PERMISSION_CODE = 100
    private val CAMERA_REQUEST_CODE = 101

    private var fotoByteArray: ByteArray? = null

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        capturedImageBtn = binding.CameraImageView
        userDao = AppDatabase.getInstance(this).userDao()
        binding.TermsOfUse.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                showTermsDialog()
            }
        }
        cameraImageView = binding.CameraImageView
        binding.chooseRandomImageButton.setOnClickListener {
            fetchRandomUserAvatars()
        }

        binding.BirthDate.setOnClickListener {
            showDatePickerDialog()
        }

        binding.takePhotoButton.setOnClickListener {
            checkCameraPermissionAndOpen()
        }

        binding.VerifyRegisterbutton.setOnClickListener {
            val username = binding.Username.text.toString()
            val password = binding.Password.text.toString()
            val repeatPassword = binding.RepeatPassword.text.toString()
            val birthDate = binding.BirthDate.text.toString()
            val termsAccepted = binding.TermsOfUse.isChecked

            if (validateInputs(username, password, repeatPassword, birthDate, termsAccepted)) {
                val encryptedPassword = passwordEncrypter(password)

                userDao.findUserByUsername(username).observe(this) { existingUser ->
                    if (existingUser != null) {
                        Toast.makeText(
                            this,
                            "Username already in use. Please choose another one.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        val newUser = User(
                            username = username,
                            password = encryptedPassword,
                            birthDate = birthDate,
                            picture = fotoByteArray
                        )

                        GlobalScope.launch(Dispatchers.IO) {
                            userDao.insertUsers(newUser)
                        }

                        Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                        saveUserDataToDatabase(username, password, birthDate)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                }
            }
        }

        fetchRandomUserAvatars()
        setupAvatarSelection()
    }

    private fun setupAvatarSelection() {
        val imageButtonList = listOf(binding.AvatarimageButton, binding.AvatarimageButton2, binding.AvatarimageButton3)
        imageButtonList.forEach { imageButton ->
            imageButton.setOnClickListener { view ->
                val drawable = (view as ImageButton).drawable
                val bitmap = (drawable as BitmapDrawable).bitmap
                binding.CameraImageView.setImageBitmap(bitmap)
                fotoByteArray = convertBitmapToByteArray(bitmap)
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

    private fun saveUserDataToDatabase(username: String, password: String, birthDate: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val existingUser = userDao.findUserByUsername(username)

            launch(Dispatchers.Main) {
                if (existingUser != null) {
                    showToast("Username already in use. Please choose another one.")
                } else {
                    val newUser = User(
                        username = username,
                        password = password,
                        birthDate = birthDate,
                        picture = readImageFileToByteArray(currentPhotoPath)
                    )

                    userDao.insertUsers(newUser)

                    binding.Username.text.clear()
                    binding.Password.text.clear()
                    binding.RepeatPassword.text.clear()
                    binding.BirthDate.text.clear()

                    showToast("Registration saved successfully")

                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }

    private fun readImageFileToByteArray(filePath: String): ByteArray? {
        return try {
            val file = File(filePath)
            val fileInputStream = FileInputStream(file)
            val byteArray = ByteArray(file.length().toInt())
            fileInputStream.read(byteArray)
            byteArray
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
    }
    private fun takePicture() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
                photoFile = createImageFile()
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageBitmap = data?.extras?.get("data") as? Bitmap
            cameraImageView.setImageBitmap(imageBitmap)
            fotoByteArray = convertBitmapToByteArray(imageBitmap)
        }
    }
    private fun convertBitmapToByteArray(bitmap: Bitmap?): ByteArray? {
        return bitmap?.let {
            ByteArrayOutputStream().apply {
                it.compress(Bitmap.CompressFormat.JPEG, 100, this)
            }.toByteArray()
        }
    }

    private fun validateInputs(
        username: String,
        password: String,
        repeatPassword: String,
        birthDate: String,
        termsAccepted: Boolean
    ): Boolean {
        if (username.length < 4 || username.length > 15) {
            showToast("Username must be between 4 and 15 characters.")
            return false
        }

        val passwordRegex = Regex("^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?:{}|<>]).{6,15}$")
        if (!password.matches(passwordRegex)) {
            showToast("Password must be 6-15 characters with at least one number, one uppercase letter, and one special character (!@#$%^&*(),.?:{}|<>).")
            return false
        }

        if (password != repeatPassword) {
            showToast("Passwords do not match.")
            return false
        }

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val birthDateObject = dateFormat.parse(birthDate)
            val currentDate = Date()

            if (birthDateObject == null || birthDateObject >= currentDate) {
                showToast("Enter a valid birth date that is earlier than the current date.")
                return false
            }
        } catch (e: ParseException) {
            showToast("Enter a valid birth date in the format dd/MM/yyyy.")
            return false
        }

        if (!termsAccepted) {
            showToast("You must accept the terms of use.")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            binding.BirthDate.setText(selectedDate)
        }, year, month, day)

        datePickerDialog.show()
    }

    private fun showTermsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.terms_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this)
            .setTitle("Terms and Conditions")
            .setView(dialogView)
            .setPositiveButton("Accept") { _, _ ->

            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                binding.TermsOfUse.isChecked = false
            }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        val scrollView = dialogView.findViewById<ScrollView>(R.id.ScrollView)
        scrollView.isVerticalScrollBarEnabled = true
    }

    private fun fetchRandomUserAvatars() {
        val call = randomUserService.getRandomUserPictures()

        call.enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val results = response.body()?.results

                    if ((results != null) && results.isNotEmpty()) {
                        displayRandomUserAvatars(results)
                    } else {
                        Toast.makeText(this@RegisterActivity, "Could not get random images", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Request error", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun displayRandomUserAvatars(randomUsers: List<Result>?) {
        val imageButtons = listOf(binding.AvatarimageButton, binding.AvatarimageButton2, binding.AvatarimageButton3)
        randomUsers?.take(3)?.forEachIndexed { index, result ->
            Glide.with(this)
                .load(result.picture.large)
                .into(imageButtons[index])
        }
    }


    private var currentPhotoPath: String = ""

    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
}
