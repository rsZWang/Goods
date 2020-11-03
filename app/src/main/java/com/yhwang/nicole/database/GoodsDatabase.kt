package com.yhwang.nicole.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yhwang.nicole.model.Object2D
import com.yhwang.nicole.model.Object2DDao

@Database(entities = [Object2D::class], version = 1)
abstract class GoodsDatabase : RoomDatabase() {
    abstract fun object2DDao(): Object2DDao

    companion object {
        private var instance: GoodsDatabase? = null
        fun getInstance(context: Context) : GoodsDatabase? {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also {
                    instance = it
                }
            }
        }

        private fun buildDatabase(context: Context): GoodsDatabase {
            return Room.databaseBuilder(
                context,
                GoodsDatabase::class.java,
                "goods-db"
            ).build()
        }
    }
}