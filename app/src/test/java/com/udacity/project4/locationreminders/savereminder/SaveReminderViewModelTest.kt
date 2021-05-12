package com.udacity.project4.locationreminders.savereminder

import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.nullValue
import org.hamcrest.core.Is.`is`
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var dataSource: FakeDataSource
    private lateinit var saveRemindersListViewModel: SaveReminderViewModel

    @Before
    fun setupViewModel() {
        stopKoin()
        dataSource = FakeDataSource()
        saveRemindersListViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource
        )
    }

    @Test
    fun clearVariables_onClear_nullValues(){
        // GIVEN - Variables with not null values
        val poi = PointOfInterest(LatLng(0.0,0.0),"Golden Gate","California USA")
        saveRemindersListViewModel.reminderTitle.value = "Location 1"
        saveRemindersListViewModel.reminderDescription.value = "New Location"
        saveRemindersListViewModel.reminderSelectedLocationStr.value = "Golden Gate"
        saveRemindersListViewModel.selectedPOI.value = poi
        saveRemindersListViewModel.latitude.value = 0.0
        saveRemindersListViewModel.longitude.value = 0.0

        // WHEN - The clear method is called
        saveRemindersListViewModel.onClear()

        // THEN - Assert that the variables are null
        assertThat(saveRemindersListViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersListViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersListViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersListViewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersListViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveRemindersListViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun validateEnteredData_whenTitleEmpty_returnFalse(){
        // GIVEN - An empty title
        val reminder = ReminderDataItem("","","",0.0,0.0)

        // WHEN - The reminder is validated
        val state = saveRemindersListViewModel.validateEnteredData(reminder)

        // THEN - Assert that the state is false and the snack bar displays the right message
        assertThat(state, `is`(false))
        assertThat(
            saveRemindersListViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title))
    }

    @Test
    fun validateEnteredData_whenLocationEmpty_returnFalse(){
        // GIVEN - An empty location
        val reminder = ReminderDataItem("Title1","","",0.0,0.0)

        // WHEN - The reminder is validated
        val state = saveRemindersListViewModel.validateEnteredData(reminder)

        // THEN - Assert that the state is false and the snack bar displays the right message
        assertThat(state, `is`(false))
        assertThat(
            saveRemindersListViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location))
    }

    @Test
    fun saveReminder_loadingState(){
        // GIVEN - A new reminder to save
        val reminder = ReminderDataItem(
            "Fort Point",
            "National Historic Site",
            "California, USA",
            37.810658944356604,
            -122.47701014855258
        )
        mainCoroutineRule.pauseDispatcher()

        // WHEN - Save reminder method is called
        saveRemindersListViewModel.saveReminder(reminder)

        // THEN - Assert that the values of showLoading, showToast and navigationCommand
        assertThat(saveRemindersListViewModel.showLoading.getOrAwaitValue(),`is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(saveRemindersListViewModel.showLoading.getOrAwaitValue(),`is`(false))
        assertThat(saveRemindersListViewModel.showToast.getOrAwaitValue(),`is`(
            ApplicationProvider.getApplicationContext<Context>().getString(R.string.reminder_saved)))
        assertThat(saveRemindersListViewModel.navigationCommand.getOrAwaitValue(),`is`(
            NavigationCommand.Back))
    }


}