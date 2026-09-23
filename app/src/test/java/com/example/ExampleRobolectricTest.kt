package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.CalculationEngine
import com.example.model.EvaluationResult
import com.example.model.UnitCategory
import com.example.model.UnitConverter
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
  fun `read app_name from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Calculator", appName)
  }

  @Test
  fun `test basic arithmetic and precedence`() {
    val result1 = CalculationEngine.evaluate("2 + 3 × 4")
    assertTrue(result1 is EvaluationResult.Success)
    assertEquals("14", (result1 as EvaluationResult.Success).formattedText)

    val result2 = CalculationEngine.evaluate("(2 + 3) × 4")
    assertTrue(result2 is EvaluationResult.Success)
    assertEquals("20", (result2 as EvaluationResult.Success).formattedText)
  }

  @Test
  fun `test floating point precision fix`() {
    val result = CalculationEngine.evaluate("0.1 + 0.2")
    assertTrue(result is EvaluationResult.Success)
    assertEquals("0.3", (result as EvaluationResult.Success).formattedText)
  }

  @Test
  fun `test percentage logic`() {
    val res1 = CalculationEngine.evaluate("100 + 10%")
    assertTrue(res1 is EvaluationResult.Success)
    assertEquals("110", (res1 as EvaluationResult.Success).formattedText)

    val res2 = CalculationEngine.evaluate("200 × 15%")
    assertTrue(res2 is EvaluationResult.Success)
    assertEquals("30", (res2 as EvaluationResult.Success).formattedText)
  }

  @Test
  fun `test division by zero`() {
    val result = CalculationEngine.evaluate("10 ÷ 0")
    assertTrue(result is EvaluationResult.Error)
    assertEquals("Cannot divide by zero", (result as EvaluationResult.Error).message)
  }

  @Test
  fun `test scientific calculations`() {
    // deg mode sin(90) = 1
    val sinRes = CalculationEngine.evaluate("sin(90)", isRadMode = false)
    assertTrue(sinRes is EvaluationResult.Success)
    assertEquals("1", (sinRes as EvaluationResult.Success).formattedText)

    // square root
    val sqrtRes = CalculationEngine.evaluate("√(16)")
    assertTrue(sqrtRes is EvaluationResult.Success)
    assertEquals("4", (sqrtRes as EvaluationResult.Success).formattedText)

    // power & factorial
    val powRes = CalculationEngine.evaluate("2^5")
    assertTrue(powRes is EvaluationResult.Success)
    assertEquals("32", (powRes as EvaluationResult.Success).formattedText)

    val factRes = CalculationEngine.evaluate("5!")
    assertTrue(factRes is EvaluationResult.Success)
    assertEquals("120", (factRes as EvaluationResult.Success).formattedText)
  }

  @Test
  fun `test unit converter length conversion`() {
    val units = UnitConverter.getUnitsForCategory(UnitCategory.LENGTH)
    val km = units.first { it.id == "km" }
    val m = units.first { it.id == "m" }
    val converted = UnitConverter.convert(2.5, km, m)
    assertEquals(2500.0, converted, 0.001)
  }
}
