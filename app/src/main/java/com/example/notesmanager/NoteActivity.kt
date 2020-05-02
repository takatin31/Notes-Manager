package com.example.notesmanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_note.*
import java.text.SimpleDateFormat
import java.util.*

class NoteActivity : AppCompatActivity() {

    private lateinit var noteDatabase : NoteRoomDatabase
    private var exist : Boolean = true
    private var currentCol : Int = R.color.white
    var checkSpinner : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)


        ArrayAdapter.createFromResource(
            this,
            R.array.color_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_list_item_1)
            spinnerColor.adapter = adapter
        }

        spinnerColor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if(++checkSpinner > 1) {
                    getColorId(position)
                }
            }
        }


        noteDatabase = NoteRoomDatabase.getDatabase(this)

        val noteId : Int = intent.getIntExtra("id", 0)

        getNote(noteId)

        arrowBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        check.setOnClickListener{
            val newTitle = titleNote.text.toString()
            val newBody = noteBody.text.toString()
            val newColor = currentCol
            val sdf = SimpleDateFormat("dd/M/yyyy hh:mm")
            val currentDate = sdf.format(Date())
            val note = Notes(noteId, newTitle, currentDate, newColor, newBody, false)

            if (exist){
                modifyNote(note)
            }else{
                addNote(note)
            }
        }



    }


    fun getNote(note : Int) {
        AppExecutors.instance!!.diskIO().execute {
            var noteSearched = noteDatabase.noteDao().findNote(note)
            AppExecutors.instance!!.mainThread().execute( Runnable {
                if (noteSearched.isEmpty()){
                    exist = false
                    titleNote.setText("NewNote")
                    val sdf = SimpleDateFormat("dd/M/yyyy hh:mm")
                    val currentDate = sdf.format(Date())
                    dateNote.text = currentDate
                    noteBody.setText("")
                    noteBodyContent.setBackgroundColor(ContextCompat.getColor(this, R.color.white))
                }else{
                    exist = true
                    var selectedNote = noteSearched[0]
                    titleNote.setText(selectedNote.titre)
                    dateNote.text = selectedNote.date
                    noteBody.setText(selectedNote.content)
                    currentCol = selectedNote.color
                    noteBodyContent.setBackgroundColor(ContextCompat.getColor(this, currentCol))
                }

            })
        }
    }

    fun modifyNote(note : Notes) {
        AppExecutors.instance!!.diskIO().execute {
            noteDatabase.noteDao().modifyNote(note)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun addNote(note : Notes) {
        AppExecutors.instance!!.diskIO().execute {
            noteDatabase.noteDao().addNote(note)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun getColorId(position : Int){
        currentCol =  when(position){
            0 -> R.color.white
            1 -> R.color.yellow
            2 -> R.color.green
            3 -> R.color.blue
            4 -> R.color.red
            else -> R.color.white
        }
        noteBodyContent.setBackgroundColor(ContextCompat.getColor(this, currentCol))
    }
}
