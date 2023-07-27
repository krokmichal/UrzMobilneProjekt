// Importowanie odpowiednich klas i pakietów
package com.example.myapplication

import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.EditText
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

// Klasa głównego ekranu aplikacji, dziedziczy po AppCompatActivity
class MainActivity : AppCompatActivity() {

    // Zmienna przechowująca nazwę miasta, domyślnie ustawiona na "London,GB"
    var city: String = "London,GB"

    // Klucz API do OpenWeatherMap
    val MY_API_KEY: String = "e2eb1bb37b5d2264a4a2402e1a795c25"

    // Metoda wywoływana przy tworzeniu ekranu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicjalizacja widoków z pliku layout.xml
        val cityInput: EditText = findViewById(R.id.cityInput)
        val getWeatherButton: Button = findViewById(R.id.getWeatherButton)

        // Ustawienie listenera dla przycisku "Get Weather"
        getWeatherButton.setOnClickListener {
            // Pobranie nazwy miasta wprowadzonej przez użytkownika z pola EditText
            city = cityInput.text.toString()
            // Pokazanie widoków pola EditText i przycisku "Get Weather"
            cityInput.visibility = View.VISIBLE
            getWeatherButton.visibility = View.VISIBLE
            // Wywołanie metody do pobrania danych pogodowych
            getWeather()
        }

        // Wywołanie metody do pobrania początkowych danych pogodowych na ekranie
        getWeather()
    }

    // Metoda do pobierania danych pogodowych
    private fun getWeather() {
        // Utworzenie i wykonanie zadania AsyncTask, które pobierze dane pogodowe w tle
        weatherTask().execute(city)
    }

    // AsyncTask, który pobiera dane pogodowe w tle
    inner class weatherTask() : AsyncTask<String, Void, String>() {

        // Metoda wykonywana przed rozpoczęciem zadania
        override fun onPreExecute() {
            super.onPreExecute()
            /* Wyświetlenie paska postępu i ukrycie głównego widoku, aby zasygnalizować użytkownikowi, że dane są właśnie pobierane */
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.GONE
            findViewById<TextView>(R.id.errorText).visibility = View.GONE
        }

        // Metoda do pobrania danych pogodowych w tle, przyjmuje miasto jako parametr
        override fun doInBackground(vararg params: String?): String? {
            var response: String?
            try {
                // Pobranie danych pogodowych z API OpenWeatherMap za pomocą klucza API i nazwy miasta
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$MY_API_KEY").readText(
                    Charsets.UTF_8
                )
            } catch (e: Exception) {
                // W przypadku błędu zwróci null
                response = null
            }
            return response
        }

        // Metoda wywoływana po zakończeniu zadania, parametr "result" zawiera odpowiedź z API
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Parsowanie danych JSON z odpowiedzi API */
                val jsonObj = JSONObject(result)
                val main = jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)

                // Wydobycie i formatowanie danych pogodowych
                val updatedAt: Long = jsonObj.getLong("dt")
                val updatedAtText = "Updated at: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt * 1000))
                val temp = main.getString("temp") + "°C"
                val tempMin = "Min Today: " + main.getString("temp_min") + "°C"
                val tempMax = "Max Today: " + main.getString("temp_max") + "°C"
                val pressure = main.getString("pressure")
                val humidity = main.getString("humidity")

                val sunrise: Long = sys.getLong("sunrise")
                val sunset: Long = sys.getLong("sunset")
                val windSpeed = wind.getString("speed")
                val weatherDescription = weather.getString("description")

                val address = jsonObj.getString("name") + ", " + sys.getString("country")

                /* Wypełnianie widoków danymi pogodowymi */
                findViewById<TextView>(R.id.address).text = address
                findViewById<TextView>(R.id.updated_at).text = updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise * 1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset * 1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity

                /* Wyświetlanie widoków z danymi pogodowymi i ukrycie paska postępu */
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE

            } catch (e: Exception) {
                // W przypadku błędu wyświetlenie informacji o tym błędzie
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }
        }
    }
}
