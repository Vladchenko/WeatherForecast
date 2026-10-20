package io.github.vladchenko.weatherforecast.core.navigation.api

/**
 * Navigation routes for the application.
 *
 * Used to avoid hardcoding string literals when navigating between screens.
 * Follows the pattern: `{screen_name}` for simple routes or `{screen_name}/{params}` for parameterized routes.
 *
 * ## Examples
 * - `Route.WEATHER` → `current_weather`
 * - `Route.weather()` → `current_weather/Moscow/55.7558/37.6173`
 * - `Route.CITY_SEARCH` → `city_search`
 */
object Route {
    /**
     * Route for the current weather screen.
     *
     * Accepts path parameters:
     * - `{city}`: City name (in URL-encoded format)
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
     * Route for the settings screen.
     */
    const val SETTINGS: String = "settings"

    /**
     * Parameter name for city name in the weather route.
     */
    const val CITY_PARAM = "city"

    /**
     * Parameter name for latitude in the weather route.
     */
    const val LATITUDE_PARAM = "lat"

    /**
     * Parameter name for longitude in the weather route.
     */
    const val LONGITUDE_PARAM = "lon"

    /**
     * Route for the AI weather summary screen.
     */
    const val AI_WEATHER_SUMMARY = "ai_weather_summary"

    /**
     * Formats the weather route with path parameters.
     *
     * @param city City name (must be in URL-encoded format)
     * @param lat Latitude
     * @param lon Longitude
     * @return Formatted route string, e.g., "current_weather/Moscow/55.7558/37.6173"
     */
    fun weather(city: String, lat: Double, lon: Double): String =
        "$WEATHER/$city/$lat/$lon"
}