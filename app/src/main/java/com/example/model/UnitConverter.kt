package com.example.model

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

enum class UnitCategory(val displayName: String, val iconName: String) {
  LENGTH("Length", "straighten"),
  WEIGHT("Weight", "scale"),
  TEMPERATURE("Temperature", "thermostat"),
  AREA("Area", "crop_square"),
  VOLUME("Volume", "opacity"),
  DATA("Data", "memory"),
  SPEED("Speed", "speed"),
  TIME("Time", "schedule")
}

data class ConversionUnit(
  val id: String,
  val name: String,
  val symbol: String,
  val toBaseFactor: Double = 1.0, // Factor to convert this unit into base unit
  val isTemperature: Boolean = false
)

object UnitConverter {

  val categories: List<UnitCategory> = UnitCategory.entries

  fun getUnitsForCategory(category: UnitCategory): List<ConversionUnit> {
    return when (category) {
      UnitCategory.LENGTH -> listOf(
        ConversionUnit("m", "Meter", "m", 1.0),
        ConversionUnit("km", "Kilometer", "km", 1000.0),
        ConversionUnit("cm", "Centimeter", "cm", 0.01),
        ConversionUnit("mm", "Millimeter", "mm", 0.001),
        ConversionUnit("mi", "Mile", "mi", 1609.344),
        ConversionUnit("yd", "Yard", "yd", 0.9144),
        ConversionUnit("ft", "Foot", "ft", 0.3048),
        ConversionUnit("in", "Inch", "in", 0.0254)
      )
      UnitCategory.WEIGHT -> listOf(
        ConversionUnit("kg", "Kilogram", "kg", 1.0),
        ConversionUnit("g", "Gram", "g", 0.001),
        ConversionUnit("mg", "Milligram", "mg", 0.000001),
        ConversionUnit("t", "Metric Ton", "t", 1000.0),
        ConversionUnit("lb", "Pound", "lb", 0.45359237),
        ConversionUnit("oz", "Ounce", "oz", 0.028349523125)
      )
      UnitCategory.TEMPERATURE -> listOf(
        ConversionUnit("c", "Celsius", "°C", 1.0, isTemperature = true),
        ConversionUnit("f", "Fahrenheit", "°F", 1.0, isTemperature = true),
        ConversionUnit("k", "Kelvin", "K", 1.0, isTemperature = true)
      )
      UnitCategory.AREA -> listOf(
        ConversionUnit("sqm", "Square Meter", "m²", 1.0),
        ConversionUnit("sqkm", "Square Kilometer", "km²", 1_000_000.0),
        ConversionUnit("sqft", "Square Foot", "ft²", 0.09290304),
        ConversionUnit("sqmi", "Square Mile", "mi²", 2_589_988.110336),
        ConversionUnit("acre", "Acre", "ac", 4046.8564224),
        ConversionUnit("ha", "Hectare", "ha", 10_000.0)
      )
      UnitCategory.VOLUME -> listOf(
        ConversionUnit("l", "Liter", "L", 1.0),
        ConversionUnit("ml", "Milliliter", "mL", 0.001),
        ConversionUnit("gal", "Gallon (US)", "gal", 3.785411784),
        ConversionUnit("qt", "Quart (US)", "qt", 0.946352946),
        ConversionUnit("pt", "Pint (US)", "pt", 0.473176473),
        ConversionUnit("floz", "Fluid Ounce", "fl oz", 0.0295735295625),
        ConversionUnit("m3", "Cubic Meter", "m³", 1000.0)
      )
      UnitCategory.DATA -> listOf(
        ConversionUnit("b", "Byte", "B", 1.0),
        ConversionUnit("kb", "Kilobyte", "KB", 1024.0),
        ConversionUnit("mb", "Megabyte", "MB", 1024.0 * 1024.0),
        ConversionUnit("gb", "Gigabyte", "GB", 1024.0 * 1024.0 * 1024.0),
        ConversionUnit("tb", "Terabyte", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0)
      )
      UnitCategory.SPEED -> listOf(
        ConversionUnit("mps", "Meter/sec", "m/s", 1.0),
        ConversionUnit("kmh", "Kilometer/hour", "km/h", 1.0 / 3.6),
        ConversionUnit("mph", "Mile/hour", "mph", 0.44704),
        ConversionUnit("knot", "Knot", "kn", 0.514444)
      )
      UnitCategory.TIME -> listOf(
        ConversionUnit("s", "Second", "s", 1.0),
        ConversionUnit("ms", "Millisecond", "ms", 0.001),
        ConversionUnit("min", "Minute", "min", 60.0),
        ConversionUnit("hr", "Hour", "hr", 3600.0),
        ConversionUnit("day", "Day", "d", 86400.0),
        ConversionUnit("wk", "Week", "wk", 604800.0)
      )
    }
  }

  fun convert(value: Double, fromUnit: ConversionUnit, toUnit: ConversionUnit): Double {
    if (fromUnit.id == toUnit.id) return value

    if (fromUnit.isTemperature && toUnit.isTemperature) {
      // Base temperature: Celsius
      val inCelsius = when (fromUnit.id) {
        "c" -> value
        "f" -> (value - 32.0) * (5.0 / 9.0)
        "k" -> value - 273.15
        else -> value
      }
      return when (toUnit.id) {
        "c" -> inCelsius
        "f" -> (inCelsius * (9.0 / 5.0)) + 32.0
        "k" -> inCelsius + 273.15
        else -> inCelsius
      }
    }

    // Convert from -> base unit -> to unit
    val baseValue = value * fromUnit.toBaseFactor
    return baseValue / toUnit.toBaseFactor
  }

  fun formatConvertedValue(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    val absVal = kotlin.math.abs(value)
    if (absVal >= 1e9 || (absVal < 1e-4 && absVal > 0)) {
      val symbols = DecimalFormatSymbols(Locale.US)
      return DecimalFormat("0.####E0", symbols).format(value)
    }
    val symbols = DecimalFormatSymbols(Locale.US)
    val df = DecimalFormat("#,##0.######", symbols)
    return df.format(value)
  }
}
