package com.example.notesmanager

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class MainActivity : AppCompatActivity() {

    var listNotes = arrayListOf<Notes>()
    lateinit var adapter: NoteAdapter
    lateinit var layoutManager : LinearLayoutManager
    var deleteMode : Boolean = false
    var searchedList = arrayListOf<Notes>()
    private lateinit var noteDatabase : NoteRoomDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        noteDatabase = NoteRoomDatabase.getDatabase(this)

//initNotes()

        getAllList()


        layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager

        adapter = NoteAdapter(this)
        recyclerView.adapter = adapter

        refreshList()

        val searchNote = searchNoteView
        searchNote.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                searchedList.clear()
                val noteText = searchNote.text.toString().toLowerCase()
                for (note in listNotes){
                    if (note.titre.toLowerCase().contains(noteText)){
                        searchedList.add(note)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })

        floatingActionButton.setOnClickListener {
            if (deleteMode){
                var deleteList = arrayListOf<Notes>()
                for (note in searchedList){
                    if (note.checked){
                        deleteNote(note)
                    }
                }
                getAllList()
            }else{
                val intent = Intent(this, NoteActivity::class.java)
                intent.putExtra("id", listNotes.size)
                startActivity(intent)
                finish()
            }
        }
    }

    fun refreshList(){
        adapter.notifyDataSetChanged()
    }

    fun getAllList() {
        val notes = arrayListOf<Notes>()
        AppExecutors.instance!!.diskIO().execute(Runnable {
            listNotes.clear()
            listNotes.addAll(noteDatabase.noteDao().loadAllNotes())
            searchedList.clear()
            searchedList.addAll(listNotes)
            //refreshList()
            AppExecutors.instance!!.mainThread().execute( Runnable {
                adapter.notifyDataSetChanged()
                checkDeleteMode()
            })
        })

    }

    fun addNote(note : Notes) {
         AppExecutors.instance!!.diskIO().execute {
             noteDatabase.noteDao().addNote(note)
         }
    }

    fun deleteNote(note : Notes){
        AppExecutors.instance!!.diskIO().execute {
            noteDatabase.noteDao().deleteNote(note)
        }
    }

    fun initNotes(){

        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm")
        val currentDate = sdf.format(Date())

        val note1 = Notes(1 ,"Note1552", currentDate, R.color.blue, "some conetn", false)

        addNote(note1)

        getAllList()

    }

    fun checkDeleteMode(){
        deleteMode = false
        for (note in searchedList){
            deleteMode = deleteMode || note.checked
        }

        if (deleteMode){
            floatingActionButton.setImageResource(R.drawable.ic_delete)
        }else{
            floatingActionButton.setImageResource(R.drawable.ic_note_add)
        }
    }

    class NoteAdapter(val activity : MainActivity) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>(){
        class NoteViewHolder(v : View) : RecyclerView.ViewHolder(v){
            val noteLayout = v.findViewById<RelativeLayout>(R.id.noteLayout)
            val noteTitle = v.findViewById<TextView>(R.id.noteTitle)
            val noteDate = v.findViewById<TextView>(R.id.noteDate)
            val noteSelected = v.findViewById<CheckBox>(R.id.noteSelected)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
            return NoteViewHolder(LayoutInflater.from(activity).inflate(R.layout.note_layout, parent, false))
        }

        override fun getItemCount(): Int {
            return activity.searchedList.size
        }

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            holder.noteTitle.text = activity.searchedList[position].titre
            holder.noteDate.text = activity.searchedList[position].date
            holder.noteLayout.setBackgroundColor(ContextCompat.getColor(activity, activity.searchedList[position].color))
            holder.noteSelected.isChecked = false
            holder.noteSelected.setOnClickListener {
                activity.searchedList[position].checked = holder.noteSelected.isChecked
                activity.checkDeleteMode()
            }
            holder.noteLayout.setOnClickListener {
                val intent = Intent(activity, NoteActivity::class.java)
                intent.putExtra("id", activity.searchedList[position].id)
                activity.startActivity(intent)
                activity.finish()
            }

        }
    }
}
