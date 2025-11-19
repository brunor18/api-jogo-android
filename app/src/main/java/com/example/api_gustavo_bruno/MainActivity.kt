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

data class Game (
    val price: String, // price: preco-do-jogo
    var title: String, // title: nome-do-jogo
    var picture: String // picture: link-da-imagem
)


class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    private val gameList = mutableListOf<Game>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
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

                    gameList.clear()


                for (i in 0 until jsonArray.length()) {
                        val showObject = jsonArray.getJSONObject(i)

                        val title = showObject.optString("internalName", "Desconhecido")
                        val price = showObject.optString("cheapest", "0")
                        val picture = showObject.optString("thumb", "")

                        val game = Game(
                            title = title,
                            price = price,
                            picture = picture
                        )
                        gameList.add(game)
                    }

                    val resultQuery = gameList.joinToString("\n\n") { game ->
                        """
                        Nome: ${game.title}
                        Preço: ${game.price}
                        Foto: ${game.picture}
                        """.trimIndent()
                    }


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