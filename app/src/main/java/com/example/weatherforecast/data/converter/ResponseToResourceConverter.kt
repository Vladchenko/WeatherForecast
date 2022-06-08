package com.example.weatherforecast.data.converter

import com.example.weatherforecast.data.models.WeatherForecastResponse
import com.example.weatherforecast.data.util.Resource
import retrofit2.Response

/**
 * Converts [Response] (data-layer model) to [Resource] (domain-layer model).
 */
class ResponseToResourceConverter {

    fun convert(response: Response<WeatherForecastResponse>): Resource<WeatherForecastResponse> {
        if (response.isSuccessful) {
            response.body()?.let { result ->
                return Resource.Success(result)
            }
        }
        return Resource.Error(Throwable(response.message()))
    }

}