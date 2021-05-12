package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase

    @Before
    fun initDB() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun insertReminder_getById_returnNotNull() = runBlockingTest {
        // GIVEN - Insert a reminder
        val reminder1 = ReminderDTO(
            "Golden Gate",
            "Golden Gate Bridge",
            "California, USA",
            37.820056594732996,
            -122.47825717209413
        )
        database.reminderDao().saveReminder(reminder1)

        // WHEN - Get the reminder from the database from id
        val loadReminder = database.reminderDao().getReminderById(reminder1.id)

        // THEN - The loaded reminder has the expected values
        assertThat(loadReminder as ReminderDTO, notNullValue())
        assertThat(loadReminder.id, `is`(reminder1.id))
        assertThat(loadReminder.title, `is`(reminder1.title))
        assertThat(loadReminder.description, `is`(reminder1.description))
        assertThat(loadReminder.location, `is`(reminder1.location))
        assertThat(loadReminder.latitude, `is`(reminder1.latitude))
        assertThat(loadReminder.longitude, `is`(reminder1.longitude))
    }

    @Test
    fun insertReminder_getReminders_returnNotNull() = runBlockingTest {
        // GIVEN - Two reminders save in he database
        val reminder1 = ReminderDTO("Title1","Description1","Location1",0.0,0.0)
        val reminder2 = ReminderDTO("Title2","Description2","Location2",1.0,1.0)
        database.reminderDao().deleteAllReminders()
        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN - Get reminders is called
        val remindersList = database.reminderDao().getReminders()

        // THEN - Assert that the values are right
        assertThat(remindersList.size,`is`(2))
        assertThat(remindersList[0].id,`is`(reminder1.id))
        assertThat(remindersList[1].id,`is`(reminder2.id))
    }

    @Test
    fun getReminderWithInvalidId_returnNull() = runBlockingTest {
        // GIVEN - An invalid ID
        val id = ""

        // WHEN - Get reminder by ID is called
        val reminder = database.reminderDao().getReminderById(id)

        // THEN - Assert that result is null
        assertThat(reminder, `is`(nullValue()))
    }

}