This app is based on udemy course
https://www.udemy.com/course/android-beginners-weather-forecast-app/
Project was amended with a DataBinding, Jetpack Navigation, Retrofit, Hilt, Room and MVVM.

API for the project - https://openweathermap.org/current
Test request on a following link - http://api.openweathermap.org/data/2.5/weather?q=Kazan&appid=a8f0e797059b7959399040200fd43231

JSON data to data-class conversion - https://app.quicktype.io/

TODO:
    - TemperatureType is passed all over around. Make it not so.
    - When no inet, one needs to show all the saved cities, but not a filtered ones.
    - Attempt to invoke virtual method 'java.lang.String com.example.weatherforecast.models.domain.WeatherForecastDomainModel.getCity()' on a null object reference
    - Show alert dialog instead of error in toolbar, when locating permission was not granted
    - When city is chosen and city forecast fragment shows up, old forecast is diplayed for some time
        - Emptying a views doesn't help
            - Tried in onViewCreated and in onPause
        ! Probably, one has to remove fragment
    - When permission is not granted (user tapped outside of permission alert), following app opening behaves like permission was given
    - When selecting a city and user taps back, the main fragment loads a Kazan forecast, but should just show a dialog on geolocation
    - isNetworkAvailable doesn't check network appearing fast enough

    - When network connection appears, app is not aware about that.
    - Put all texts to string.xml
    - Remove all superfluous Log.d
    - Check statuses
    - ? Center a toolbar texts
        Following attributes for toolbar and appbar layout do not help
                    android:foregroundGravity="center"
                    android:gravity="center"
                    android:textAlignment="center"
    - Make data source to return a data not domain model
    - Add "fog", "moderate rain", "light intensity shower rain", "heavy intensity rain" to weather images
    - Add unit tests