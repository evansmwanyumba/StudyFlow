package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Exam
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams ORDER BY examDate ASC, examTime ASC")
    fun getAllExams(): Flow<List<Exam>>

    @Query("SELECT * FROM exams ORDER BY examDate ASC, examTime ASC")
    suspend fun getAllExamsList(): List<Exam>

    @Query("SELECT * FROM exams WHERE examDate = :date")
    suspend fun getExamsForDate(date: String): List<Exam>

    @Query("SELECT * FROM exams WHERE id = :id LIMIT 1")
    suspend fun getExamById(id: Long): Exam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExam(exam: Exam): Long

    @Update
    suspend fun updateExam(exam: Exam)

    @Delete
    suspend fun deleteExam(exam: Exam)

    @Query("DELETE FROM exams WHERE id = :id")
    suspend fun deleteExamById(id: Long)

    @Query("DELETE FROM exams")
    suspend fun clearAllExams()
}
