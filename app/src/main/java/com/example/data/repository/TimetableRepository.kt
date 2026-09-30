package com.example.data.repository

import com.example.data.dao.TimetableDao
import com.example.data.model.TimetableClass
import kotlinx.coroutines.flow.Flow

class TimetableRepository(private val timetableDao: TimetableDao) {
    val allClasses: Flow<List<TimetableClass>> = timetableDao.getAllClasses()

    suspend fun getAllClassesList(): List<TimetableClass> = timetableDao.getAllClassesList()

    fun getClassesForDay(dayOfWeek: Int): Flow<List<TimetableClass>> =
        timetableDao.getClassesForDay(dayOfWeek)

    suspend fun getClassesForDayList(dayOfWeek: Int): List<TimetableClass> =
        timetableDao.getClassesForDayList(dayOfWeek)

    suspend fun getClassById(id: Long): TimetableClass? = timetableDao.getClassById(id)

    suspend fun insertClass(timetableClass: TimetableClass): Long =
        timetableDao.insertClass(timetableClass)

    suspend fun insertClasses(classes: List<TimetableClass>): List<Long> =
        timetableDao.insertClasses(classes)

    suspend fun updateClass(timetableClass: TimetableClass) =
        timetableDao.updateClass(timetableClass)

    suspend fun deleteClass(timetableClass: TimetableClass) =
        timetableDao.deleteClass(timetableClass)

    suspend fun deleteClassById(id: Long) = timetableDao.deleteClassById(id)

    suspend fun clearAll() = timetableDao.clearAllClasses()
}
