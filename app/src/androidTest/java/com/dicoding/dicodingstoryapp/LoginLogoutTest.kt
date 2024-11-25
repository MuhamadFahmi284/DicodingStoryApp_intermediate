package com.dicoding.dicodingstoryapp

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withContentDescription
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dicoding.dicodingstoryapp.utils.EspressoIdlingResource
import com.dicoding.dicodingstoryapp.view.login.LoginActivity
import com.dicoding.dicodingstoryapp.view.main.MainActivity
import com.dicoding.dicodingstoryapp.view.welcome.WelcomeActivity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginLogoutTest {

    val dummyEmail = "okey@gmail.com"
    val dummyPassword = "12345678"

    @get:Rule
    val activityRule = ActivityScenarioRule(WelcomeActivity::class.java)

    @Before
    fun setUp() {
        Intents.init()
        IdlingRegistry.getInstance().register(EspressoIdlingResource.getIdlingResource())
    }

    @After
    fun tearDown() {
        Intents.release()
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.getIdlingResource())
    }

    private fun performLogin() {
        // Klik tombol "Masuk" di WelcomeActivity
        onView(withId(R.id.buttonLogin))
            .check(matches(isDisplayed()))
            .perform(click())

        // Pastikan pindah ke LoginActivity
        intended(hasComponent(LoginActivity::class.java.name))

        // Masukkan email dan password
        onView(withId(R.id.ed_login_email)).perform(typeText(dummyEmail), closeSoftKeyboard())
        onView(withId(R.id.ed_login_password)).perform(typeText(dummyPassword), closeSoftKeyboard())

        // Klik tombol login
        onView(withId(R.id.loginButton)).perform(click())

        // Verifikasi dialog login berhasil
        onView(withText("Login berhasil"))
            .check(matches(isDisplayed()))

        onView(withText("Lanjut"))
            .check(matches(isDisplayed()))
            .perform(click())

        // Pastikan pindah ke MainActivity
        intended(hasComponent(MainActivity::class.java.name))
    }

    @Test
    fun loginSuccessTest() {
        performLogin()
    }

    @Test
    fun logoutSuccessTest() {
        // Lakukan login terlebih dahulu
        performLogin()

        // Aksi logout di MainActivity
        onView(withId(R.id.action_logout))
            .check(matches(isDisplayed()))
            .perform(click())

        // Verifikasi bahwa kembali ke WelcomeActivity
        intended(hasComponent(WelcomeActivity::class.java.name))
    }
}