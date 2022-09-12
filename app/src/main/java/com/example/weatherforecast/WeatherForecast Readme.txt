This app is based on udemy course
https://www.udemy.com/course/android-beginners-weather-forecast-app/
Project was amended with a DataBinding, Jetpack Navigation, Retrofit, Hilt, Room and MVVM.

API for the project - https://openweathermap.org/current
Test request on a following link - http://api.openweathermap.org/data/2.5/weather?q=Kazan&appid=a8f0e797059b7959399040200fd43231

JSON data to data-class conversion - https://app.quicktype.io/

TODO:
    - When app run for first time, selecting a city and taping back, loads a Kazan forecast, but should just show a dialog on geolocation
    - Sometimes geoCoder.getFromLocation(location.latitude, location.longitude, 1).first().locality
    throws IOException: dhmg: DEADLINE_EXCEEDED: Deadline exceeded after 4.999926154s.
        - Ask Alexey if solution for this is ok
    - When app is installed from scratch, it loads a Kazan city from DB, but DB is supposed to be empty.
    - When no inet, one needs to show all the saved cities, but not a filtered ones.
    - isNetworkAvailable doesn't check network appearing fast enough
    - When forecast fails to download, sometimes it calls cities screen for 2 times
    - As for network connection availability, is there a way to remove double check ?

    - Put all texts to string.xml
    - Remove all superfluous Log.d
    - Make data source to return a data not domain model
    - Add "fog", "moderate rain", "light intensity shower rain", "heavy intensity rain" to weather images
    - Add unit tests

    ?
    - Where to keep app settings? Shared prefs / XML / else ?
    - When I try add coroutines to geo location in view model, it says "inappropriate blocking method call"
    - Should one process a database exceptions
    - Should settings be kept in a separate storage (shared prefs in data->domain->presentation)
    CurrentTimeForecastFragment
        - geoLocator = WeatherForecastGeoLocator(viewModel)   //TODO Is this instantiating correct ?
        - What can be moved out of it and how
    ViewModel
        - What can be moved out of it and how
        - Should AlertDialogs refer to interface, but not to viewModel itself

    - Center a toolbar texts (not a nice idea, after all, I suppose)
                Following attributes for toolbar and appbar layout do not help
                            android:foregroundGravity="center"
                            android:gravity="center"
                            android:textAlignment="center"