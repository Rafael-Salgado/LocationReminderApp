package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setupViewModel() {
        stopKoin()
        dataSource = FakeDataSource()
        val reminder1 = ReminderDTO(
            "Golden Gate",
            "Golden Gate Bridge",
            "California, USA",
            37.820056594732996,
            -122.47825717209413
        )
        val reminder2 = ReminderDTO(
            "Lime Point Historic Lighthouse",
            "Lighthouse",
            "California, USA",
            37.82576014431496,
            -122.47833227394693
        )
        dataSource.addReminders(reminder1, reminder2)
        remindersListViewModel = RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource
        )
    }

    @Test
    fun showLoading_loadReminders_loadingState() {
        // GIVEN - A pause dispatcher so we can verify initial values
        mainCoroutineRule.pauseDispatcher()

        // WHEN - The reminders are load in the view model
        remindersListViewModel.loadReminders()

        // THEN - Assert that the status of the charge indicator
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(remindersListViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun getRemindersWhenUnavailable_callErrorToDisplayMessage()= runBlockingTest{
        // GIVEN - Make the repository to return an error
        dataSource.setReturnError(true)
        val result = dataSource.getReminders() as Result.Error

        // WHEN - The reminders are load in the view model
        remindersListViewModel.loadReminders()

        // THEN - Assert that the snackbar displays the right message
        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(result.message))
    }

    @Test
    fun retrieveRemindersAndCheckValues_loadReminders_reminderContent() = runBlockingTest{
        // GIVEN - A reminder and save in the database
        val reminder3 = ReminderDTO(
            "Fort Point",
            "National Historic Site",
            "California, USA",
            37.810658944356604,
            -122.47701014855258
        )
        dataSource.addReminders(reminder3)

        // WHEN - The reminders are load in the view model
        remindersListViewModel.loadReminders()

        //THEN - Check the values
        val reminders = remindersListViewModel.remindersList.getOrAwaitValue()
        val reminder = reminders.filter {
            it.id==reminder3.id
        }
        assertThat(reminder[0].title,`is`(reminder3.title))
        assertThat(reminder[0].description,`is`(reminder3.description))
        assertThat(reminder[0].location,`is`(reminder3.location))
        assertThat(reminder[0].latitude,`is`(reminder3.latitude))
        assertThat(reminder[0].longitude,`is`(reminder3.longitude))
    }

}