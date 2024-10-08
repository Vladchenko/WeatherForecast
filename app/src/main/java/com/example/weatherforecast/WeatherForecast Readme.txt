This app is based on udemy course
https://www.udemy.com/course/android-beginners-weather-forecast-app/
Project was amended with a DataBinding, Jetpack Navigation, Retrofit, Hilt, Room and MVVM.

API for the project - https://openweathermap.org/current
Test request on a following link - http://api.openweathermap.org/data/2.5/weather?q=Kazan&appid=a8f0e797059b7959399040200fd43231

Autocompletetextview code sample taken from
https://www.devbitsandbytes.com/jetpack-compose-a-simple-opiniated-autocompletetextview/

JSON data to data-class conversion - https://app.quicktype.io/

Running a saving for a forecast and a chosen city in separate coroutines instead of one, drastically shortened these operations.
From 200-150ms to 3-1 msec. Measured using val time = measureTimeMillis { ...code to measure... }

TODO:
    - City picking:
        - Replace Result with sealed class just like in Forecast request
        - Save city names(tokens) to data base and load them when no inet
        - When no inet, one needs to show all the saved cities, but not a filtered ones.
    - Turn toolbar message yellow for cities responses
    - Replace LiveDatas with UIStates
    - ! Create custom widget in Jetpack Compose, since it interviewers ask this technology
    - When loaded remotely and no forecast in DB, an error is shown during this time.
        - Work out a case, when city not defined for first app running
    - Add a ? picture for a case when a weather type image is not defined
    - Implement swipeToRefresh
    - Move TemperatureType from DI to some settings providing mechanism. Probably a shared preferences with data-domain wrapping.

    - Fix such case of first app run:
        1) Dialog of local city recognition appeared, one taps OK
        2) Remote forecast for city downloading fails
        3) Local forecast for city downloading also fails, since no initial city saved in DB
        4) Next, app crashes, saying "city has to be not null". So, one has to show some status that both - remote and local failed. And maybe go to a city selection screen.
    - When forecast fails to download, sometimes it calls cities screen for 2 times
    - As for network connection availability, is there a way to remove double check ?
    - App doesn't ask for permission when I remove it, after it was granted before
    - 2022-09-20 15:12:57.064 1722-1722/? E/WeatherForecastViewModel: Kazan
      2022-09-20 15:12:57.065 1722-1722/? E/WeatherForecastViewModel: Downloading weather forecast for a city failed, trying for geo location
      2022-09-20 15:12:57.066 1722-1722/? E/WeatherForecastViewModel: com.example.weatherforecast.data.api.customexceptions.NoSuchDatabaseEntryException: Kazan
              at com.example.weatherforecast.data.repository.datasourceimpl.WeatherForecastLocalDataSourceImpl.loadWeatherForecastData(WeatherForecastLocalDataSourceImpl.kt:18)
              at com.example.weatherforecast.data.repository.WeatherForecastRepositoryImpl$loadLocalForecast$2.invokeSuspend(WeatherForecastRepositoryImpl.kt:62)

    - Remove all superfluous Log.d
    - Add "fog", "light intensity shower rain", "heavy intensity rain", "thunderstorm with light rain" to weather images
    - Add unit tests
    - Rename string.xml strings so that could match common pattern (related to city begin with "city_...")

    ?
    - "Ask about it"
    - Ask about new Exception in exception handler
    - Where to keep app settings? Shared prefs / XML / else ?
    - Should one process a database exceptions
    - Should settings be kept in a separate storage (shared prefs in data->domain->presentation)
    CurrentTimeForecastFragment
        - What can be moved out of it and how
    ViewModel
        - Try turning AndroidViewModel into ViewModel
        - What can be moved out of it and how
        - Should AlertDialogs refer to interface, but not to viewModel itself
            ! No need, since there is only one implementation of viewModel exists

    - Center a toolbar texts (not a nice idea, after all, I suppose)
                Following attributes for toolbar and appbar layout do not help
                            android:foregroundGravity="center"
                            android:gravity="center"
                            android:textAlignment="center"

   Possiblу resolved (check if to appear later):
        - When app runs for first time, selecting a city and taping back, loads a Kazan forecast, but should just show a dialog on geolocation
        - When app is installed from scratch, it loads a Kazan city from DB, but DB is supposed to be empty.
        - Sometimes geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
                throws IOException: dhmg: DEADLINE_EXCEEDED: Deadline exceeded after 4.999926154s.
        - WindowLeaked: Activity com.example.weatherforecast.presentation.WeatherForecastActivity has leaked window DecorView@f309362[WeatherForecastActivity] that was originally added here
                at android.view.ViewRootImpl.<init>(ViewRootImpl.java:797), when device location permission is asked
        - Location definition alert dialog is shown twice


   INFO:
        - WorkManager to work correctly should have this app to run at least once and save a triangulated or typed city to database.
        WorkManager is only injected in WeatherForecastActivity, but not used. The purpose here is just to launch it and its launch performs downloading and saving of forecast to database.

