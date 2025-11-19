package com.example.api_gustavo_bruno

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import org.json.JSONArray


class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//
        val editQuery = findViewById<EditText>(R.id.edit_query)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val result = findViewById<TextView>(R.id.result)


        btnSearch.setOnClickListener()
        {
            val gameName = editQuery.text.toString()

            thread {
                try {
                    val url = URL("https://www.cheapshark.com/api/1.0/games?title=$gameName")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.requestMethod = "GET"

                    val data = connection.inputStream.bufferedReader().readText()
                    val jsonArray = JSONArray(data)
                    val showObject = jsonArray.getJSONObject(0)

                    val internalName = showObject.optString("internalName", "Desconhecido")
                    val cheapest = showObject.getJSONArray("cheapest")
                    val thumb = showObject.optString("thumb")

                    val resultQuery = """
                        internalName: $internalName
                        cheapest: $cheapest
                        thumb: $thumb
                    """.trimIndent()


                    runOnUiThread {
                        result.text = resultQuery
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        result.text = "ERRO: ${e.message}"
                    }
                }
            }
        }

    }
}