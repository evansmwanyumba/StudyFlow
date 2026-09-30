package com.example.ai

import com.example.data.model.TimetableClass

object SampleTimetables {

    val ComputerScience = listOf(
        TimetableClass(
            courseCode = "CS 201",
            courseName = "Data Structures & Algorithms",
            instructor = "Prof. Alan Vance",
            location = "Turing Hall 302",
            dayOfWeek = 1, // Monday
            startTime = "08:30",
            endTime = "10:00",
            colorHex = "#2563EB",
            category = "Lecture",
            notes = "Review dynamic programming exercises before session"
        ),
        TimetableClass(
            courseCode = "MATH 240",
            courseName = "Discrete Mathematics & Logic",
            instructor = "Dr. Elena Chen",
            location = "Math Annex 105",
            dayOfWeek = 1, // Monday
            startTime = "10:30",
            endTime = "12:00",
            colorHex = "#0D9488",
            category = "Lecture",
            notes = "Bring Graph theory problem set"
        ),
        TimetableClass(
            courseCode = "CS 201L",
            courseName = "Algorithms Lab",
            instructor = "TA Marcus Brody",
            location = "Computing Lab B",
            dayOfWeek = 1, // Monday
            startTime = "14:00",
            endTime = "16:00",
            colorHex = "#2563EB",
            category = "Lab",
            notes = "Benchmark sorting algorithms"
        ),
        TimetableClass(
            courseCode = "CS 220",
            courseName = "Computer Architecture",
            instructor = "Prof. Sarah Miller",
            location = "Engineering Hall 204",
            dayOfWeek = 2, // Tuesday
            startTime = "09:00",
            endTime = "10:30",
            colorHex = "#8B5CF6",
            category = "Lecture",
            notes = "RISC-V pipeline hazard review"
        ),
        TimetableClass(
            courseCode = "PHYS 102",
            courseName = "Electromagnetism & Waves",
            instructor = "Dr. Robert Singh",
            location = "Science Quad 410",
            dayOfWeek = 2, // Tuesday
            startTime = "11:00",
            endTime = "12:30",
            colorHex = "#D97706",
            category = "Lecture"
        ),
        TimetableClass(
            courseCode = "CS 201",
            courseName = "Data Structures & Algorithms",
            instructor = "Prof. Alan Vance",
            location = "Turing Hall 302",
            dayOfWeek = 3, // Wednesday
            startTime = "08:30",
            endTime = "10:00",
            colorHex = "#2563EB",
            category = "Lecture"
        ),
        TimetableClass(
            courseCode = "MATH 240",
            courseName = "Discrete Mathematics Tutorial",
            instructor = "TA Jessica Adams",
            location = "Math Annex 108",
            dayOfWeek = 3, // Wednesday
            startTime = "13:00",
            endTime = "14:30",
            colorHex = "#0D9488",
            category = "Tutorial"
        ),
        TimetableClass(
            courseCode = "CS 220",
            courseName = "Computer Architecture",
            instructor = "Prof. Sarah Miller",
            location = "Engineering Hall 204",
            dayOfWeek = 4, // Thursday
            startTime = "09:00",
            endTime = "10:30",
            colorHex = "#8B5CF6",
            category = "Lecture"
        ),
        TimetableClass(
            courseCode = "ENG 210",
            courseName = "Technical Writing for Engineers",
            instructor = "Dr. Claire Dupont",
            location = "Humanities 102",
            dayOfWeek = 4, // Thursday
            startTime = "11:00",
            endTime = "12:30",
            colorHex = "#EC4899",
            category = "Seminar"
        ),
        TimetableClass(
            courseCode = "PHYS 102L",
            courseName = "Physics Optics Lab",
            instructor = "Dr. Robert Singh",
            location = "Physics Lab 3",
            dayOfWeek = 5, // Friday
            startTime = "09:30",
            endTime = "11:30",
            colorHex = "#D97706",
            category = "Lab"
        ),
        TimetableClass(
            courseCode = "CS 290",
            courseName = "Software Engineering Project",
            instructor = "Prof. Keith Larson",
            location = "Innovation Hub 4B",
            dayOfWeek = 5, // Friday
            startTime = "13:30",
            endTime = "15:30",
            colorHex = "#06B6D4",
            category = "Seminar"
        )
    )

