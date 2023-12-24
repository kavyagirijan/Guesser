package com.example.newguesser

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView

class ScoreBoard : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score_board)

        var scoreBoardTextView = findViewById<TextView>(R.id.scoreBoardTextView)
        var playAgainButton = findViewById<Button>(R.id.playAgainButton)

        var db = GuesserDatabaseHelper(this)
        var allScores = db.getScores()
        val stringBuilder = StringBuilder()
        for (score in allScores) {
            stringBuilder.append("Name: ${score.username}, Score: ${score.point}\n\n")
        }
        scoreBoardTextView.text = stringBuilder.toString()

        Log.i("AllScores", allScores.toString())

        playAgainButton.setOnClickListener {
            val intent = Intent(this, OptionSelect::class.java)
            startActivity(intent)
        }
    }
}