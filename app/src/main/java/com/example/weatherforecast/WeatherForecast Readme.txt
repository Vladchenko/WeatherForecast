This app is based on udemy course
https://www.udemy.com/course/android-beginners-weather-forecast-app/
Project was amended with a DataBinding, Jetpack Navigation, Retrofit, Hilt, Room and MVVM.

API for the project - https://openweathermap.org/current
Test request on a following link - http://api.openweathermap.org/data/2.5/weather?q=Kazan&appid=a8f0e797059b7959399040200fd43231

JSON data to data-class conversion - https://app.quicktype.io/

TODO:
    - Add unit tests
    - When no internet, initial message shows up in about 10 secs (too long)
    - When city is chosen and city forecast fragment shows up, old forecast is diplayed for some time