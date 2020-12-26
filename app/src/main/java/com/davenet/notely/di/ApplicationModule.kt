package com.davenet.notely.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@InstallIn(ActivityRetainedComponent::class)
@Module
class ApplicationModule {

    @Provides
    fun provideAppContext(@ApplicationContext context: Context): Context {
        return context
    }
}