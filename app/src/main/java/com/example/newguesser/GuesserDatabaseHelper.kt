package com.example.newguesser

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class  GuesserDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "guesser.db"
        private const val DATABASE_VERSION = 3
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create your tables here
        db.execSQL("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY, name TEXT)")
        db.execSQL("CREATE TABLE IF NOT EXISTS score (id INTEGER PRIMARY KEY, username TEXT, point INTEGER, time_remaining INTEGER)")
        db.execSQL("CREATE TABLE IF NOT EXISTS content (id INTEGER PRIMARY KEY AUTOINCREMENT, question TEXT, answer TEXT, first_hint TEXT, second_hint TEXT, language TEXT)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades here
        // You can modify table schema or perform any necessary data migration
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS score (id INTEGER PRIMARY KEY, username TEXT, point INTEGER, time_remaining INTEGER)")
        }
        if (oldVersion < 3) {
            db.execSQL("CREATE TABLE IF NOT EXISTS content (id INTEGER PRIMARY KEY AUTOINCREMENT, question TEXT, answer TEXT, first_hint TEXT, second_hint TEXT, language TEXT)")
        }
    }

    fun createUser(givenUserName: String): Long {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put("name", givenUserName)

        return db.insert("users", null, values)
    }


    fun getUsers(): MutableList<User> {
        val userList: MutableList<User> = mutableListOf()

        val db = this.readableDatabase
        val query = "SELECT * from users"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                var user = User()
                user.name = result.getString(1)
                userList.add(user)
            } while (result.moveToNext())
        }

        return userList
    }

    fun addScore(userName: String, point: Int, timeRemaining: Long?): Long {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put("username", userName)
        values.put("point", point)
        values.put("time_remaining", timeRemaining)

        return db.insert("score", null, values)
    }

    fun getScores(): MutableList<Score> {
        val scores: MutableList<Score> = mutableListOf()

        val db = this.readableDatabase
        val query = "SELECT username, MAX(point) AS highest_score FROM score GROUP BY username"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                var score = Score()
                score.username = result.getString(0)
                score.point = result.getInt(1)
                scores.add(score)
            } while (result.moveToNext())
        }

        return scores
    }

    fun addContent(question: String, answer: String, first_hint: String, second_hint: String, language: String): Long {
        val db = this.writableDatabase

        val values = ContentValues()
        values.put("question", question)
        values.put("answer", answer)
        values.put("first_hint", first_hint)
        values.put("second_hint", second_hint)
        values.put("language", language)

        return db.insert("content", null, values)
    }

    fun getContent(receivedLanguage: String): MutableList<Content> {
        val contentList: MutableList<Content> = mutableListOf()
        val parsedLanguage = if (receivedLanguage == "de"){
            "Deutsch"
        } else {
            "English"
        }
        val db = this.readableDatabase
        val query = "SELECT * FROM content WHERE language = '$parsedLanguage'"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                var content = Content()
                content.id = result.getInt(0)
                content.question = result.getString(1)
                content.answer = result.getString(2)
                content.first_hint = result.getString(3)
                content.second_hint = result.getString(4)
                content.language = result.getString(5)
                contentList.add(content)
            } while (result.moveToNext())
        }

        return contentList
    }
}