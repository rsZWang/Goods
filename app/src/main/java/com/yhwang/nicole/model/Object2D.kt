package com.yhwang.nicole.model

import android.os.Parcelable
import androidx.room.*
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "object2D")
@Parcelize
@TypeConverters(com.yhwang.nicole.database.TypeConverters::class)
class Object2D(
    @ColumnInfo(name = "file_name") val objectFileName: String,
    @ColumnInfo(name = "x") val x: Float,
    @ColumnInfo(name = "y") val y: Float,
    @ColumnInfo(name = "background_file_name") val backgroundFileName: String,
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0
) : Parcelable

@Dao
interface Object2DDao {
    @Query("SELECT * FROM object2D")
    fun getAllObject() : List<Object2D>

    @Insert
    fun insertObject(object2D: Object2D)

    @Delete
    fun deleteObject(object2D: Object2D)
}