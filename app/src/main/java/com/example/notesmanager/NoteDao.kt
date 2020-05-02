package com.example.notesmanager

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NoteDao {

    @Query("Select * from Notes")
    fun loadAllNotes(): List<Notes>

    @Query("Select titre from Notes")
    fun loadNotesTitle():List<String>

    @Query("Select * from Notes where checked = 1")
    fun loadSelectedNotes():List<Notes>

    @Query("Select * from Notes where titre like :text")
    fun loadSearchedNotes(text : String):List<Notes>

    @Query("Select * from Notes where id = :noteId")
    fun findNote(noteId : Int):List<Notes>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addNote(note: Notes)

    @Update
    fun modifyNote(note: Notes)

    @Delete
    fun deleteNote(note : Notes)

}