package com.example.appvicentemiscocos

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["Username"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    @ColumnInfo(name = "Username") val username: String?,
    @ColumnInfo(name = "Password") val password: String?,
    @ColumnInfo(name = "BirthDate") val birthDate: String?,
    @ColumnInfo(name = "picture") val picture: ByteArray?
)
