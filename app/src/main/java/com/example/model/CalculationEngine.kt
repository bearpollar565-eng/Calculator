package com.example.model

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

sealed class EvaluationResult {
  data class Success(val value: Double, val formattedText: String) : EvaluationResult()
  data class Error(val message: String) : EvaluationResult()
}

object CalculationEngine {

  /**
   * Evaluates an arithmetic / scientific expression.
   *
   * @param rawExpr The input formula string (e.g., "12.5 × (4 + 6) ÷ 2")
   * @param isRadMode True if trig calculations should use radians; false for degrees
   */
  fun evaluate(rawExpr: String, isRadMode: Boolean = false): EvaluationResult {
    val clean = rawExpr.trim()
    if (clean.isEmpty()) {
      return EvaluationResult.Error("")
    }

    return try {
      val parser = ExpressionParser(clean, isRadMode)
      val value = parser.parse()
      if (value.isNaN()) {
        EvaluationResult.Error("Undefined")
      } else if (value.isInfinite()) {
        if (value > 0) EvaluationResult.Error("Value too large") else EvaluationResult.Error("Value too small")
      } else {
        EvaluationResult.Success(value, formatNumber(value))
      }
    } catch (e: ArithmeticException) {
      EvaluationResult.Error(e.message ?: "Math error")
    } catch (e: IllegalArgumentException) {
      EvaluationResult.Error(e.message ?: "Invalid format")
    } catch (e: Exception) {
      EvaluationResult.Error("Invalid format")
    }
  }

  /**
   * Formats a double into a clean, human-readable string without trailing zeros,
   * avoiding 0.1 + 0.2 = 0.30000000000000004 floating point issues.
   */
  fun formatNumber(value: Double): String {
    if (value.isNaN()) return "NaN"
    if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"
    if (value == 0.0 || value == -0.0) return "0"

    // Avoid floating point precision noise by rounding to 12 significant digits
    val rounded = try {
      val bd = BigDecimal(value.toString())
      val mc = MathContext(12, RoundingMode.HALF_UP)
      bd.round(mc).stripTrailingZeros()
    } catch (_: Exception) {
      BigDecimal.valueOf(value)
    }

    val absVal = abs(value)
    if (absVal >= 1e12 || (absVal < 1e-5 && absVal > 0)) {
      // Use scientific notation for very large or tiny numbers
      val symbols = DecimalFormatSymbols(Locale.US)
      val sciFormat = DecimalFormat("0.######E0", symbols)
      return sciFormat.format(value).replace("E", "e")
    }

    val plain = rounded.toPlainString()
    return if (plain.contains('.')) {
      plain.trimEnd('0').trimEnd('.')
    } else {
      plain
    }
  }

  /**
   * Computes factorial for non-negative integers up to 170 (where Double overflows).
   */
  fun factorial(n: Double): Double {
    if (n < 0 || n != kotlin.math.floor(n)) {
      throw ArithmeticException("Invalid input for factorial")
    }
    if (n > 170) {
      throw ArithmeticException("Overflow")
    }
    var result = 1.0
    val intN = n.toInt()
    for (i in 2..intN) {
      result *= i
    }
    return result
  }
}

/**
 * Recursive descent parser with operator precedence, unary signs, functions,
 * percentage rules, and constants.
 */
