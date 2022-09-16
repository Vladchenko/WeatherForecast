This app is based on udemy course
https://www.udemy.com/course/android-beginners-weather-forecast-app/
Project was amended with a DataBinding, Jetpack Navigation, Retrofit, Hilt, Room and MVVM.

API for the project - https://openweathermap.org/current
Test request on a following link - http://api.openweathermap.org/data/2.5/weather?q=Kazan&appid=a8f0e797059b7959399040200fd43231

JSON data to data-class conversion - https://app.quicktype.io/

TODO:
    - When app runs for first time, selecting a city and taping back, loads a Kazan forecast, but should just show a dialog on geolocation
    - When app is installed from scratch, it loads a Kazan city from DB, but DB is supposed to be empty.
    - When no inet, one needs to show all the saved cities, but not a filtered ones.
    - When forecast fails to download, sometimes it calls cities screen for 2 times
    - As for network connection availability, is there a way to remove double check ?
    - Sometimes geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
        throws IOException: dhmg: DEADLINE_EXCEEDED: Deadline exceeded after 4.999926154s.
            - Seems ok
    - WindowLeaked: Activity com.example.weatherforecast.presentation.WeatherForecastActivity has leaked window DecorView@f309362[WeatherForecastActivity] that was originally added here
              at android.view.ViewRootImpl.<init>(ViewRootImpl.java:797), when device location permission is asked
    - App doesn't ask for permission when I remove it, after it was granted before

    - Put all texts to string.xml
    - Remove all superfluous Log.d
    - Make data source to return a data not domain model
    - Add "fog", "moderate rain", "light intensity shower rain", "heavy intensity rain" to weather images
    - Add unit tests

    ?
    - "Ask about it"
    - Ask about new Exception in exception handler
    - Network monitor is singleton in DI, but used for 2 viewmodels. Is there a conflict ?
    - Where to keep app settings? Shared prefs / XML / else ?
    - Should one process a database exceptions
    - Should settings be kept in a separate storage (shared prefs in data->domain->presentation)
    CurrentTimeForecastFragment
        - geoLocator = WeatherForecastGeoLocator(viewModel)   //TODO Is this instantiating correct ?
        - What can be moved out of it and how
    ViewModel
        - What can be moved out of it and how
        - Should AlertDialogs refer to interface, but not to viewModel itself
            ! No need, since there is only one implementation of viewModel exists

    - Center a toolbar texts (not a nice idea, after all, I suppose)
                Following attributes for toolbar and appbar layout do not help
                            android:foregroundGravity="center"
                            android:gravity="center"
                            android:textAlignment="center"


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