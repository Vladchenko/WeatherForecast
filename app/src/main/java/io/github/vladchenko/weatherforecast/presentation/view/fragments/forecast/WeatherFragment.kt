package io.github.vladchenko.weatherforecast.presentation.view.fragments.forecast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.location.permission.PermissionResolver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogHelper
import io.github.vladchenko.weatherforecast.core.ui.navigation.WeatherNavigator
import io.github.vladchenko.weatherforecast.presentation.coordinator.WeatherCoordinator
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogFactory
import io.github.vladchenko.weatherforecast.presentation.dialog.dialogcontroller.WeatherDialogControllerFactory
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.themeColor
import io.github.vladchenko.weatherforecast.presentation.view.fragments.forecast.current.CurrentWeatherLayout
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.forecast.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.forecast.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.viewmodel.geolocation.GeoLocationViewModel
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
    @Inject lateinit var alertDialogFactory: WeatherDialogFactory

    private val args: WeatherFragmentArgs by navArgs()
    private val forecastViewModel: CurrentWeatherViewModel by activityViewModels()
    private val appBarViewModel: AppBarViewModel by activityViewModels()
    private val geoLocationViewModel: GeoLocationViewModel by activityViewModels()
    private val hourlyWeatherViewModel: HourlyWeatherViewModel by activityViewModels()

    private lateinit var coordinator: WeatherCoordinator
    private val navigator by lazy { WeatherNavigator(findNavController()) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionResolver.handlePermissionResult(isGranted)
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                CurrentWeatherLayout(
                    mainContentTextColor = themeColor(R.attr.colorMainText),
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
        ).create()

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

        // Delay to ensure fragment's enter animation completes before updating weather.
        // Prevents overlap between screen fade-in and content refresh animation.
        view.postDelayed({
            forecastViewModel.launchWeatherForecast(args.chosenCity, args.latitude, args.longitude)
        }, 800)
    }

    private fun gotoCitySelectionScreen() {
        navigator.navigateToCitySelection()
    }
}