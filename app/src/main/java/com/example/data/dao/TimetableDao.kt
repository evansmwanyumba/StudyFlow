package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.TimetableClass
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {
    @Query("SELECT * FROM timetable_classes ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllClasses(): Flow<List<TimetableClass>>

    @Query("SELECT * FROM timetable_classes ORDER BY dayOfWeek ASC, startTime ASC")
    suspend fun getAllClassesList(): List<TimetableClass>

    @Query("SELECT * FROM timetable_classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    fun getClassesForDay(dayOfWeek: Int): Flow<List<TimetableClass>>

    @Query("SELECT * FROM timetable_classes WHERE dayOfWeek = :dayOfWeek ORDER BY startTime ASC")
    suspend fun getClassesForDayList(dayOfWeek: Int): List<TimetableClass>

    @Query("SELECT * FROM timetable_classes WHERE id = :id LIMIT 1")
    suspend fun getClassById(id: Long): TimetableClass?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(timetableClass: TimetableClass): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<TimetableClass>): List<Long>

    @Update
    suspend fun updateClass(timetableClass: TimetableClass)

    @Delete
    suspend fun deleteClass(timetableClass: TimetableClass)

    @Query("DELETE FROM timetable_classes WHERE id = :id")
    suspend fun deleteClassById(id: Long)

    @Query("DELETE FROM timetable_classes")
    suspend fun clearAllClasses()
}
