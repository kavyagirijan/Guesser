package com.example.newguesser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.util.Log
import android.widget.Button
import android.widget.EditText

private const val TAG = "Add Questions"
class AddQuestions : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    private lateinit var enterQuestionEditText: EditText
    private lateinit var enterAnswerEditText: EditText
    private lateinit var enterFirstHintEditText: EditText
    private lateinit var enterSecondHintEditText: EditText
    private lateinit var addQuestionButton: Button
    private lateinit var cancelButton: Button

    private var selectedOption: String = ""
    private var enteredQuestion: String = ""
    private var enteredAnswer: String = ""
    private var enteredFirstHint: String = ""
    private var enteredSecondHint: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_questions)

        enterQuestionEditText = findViewById(R.id.enter_question)
        enterAnswerEditText = findViewById(R.id.enter_answer)
        enterFirstHintEditText = findViewById(R.id.first_hint)
        enterSecondHintEditText = findViewById(R.id.second_hint)
        addQuestionButton = findViewById(R.id.addQuestionButton)
        cancelButton = findViewById(R.id.cancelButton)

        val spinner: Spinner = findViewById(R.id.spinner)
        val options = arrayOf("Deutsch", "English")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        addQuestionButton.setOnClickListener {
            enteredQuestion = enterQuestionEditText.text.toString()
            enteredAnswer = enterAnswerEditText.text.toString()
            enteredFirstHint = enterFirstHintEditText.text.toString()
            enteredSecondHint = enterSecondHintEditText.text.toString()

            var db = GuesserDatabaseHelper(this)
            var ifDataInserted = db.addContent(enteredQuestion,enteredAnswer,enteredFirstHint,enteredSecondHint,selectedOption)

            // Check if the insertion was successful
            if (ifDataInserted != -1L) {
                val intent = Intent(this, OptionSelect::class.java)
                Log.i(TAG, "Added Question Successfully")
                startActivity(intent)
            }
        }

        cancelButton.setOnClickListener {
            val intent = Intent(this, OptionSelect::class.java)
            startActivity(intent)
        }
    }

    override fun onItemSelected(
        parent: AdapterView<*>,
        view: View?,
        position: Int,
        id: Long
    ) {
        selectedOption = parent.getItemAtPosition(position).toString()
        Log.i(TAG, selectedOption)
        if (selectedOption == "Deutsch") {
            enterQuestionEditText.hint = "Frage Eingeben"
            enterAnswerEditText.hint = "Antwort Eingeben"
            enterFirstHintEditText.hint = "Erste Hinweise Eingeben"
            enterSecondHintEditText.hint = "Zweite Hinweise Eingeben"
            addQuestionButton.text = "Hinzufügen"
            cancelButton.text = "Abbrechen"
        } else {
            enterQuestionEditText.hint = "Enter question"
            enterAnswerEditText.hint = "Enter answer"
            enterFirstHintEditText.hint = "Enter first hint"
            enterSecondHintEditText.hint = "Enter second hint"
            addQuestionButton.text = "Add"
            cancelButton.text = "Cancel"
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}
