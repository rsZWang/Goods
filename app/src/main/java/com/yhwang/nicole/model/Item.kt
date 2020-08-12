package com.yhwang.nicole.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "item")
@Parcelize
@TypeConverters(com.yhwang.nicole.database.TypeConverters::class)
class Item(
    @ColumnInfo(name = "item_file_name") val itemFileName: String,
    @ColumnInfo(name = "x") val x: Float,
    @ColumnInfo(name = "y") val y: Float,
    @ColumnInfo(name = "item_background_file_name") val backgroundFileName: String,
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
) : Parcelable

@Dao
interface ItemDao {
    @Query("SELECT * FROM item ")
    fun getAllItem() : List<Item>

    @Insert
    fun insertItem(item: Item)

    @Delete
    fun deleteItem(item: Item)
}