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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread
import org.json.JSONObject
import org.json.JSONArray

data class Game(
    val price: String?,
    var title: String,
    var picture: String
)

class MainActivity : AppCompatActivity() {

    private val gameList = mutableListOf<Game>()

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

        val editQuery = findViewById<EditText>(R.id.edit_query)
        val editQueryNum = findViewById<EditText>(R.id.edit_query_numero)
        val btnSearch = findViewById<Button>(R.id.btnSearch)
        val result = findViewById<TextView>(R.id.result)
        val recycler = findViewById<RecyclerView>(R.id.recycler_games)


        recycler.layoutManager = LinearLayoutManager(this)

        btnSearch.setOnClickListener {
            val gameName = editQuery.text.toString()
            val gameId= editQueryNum.text.toString()

            val editMin = findViewById<EditText>(R.id.menor_que)
            val editMax = findViewById<EditText>(R.id.maior_que)

            val minPrice = editMin.text.toString().toDoubleOrNull()
            val maxPrice = editMax.text.toString().toDoubleOrNull()

            val nomeON = gameName.isNotEmpty()
            val idON = gameId.isNotEmpty()

            thread {
                try {
                    when {
                        nomeON && idON -> {
                            result.text = "Somente um campo pode ser preenchido"
                        }

                        nomeON -> {
                            val url =
                                URL("https://www.cheapshark.com/api/1.0/games?title=$gameName")
                            val connection = url.openConnection() as HttpURLConnection
                            connection.requestMethod = "GET"

                            val data = connection.inputStream.bufferedReader().readText()
                            val jsonArray = JSONArray(data)

                            gameList.clear()

                            for (i in 0 until jsonArray.length()) {
                                val obj = jsonArray.getJSONObject(i)

                                val title = obj.optString("external", "Desconhecido")
                                val price = obj.optString("cheapest", "0")
                                val picture = obj.optString("thumb", "")

                                gameList.add(Game(price, title, picture))
                            }

                            val filtro = gameList.filter { game ->
                                val value = game.price?.toDoubleOrNull() ?: 0.0
                                val okMin = minPrice?.let { value >= it } ?: true
                                val okMax = maxPrice?.let { value <= it } ?: true
                                okMin && okMax
                            }

                            runOnUiThread {
                                result.text = "Encontrados: ${gameList.size}"

                                recycler.adapter = GameAdapter(filtro.toMutableList())

                            }
                        }


                        idON -> {
                            val url = URL("https://www.cheapshark.com/api/1.0/games?id=$gameId")
                            val connection = url.openConnection() as HttpURLConnection
                            connection.requestMethod = "GET"

                            val data = connection.inputStream.bufferedReader().readText()
                            val jsonObject = JSONObject(data)

                            gameList.clear()

                            val infoObj = jsonObject.optJSONObject("info")
                            val cheapestObj = jsonObject.optJSONObject("cheapestPriceEver")

                            if (infoObj != null) {
                                val title = infoObj.optString("title", "Desconhecido")
                                val picture = infoObj.optString("thumb", "")
                                val price = cheapestObj?.optString("price", "0") ?: "0"

                                gameList.add(Game(price, title, picture))
                            }

                            // FILTRO DE PREÇO
                            val filtro = gameList.filter { game ->
                                val value = game.price?.toDoubleOrNull() ?: 0.0
                                val okMin = minPrice?.let { value >= it } ?: true
                                val okMax = maxPrice?.let { value <= it } ?: true
                                okMin && okMax
                            }


                            runOnUiThread {
                                result.text = "Encontrados: ${gameList.size}"
                                recycler.adapter = GameAdapter(filtro.toMutableList())

                            }
                        }

                        else -> {
                            result.text = "ERRO: Preencha pelo menos um campo"

                        }
                    }
                }catch (e: Exception) {
                        runOnUiThread {
                            result.text = "ERRO DE CONEXÃO/DADOS: ${e.message}"
                        }
                    }
                }
            }
        }
    }