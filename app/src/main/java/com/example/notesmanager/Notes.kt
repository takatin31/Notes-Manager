package com.example.notesmanager

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.util.*

@Entity(tableName = "Notes")
data class Notes (@PrimaryKey var id : Int, var titre: String, var date: String, var color: Int, var content: String, var checked : Boolean) {


}