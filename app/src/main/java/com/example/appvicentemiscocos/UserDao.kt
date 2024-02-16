package com.example.appvicentemiscocos



import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {

    @Query("SELECT * FROM user")
    fun getAllUsers(): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE uid IN (:userIds)")
    fun loadUsersByIds(userIds: IntArray): LiveData<List<User>>

    @Query("SELECT * FROM user WHERE Username LIKE :username")
    fun findUserByUsername(username: String): LiveData<User>

    @Insert
    fun insertUsers(vararg users: User)

    @Delete
    fun deleteUser(user: User)

    @Query("DELETE FROM user")
    fun deleteAllUsers()

    @Query("UPDATE user SET Password = :newPassword WHERE Username = :username")
    fun updatePasswordByUsername(username: String, newPassword: String)


    
}
