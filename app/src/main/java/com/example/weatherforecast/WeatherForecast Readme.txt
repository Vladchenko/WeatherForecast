This app is based on udemy course
https://www.udemy.com/course/android-beginners-weather-forecast-app/
Project was amended with a DataBinding, Jetpack Navigation, Retrofit, Hilt, Room and MVVM.

API for the project - https://openweathermap.org/current
Test request on a following link - http://api.openweathermap.org/data/2.5/weather?q=Kazan&appid=a8f0e797059b7959399040200fd43231

JSON data to data-class conversion - https://app.quicktype.io/

TODO:
    - ! Make data source to return a data not domain model
    - Put connectionLiveData to DI
    - Add unit tests
    - When no internet
        - "Cannot get location" message shows up in about 10 secs (too long)
        - Once internet is back, app doesn't see that
    - When city is chosen and city forecast fragment shows up, old forecast is diplayed for some time
    - Add "heavy intensity rain" to weather images
    - Activity leaking when passed to geoLocator.getCityByLocation(activity as Activity, locationListener)