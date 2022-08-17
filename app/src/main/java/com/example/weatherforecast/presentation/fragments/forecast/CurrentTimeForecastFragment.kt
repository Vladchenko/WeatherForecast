package com.example.weatherforecast.presentation.fragments.forecast

import android.Manifest
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weatherforecast.R
import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.data.util.WeatherForecastUtils.getCurrentDate
import com.example.weatherforecast.databinding.FragmentCurrentTimeForecastBinding
import com.example.weatherforecast.geolocation.GeoLocationAlertDialogDelegate
import com.example.weatherforecast.geolocation.WeatherForecastGeoLocator
import com.example.weatherforecast.models.domain.CityLocationModel
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.presentation.PresentationUtils.animateFadeOut
import com.example.weatherforecast.presentation.PresentationUtils.getWeatherTypeIcon
import com.example.weatherforecast.presentation.PresentationUtils.setToolbarSubtitleFontSize
import com.example.weatherforecast.presentation.fragments.cityselection.CityApprovalAlertDialogListenerImpl
import com.example.weatherforecast.presentation.fragments.cityselection.CityClickListener
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

/**
 * Fragment representing a weather forecast for current time.
 */
@AndroidEntryPoint
class CurrentTimeForecastFragment : Fragment() {

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission())
    { isGranted ->
        if (isGranted) {
            viewModel.locateCityOrDownloadForecastData()
        } else {
            showError(getString(R.string.no_permission_app_cannot_proceed)) //TODO Show alert dialog instead
        }
    }

    private lateinit var chosenCity: String

    private val arguments: CurrentTimeForecastFragmentArgs by navArgs()
    private val viewModel by activityViewModels<WeatherForecastViewModel>()
    private var geoLocationAlertDialogDelegate: GeoLocationAlertDialogDelegate? = null
    private var temperatureType: TemperatureType = TemperatureType.CELSIUS

    private lateinit var fragmentDataBinding: FragmentCurrentTimeForecastBinding

    private lateinit var geoLocator: WeatherForecastGeoLocator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        chosenCity = arguments.chosenCity
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_current_time_forecast, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentDataBinding = FragmentCurrentTimeForecastBinding.bind(view)
        geoLocator = WeatherForecastGeoLocator(viewModel)   //TODO Is this instantiating correct ?

        fragmentDataBinding.cityNameTextView.setOnClickListener(
            CityClickListener(findNavController())
        )
        fragmentDataBinding.toolbar.title = getString(R.string.app_name)
        toggleProgressBar(true)

        initLiveDataObservers()

        viewModel.checkNetworkConnectionAvailability()
        viewModel.downloadWeatherForecast(chosenCity)
    }

    override fun onDestroy() {
        super.onDestroy()
        geoLocationAlertDialogDelegate?.dismissAlertDialog()
    }

    private fun initLiveDataObservers() {
        viewModel.getWeatherForecastLiveData.observe(viewLifecycleOwner) { showForecastData(it) }
        viewModel.showErrorLiveData.observe(viewLifecycleOwner) { showError(it) }
        viewModel.showStatusLiveData.observe(viewLifecycleOwner) { showStatus(it) }
        viewModel.showProgressBarLiveData.observe(viewLifecycleOwner) { toggleProgressBar(it) }
        viewModel.locationSuccessLiveData.observe(viewLifecycleOwner) { showGeoLocationAlertDialog(it) }
        viewModel.defineCityByGeoLocationLiveData.observe(viewLifecycleOwner) { locateCityByLatLong(it) }
        viewModel.requestPermissionLiveData.observe(viewLifecycleOwner) { requestLocationPermission() }
        viewModel.locateCityLiveData.observe(viewLifecycleOwner) { locateCityByGeoLocation() }
        viewModel.gotoCitySelectionLiveData.observe(viewLifecycleOwner) { gotoCitySelection() }
        viewModel.chooseAnotherCity.observe(viewLifecycleOwner) { showAlertDialogToChooseAnotherCity(it) }
    }

    private fun showForecastData(dataModel: WeatherForecastDomainModel) {
        showStatus(getString(R.string.forecast_for_city, dataModel.city))
        fragmentDataBinding.dateTextView.text = getCurrentDate(dataModel.date, getString(R.string.bad_date_format))
        fragmentDataBinding.cityNameTextView.text = dataModel.city
        fragmentDataBinding.degreesValueTextView.text = dataModel.temperature
        fragmentDataBinding.degreesTypeTextView.text = dataModel.temperatureType
        fragmentDataBinding.weatherTypeTextView.text = dataModel.weatherType
        fragmentDataBinding.weatherTypeImageView.visibility = View.VISIBLE
        fragmentDataBinding.weatherTypeImageView.setImageResource(
            getWeatherTypeIcon(resources, requireActivity().packageName, dataModel.weatherType)
        )
        toggleProgressBar(false)
    }

    private fun showError(errorMessage: String) {
        Log.e("CurrentTimeForecastFragment", errorMessage)
        toggleProgressBar(false)
        setToolbarSubtitleFontSize(fragmentDataBinding.toolbar, errorMessage)
        fragmentDataBinding.toolbar.subtitle = errorMessage
        fragmentDataBinding.toolbar.setBackgroundColor((activity as Context).getColor(R.color.colorAccent))
    }

    private fun showStatus(statusMessage: String) {
        Log.d("CurrentTimeForecastFragment", statusMessage)
        setToolbarSubtitleFontSize(fragmentDataBinding.toolbar, statusMessage)
        fragmentDataBinding.toolbar.subtitle = statusMessage
        fragmentDataBinding.toolbar.setBackgroundColor((activity as Context).getColor(R.color.colorPrimary))
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun locateCityByGeoLocation() {
        Log.d("CurrentTimeForecastFragment", "Locating city...")
        geoLocator.getCityByLocation(requireActivity())
    }

    private fun gotoCitySelection() {
        findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
    }

    private fun showAlertDialogToChooseAnotherCity(city: String) {
        CitySelectionAlertDialogDelegate(
            city,
            CityWrongAlertDialogListenerImpl(viewModel, requireContext())
        ).showAlertDialog(requireContext())
    }

    private fun locateCityByLatLong(location: Location) {
        val geoCoder = Geocoder(activity as Context, Locale.getDefault())
        val locality = geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
        viewModel.onGeoLocationSuccess(locality, location)
    }

    private fun toggleProgressBar(isVisible: Boolean) {
        if (isVisible) {
            fragmentDataBinding.progressBar.alpha = resources.getFloat(R.dimen.progressbar_background_transparency)
            fragmentDataBinding.progressBar.visibility = View.VISIBLE
        } else {
            animateFadeOut(
                fragmentDataBinding.progressBar,
                resources.getInteger(android.R.integer.config_mediumAnimTime)
            )
        }
    }

    private fun showGeoLocationAlertDialog(cityLocationModel: CityLocationModel) {
        Log.d("CurrentTimeForecastFragment", "showAlertDialog")
        geoLocationAlertDialogDelegate = GeoLocationAlertDialogDelegate(
            cityLocationModel.city,
            cityLocationModel.location,
            CityApprovalAlertDialogListenerImpl(viewModel, requireContext())
        )
        geoLocationAlertDialogDelegate?.showAlertDialog(requireContext())
    }

    companion object {
        const val CITY_ARGUMENT_KEY = "City argument"
        const val CITY_LATITUDE_ARGUMENT_KEY = "City latitude argument"
        const val CITY_LONGITUDE_ARGUMENT_KEY = "City longitude argument"
    }
}