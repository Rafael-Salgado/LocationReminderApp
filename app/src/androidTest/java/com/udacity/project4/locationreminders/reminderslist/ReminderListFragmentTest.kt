package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.test.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var dataSource: ReminderDataSource
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin() //stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        dataSource = get()

        //clear the data to start fresh
        runBlocking {
            dataSource.deleteAllReminders()
        }
    }


    @Test
    fun navigateToSaveReminderFragment_whenClickingFAB_navigationSuccessful() {
        // GIVEN - An init point in navigation and a navigation controller
        val scenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN - FAB button is clicked
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN -  Verify that we navigate to the Save Reminder Fragment
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }


    @Test
    fun displayDataOnReminderListFragment_reminderDataDisplayed() {
        // GIVEN - A reminder data
        val reminder1 = ReminderDTO(
            "Golden Gate",
            "Golden Gate Bridge",
            "California, USA",
            37.820056594732996,
            -122.47825717209413
        )

        // WHEN - Added to the Repository
        runBlocking {
            dataSource.apply {
                deleteAllReminders()
                saveReminder(reminder1)
            }

        }

        // THEN - Assert that the data is display and with the correct values
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.remindersRecyclerView)).check(matches(isDisplayed()))
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder1.description)).check(matches(isDisplayed()))
        onView(withText(reminder1.location)).check(matches(isDisplayed()))
    }

    @Test
    fun displayErrorMessage_whenDataEmpty() {
        // GIVEN - An empty data source
        runBlocking { dataSource.deleteAllReminders() }

        // WHEN - The fragment is launched
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        // THEN - Assert that the error message is displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

}