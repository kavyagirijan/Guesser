package com.example.newguesser

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.content.Context
import android.content.Intent
import android.os.CountDownTimer
import android.widget.EditText
import android.widget.TextView
import com.google.gson.Gson
import java.io.InputStream
import android.util.Log
import android.text.method.ScrollingMovementMethod
import android.app.AlertDialog
import android.app.Dialog
import android.view.Gravity
import androidx.fragment.app.DialogFragment
import android.media.MediaPlayer
import kotlin.random.Random

private const val TAG = "TimeGameModeActivity"

class TimedGameMode : AppCompatActivity() {
    private lateinit var questionText: TextView
    private lateinit var pointText: TextView
    private lateinit var scoreText: TextView
    private lateinit var answerTextBox: EditText
    private lateinit var hintButton: Button
    private lateinit var submitButton: Button
    private lateinit var questionCountText: TextView
    private lateinit var exitButton:Button
    private lateinit var gameModeText: TextView
    private lateinit var correctAnswerVoice: MediaPlayer
    private lateinit var wrongAnswerVoice: MediaPlayer
    private lateinit var userNameTextView: TextView
    private lateinit var extraQuestionData: MutableList<Content>

    private lateinit var questionData: JsonData
    private lateinit var language: String
    private var attemptedQuestions: Array<Int> = arrayOf()
    private var selectedNumber: Int = 0
    private var questionPoint: Int = 0
    private var userScore: Int = 0
    private var currentHint: Int = 0
    private var userTypedAnswer: String = ""
    private var actualAnswer: String = ""
    private var maxQuestions: Int = 10
    private var userName: String = ""
    private var attemptedDbQuestions: Array<Int> = arrayOf()

    private var decider: String = "Inbuilt"
    private var roll: Int = 0

