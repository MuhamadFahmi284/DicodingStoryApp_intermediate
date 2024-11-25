package com.dicoding.dicodingstoryapp.utils

import androidx.test.espresso.IdlingResource
import androidx.test.espresso.idling.CountingIdlingResource

object EspressoIdlingResource {
    private val countingIdlingResource = CountingIdlingResource("GLOBAL")

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }

    fun getIdlingResource(): IdlingResource = countingIdlingResource
}