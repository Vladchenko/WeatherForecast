package com.example.weatherforecast.presentation

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.weatherforecast.R
import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import javax.inject.Inject

/**
 * Weather forecast main activity
 */
@AndroidEntryPoint
class WeatherForecastActivity : AppCompatActivity() {

    private lateinit var cityNameTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var degreesTypeTextView: TextView
    private lateinit var degreesValueTextView: TextView
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
        viewModel = ViewModelProvider(this, viewModelFactory).get(WeatherForecastViewModel::class.java)
        viewWeatherForecastData(TemperatureType.CELSIUS, "Kazan")    //TODO Get city name from some place. Some dropdown list or from cellphone local area ?
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

    private fun viewWeatherForecastData(temperatureType: TemperatureType, city: String) {
        viewModel.getWeatherForecast(temperatureType, city)
        viewModel.getWeatherForecastLiveData.observe(this) { showForecastData(it) }
        viewModel.showErrorLiveData.observe(this) { showError(it) }
    }

    private fun showForecastData(dataModel: WeatherForecastDomainModel) {
        hideProgressBar()
        dateTextView.text = getCurrentDate()
        cityNameTextView.text = dataModel.city
        degreesValueTextView.text = dataModel.temperature
        degreesTypeTextView.text = dataModel.temperatureType
        weatherImageView.setImageResource(getWeatherTypeIcon(dataModel.weatherType))
    }

    private fun showError(errorMessage: String) {
        hideProgressBar()
        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance()
        return formatter.format(date)
    }

    private fun getWeatherTypeIcon(weatherType: String) =
        resources.getIdentifier(ICON_PREFIX + weatherType.replace(" ", ""), DRAWABLE_RESOURCE_TYPE, packageName)

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private companion object {
        private const val ICON_PREFIX = "icon_"
        private const val DRAWABLE_RESOURCE_TYPE = "drawable"
    }
}