package com.example.appvicentemiscocos

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class SuccesfulRegister : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_succesful_register)

        val username = intent.getStringExtra("USERNAME")
        val userAvatarByteArray = intent.getByteArrayExtra("USER_AVATAR")
        val backButton = findViewById<Button>(R.id.BackButton)
        val userTextView = findViewById<TextView>(R.id.UserTextView)
        userTextView.text = "Welcome, $username!"

        val userAvatarImageView = findViewById<ImageView>(R.id.UserAvatar)
        userAvatarByteArray?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            userAvatarImageView.setImageBitmap(bitmap)
        }

        backButton.setOnClickListener {
            finish()
        }

        val VicenteButton = findViewById<Button>(R.id.VicenteButton)
        VicenteButton.setOnClickListener {
            val intent = Intent(this, Vicente::class.java)
            intent.putExtra("USERNAME", username)
            intent.putExtra("AVATAR", userAvatarByteArray)
            startActivity(intent)
        }
        val mostrarHorarioButton = findViewById<Button>(R.id.MostrarHorarioButton)
        mostrarHorarioButton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.horario, null)
            val imageView = dialogLayout.findViewById<ImageView>(R.id.HorarioImagen)

            imageView.setImageResource(R.drawable.imagen_horario)

            builder.setView(dialogLayout)
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }

        val notificacionesProfesoresButton =
            findViewById<Button>(R.id.NotificacionesProfesoresButton)
        notificacionesProfesoresButton.setOnClickListener {
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.notificaciones, null)
            val recyclerView =
                dialogLayout.findViewById<RecyclerView>(R.id.notificationsRecyclerView)


            val notificationsList = getNotificationsList()


            val adapter = NotificationsAdapter(notificationsList)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogLayout)
            builder.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            builder.show()
        }
        }

    private fun getNotificationsList(): List<NotificationItem> {
        return listOf(
            NotificationItem("2º DAM - Programación de Servicios y Procesos", "restaurante", "hace 39 minutos"),
        NotificationItem("2º DAM - Programación de Servicios y Procesos", "Diego DG 2023-2024 contenido nuevo", "hace 1 hora 30 minutos"),
        NotificationItem("2º DAM - Programación Multimedia y Dispositivos Móviles", "Orla", "hace 1 día 22 horas"),
        NotificationItem("2º DAM - Programación de Servicios y Procesos", "Re: Publicadas notas de RA3", "hace 1 día 23 horas"),
        NotificationItem("Usted ha realizado su entrega en la tarea Ejercicio BBDD 4 (CRUD-Sentencias Preparadas) - Gestión Alumnos", "", "hace 2 días 1 hora"),
        NotificationItem("Usted ha realizado su entrega en la tarea Tarea 6.2 - Ejercicio menú desplegable con los Look And Feel", "", "hace 2 días 1 hora"),


        )
    }
}
