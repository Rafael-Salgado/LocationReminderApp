package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        remindersLocalRepository =
            RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun getReminder_retrieveReminderWithInvalidId_returnResultError() = runBlocking{
        // GIVEN - An invalid ID
        val id = ""

        // WHEN - Get reminder method is called
        val result = remindersLocalRepository.getReminder(id) as Result.Error

        // THEN - Assert that the result has the expected message
        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun saveReminder_getSameReminderById() = runBlocking{
        // GIVEN - A new reminder save in the database
        val reminder1 = ReminderDTO(
            "Golden Gate",
            "Golden Gate Bridge",
            "California, USA",
            37.820056594732996,
            -122.47825717209413
        )
        remindersLocalRepository.saveReminder(reminder1)

        // WHEN - Reminder retrieve by id
        val loadReminder = remindersLocalRepository.getReminder(reminder1.id)

        // THEN - Same reminder is returned
        loadReminder as Result.Success
        assertThat(loadReminder.data.id,`is`(reminder1.id))
        assertThat(loadReminder.data.title,`is`(reminder1.title))
        assertThat(loadReminder.data.description,`is`(reminder1.description))
        assertThat(loadReminder.data.location,`is`(reminder1.location))
        assertThat(loadReminder.data.latitude,`is`(reminder1.latitude))
        assertThat(loadReminder.data.longitude,`is`(reminder1.longitude))
    }
}