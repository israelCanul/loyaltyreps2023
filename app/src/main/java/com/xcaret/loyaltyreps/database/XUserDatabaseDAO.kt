package com.xcaret.loyaltyreps.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.xcaret.loyaltyreps.model.XUser

@Dao
interface XUserDatabaseDAO {

    @Insert
    fun insert(xUser: XUser)

    @Update
    fun update(xUser: XUser)

    @Query("SELECT * FROM xuser_table WHERE id = :key")
    fun get(key: Long) : XUser?

    @Query("SELECT * FROM xuser_table ORDER BY id DESC LIMIT 1")
    fun getCurrentUser() : XUser?

    @Query("DELETE FROM xuser_table")
    fun clear()
}