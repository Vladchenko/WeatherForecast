package com.example.weatherforecast.presentation

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.R
import com.example.weatherforecast.data.models.Main
import com.example.weatherforecast.data.util.Resource
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject
import kotlin.math.roundToInt

/**
 * Weather forecast main activity
 */
@AndroidEntryPoint
class WeatherForecastActivity : AppCompatActivity() {

    private enum class TemperatureType {
        CELSIUS,
        FAHRENHEIT
    }

    private lateinit var cityNameTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var degreesTypeTextView: TextView
    private lateinit var degreesValueTextView: TextView
    private lateinit var temperatureType: TemperatureType
    private lateinit var weatherTypeTextView: TextView
    private lateinit var weatherImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: WeatherForecastViewModel

    @Inject
    lateinit var viewModelFactory: WeatherForecastViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.weather_forecast_activity)
        initViews()
        temperatureType = TemperatureType.CELSIUS
        viewModel = ViewModelProvider(this, viewModelFactory).get(WeatherForecastViewModel::class.java)
        viewWeatherForecastData("Kazan")    //TODO Get city name from some place. Some dropdown list or from cellphone local area ?
    }

    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        dateTextView = findViewById(R.id.date_text_view)
        cityNameTextView = findViewById(R.id.city_name_text_view)
        weatherImageView = findViewById(R.id.weather_type_image_view)
        degreesTypeTextView = findViewById(R.id.degrees_type_text_view)
        weatherTypeTextView = findViewById(R.id.weather_type_text_view)
        degreesValueTextView = findViewById(R.id.degrees_value_text_view)
    }

    private fun viewWeatherForecastData(city: String) {
        viewModel.getWeatherForecast(city)
        viewModel.getWeatherForecastLiveData.observe(this, Observer {
            when (it) {
                is Resource.Success -> {
                    hideProgressBar()
                    dateTextView.text = getCurrentDate()
                    cityNameTextView.text = city
                    viewDegreesValueAndType(it.data.main)
                    weatherImageView.setImageResource(getWeatherTypeIcon(it.data.weather[0].description))
                }
                is Resource.Error -> {
                    hideProgressBar()
                    it.exception.message?.let { errorMessage ->
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun getCelsiusFromKelvinTemperature(kelvinTemp: Double) = kelvinTemp - 273.15

    private fun getFahrenheitFromKelvinTemperature(kelvinTemp: Double) = 1.8 * (kelvinTemp - 273) + 32.0

    private fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance()
        return formatter.format(date)
    }

    private fun getWeatherTypeIcon(weatherType: String) =
        resources.getIdentifier(ICON_PREFIX + weatherType.replace(" ", ""), DRAWABLE_RESOURCE_TYPE, packageName)

    private fun viewDegreesValueAndType(resourceMain: Main) {
        // val kelvinTemperature = response.getJSONObject("main").getDouble("temp")
        val kelvinTemperature = resourceMain.temp
        if (temperatureType == TemperatureType.CELSIUS) {
            degreesValueTextView.text =
                getCelsiusFromKelvinTemperature(kelvinTemperature).roundToInt().toString()
            degreesTypeTextView.text = "℃"
        } else {
            degreesValueTextView.text =
                getFahrenheitFromKelvinTemperature(kelvinTemperature).roundToInt().toString()
            degreesTypeTextView.text = "℉"
        }
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
    }

    private companion object {
        private const val ICON_PREFIX = "icon_"
        private const val DRAWABLE_RESOURCE_TYPE = "drawable"
    }
}