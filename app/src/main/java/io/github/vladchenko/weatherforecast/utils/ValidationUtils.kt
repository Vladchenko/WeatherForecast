package io.github.vladchenko.weatherforecast.utils

/**
 * Utility object for validating user input related to city names.
 *
 * Provides methods to verify the correctness of a string entered by the user as a city search query.
 * The validation rules include:
 * - Non-empty value
 * - Acceptable length (up to 100 characters)
 * - Allowed characters: letters (Latin, Cyrillic), spaces, and hyphens
 */
object ValidationUtils {

    /**
     * Validates a string as a proper city search query.
     *
     * Returns a [Result] containing either:
     * - Success with the original string, or
     * - Failure with an [IllegalArgumentException] describing the issue.
     *
     * Designed for use in the domain layer (Interactor), where it's important to provide
     * meaningful feedback to the user about invalid input.
     *
     * @param token The string to validate
     * @return [Result.success] with the token if valid,
     *         or [Result.failure] with an [IllegalArgumentException] on failure
     *
     * @throws IllegalArgumentException with one of the following messages:
     *   - "City name cannot be empty" — if the string is blank
     *   - "City name too long" — if length exceeds 100 characters
     *   - "Invalid characters in city name" — if disallowed characters are present
     */
    fun validateCityToken(token: String): Result<String> {
        return when {
            token.isBlank() -> Result.failure(IllegalArgumentException("City name cannot be empty"))
            token.length > 130 -> Result.failure(IllegalArgumentException("City name too long"))
            !token.matches(Regex("^[a-zA-Zа-яА-ЯёЁ\\s\\-]*\$")) -> {
                Result.failure(IllegalArgumentException("Invalid characters in city name"))
            }
            else -> Result.success(token)
        }
    }
}