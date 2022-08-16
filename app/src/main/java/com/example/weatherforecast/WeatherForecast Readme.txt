This app is based on udemy course
https://www.udemy.com/course/android-beginners-weather-forecast-app/
Project was amended with a DataBinding, Jetpack Navigation, Retrofit, Hilt, Room and MVVM.

API for the project - https://openweathermap.org/current
Test request on a following link - http://api.openweathermap.org/data/2.5/weather?q=Kazan&appid=a8f0e797059b7959399040200fd43231

JSON data to data-class conversion - https://app.quicktype.io/

TODO:
    - Provide a city to alert dialog, when there is an exception 404.
    - SharedPrefs move to datasource and provide from DI with interface (CORRECT) to repo - interactor - viewmodel
    - When app starts for the very first time and one selects a city, then geolocation alert pops up, but should not
    - When city is chosen and city forecast fragment shows up, old forecast is diplayed for some time
        - Emptying a views doesn't help
            - Tried in onViewCreated and in onPause
        ! Probably, one has to remove fragment

    - Put all texts to string.xml
    - Remove all superfluous Log.d
    - Check statuses
    - ? Center a toolbar texts
        Following attributes for toolbar and appbar layout do not help
                    android:foregroundGravity="center"
                    android:gravity="center"
                    android:textAlignment="center"
    - Make data source to return a data not domain model
    - Add "light intensity shower rain", "heavy intensity rain" to weather images
    - Add unit tests