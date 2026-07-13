package io.github.vladchenko.weatherforecast.presentation.view.activities

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import io.github.vladchenko.weatherforecast.core.network.connectivity.ConnectivityObserver
import io.github.vladchenko.weatherforecast.core.resourcemanager.ResourceManager
import io.github.vladchenko.weatherforecast.core.ui.systembars.hideBottomNavigationBar
import io.github.vladchenko.weatherforecast.core.ui.systembars.setLightStatusBars
import io.github.vladchenko.weatherforecast.core.ui.systembars.setTransparentSystemBars
import io.github.vladchenko.weatherforecast.feature.citysearch.presentation.viewmodel.CitySearchViewModel
import io.github.vladchenko.weatherforecast.feature.currentweather.presentation.viewmodel.CurrentWeatherViewModel
import io.github.vladchenko.weatherforecast.feature.geolocation.data.permission.PermissionResolver
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallback
import io.github.vladchenko.weatherforecast.feature.geolocation.domain.GeoLocationCallbackEvent
import io.github.vladchenko.weatherforecast.feature.geolocation.presentation.viewmodel.GeoLocationViewModel
import io.github.vladchenko.weatherforecast.feature.hourlyforecast.presentation.viewmodel.HourlyWeatherViewModel
import io.github.vladchenko.weatherforecast.presentation.coordinator.NetworkStatusCoordinator
import io.github.vladchenko.weatherforecast.presentation.coordinator.WeatherCoordinator
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogController
import io.github.vladchenko.weatherforecast.presentation.dialog.WeatherDialogControllerImpl
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEvent
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEventDispatcher
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEventDispatcherImpl
import io.github.vladchenko.weatherforecast.presentation.navigation.WeatherAppNavHost
import io.github.vladchenko.weatherforecast.presentation.status.StatusRenderer
import io.github.vladchenko.weatherforecast.presentation.theme.WeatherForecastTheme
import io.github.vladchenko.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

/**
 * Main activity serving as the app's entry point.
 * Hosts navigation graph and manages system UI appearance.
 *
 * Key features:
 * Manages [WeatherAppNavHost] for screen navigation
 * Coordinates network connectivity via [NetworkStatusCoordinator]
 * Provides shared view models for weather, forecast, and city search
 * Configures status and navigation bars appearance
 */
@FlowPreview
@AndroidEntryPoint
class WeatherActivity : AppCompatActivity() {

    @Inject
    lateinit var workManager: WorkManager

    @Inject
    lateinit var connectivityObserver: ConnectivityObserver

    @Inject
    lateinit var statusRenderer: StatusRenderer

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var permissionResolver: PermissionResolver

    @Inject
    lateinit var dialogController: WeatherDialogController

    private val appBarViewModel: AppBarViewModel by viewModels()
    private val citySearchViewModel: CitySearchViewModel by viewModels()
    private val weatherViewModel: CurrentWeatherViewModel by viewModels()
    private val geoLocationViewModel: GeoLocationViewModel by viewModels()
    private val hourlyWeatherViewModel: HourlyWeatherViewModel by viewModels()

    private val networkCoordinatorRef: NetworkStatusCoordinator by lazy {
        NetworkStatusCoordinator(
            navController = navControllerRef,
            statusRenderer = statusRenderer,
            resourceManager = resourceManager,
            connectivityObserver = connectivityObserver,
            currentWeatherViewModel = weatherViewModel
        )
    }
    
    private val weatherCoordinatorRef: WeatherCoordinator by lazy {
        val geoLocationCallback = GeoLocationCallback { event ->
            when (event) {
                GeoLocationCallbackEvent.GotoCitySelection -> {
                    navigationDispatcher.navigate(NavigationEvent.NavigateToCitySelection())
                }
                GeoLocationCallbackEvent.RequestPermission -> {
                    permissionResolver.requestLocationPermission()
                }
                GeoLocationCallbackEvent.OnPermanentlyDenied,
                GeoLocationCallbackEvent.OnNegativeNoPermission -> {
                    finishAffinity()
                }
                is GeoLocationCallbackEvent.OnForecastLoadForLocation -> {
                    weatherViewModel.launchWeatherForecast(
                        event.locationModel.city,
                        event.locationModel.location.latitude,
                        event.locationModel.location.longitude
                    )
                }
            }
        }
        
        WeatherCoordinator.Factory().create(
            callback = geoLocationCallback,
            statusRenderer = statusRenderer,
            appBarViewModel = appBarViewModel,
            resourceManager = resourceManager,
            permissionResolver = permissionResolver,
            dialogController = dialogController,
            forecastViewModel = weatherViewModel,
            geoLocationViewModel = geoLocationViewModel
        )
    }
    
    private lateinit var navigationDispatcher: NavigationEventDispatcher
    private lateinit var navControllerRef: NavController
    
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            permissionResolver.handlePermissionResult(isGranted)
        }

    @ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Connect permission resolver to activity result launcher
        permissionResolver.connect(
            launcher = requestPermissionLauncher,
            onPermissionResult = { isGranted ->
                geoLocationViewModel.onPermissionResolution(isGranted)
            }
        )
        
        // Set Activity context for dialog controller to use AppCompat theme
        (dialogController as WeatherDialogControllerImpl).setActivityContext(this)
        
        setContent {
            val navController = rememberNavController()
            navigationDispatcher = NavigationEventDispatcherImpl(
                navController,
                {
                    this.finishAffinity()
                } )
            navControllerRef = navController
            // Initialize coordinators after navController is set
            initNetworkCoordinator()
            WeatherForecastTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeatherAppNavHost(
                        navController = navController,
                        appBarViewModel = appBarViewModel,
                        weatherViewModel = weatherViewModel,
                        hourlyViewModel = hourlyWeatherViewModel,
                        citySearchViewModel = citySearchViewModel,
                        navigationDispatcher = navigationDispatcher
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setTransparentSystemBars()
        setLightStatusBars(isLight = isLightTheme())
        hideBottomNavigationBar()
    }

    private fun initNetworkCoordinator() {
        lifecycle.addObserver(networkCoordinatorRef)
    }

    override fun onStart() {
        super.onStart()
        val scope = kotlinx.coroutines.CoroutineScope(SupervisorJob())
        weatherCoordinatorRef.startObserving(scope, lifecycle)
    }

    override fun onStop() {
        super.onStop()
        // Observation stops automatically when lifecycle goes to STOPPED state
    }

    private fun isLightTheme(): Boolean {
        val currentNightMode =
            resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode != android.content.res.Configuration.UI_MODE_NIGHT_YES
    }
}