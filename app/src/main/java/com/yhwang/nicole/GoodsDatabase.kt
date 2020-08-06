package com.yhwang.nicole

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.yhwang.nicole.model.Item
import com.yhwang.nicole.model.ItemDao

@Database(entities = [Item::class], version = 1)
abstract class GoodsDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao

    companion object {
        private var instance: GoodsDatabase? = null
        fun getInstance(context: Context) : GoodsDatabase? {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
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