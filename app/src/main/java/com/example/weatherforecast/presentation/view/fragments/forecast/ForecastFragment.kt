package com.example.weatherforecast.presentation.view.fragments.forecast

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper
import com.example.weatherforecast.presentation.alertdialog.ForecastDialogControllerFactory
import com.example.weatherforecast.presentation.coordinator.ForecastCoordinator
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.view.fragments.forecast.current.CurrentTimeForecastLayout
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModel
import com.example.weatherforecast.utils.ResourceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.system.exitProcess

/**
 * Fragment representing a weather forecast.
 * Orchestration delegated to Hilt-provided factories and their products.
 */
@AndroidEntryPoint
class ForecastFragment : Fragment() {

    @Inject lateinit var statusRendererFactory: StatusRenderer.Factory
    @Inject lateinit var forecastCoordinatorFactory: ForecastCoordinator.Factory
    @Inject lateinit var resourceManager: ResourceManager

    private var mainView: View? = null
    private val arguments: ForecastFragmentArgs by navArgs()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission())
        { isGranted ->
            geoLocationViewModel.onPermissionResolution(isGranted)
        }
    private val forecastViewModel by activityViewModels<WeatherForecastViewModel>()
    private val appBarViewModel by activityViewModels<AppBarViewModel>()
    private val geoLocationViewModel by activityViewModels<GeoLocationViewModel>()
    private val hourlyForecastViewModel by activityViewModels<HourlyForecastViewModel>()

    private lateinit var statusRenderer: StatusRenderer
    private lateinit var coordinator: ForecastCoordinator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            // Пока нет данных — можно показать заглушку или пустой экран
            setContent {
                // Будет обновляться динамически в onViewCreated
                // TODO Отобразить заглушку
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mainView = view
        super.onViewCreated(view, savedInstanceState)
        val alertDialogHelper = AlertDialogHelper(requireActivity())
        val dialogController = ForecastDialogControllerFactory(alertDialogHelper)
            .create(requireActivity() as AppCompatActivity)

        statusRenderer = statusRendererFactory.create(appBarViewModel)
        coordinator = forecastCoordinatorFactory.create(
            forecastViewModel = forecastViewModel,
            appBarViewModel = appBarViewModel,
            geoLocationViewModel = geoLocationViewModel,
            hourlyForecastViewModel = hourlyForecastViewModel,
            statusRenderer = statusRenderer,
            dialogController = dialogController,
            resourceManager = resourceManager,
            onGotoCitySelection = { gotoCitySelectionScreen() },
            onRequestLocationPermission = { requestLocationPermission() },
            onPermanentlyDenied = { showToastAndOpenAppSettings() },
            onNegativeNoPermission = { activity?.finish() }
        )
        coordinator.startObserving(viewLifecycleOwner.lifecycleScope, viewLifecycleOwner.lifecycle)
        val chosenCity = arguments.chosenCity
        statusRenderer.showDownloadingStatusFor(chosenCity)
        forecastViewModel.launchWeatherForecast(chosenCity)

        (view as ComposeView).setContent {
            CurrentTimeForecastLayout(
                mainContentTextColor = Color.Black,
                onCityClick = {
                    gotoCitySelectionScreen()
                },
                onBackClick = {
                    activity?.finish()
                },
                appBarViewModel = appBarViewModel,
                viewModel = forecastViewModel,
                hourlyViewModel = hourlyForecastViewModel
            )
        }
    }

    private fun requestLocationPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun showToastAndOpenAppSettings() {
        val intent = Intent(
            ACTION_APPLICATION_DETAILS_SETTINGS,
            ("package:" + activity?.packageName).toUri()
        )
        startActivity(intent)
        // TODO replace with alert dialog
        showToast(getString(R.string.geo_location_permission_denied))
        showToast(getString(R.string.geo_location_permission_denied))
        showToast(getString(R.string.geo_location_permission_denied))
        exitProcess(0)
    }

    private fun showToast(toastMessage: String) {
        Toast.makeText(
            requireContext(),
            toastMessage,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun gotoCitySelectionScreen() {
        findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
    }
}