    val PreMedBiology = listOf(
        TimetableClass(
            courseCode = "BIO 150",
            courseName = "Cellular & Molecular Biology",
            instructor = "Dr. Rebecca Sterling",
            location = "Bio Science 101",
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "09:30",
            colorHex = "#10B981",
            category = "Lecture"
        ),
        TimetableClass(
            courseCode = "CHEM 210",
            courseName = "Organic Chemistry I",
            instructor = "Prof. Arthur Pendelton",
            location = "Chemistry Hall A",
            dayOfWeek = 1,
            startTime = "10:00",
            endTime = "11:30",
            colorHex = "#F59E0B",
            category = "Lecture"
        ),
        TimetableClass(
            courseCode = "BIO 150L",
            courseName = "Cellular Biology Lab",
            instructor = "Dr. Rebecca Sterling",
            location = "Bio Lab 204",
            dayOfWeek = 2,
            startTime = "09:00",
            endTime = "12:00",
            colorHex = "#10B981",
            category = "Lab"
        ),
        TimetableClass(
            courseCode = "CHEM 210L",
            courseName = "Organic Chem Synthesis Lab",
            instructor = "Prof. Arthur Pendelton",
            location = "Chemistry Lab 10",
            dayOfWeek = 3,
            startTime = "13:00",
            endTime = "16:00",
            colorHex = "#F59E0B",
            category = "Lab"
        ),
        TimetableClass(
            courseCode = "STATS 200",
            courseName = "Biostatistics",
            instructor = "Dr. Linnea Hayes",
            location = "Science Hall 202",
            dayOfWeek = 4,
            startTime = "08:30",
            endTime = "10:00",
            colorHex = "#6366F1",
            category = "Lecture"
        ),
        TimetableClass(
            courseCode = "BIO 150",
            courseName = "Cellular & Molecular Biology",
            instructor = "Dr. Rebecca Sterling",
            location = "Bio Science 101",
            dayOfWeek = 5,
            startTime = "08:00",
            endTime = "09:30",
            colorHex = "#10B981",
            category = "Lecture"
        )
    )

    val BusinessFinance = listOf(
        TimetableClass(
            courseCode = "FIN 310",
            courseName = "Corporate Finance & Valuation",
            instructor = "Prof. David Thorne",
            location = "Business Center 401",
            dayOfWeek = 1,
            startTime = "09:00",
            endTime = "10:30",
            colorHex = "#0D9488",
            category = "Lecture"
        ),
        TimetableClass(
            courseCode = "MKT 201",
            courseName = "Marketing Strategy & Brand",
            instructor = "Prof. Chloe Martinez",
            location = "Business Center 205",
            dayOfWeek = 2,
            startTime = "10:00",
            endTime = "11:30",
            colorHex = "#EC4899",
            category = "Lecture"
        ),
        TimetableClass(
            courseCode = "ECON 202",
            courseName = "Macroeconomics Analysis",
            instructor = "Dr. Julian Walsh",
            location = "Auditorium 1",
            dayOfWeek = 3,
            startTime = "09:00",
            endTime = "10:30",
            colorHex = "#3B82F6",
            category = "Lecture"
        ),
        TimetableClass(
            courseCode = "MGMT 330",
            courseName = "Organizational Leadership",
            instructor = "Dr. Harrison Ford",
            location = "Seminar Hall 3",
            dayOfWeek = 4,
            startTime = "13:00",
            endTime = "14:30",
            colorHex = "#8B5CF6",
            category = "Seminar"
        )
    )
}
