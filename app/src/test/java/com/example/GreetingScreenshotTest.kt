package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.Note
import com.example.data.NoteRepository
import com.example.ui.NoteViewModel
import com.example.ui.NotesScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
    val noteDao = database.noteDao()
    val repository = NoteRepository(noteDao)

    runBlocking {
      repository.insert(
        Note(
          title = "Shopping List",
          content = "1. Milk\n2. Bread\n3. Fresh avocados 🥑",
          category = "Personal",
          colorIndex = 1,
          isPinned = true
        )
      )
      repository.insert(
        Note(
          title = "App Architecture Ideas",
          content = "Implement lightweight unidirectional data flow with Kotlin Flow.",
          category = "Work",
          colorIndex = 6,
          isPinned = false
        )
      )
    }

    val viewModel = NoteViewModel(repository)

    composeTestRule.setContent {
      MyApplicationTheme {
        NotesScreen(viewModel = viewModel)
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    database.close()
  }
}
