package com.example.weatherforecast.di

import com.example.weatherforecast.data.util.TemperatureType
import com.example.weatherforecast.dispatchers.CoroutineDispatchers
import com.example.weatherforecast.dispatchers.CoroutineDispatchersImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CommonModule {

    @Singleton
    @Provides
    fun provideTemperatureType(): TemperatureType {
        return TemperatureType.CELSIUS
    }

    @Singleton
    @Provides
    fun provideCoroutineDispatchers(): CoroutineDispatchers {
        return CoroutineDispatchersImpl()
    }

    @Singleton
    @Provides
    fun provideCoroutineScope(coroutineDispatchers: CoroutineDispatchers): CoroutineScope {
        return CoroutineScope(SupervisorJob() + coroutineDispatchers.default)
    }
}