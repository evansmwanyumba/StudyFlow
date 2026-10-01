package com.example.data.repository

import com.example.data.dao.ExamDao
import com.example.data.model.Exam
import kotlinx.coroutines.flow.Flow

class ExamRepository(private val examDao: ExamDao) {
    val allExams: Flow<List<Exam>> = examDao.getAllExams()

    suspend fun getAllExamsList(): List<Exam> = examDao.getAllExamsList()

    suspend fun getExamsForDate(date: String): List<Exam> = examDao.getExamsForDate(date)

    suspend fun insertExam(exam: Exam): Long = examDao.insertExam(exam)

    suspend fun updateExam(exam: Exam) = examDao.updateExam(exam)

    suspend fun deleteExam(exam: Exam) = examDao.deleteExam(exam)

    suspend fun deleteExamById(id: Long) = examDao.deleteExamById(id)

    suspend fun clearAll() = examDao.clearAllExams()
}
