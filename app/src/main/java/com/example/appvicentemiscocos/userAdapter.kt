package com.example.appvicentemiscocos

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class UserAdapter(private val userList: List<User>) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val usernameTextView: TextView = view.findViewById(R.id.usernameTextView)
        val birthDateTextView: TextView = view.findViewById(R.id.birthDateTextView)
        val userImageView: ImageView = view.findViewById(R.id.userImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_adapter, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.usernameTextView.text = user.username
        holder.birthDateTextView.text = user.birthDate

        // Convertir ByteArray a Bitmap y asignarlo al ImageView
        user.picture?.let { byteArray ->
            val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
            holder.userImageView.setImageBitmap(bitmap)
        }
    }

    override fun getItemCount() = userList.size
}