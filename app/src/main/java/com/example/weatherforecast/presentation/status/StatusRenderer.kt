package com.example.weatherforecast.presentation.status

import com.example.weatherforecast.R
import com.example.weatherforecast.models.presentation.Message
import com.example.weatherforecast.models.presentation.MessageType
import com.example.weatherforecast.utils.ResourceManager

/**
 * Уведомляет о статусе, в котором находится приложение.
 * Например загрузка данных прогноза погоды, сообщение, требующее внимание (warning) или ошибка и т.д.
 *
 * @property statusDisplay компонент для отображения статуса
 * @property resourceManager менеджер ресурсов
 */
class StatusRenderer(
    private val statusDisplay: StatusDisplay,
    private val resourceManager: ResourceManager
) {

    fun updateFromMessage(message: Message) {
        val status = when (message) {
            is Message.Success -> {
                when (val content = message.content) {
                    is Message.Content.Text ->
                        StatusDisplay.Status(text = content.message, type = MessageType.INFO)

                    is Message.Content.Resource ->
                        StatusDisplay.Status(
                            text = resourceManager.getString(content.resId, *content.args),
                            type = MessageType.INFO
                        )
                }
            }

            is Message.Error -> {
                when (val content = message.content) {
                    is Message.Content.Text ->
                        StatusDisplay.Status(text = content.message, type = MessageType.ERROR)

                    is Message.Content.Resource ->
                        StatusDisplay.Status(
                            text = resourceManager.getString(content.resId, *content.args),
                            type = MessageType.ERROR
                        )
                }
            }

            is Message.Warning -> {
                when (val content = message.content) {
                    is Message.Content.Text ->
                        StatusDisplay.Status(text = content.message, type = MessageType.WARNING)

                    is Message.Content.Resource ->
                        StatusDisplay.Status(
                            text = resourceManager.getString(content.resId, *content.args),
                            type = MessageType.WARNING
                        )
                }
            }
        }
        statusDisplay.showStatus(status)
    }

    fun showStatus(text: String) {
        statusDisplay.showStatus(StatusDisplay.Status(text = text, type = MessageType.INFO))
    }

    fun showError(text: String) {
        statusDisplay.showStatus(StatusDisplay.Status(text = text, type = MessageType.ERROR))
    }

    fun showWarning(text: String) {
        statusDisplay.showStatus(StatusDisplay.Status(text = text, type = MessageType.WARNING))
    }

    fun showCitySelectionStatus() {
        statusDisplay.showStatus(
            StatusDisplay.Status(
                text = resourceManager.getString(R.string.city_selection_title),
                type = MessageType.INFO
            )
        )
    }

    fun showDownloadingStatusFor(city: String) {
        if (city.isBlank()) {
            statusDisplay.showStatus(
                StatusDisplay.Status(
                    text = resourceManager.getString(R.string.forecast_downloading),
                    type = MessageType.INFO
                )
            )
        } else {
            statusDisplay.showStatus(
                StatusDisplay.Status(
                    text = resourceManager.getString(
                        R.string.forecast_for_city_loading,
                        city
                    ),
                    type = MessageType.INFO
                )
            )
        }
    }

    /**
     * Factory for creating [StatusRenderer] with ViewModel (not available in DI graph).
     * Provided via [com.example.weatherforecast.di.ForecastPresentationModule].
     */
    class Factory(private val resourceManager: ResourceManager) {
        fun create(statusTarget: StatusDisplay): StatusRenderer =
            StatusRenderer(statusTarget, resourceManager)
    }
}