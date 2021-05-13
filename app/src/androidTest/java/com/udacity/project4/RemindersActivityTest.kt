package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
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
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun endToEndTest_addNewReminder() = runBlocking {

        // 1- Clear the repository and define variables
        val title = "Title1"
        val description = "Description1"
        val location = "Dropped Pin"
        repository.deleteAllReminders()
        // 2- Start RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario) // LOOK HERE
        // 3- Add a new reminder by clicking on the FAB button
        onView(withId(R.id.addReminderFAB)).perform(click())
        // 4- Add Title, description and click on select location
        onView(withId(R.id.reminderTitle)).perform(replaceText(title))
        onView(withId(R.id.reminderDescription)).perform(replaceText(description))
        onView(withId(R.id.selectLocation)).perform(click())
        // 5- Try the different map style
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText("Hybrid Map")).perform(click())
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText("Normal Map")).perform(click())
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText("Satellite Map")).perform(click())
        openActionBarOverflowOrOptionsMenu(getApplicationContext())
        onView(withText("Terrain Map")).perform(click())
        // 6- Select the location and save
        onView(withId(R.id.map_fragment)).perform(longClick())
        onView(withId(R.id.save_location)).perform(click())
        // 7- Save the remainder
        onView(withId(R.id.saveReminder)).perform(click())
        // 8- Confirm that the data displayed is right
        onView(withText(title)).check(matches(ViewMatchers.isDisplayed()))
        onView(withText(description)).check(matches(ViewMatchers.isDisplayed()))
        onView(withText(location)).check(matches(ViewMatchers.isDisplayed()))
        // 9- Make sure the activity is closed
        activityScenario.close()
    }

    @Test
    fun addNewReminder_incompleteData_showSnackBar()= runBlocking{
        // 1- Clear the repository and define variables
        val title = "Title1"
        val description = "Description1"
        repository.deleteAllReminders()
        // 2- Start RemindersActivity
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario) // LOOK HERE
        // 3- Add a new reminder by clicking on the FAB button
        onView(withId(R.id.addReminderFAB)).perform(click())
        // 4- Add Title and description
        onView(withId(R.id.reminderTitle)).perform(replaceText(title))
        onView(withId(R.id.reminderDescription)).perform(replaceText(description))
        // 5- Save the remainder
        onView(withId(R.id.saveReminder)).perform(click())
        // 6- Confirm that the snackbar displays the correct message
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))
        onView(withText(R.string.err_select_location)).check(matches(withEffectiveVisibility(
                Visibility.VISIBLE
            )))
        // 7- Make sure the activity is closed
        activityScenario.close()
    }

}
