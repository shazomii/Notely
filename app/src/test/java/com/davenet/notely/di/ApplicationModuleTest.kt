package com.davenet.notely.di

import android.app.Application
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class ApplicationModuleTest {

    @MockK(relaxed = true)
    lateinit var application: Application

    private lateinit var applicationModule: ApplicationModule

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        applicationModule = ApplicationModule()
    }

    @Test
    fun verifyProvidedAppContext() {
        assertEquals(application, applicationModule.provideAppContext(application))
    }
}