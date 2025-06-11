package com.example.rescueapp.di

import android.content.Context
import com.example.rescueapp.data.repository.ContactRepository
import com.example.rescueapp.data.repository.LocationRepository
import com.example.rescueapp.data.repository.RouteRepository
import com.example.rescueapp.service.SmsService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideLocationRepository(@ApplicationContext context: Context): LocationRepository {
        return LocationRepository(context)
    }

    @Provides
    @Singleton
    fun provideContactRepository(@ApplicationContext context: Context): ContactRepository {
        return ContactRepository(context)
    }

    @Provides
    @Singleton
    fun provideRouteRepository(@ApplicationContext context: Context): RouteRepository {
        return RouteRepository(context)
    }

    @Provides
    @Singleton
    fun provideSmsService(
        @ApplicationContext context: Context,
        contactRepository: ContactRepository,
        routeRepository: RouteRepository
    ): SmsService {
        return SmsService(context, contactRepository, routeRepository)
    }
}
