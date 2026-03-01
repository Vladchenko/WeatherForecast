package com.example.weatherforecast.presentation.view.fragments.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.weatherforecast.R
import com.example.weatherforecast.geolocation.PermissionResolver
import com.example.weatherforecast.presentation.alertdialog.AlertDialogFactory
import com.example.weatherforecast.presentation.alertdialog.AlertDialogHelper
import com.example.weatherforecast.presentation.alertdialog.dialogcontroller.WeatherDialogControllerFactory
import com.example.weatherforecast.presentation.coordinator.WeatherCoordinator
import com.example.weatherforecast.presentation.status.StatusRenderer
import com.example.weatherforecast.presentation.view.fragments.forecast.current.CurrentWeatherLayout
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.CurrentWeatherViewModel
import com.example.weatherforecast.presentation.viewmodel.forecast.HourlyWeatherViewModel
import com.example.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModel
import com.example.weatherforecast.utils.ResourceManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Fragment representing a weather forecast.
 */
@AndroidEntryPoint
class WeatherFragment : Fragment() {

    @Inject lateinit var statusRenderer: StatusRenderer
    @Inject lateinit var weatherCoordinatorFactory: WeatherCoordinator.Factory
    @Inject lateinit var permissionResolver: PermissionResolver
    @Inject lateinit var resourceManager: ResourceManager
    @Inject lateinit var alertDialogFactory: AlertDialogFactory

    private val args: WeatherFragmentArgs by navArgs()
    private val forecastViewModel: CurrentWeatherViewModel by activityViewModels()
    private val appBarViewModel: AppBarViewModel by activityViewModels()
    private val geoLocationViewModel: GeoLocationViewModel by activityViewModels()
    private val hourlyWeatherViewModel: HourlyWeatherViewModel by activityViewModels()

    private lateinit var coordinator: WeatherCoordinator

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionResolver.handlePermissionResult(isGranted)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CurrentWeatherLayout(
                    mainContentTextColor = Color.Black,
                    onCityClick = { gotoCitySelectionScreen() },
                    onBackClick = { activity?.finish() },
                    appBarViewModel = appBarViewModel,
                    viewModel = forecastViewModel,
                    hourlyViewModel = hourlyWeatherViewModel
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        permissionResolver.connect(
            launcher = requestPermissionLauncher,
            onPermissionResult = { isGranted ->
                geoLocationViewModel.onPermissionResolution(isGranted)
            }
        )

        val dialogController = WeatherDialogControllerFactory(
            alertDialogFactory,
            AlertDialogHelper(requireActivity())
        )
            .create()

        coordinator = weatherCoordinatorFactory.create(
            forecastViewModel = forecastViewModel,
            appBarViewModel = appBarViewModel,
            geoLocationViewModel = geoLocationViewModel,
            statusRenderer = statusRenderer,
            dialogController = dialogController,
            resourceManager = resourceManager,
            permissionResolver = permissionResolver,
            onGotoCitySelection = { gotoCitySelectionScreen() },
            onRequestLocationPermission = { permissionResolver.requestLocationPermission() },
            onNegativeNoPermission = { activity?.finish() },
            onPermanentlyDenied = { activity?.finish() }
        )

        coordinator.startObserving(viewLifecycleOwner.lifecycleScope, viewLifecycleOwner.lifecycle)

        statusRenderer.showLoadingStatusFor(args.chosenCity)
        forecastViewModel.launchWeatherForecast(args.chosenCity)
    }

    private fun gotoCitySelectionScreen() {
        findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
    }
}