    private lateinit var countdownTextView: TextView
    private lateinit var countDownTimer: CountDownTimer
    private var secondsRemaining: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timed_game_mode)
        correctAnswerVoice = MediaPlayer.create(this,R.raw.correct_voice)
        wrongAnswerVoice = MediaPlayer.create(this,R.raw.wrong_answer)

        questionText = findViewById(R.id.questionText)
        pointText = findViewById(R.id.point)
        answerTextBox = findViewById(R.id.myTextBox)
        scoreText = findViewById(R.id.score)
        hintButton = findViewById(R.id.hintButton)
        submitButton = findViewById(R.id.submitButton)
        questionCountText = findViewById(R.id.questionCount)
        exitButton= findViewById(R.id.exit_button)
        gameModeText = findViewById(R.id.gameMode)

        language = intent.getStringExtra("LANGUAGE").toString()
        Log.i(TAG, "Fetched Language Preference")
        userName = intent.getStringExtra("USERNAME").toString()
        questionData = Gson().fromJson(loadAllQuestions(this), JsonData::class.java)
        Log.i(TAG, "Loaded Questions from JSON")

        questionText.text = ""

        // Username Text
        userNameTextView = findViewById(R.id.userName)
        userNameTextView.text = userName

        // Get Data from Database
        var db = GuesserDatabaseHelper(this)
        extraQuestionData = db.getContent(language)

        // Game Mode Text
        gameModeText.text = if (language == "de"){
            "Zeitmodus"
        } else {
            "Timed Mode"
        }

        // Countdown
        countdownTextView = findViewById(R.id.countdownTextView)

        val countdownDuration = 60000L * 3 // Countdown duration in milliseconds
        val countdownInterval = 1000L // Interval for countdown updates in milliseconds

        countDownTimer = object : CountDownTimer(countdownDuration, countdownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                loadLanguageBasedStrings("TimeRemaining")
            }

            override fun onFinish() {
                countdownTextView.text = if (language == "en") {
                    "Time up"
                } else {
                    "Zeit um"
                }
                countDownTimer.cancel()
                // Perform any actions you want when the countdown finishes
                hintButton.isEnabled = false
                hintButton.isClickable = false
                submitButton.isEnabled = false
                submitButton.isClickable = false
                questionText.text = if (language == "en") {
                    "Game over"
                } else {
                    "Spiel ist aus"
                }
            }
        }

        startCountdown()

        // Decider
        decider = dataDecider()

        // Question
        if (decider == "Inbuilt") {
            selectedNumber = rollInbuiltQuestionNumber()
            attemptedQuestions += selectedNumber
        } else {
            selectedNumber = rollDbQuestionNumber()
            if (selectedNumber == -1) {
                decider = "Inbuilt"
                selectedNumber = rollInbuiltQuestionNumber()
                attemptedQuestions += selectedNumber
            } else {
                attemptedDbQuestions += selectedNumber
            }
        }
        updateMainTextbox("QUESTION")
        questionPoint = 10
        loadLanguageBasedStrings("Point")
        loadLanguageBasedStrings("QuestionCount")
        questionText.movementMethod = ScrollingMovementMethod()

        // Score
        userScore = 0
        loadLanguageBasedStrings("Score")

        // Hint Button and Text
        currentHint = 2
        loadLanguageBasedStrings("Hint")
        Log.i(TAG, "Current hint $currentHint")
        hintButton.setOnClickListener {
            if (currentHint > 0) {
                currentHint -= 1
                updateMainTextbox("HINT")
                if (currentHint !== 0) {
                    loadLanguageBasedStrings("Hint")
                } else {
                    loadLanguageBasedStrings("ViewAnswer")
                }
                questionPoint -= 3
                loadLanguageBasedStrings("Point")
            } else {
                hintButton.isEnabled = false
                hintButton.isClickable = false
                updateMainTextbox("ANSWER")
            }
        }

        exitButton.setOnClickListener{
            val dialogFragment = MyDialog()
            dialogFragment.show(supportFragmentManager, "my_dialog_tag")
        }

        // Submit Button
        loadLanguageBasedStrings("Submit")
        submitButton.setOnClickListener {
            if (submitButton.text == "Next Question" || submitButton.text == "Nachste Frage") {
                if (attemptedQuestions.size + attemptedDbQuestions.size < maxQuestions) {
                    decider = dataDecider()
                    if (decider == "Inbuilt"){
                        selectedNumber = rollInbuiltQuestionNumber()
                        attemptedQuestions += selectedNumber
                    } else {
                        selectedNumber = rollDbQuestionNumber()
                        if (selectedNumber == -1) {
                            decider = "Inbuilt"
                            selectedNumber = rollInbuiltQuestionNumber()
                            attemptedQuestions += selectedNumber
                        } else {
                            attemptedDbQuestions += selectedNumber
                        }
                    }
                    updateMainTextbox("QUESTION")
                    loadLanguageBasedStrings("QuestionCount")
                    hintButton.isEnabled = true
                    hintButton.isClickable = true
                } else {
                    questionText.text = if (language == "en") {
                        "Game over."
                    } else {
                        "Spiel ist aus"
                    }
                }
            } else {
                userTypedAnswer = answerTextBox.text.toString().lowercase()
                // Validation
                actualAnswer = if (decider == "Inbuilt") {
                    if (language == "en") {
                        questionData.data[selectedNumber].answer.en.lowercase()
                    } else {
                        questionData.data[selectedNumber].answer.de.lowercase()
                    }
                } else {
                    extraQuestionData[selectedNumber].answer
                }

                var toastString: String
                val similarity = calculateAlphabetPositionSimilarity(userTypedAnswer,actualAnswer)
                if (similarity >= 0.7) {
                    correctAnswerVoice.start()
                    toastString = if (language == "en") {
                        "Correct"
                    } else {
                        "Richtig"
                    }
                    answerTextBox.setText("")
                    userScore += questionPoint
                    loadLanguageBasedStrings("Score")
                    Log.i("Attempt", attemptedQuestions.joinToString())
                    if (attemptedQuestions.size + attemptedDbQuestions.size < maxQuestions) {
                        decider = dataDecider()
                        if (decider == "Inbuilt"){
                            selectedNumber = rollInbuiltQuestionNumber()
                            attemptedQuestions += selectedNumber
                        } else {
                            selectedNumber = rollDbQuestionNumber()
                            if (selectedNumber == -1) {
                                decider = "Inbuilt"
                                selectedNumber = rollInbuiltQuestionNumber()
                                attemptedQuestions += selectedNumber
                            } else {
                                attemptedDbQuestions += selectedNumber
                            }
                        }
                        currentHint = 2
                        loadLanguageBasedStrings("Hint")
                        updateMainTextbox("QUESTION")
                    } else {
                        countDownTimer.onFinish()

                        var ifDataInserted = db.addScore(userName, userScore, secondsRemaining)

                        // Check if the insertion was successful
                        if (ifDataInserted != -1L) {
                            val intent = Intent(this, ScoreBoard::class.java)
                            startActivity(intent)
                        }
                    }
                } else {
                    wrongAnswerVoice.start()
                    toastString = if (language == "en") {
                        "Wrong"
                    } else {
                        "Falsch"
                    }
                }

                // Output
                val toast = Toast.makeText(applicationContext, toastString, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.TOP, 0, 0)
                toast.show()
            }
        }
    }
    override fun onBackPressed() {
        // Add your back button functionality here
        // For example, you can navigate back or perform any custom action
        val dialogFragment = MyDialog()
        dialogFragment.show(supportFragmentManager, "my_dialog_tag")
        super.onBackPressed()
    }
    private fun loadAllQuestions(context: Context): String? {
        var input: InputStream? = null
        var jsonString: String

        try {
            // Create InputStream
            input = context.assets.open("data.json")

            val size = input.available()

            // Create a buffer with the size
            val buffer = ByteArray(size)

            // Read data from InputStream into the Buffer
            input.read(buffer)

            // Create a json String
            jsonString = String(buffer)
            return jsonString;
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            // Must close the stream
            input?.close()
        }

        return null
    }

    private fun dataDecider(): String {
        return if (Random.nextInt(2) == 0) {
            "Inbuilt"
        } else {
            "DB"
        }
    }

    private fun rollInbuiltQuestionNumber(): Int {
        val selectedNumber = (0 until questionData.data.size).random()
        if (selectedNumber in attemptedQuestions) {
            rollInbuiltQuestionNumber()
        }
        return selectedNumber
    }

    private fun rollDbQuestionNumber(): Int {
        if (attemptedDbQuestions.size != extraQuestionData.size && extraQuestionData.size > 0) {
            val selectedNumber = (0 until extraQuestionData.size).random()
            if (selectedNumber in attemptedDbQuestions) {
                rollDbQuestionNumber()
            }
            return selectedNumber
        }
        return -1
    }

    private fun updateMainTextbox(mode: String) {
        questionText.text = if (mode == "QUESTION") {
            if (decider == "Inbuilt") {
                if (language == "en") {
                    "Question: " + questionData.data[selectedNumber].question.en + "\n\n"
                } else {
                    "Frage: " + questionData.data[selectedNumber].question.de + "\n\n"
                }
            } else {
                if (language == "en") {
                    "Question: " + extraQuestionData[selectedNumber].question + "\n\n"
                } else {
                    "Frage: " + extraQuestionData[selectedNumber].question + "\n\n"
                }
            }
        } else if (mode == "HINT") {
            if (decider == "Inbuilt") {
                if (language == "en") {
                    "Hint: " + questionData.data[selectedNumber].hint[currentHint].en + "\n\n" + questionText.text
                } else {
                    "Hinweise:  " + questionData.data[selectedNumber].hint[currentHint].de + "\n\n" + questionText.text
                }
            } else {
                if (language == "en" && currentHint == 2) {
                    "Hint: " + extraQuestionData[selectedNumber].first_hint + "\n\n" + questionText.text
                } else {
                    "Hinweise:  " + extraQuestionData[selectedNumber].first_hint + "\n\n" + questionText.text
                }
                if (language == "en" && currentHint == 1) {
                    "Hint: " + extraQuestionData[selectedNumber].second_hint + "\n\n" + questionText.text
                } else {
                    "Hinweise:  " + extraQuestionData[selectedNumber].second_hint + "\n\n" + questionText.text
                }
            }
        } else {
            if (decider == "Inbuilt") {
                if (language == "en") {
                    "Answer was " + questionData.data[selectedNumber].answer.en
                } else {
                    "Die Antwort war " + questionData.data[selectedNumber].answer.de
                }
            } else {
                if (language == "en") {
                    "Answer was " + extraQuestionData[selectedNumber].answer
                } else {
                    "Die Antwort war " + extraQuestionData[selectedNumber].answer
                }
            }
        }
        if (mode == "QUESTION") {
            currentHint = questionData.data[selectedNumber].hint.size
            loadLanguageBasedStrings("Hint")
            loadLanguageBasedStrings("Submit")
            questionPoint = 10
            loadLanguageBasedStrings("Point")
        }
        if (mode =="ANSWER"){
            loadLanguageBasedStrings("NextQuestion")
        }
    }

    private fun startCountdown() {
        countDownTimer.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer.cancel()
        correctAnswerVoice.release()
        wrongAnswerVoice.release()
    }

    override fun onPause() {
        super.onPause()
        countDownTimer.cancel()
    }

    private fun loadLanguageBasedStrings(mode: String) {
        if (mode == "Score") {
            if (language == "en") {
                scoreText.text = getString(R.string.score_en, userScore)
            } else {
                scoreText.text = getString(R.string.score_de, userScore)
            }
        }
        if (mode == "Point") {
            if (language == "en") {
                pointText.text = getString(R.string.point_en, questionPoint)
            } else {
                pointText.text = getString(R.string.point_de, questionPoint)
            }
        }
        if (mode == "Hint") {
            if (language == "en") {
                hintButton.text = getString(R.string.next_hint_en, currentHint)
            } else {
                hintButton.text = getString(R.string.next_hint_de, currentHint)
            }
        }
        if (mode == "QuestionCount") {
            if (language == "en") {
                questionCountText.text =
                    getString(R.string.question_count_en, attemptedQuestions.size + attemptedDbQuestions.size, maxQuestions)
            } else {
                questionCountText.text =
                    getString(R.string.question_count_de, attemptedQuestions.size + attemptedDbQuestions.size, maxQuestions)
            }
        }
        if (mode == "TimeRemaining") {
            if (language == "en") {
                countdownTextView.text = getString(R.string.time_left_en, secondsRemaining)
            } else {
                countdownTextView.text = getString(R.string.time_left_de, secondsRemaining)
            }
        }
        if (mode == "Submit") {
            if (language == "en") {
                submitButton.text = "Submit"
            } else {
                submitButton.text = "Einreichen"
            }
        }
        if (mode == "ViewAnswer") {
            if (language == "en") {
                hintButton.text = "View Answer"
            } else {
                hintButton.text = "Antwort Ansehen"
            }
        }
        if (mode== "NextQuestion"){
            if (language == "en") {
                submitButton.text = "Next Question"
            } else {
                submitButton.text = "Nachste Frage"
            }
        }

    }

    fun calculateAlphabetPositionSimilarity(userTypedAnswer: String, expectedAnswer: String): Double {
        val userChars = userTypedAnswer.lowercase()
        val expectedChars = expectedAnswer.lowercase()

        val userCharacterPositions = userChars.mapIndexed { index, char -> CharacterPosition(char, index) }
        val expectedCharacterPositions = expectedChars.mapIndexed { index, char -> CharacterPosition(char, index) }

        val intersectionSize = userCharacterPositions.intersect(expectedCharacterPositions).size
        val unionSize = userCharacterPositions.union(expectedCharacterPositions).size

        return intersectionSize.toDouble() / unionSize.toDouble()
    }

    class MyDialog : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Warning")
                .setMessage("Are You Sure ,You want To Quit?")
                .setPositiveButton("Yes") { _, _ ->
                    val intent = Intent(context, OptionSelect::class.java)
                    startActivity(intent)
                    Log.i("yes worked","")
                }
                .setNegativeButton("No") { _, _ ->
                    Log.i("no worked","")
                }

            return builder.create()
        }
    }
}