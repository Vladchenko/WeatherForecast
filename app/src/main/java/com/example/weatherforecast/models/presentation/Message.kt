package com.example.weatherforecast.models.presentation

sealed class Message {
    data class Error(val content: Content) : Message()
    data class Success(val content: Content) : Message()
    data class Warning(val content: Content) : Message()

    sealed interface Content {
        data class Text(val message: String) : Content
        data class Resource(val resId: Int, val args: Array<out Any> = emptyArray()) : Content {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as Resource

                if (resId != other.resId) return false
                if (!args.contentEquals(other.args)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = resId
                result = 31 * result + args.contentHashCode()
                return result
            }
        }
    }
}