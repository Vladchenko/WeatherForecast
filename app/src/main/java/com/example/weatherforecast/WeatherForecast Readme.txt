This app is based on udemy course
https://www.udemy.com/course/android-beginners-weather-forecast-app/
Project was amended with a DataBinding, Jetpack Navigation, Retrofit, Hilt, Room and MVVM.

API for the project - https://openweathermap.org/current
Test request on a following link - http://api.openweathermap.org/data/2.5/weather?q=Kazan&appid=a8f0e797059b7959399040200fd43231

JSON data to data-class conversion - https://app.quicktype.io/

TODO:
    - okhttp3.ResponseBody$Companion$asResponseBody$1@a4cae72
    - Activity leaking persists
    - Put most code from fragments to viewmodels
    - When app starts for the very first time and one selects a city, then geolocation alert pops up, but should not
    - When city is chosen and city forecast fragment shows up, old forecast is diplayed for some time
        - Emptying a views doesn't help
            - Tried in onViewCreated and in onPause
        ! Probably, one has to remove fragment
    - WindowManager: android.view.WindowLeaked: Activity com.example.weatherforecast.presentation.WeatherForecastActivity has leaked window DecorView@bee3763[WeatherForecastActivity] that was originally added here
              at android.view.ViewRootImpl.<init>(ViewRootImpl.java:797)
              at android.view.ViewRootImpl.<init>(ViewRootImpl.java:781)
              at android.view.WindowManagerGlobal.addView(WindowManagerGlobal.java:399)
              at android.view.WindowManagerImpl.addView(WindowManagerImpl.java:109)
              at android.app.Dialog.show(Dialog.java:342)
              at androidx.appcompat.app.AlertDialog$Builder.show(AlertDialog.java:1009)
              at com.example.weatherforecast.geolocation.AlertDialogDelegate.showAlertDialog(AlertDialogDelegate.kt:34)
              at com.example.weatherforecast.presentation.fragments.CurrentTimeForecastFragment.showAlertDialog(CurrentTimeForecastFragment.kt:188)
              at com.example.weatherforecast.presentation.fragments.CurrentTimeForecastFragment.initLiveDataObservers$lambda-6(CurrentTimeForecastFragment.kt:113)
              at com.example.weatherforecast.presentation.fragments.CurrentTimeForecastFragment.$r8$lambda$Tca9sM4y3frWmHOFcrY-iyx18V4(Unknown Source:0)
              at com.example.weatherforecast.presentation.fragments.CurrentTimeForecastFragment$$ExternalSyntheticLambda7.onChanged(Unknown Source:4)

    - Put all texts to string.xml
    - Remove all superfluous Log.d
    - Check statuses
    - ? Center a toolbar texts
        Following attributes for toolbar and appbar layout do not help
                    android:foregroundGravity="center"
                    android:gravity="center"
                    android:textAlignment="center"
    - Make data source to return a data not domain model
    - Add "heavy intensity rain" to weather images
    - Add unit tests