private class ExpressionParser(
  private val input: String,
  private val isRadMode: Boolean
) {
  private var pos = 0
  private val len: Int

  // Normalized characters and tokens
  private val normalized: String = normalize(input)

  init {
    len = normalized.length
  }

  private fun normalize(expr: String): String {
    return expr
      .replace("×", "*")
      .replace("÷", "/")
      .replace("−", "-")
      .replace("–", "-")
      .replace("π", "PI")
      .replace("sin⁻¹", "asin")
      .replace("cos⁻¹", "acos")
      .replace("tan⁻¹", "atan")
      .replace("√", "sqrt")
      .replace("∛", "cbrt")
      .replace("ln", "ln")
      .replace("log", "log")
      .replace(" ", "")
  }

  fun parse(): Double {
    pos = 0
    val result = parseExpression()
    if (pos < len) {
      throw IllegalArgumentException("Unexpected: '${normalized.substring(pos)}'")
    }
    return result
  }

  private data class TermValue(val value: Double, val isPercent: Boolean = false)

  // Expression: Addition & Subtraction (with intuitive percentage handling)
  private fun parseExpression(): Double {
    var term = parseTerm()
    var v = term.value
    while (pos < len) {
      val c = normalized[pos]
      if (c == '+' || c == '-') {
        pos++
        val op = c
        val nextTerm = parseTerm()

        if (nextTerm.isPercent) {
          // Intuitive percentage: A + B% = A + (A * B/100), A - B% = A - (A * B/100)
          val percentDelta = v * nextTerm.value
          v = if (op == '+') v + percentDelta else v - percentDelta
        } else {
          v = if (op == '+') v + nextTerm.value else v - nextTerm.value
        }
      } else {
        break
      }
    }
    return v
  }

  // Term: Multiplication & Division
  private fun parseTerm(): TermValue {
    var current = parseFactorialAndExponent()
    var v = current.value
    var hadPercent = current.isPercent
    while (pos < len) {
      val c = normalized[pos]
      if (c == '*' || c == '/') {
        pos++
        val op = c
        val nextVal = parseFactorialAndExponent()
        if (op == '*') {
          v *= nextVal.value
        } else {
          if (nextVal.value == 0.0) throw ArithmeticException("Cannot divide by zero")
          v /= nextVal.value
        }
        hadPercent = false
      } else if (c == '(' || isStartOfFactor(c)) {
        // Implicit multiplication: e.g. 2(3), 2PI, 2sqrt(4)
        val nextVal = parseFactorialAndExponent()
        v *= nextVal.value
        hadPercent = false
      } else {
        break
      }
    }
    return TermValue(v, hadPercent)
  }

  // Factorial and Exponents: ^ and !
  private fun parseFactorialAndExponent(): TermValue {
    var v = parseFactor()
    var isPercent = false

    // Handle postfix operators like ! or %
    while (pos < len) {
      if (normalized[pos] == '!') {
        pos++
        v = CalculationEngine.factorial(v)
      } else if (normalized[pos] == '%') {
        pos++
        v /= 100.0
        isPercent = true
      } else if (normalized[pos] == '^') {
        pos++
        val exponent = parseFactor()
        v = v.pow(exponent)
      } else {
        break
      }
    }
    return TermValue(v, isPercent)
  }

  // Factor: Numbers, Unary Signs, Parentheses, Functions, Constants
  private fun parseFactor(): Double {
    skipWhitespace()
    if (pos >= len) throw IllegalArgumentException("Unexpected end of expression")

    // Unary plus and minus
    if (normalized[pos] == '+') {
      pos++
      return parseFactor()
    }
    if (normalized[pos] == '-') {
      pos++
      return -parseFactor()
    }

    // Parentheses
    if (normalized[pos] == '(') {
      pos++
      val v = parseExpression()
      if (pos < len && normalized[pos] == ')') {
        pos++
      } else {
        throw IllegalArgumentException("Missing closing parenthesis")
      }
      return v
    }

    // Check for named functions and constants
    if (normalized[pos].isLetter()) {
      return parseNamedIdentifier()
    }

    // Number
    return parseNumber()
  }

  private fun isStartOfFactor(c: Char): Boolean {
    return c.isDigit() || c == '.' || c.isLetter()
  }

  private fun parseNamedIdentifier(): Double {
    val start = pos
    while (pos < len && normalized[pos].isLetter()) {
      pos++
    }
    val name = normalized.substring(start, pos)

    return when (name.lowercase(Locale.ROOT)) {
      "pi" -> PI
      "e" -> kotlin.math.E
      "sin" -> {
        val arg = parseFunctionArg()
        val rad = if (isRadMode) arg else Math.toRadians(arg)
        // Handle exact angles for sin (e.g. sin(180 deg) = 0)
        cleanTrigResult(sin(rad))
      }
      "cos" -> {
        val arg = parseFunctionArg()
        val rad = if (isRadMode) arg else Math.toRadians(arg)
        cleanTrigResult(cos(rad))
      }
      "tan" -> {
        val arg = parseFunctionArg()
        val rad = if (isRadMode) arg else Math.toRadians(arg)
        if (!isRadMode && abs(arg % 180.0) == 90.0) {
          throw ArithmeticException("Undefined")
        }
        cleanTrigResult(tan(rad))
      }
      "asin" -> {
        val arg = parseFunctionArg()
        if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
        val rad = asin(arg)
        if (isRadMode) rad else Math.toDegrees(rad)
      }
      "acos" -> {
        val arg = parseFunctionArg()
        if (arg < -1.0 || arg > 1.0) throw ArithmeticException("Domain error")
        val rad = acos(arg)
        if (isRadMode) rad else Math.toDegrees(rad)
      }
      "atan" -> {
        val arg = parseFunctionArg()
        val rad = atan(arg)
        if (isRadMode) rad else Math.toDegrees(rad)
      }
      "sqrt" -> {
        val arg = parseFunctionArg()
        if (arg < 0) throw ArithmeticException("Cannot take square root of negative")
        sqrt(arg)
      }
      "cbrt" -> {
        val arg = parseFunctionArg()
        cbrt(arg)
      }
      "ln" -> {
        val arg = parseFunctionArg()
        if (arg <= 0) throw ArithmeticException("ln requires positive number")
        ln(arg)
      }
      "log" -> {
        val arg = parseFunctionArg()
        if (arg <= 0) throw ArithmeticException("log requires positive number")
        log10(arg)
      }
      "exp" -> {
        val arg = parseFunctionArg()
        exp(arg)
      }
      "abs" -> {
        val arg = parseFunctionArg()
        abs(arg)
      }
      else -> throw IllegalArgumentException("Unknown function '$name'")
    }
  }

  private fun cleanTrigResult(value: Double): Double {
    // Round close-to-zero values like 1.22e-16 to 0.0
    return if (abs(value) < 1e-15) 0.0 else value
  }

  private fun parseFunctionArg(): Double {
    if (pos < len && normalized[pos] == '(') {
      pos++
      val v = parseExpression()
      if (pos < len && normalized[pos] == ')') {
        pos++
      } else {
        throw IllegalArgumentException("Missing closing parenthesis for function")
      }
      return v
    } else {
      // Direct factor argument like sqrt 16 or sin 30
      return parseFactor()
    }
  }

  private fun parseNumber(): Double {
    val start = pos
    var hasDot = false
    while (pos < len) {
      val c = normalized[pos]
      if (c.isDigit()) {
        pos++
      } else if (c == '.' && !hasDot) {
        hasDot = true
        pos++
      } else {
        break
      }
    }
    if (start == pos) {
      throw IllegalArgumentException("Expected number at index $pos")
    }
    val str = normalized.substring(start, pos)
    return str.toDoubleOrNull() ?: throw IllegalArgumentException("Invalid number: '$str'")
  }

  private fun skipWhitespace() {
    while (pos < len && normalized[pos].isWhitespace()) {
      pos++
    }
  }
}
