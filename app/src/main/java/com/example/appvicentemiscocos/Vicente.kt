package com.example.appvicentemiscocos

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class Vicente : AppCompatActivity() {
    private lateinit var mensajesCocos: List<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vicente)

        val username = intent.getStringExtra("USERNAME")
        val userAvatarByteArray = intent.getByteArrayExtra("AVATAR")


          mensajesCocos = listOf(
            "No me toques los cocos $username",
            "Mensaje 2",
            "Mensaje 3",
        )


        setupCocoButtons()
    }

    private fun setupCocoButtons() {
        val botonCoco1 = findViewById<ImageButton>(R.id.imageButton4)
        val botonCoco2 = findViewById<ImageButton>(R.id.imageButton5)

        val animacionCrecer = ScaleAnimation(
            1.0f, 1.2f,
            1.0f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            fillAfter = false
        }

        val animacionEncoger = ScaleAnimation(
            1.2f, 1.0f,
            1.2f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 200
            fillAfter = false
            startOffset = 200
        }
        val text = findViewById<TextView>(R.id.textView3)
        val listener = { boton: ImageButton ->
            val mensaje = mensajesCocos[Random.nextInt(mensajesCocos.size)]
            text.text = mensaje
            //Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()

            boton.startAnimation(animacionCrecer.apply {
                setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        boton.startAnimation(animacionEncoger)
                    }

                    override fun onAnimationRepeat(animation: Animation?) {}
                })
            })
        }

        botonCoco1.setOnClickListener { listener(it as ImageButton) }
        botonCoco2.setOnClickListener { listener(it as ImageButton) }
    }
}
