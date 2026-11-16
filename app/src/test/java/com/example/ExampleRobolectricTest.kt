package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.TarotSeedData
import com.example.data.model.BuiltInSpreads
import com.example.data.remote.OpenAiApiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `app name matches sacred tarot title`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("圣光塔罗", appName)
  }

  @Test
  fun `seed deck contains all 78 unique tarot cards`() {
    val cards = TarotSeedData.createAll78Cards()
    assertEquals(78, cards.size)
    assertEquals(78, cards.map { it.id }.toSet().size)
    assertEquals(22, cards.count { it.arcana == "MAJOR" })
    assertEquals(56, cards.count { it.arcana == "MINOR" })
  }

  @Test
  fun `openai base url normalization handles diverse user inputs`() {
    val api = OpenAiApiService()
    assertEquals("https://api.openai.com/v1", api.normalizeBaseUrl("https://api.openai.com/v1/"))
    assertEquals("https://api.deepseek.com/v1", api.normalizeBaseUrl("https://api.deepseek.com"))
    assertEquals(
      "https://proxy.example.com/v1",
      api.normalizeBaseUrl("https://proxy.example.com/v1/chat/completions"),
    )
    assertEquals("http://127.0.0.1:11434/v1", api.normalizeBaseUrl("http://127.0.0.1:11434/v1"))
  }

  @Test
  fun `built in spreads match position counts`() {
    assertTrue(BuiltInSpreads.all.size >= 6)
    BuiltInSpreads.all.forEach { spread ->
      assertEquals(spread.cardCount, spread.positions.size)
    }
  }
}
