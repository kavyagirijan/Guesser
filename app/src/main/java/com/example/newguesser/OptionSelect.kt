package com.example.newguesser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class OptionSelect : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var selectedOption: String
    private lateinit var timedGameModeButton: Button
    private lateinit var basicGameModeButton: Button
    private lateinit var languageChoiceTextView: TextView
    private lateinit var goToScoreBoardButton: Button
    private lateinit var goToAddQuestionsButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option_select)

        timedGameModeButton = findViewById(R.id.timed_mode_button)
        basicGameModeButton = findViewById(R.id.basic_mode_button)
        languageChoiceTextView = findViewById(R.id.languageChoice)
        goToScoreBoardButton = findViewById(R.id.goToScoreBoard)
        goToAddQuestionsButton = findViewById(R.id.goToAddQuestion)

        val spinner: Spinner = findViewById(R.id.spinner)
        val options = arrayOf("Deutsch", "English")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        timedGameModeButton.setOnClickListener{
            val intent = Intent(this, CreateUser::class.java)
            when (selectedOption) {
                "Deutsch" -> {
                    // Perform actions specific to German language
                        intent.putExtra("LANGUAGE", "de")
                }
                "English" -> {
                    // Perform actions specific to English language
                        intent.putExtra("LANGUAGE", "en")
                    }
                }
            intent.putExtra("GAME_MODE", "TIMED")
            startActivity(intent)
            }

        basicGameModeButton.setOnClickListener{
            val intent = Intent(this, CreateUser::class.java)
            when (selectedOption) {
                "Deutsch" -> {
                    // Perform actions specific to German language
                    intent.putExtra("LANGUAGE", "de")
                }
                "English" -> {
                    // Perform actions specific to English language
                    intent.putExtra("LANGUAGE", "en")
                }
            }
            intent.putExtra("GAME_MODE", "BASIC")
            startActivity(intent)
        }

        goToScoreBoardButton.setOnClickListener {
            val intent = Intent(this, ScoreBoard::class.java)
            startActivity(intent)
        }

        goToAddQuestionsButton.setOnClickListener {
            val intent = Intent(this, AddQuestions::class.java)
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
        if(selectedOption=="Deutsch"){
            basicGameModeButton.text="Grundmodus"
            timedGameModeButton.text="Zeitmodus"
            languageChoiceTextView.text="Sprache:"
            goToScoreBoardButton.text="Gehe zur Anzeigetafel"
            goToAddQuestionsButton.text="Fragen hinzufügen"
        }
        else{
            basicGameModeButton.text="Basic Mode"
            timedGameModeButton.text="Timed Mode"
            languageChoiceTextView.text="Language:"
            goToScoreBoardButton.text="Go to ScoreBoard"
            goToAddQuestionsButton.text="Add Questions"
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    }
}