//            NetworkMonitor(app.applicationContext, this@WeatherForecastViewModel)
            // Since NetworkMonitor doesn't check if app started with no inet, following check is required
//            if (!isNetworkAvailable(app.applicationContext)) {  //TODO Ask about it
//                requestGeoLocationPermissionOrDownloadWeatherForecast()
//            }

    override fun onNetworkConnectionAvailable() {
        Log.d("NetworkConnectionViewModel", "onNetworkConnectionAvailable")
        _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.network_available_text))
        requestGeoLocationPermissionOrDownloadWeatherForecast()
    }

    override fun onNetworkConnectionLost() {
        Log.d("NetworkConnectionViewModel", "onNetworkConnectionLost")
        _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
        val city = chosenCity.ifBlank {
            savedCity
        }
        downloadWeatherForecastForCityOrGeoLocation(city)
    }

    /**
     * Download a local weather forecast for a [city].
     */
    private fun downloadLocalForecast(city: String) {
        viewModelScope.launch(exceptionHandler) {
            val result = weatherForecastLocalInteractor.loadForecast(city)
            _onLocalForecastDownloadedLiveData.postValue(result)
            _onShowWeatherForecastLiveData.postValue(result)
            Log.d("WeatherForecastViewModel", "Local forecast downloaded $result")
            _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.database_forecast_downloading))
        }
    }

    private fun downloadForecastWhenNetworkAvailable(chosenCity: String) {
        if (chosenCity.isNotBlank()) {
            Log.d("WeatherForecastViewModel", "Chosen city is $chosenCity")
            downloadWeatherForecastForCityOrGeoLocation(chosenCity)
        } else {
            Log.d("WeatherForecastViewModel", "Chosen city is empty")
            // Try loading a city model from DB
            viewModelScope.launch(exceptionHandler) {
                val cityModel = chosenCityInteractor.loadChosenCityModel()
                Log.d(
                    "WeatherForecastViewModel",
                    app.applicationContext.getString(
                        R.string.database_city_loaded,
                        cityModel.city,
                        cityModel.location.latitude,
                        cityModel.location.longitude
                    )
                )
                // When there is no loaded city model from database,
                if (cityModel.city.isBlank()) {
                    // Define local city and try downloading a forecast for it
                    _onUpdateStatusLiveData.postValue(app.applicationContext.getString(R.string.current_location_defining_text))
                    _onShowProgressBarLiveData.postValue(true)
                    // Following row defines a city and displays an alert
                    defineCurrentGeoLocation()
                } else {
                    downloadWeatherForecastForCityOrGeoLocation(cityModel.city)
                }
            }
        }
    }

    private fun downloadForecastWhenNetworkNotAvailable(chosenCity: String) {
        if (chosenCity.isBlank()) {
            // Try loading a city model from DB
            viewModelScope.launch(exceptionHandler) {
                val cityModel = chosenCityInteractor.loadChosenCityModel()
                Log.d(
                    "WeatherForecastViewModel",
                    app.applicationContext.getString(
                        R.string.database_entry_loaded,
                        cityModel.city,
                        cityModel.location.latitude,
                        cityModel.location.longitude
                    )
                )
                // If it is null, show error
                if (cityModel.city.isBlank()) {
                    _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
                } else {
                    // Else download a forecast from database
                    downloadLocalForecast(cityModel.city)
                }
            }
        } else {
            downloadLocalForecast(chosenCity)
        }
    }

    fun onNetworkNotAvailable(hasPermissionForGeoLocation: Boolean, city: String) {
        if (!isNetworkAvailable(app.applicationContext)) {
            if (!hasPermissionForGeoLocation) {
                _onShowErrorLiveData.postValue(app.applicationContext.getString(R.string.network_not_available_error_text))
            } else {
                downloadWeatherForecastForCityOrGeoLocation(city)
            }
        }
    }

    private fun defineCityByGeoLocation(location: Location) = lifecycleScope.launchWhenCreated {
        val locality = geolocationHelper.loadCityByLocation(location)
        Log.d("CurrentTimeForecastFragment", "City for $location is defined as $locality")
        forecastViewModel.onDefineCityByGeoLocationSuccess(locality, location)
    }