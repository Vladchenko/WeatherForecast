package io.github.vladchenko.weatherforecast.feature.currentweather.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import dagger.hilt.android.AndroidEntryPoint
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.dialog.AlertDialogHelper
import io.github.vladchenko.weatherforecast.core.ui.navigation.WeatherNavigator
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.event.CurrentWeatherEvent
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.geolocation.data.permission.PermissionResolver
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallback
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallbackEvent
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationViewModel
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.coordinator.WeatherCoordinator
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogFactory
import io.github.vladchenko.weatherforecast.presentation.dialog.dialogcontroller.WeatherDialogControllerFactory
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.theme.WeatherForecastTheme
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import javax.inject.Inject

/**
 * Main weather screen fragment.
 *
 * Orchestrates weather data loading, location permission handling, and UI rendering:
 * - Collects [CurrentWeatherViewModel] and [HourlyWeatherViewModel] states
 * - Delegates UI rendering to [CurrentWeatherLayout] via Compose
 * - Coordinates geo-location, dialogs, and navigation via [WeatherCoordinator]
 * - Manages lifecycle-aware subscriptions using `collectAsStateWithLifecycle`
 *
 * ## Architecture
 * - Uses **activity-scoped ViewModels** shared across screens
 * - **No ViewModel passing to UI layout** — only immutable UI states (`appBarUiState`, `weatherUiState`, etc.)
 * - Delegates loading and events to ViewModels via callbacks (`onEvent`, `onLoadHourlyWeather`)
 * - Manages fragment lifecycle explicitly via `viewLifecycleOwner`
 *
 * ## Initialization flow
 * 1. [onCreateView] — Sets up Compose with [CurrentWeatherLayout], passing:
 *    - Immutable UI states (`appBarUiState`, `weatherUiState`, `refreshingState`, `hourlyForecastUiState`)
 *    - Callbacks for events: `onEvent = { ... }`, `onLoadHourlyWeather = { ... }`
 * 2. [onViewCreated] — Connects:
 *    - [WeatherNavigator] for navigation events
 *    - [PermissionResolver] for location permission handling
 *    - [WeatherCoordinator] to orchestrate geo-location, UI updates, and dialog flow
 * 3. Launches initial weather fetch after fragment enter animation completes (800ms delay)
 *
 * ## State management
 * - `weatherUiState`, `refreshingState`, `appBarUiState`, `hourlyForecastUiState` collected here
 * - `CurrentWeatherLayout` receives **plain immutable objects**, not `StateFlow`/`ViewModel`
 * - Ensures `CurrentWeatherLayout` is fully testable without ViewModel mocking
 */
@AndroidEntryPoint
class WeatherFragment : Fragment() {

    @Inject
    lateinit var statusRenderer: StatusRenderer

    @Inject
    lateinit var weatherCoordinatorFactory: WeatherCoordinator.Factory

    @Inject
    lateinit var permissionResolver: PermissionResolver

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var alertDialogFactory: WeatherDialogFactory

    private val args: WeatherFragmentArgs by navArgs()
    private val weatherViewModel: CurrentWeatherViewModel by activityViewModels()
    private val appBarViewModel: AppBarViewModel by activityViewModels()
    private val geoLocationViewModel: GeoLocationViewModel by activityViewModels()
    private val hourlyWeatherViewModel: HourlyWeatherViewModel by activityViewModels()

    private lateinit var coordinator: WeatherCoordinator
    private val navigator by lazy { WeatherNavigator(findNavController()) }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionResolver.handlePermissionResult(isGranted)
        }

    @ExperimentalMaterial3Api
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val weatherUiState by weatherViewModel.weatherStateFlow.collectAsStateWithLifecycle()
                val refreshingState by weatherViewModel.refreshingStateFlow.collectAsStateWithLifecycle()
                val appBarUiState by appBarViewModel.appBarUiStateFlow.collectAsStateWithLifecycle()
                val hourlyForecastUiState by hourlyWeatherViewModel.hourlyWeatherStateFlow.collectAsStateWithLifecycle()
                WeatherForecastTheme {
                    CurrentWeatherLayout(
                        appBarUiState = appBarUiState,
                        weatherUiState = weatherUiState,
                        refreshingState = refreshingState,
                        hourlyWeatherUiState = hourlyForecastUiState,
                        onEvent = { event -> weatherViewModel.onEvent(event) },
                        onLoadHourlyWeather = { data -> hourlyWeatherViewModel.loadHourlyWeatherForLocation(data) }
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navigator.start(viewLifecycleOwner, weatherViewModel.navigationEventFlow)

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
            object : GeoLocationCallback {
                override fun onEvent(event: GeoLocationCallbackEvent) {
                    when (event) {
                        GeoLocationCallbackEvent.GotoCitySelection -> weatherViewModel.onEvent(
                            CurrentWeatherEvent.NavigateToCitySelection
                        )
                        GeoLocationCallbackEvent.RequestPermission -> permissionResolver.requestLocationPermission()
                        GeoLocationCallbackEvent.OnPermanentlyDenied,
                        GeoLocationCallbackEvent.OnNegativeNoPermission -> activity?.finish()

                        is GeoLocationCallbackEvent.OnForecastLoadForLocation -> {
                            weatherViewModel.launchWeatherForecast(
                                event.locationModel.city,
                                event.locationModel.location.latitude,
                                event.locationModel.location.longitude
                            )
                        }
                    }
                }
            },
            forecastViewModel = weatherViewModel,
            appBarViewModel = appBarViewModel,
            geoLocationViewModel = geoLocationViewModel,
            statusRenderer = statusRenderer,
            dialogController = dialogController,
            resourceManager = resourceManager,
            permissionResolver = permissionResolver
        )

        coordinator.startObserving(viewLifecycleOwner.lifecycleScope, viewLifecycleOwner.lifecycle)

        // Delay to ensure fragment's enter animation completes before updating weather.
        // Prevents overlap between screen fade-in and content refresh animation.
        view.postDelayed({
            weatherViewModel.launchWeatherForecast(
                args.chosenCity,
                args.latitude.toDouble(),
                args.longitude.toDouble()
            )
        }, 800)
    }
}