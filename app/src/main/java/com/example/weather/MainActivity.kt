package com.example.weather

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.weather.databinding.ActivityMainBinding
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            if (binding.userField.getText().toString().trim { it <= ' ' } == "") {
                Toast.makeText(this@MainActivity, R.string.hint_user_field, Toast.LENGTH_LONG).show()
            } else {
                val city = binding.userField.getText().toString().trim { it <= ' ' }
                val apiKey = "6180aa68740f1feb44af9212742822ae"
                val url = "https://api.openweathermap.org/data/2.5/weather?q=$city&appid=$apiKey&units=metric"
                loadData(url)
            }
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(binding.userField.windowToken, 0)
        }
    }

    private fun loadData(urlStr: String) {
        binding.progressBar.visibility = View.VISIBLE
        scope.launch {
            kotlin.runCatching {
                val url = URL(urlStr)
                val connection = url.openConnection() as HttpURLConnection
                connection.connect()
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val stringBuffer = StringBuffer()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuffer.append(line).append("\n")
                }
                val resultStr = stringBuffer.toString()
                return@runCatching parseJson(resultStr)
            }.onSuccess {
                withContext(Dispatchers.Main) {
                    binding.result.text = it
                    binding.progressBar.visibility = View.GONE
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.result.text = "Ошибка!"
                }
                Log.d("ERROR", "CoroutineExceptionHandler got ${it.toString()}")
            }
        }
    }

    private fun parseJson(str: String): String {
        val jsonObject = JSONObject(str)
        val temp = "Температура: ${jsonObject.getJSONObject("main").getString("temp")}\n"
        val pressure = "Давление: ${jsonObject.getJSONObject("main").getString("pressure")}\n"
        val humidity = "Влажность: ${jsonObject.getJSONObject("main").getString("humidity")}\n"
        val speed = "Скорость ветра: ${jsonObject.getJSONObject("wind").getString("speed")}\n"
        return temp + pressure + humidity + speed
    }
}