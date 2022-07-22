package com.example.weatherforecast.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.weatherforecast.R
import com.example.weatherforecast.data.models.WeatherForecastDomainModel
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.presentation.viewmodel.WeatherForecastViewModel
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * Fragment displaying a weather forecast for current time.
 */
class CurrentTimeForecastFragment : Fragment() {

    private lateinit var cityNameTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var degreesTypeTextView: TextView
    private lateinit var degreesValueTextView: TextView
    private lateinit var weatherTypeTextView: TextView
    private lateinit var weatherImageView: ImageView
    private lateinit var progressBar: ProgressBar

    private lateinit var viewModel: WeatherForecastViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_current_time_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        viewModel = (activity as WeatherForecastActivity).viewModel
        //TODO Get city name from some place. Some dropdown list or from cellphone local area ?
        viewWeatherForecastData(TemperatureType.CELSIUS, "Казань")  //Kazan will also do
    }

    private fun initViews() {
        progressBar = (activity as WeatherForecastActivity).findViewById(R.id.progressBar)
        dateTextView = requireActivity().findViewById(R.id.date_text_view)
        cityNameTextView = requireActivity().findViewById(R.id.city_name_text_view)
        weatherImageView = requireActivity().findViewById(R.id.weather_type_image_view)
        degreesTypeTextView = requireActivity().findViewById(R.id.degrees_type_text_view)
        weatherTypeTextView = requireActivity().findViewById(R.id.weather_type_text_view)
        degreesValueTextView = requireActivity().findViewById(R.id.degrees_value_text_view)
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
        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun getCurrentDate(): String {
        val date = Calendar.getInstance().time
        val formatter = SimpleDateFormat.getDateInstance()
        return formatter.format(date)
    }

    private fun getWeatherTypeIcon(weatherType: String) =
        resources.getIdentifier(
            ICON_PREFIX + weatherType.replace(" ", ""),
            DRAWABLE_RESOURCE_TYPE,
            requireActivity().packageName
        )

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
    }

    private companion object {
        private const val ICON_PREFIX = "icon_"
        private const val DRAWABLE_RESOURCE_TYPE = "drawable"
    }
}