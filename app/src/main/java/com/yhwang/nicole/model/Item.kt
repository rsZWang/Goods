package com.yhwang.nicole.model

import androidx.lifecycle.LiveData
import androidx.room.*

@Entity(tableName = "item")
@TypeConverters(com.yhwang.nicole.database.TypeConverters::class)
class Item(
    @ColumnInfo(name = "item_file_name") val itemFileName: String,
    @ColumnInfo(name = "item_background_file_name") val backgroundFileName: String
) {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0
}

@Dao
interface ItemDao {
    @Query("SELECT * FROM item ")
    fun getAllItem() : List<Item>

    @Insert
    fun insertItem(item: Item)

    @Delete
    fun deleteItem(item: Item)
}