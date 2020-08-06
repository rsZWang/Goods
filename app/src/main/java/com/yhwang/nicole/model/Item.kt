package com.yhwang.nicole.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "item")
class Item(
    @ColumnInfo(name = "item_file_name") val itemFileName: String,
    @ColumnInfo(name = "x") val x: Float,
    @ColumnInfo(name = "y") val y: Float,
    @ColumnInfo(name = "item_background_file_name") val backgroundFileName: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM item ")
    fun getAllItem() : LiveData<List<Item>>

    @Insert
    fun insertItem(item: Item)
}