package com.xcaret.loyaltyreps.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.xcaret.loyaltyreps.model.XUser

@Database(entities = [XUser::class], version = 4, exportSchema = false)
abstract class XCaretLoyaltyDatabase : RoomDatabase() {

    abstract val loyaltyDatabaseDAO: XUserDatabaseDAO

    companion object {
        @Volatile
        private var INSTANCE: XCaretLoyaltyDatabase? = null

        fun getInstance(context: Context) : XCaretLoyaltyDatabase {
            synchronized(this) {
                var instance = INSTANCE

                //create database
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        XCaretLoyaltyDatabase::class.java,
                        "xcaret_loyalty_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }

                return instance
            }
        }
    }

}