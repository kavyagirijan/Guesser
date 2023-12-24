package com.example.newguesser

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.util.Log
import android.widget.Button

private const val TAG = "CreateUserActivity"
class CreateUser : AppCompatActivity() {
    private lateinit var userNameTextBox: TextView
    private lateinit var createUserButton: Button
    private lateinit var userPageHeading: TextView

    private var givenUserName = ""
    private var language = ""
    private var gameMode = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_user)

        userNameTextBox = findViewById(R.id.username)
        createUserButton = findViewById(R.id.createUser)
        userPageHeading = findViewById(R.id.userPageHeading)

        language = intent.getStringExtra("LANGUAGE").toString()
        gameMode = intent.getStringExtra("GAME_MODE").toString()

        createUserButton.setOnClickListener {
            givenUserName = userNameTextBox.text.toString().lowercase()
            Log.i(TAG, givenUserName)

            if (gameMode == "TIMED"){
                val intent = Intent(this, TimedGameMode::class.java)
                intent.putExtra("LANGUAGE", language)
                intent.putExtra("USERNAME", givenUserName)
                startActivity(intent)
            } else {
                val intent = Intent(this, BasicGameMode::class.java)
                intent.putExtra("LANGUAGE", language)
                intent.putExtra("USERNAME", givenUserName)
                startActivity(intent)
            }
        }

        if (language == "de") {
            userPageHeading.text = "Geben Sie Ihre Benutzernamen ein"
            createUserButton.text = "Gehe zum Spiel"
        }

    }
}