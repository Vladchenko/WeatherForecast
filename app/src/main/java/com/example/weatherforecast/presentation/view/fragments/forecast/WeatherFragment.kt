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

    @Inject lateinit var statusRendererFactory: StatusRenderer.Factory
    @Inject lateinit var weatherCoordinatorFactory: WeatherCoordinator.Factory
    @Inject lateinit var permissionResolver: PermissionResolver
    @Inject lateinit var resourceManager: ResourceManager
    @Inject lateinit var alertDialogFactory: AlertDialogFactory

    private var mainView: View? = null
    private val args: WeatherFragmentArgs by navArgs()

    private val forecastViewModel by activityViewModels<CurrentWeatherViewModel>()
    private val appBarViewModel by activityViewModels<AppBarViewModel>()
    private val geoLocationViewModel by activityViewModels<GeoLocationViewModel>()
    private val hourlyWeatherViewModel by activityViewModels<HourlyWeatherViewModel>()

    private lateinit var statusRenderer: StatusRenderer
    private lateinit var coordinator: WeatherCoordinator

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionResolver.handlePermissionResult(isGranted)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainView = view

        permissionResolver.connect(
            launcher = requestPermissionLauncher,
            onPermissionResult = { isGranted ->
                geoLocationViewModel.onPermissionResolution(isGranted)
            }
        )

        val alertDialogHelper = AlertDialogHelper(requireActivity())
        val dialogController = WeatherDialogControllerFactory(alertDialogFactory, alertDialogHelper)
            .create()

        statusRenderer = statusRendererFactory.create(appBarViewModel)

        coordinator = weatherCoordinatorFactory.create(
            forecastViewModel = forecastViewModel,
            appBarViewModel = appBarViewModel,
            geoLocationViewModel = geoLocationViewModel,
            hourlyWeatherViewModel = hourlyWeatherViewModel,
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

        statusRenderer.showDownloadingStatusFor(args.chosenCity)
        forecastViewModel.launchWeatherForecast(args.chosenCity)

        (view as ComposeView).setContent {
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

    private fun gotoCitySelectionScreen() {
        findNavController().navigate(R.id.action_currentTimeForecastFragment_to_citiesNamesFragment)
    }

    override fun onDestroyView() {
        mainView = null
        super.onDestroyView()
    }
}