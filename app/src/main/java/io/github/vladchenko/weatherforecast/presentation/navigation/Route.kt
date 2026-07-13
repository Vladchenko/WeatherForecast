package io.github.vladchenko.weatherforecast.presentation.navigation

/**
 * Navigation routes for the app.
 *
 * Used to avoid hardcoding string literals when navigating between screens.
 * Follows the pattern: `{screen_name}` for simple routes, or `{screen_name}/{params}` for routes with parameters.
 *
 * ## Examples
 * - `Route.Weather` → `current_weather`
 * - `Route.WeatherWithParams.format()` → `current_weather/Moscow/55.7558/37.6173`
 * - `Route.CitySearch` → `city_search`
 */
object Route {
    /**
     * Route for the current weather screen.
     *
     * Accepts path parameters:
     * - `{city}`: City name (URL-encoded)
     * - `{lat}`: Latitude
     * - `{lon}`: Longitude
     *
     * Example: `current_weather/Moscow/55.7558/37.6173`
     */
    const val WEATHER: String = "current_weather"

    /**
     * Route for the city search screen.
     *
     * Example: `city_search`
     */
    const val CITY_SEARCH: String = "city_search"

    /**
     * Parameter name for city name in weather route.
     */
    const val CITY_PARAM = "city"

    /**
     * Parameter name for latitude in weather route.
     */
    const val LATITUDE_PARAM = "lat"

    /**
     * Parameter name for longitude in weather route.
     */
    const val LONGITUDE_PARAM = "lon"

    /**
     * Format a weather route with path parameters.
     *
     * @param city City name (should be URL-encoded)
     * @param lat Latitude
     * @param lon Longitude
     * @return Formatted route string, e.g., "current_weather/Moscow/55.7558/37.6173"
     */
    fun weather(city: String, lat: Double, lon: Double): String =
        "$WEATHER/$city/$lat/$lon"
}