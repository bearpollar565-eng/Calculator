package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConversionUnit
import com.example.model.UnitCategory
import com.example.model.UnitConverter
import com.example.ui.UnitConverterUiState

@Composable
fun UnitConverterView(
  converterState: UnitConverterUiState,
  onCategorySelected: (UnitCategory) -> Unit,
  onFromUnitSelected: (ConversionUnit) -> Unit,
  onToUnitSelected: (ConversionUnit) -> Unit,
  onSwapUnits: () -> Unit,
  onInputChanged: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current
  val categoryScrollState = rememberScrollState()
  val availableUnits = remember(converterState.selectedCategory) {
    UnitConverter.getUnitsForCategory(converterState.selectedCategory)
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Categories Carousel
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(categoryScrollState),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      UnitConverter.categories.forEach { category ->
        val selected = category == converterState.selectedCategory
        FilterChip(
          selected = selected,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onCategorySelected(category)
          },
          label = { Text(category.displayName) },
          leadingIcon = {
            Icon(
              imageVector = getCategoryIcon(category),
              contentDescription = null,
              modifier = Modifier.size(18.dp)
            )
          },
          colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
          ),
          modifier = Modifier.testTag("chip_${category.name}")
        )
      }
    }

    // Conversion Cards (From & To)
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(20.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
      )
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // "From" row
        UnitSelectorRow(
          label = "From",
          currentUnit = converterState.fromUnit,
          availableUnits = availableUnits,
          amountText = converterState.inputAmount.ifEmpty { "0" },
          isInput = true,
          onUnitSelected = onFromUnitSelected,
          modifier = Modifier.testTag("from_unit_selector")
        )

        // Swap button
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.Center
        ) {
          Surface(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onSwapUnits()
              }
              .testTag("swap_units_button"),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            shape = CircleShape
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = "Swap units",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
              )
            }
          }
        }

        // "To" row
        UnitSelectorRow(
          label = "To",
          currentUnit = converterState.toUnit,
          availableUnits = availableUnits,
          amountText = converterState.outputAmount,
          isInput = false,
          onUnitSelected = onToUnitSelected,
          modifier = Modifier.testTag("to_unit_selector")
        )
      }
    }

    // Numeric keypad for unit input
    ConverterKeypad(
      currentInput = converterState.inputAmount,
      onInputChanged = onInputChanged,
      modifier = Modifier.weight(1f)
    )
  }
}

@Composable
private fun UnitSelectorRow(
  label: String,
  currentUnit: ConversionUnit,
  availableUnits: List<ConversionUnit>,
  amountText: String,
  isInput: Boolean,
  onUnitSelected: (ConversionUnit) -> Unit,
  modifier: Modifier = Modifier
) {
  var menuExpanded by remember { mutableStateOf(false) }

  Row(
    modifier = modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Dropdown trigger for Unit
    Box {
      Surface(
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .clickable { menuExpanded = true },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Column {
            Text(
              text = label,
              style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
              )
            )
            Text(
              text = "${currentUnit.name} (${currentUnit.symbol})",
              style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
            )
          }
        }
      }

      DropdownMenu(
        expanded = menuExpanded,
        onDismissRequest = { menuExpanded = false }
      ) {
        availableUnits.forEach { unit ->
          DropdownMenuItem(
            text = { Text("${unit.name} (${unit.symbol})") },
            onClick = {
              onUnitSelected(unit)
              menuExpanded = false
            }
          )
        }
      }
    }

    // Amount Display
    Text(
      text = amountText,
      style = MaterialTheme.typography.headlineSmall.copy(
        fontWeight = if (isInput) FontWeight.SemiBold else FontWeight.Bold,
        color = if (isInput) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary
      ),
      textAlign = TextAlign.End,
      maxLines = 1
    )
  }
}

@Composable
private fun ConverterKeypad(
  currentInput: String,
  onInputChanged: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current

  fun appendDigit(d: String) {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    val next = if (currentInput == "0" && d != ".") d else currentInput + d
    onInputChanged(next)
  }

  fun appendDot() {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    if (!currentInput.contains(".")) {
      val next = if (currentInput.isEmpty()) "0." else "$currentInput."
      onInputChanged(next)
    }
  }

  fun backspace() {
    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    if (currentInput.isNotEmpty()) {
      val next = currentInput.dropLast(1)
      onInputChanged(if (next.isEmpty()) "0" else next)
    }
  }

  fun clear() {
    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    onInputChanged("0")
  }

  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    val rows = listOf(
      listOf("7", "8", "9"),
      listOf("4", "5", "6"),
      listOf("1", "2", "3"),
      listOf("C", "0", ".")
    )

    rows.forEachIndexed { rowIndex, row ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        row.forEach { key ->
          Box(
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(
                if (key == "C") MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
              )
              .clickable {
                when (key) {
                  "C" -> clear()
                  "." -> appendDot()
                  else -> appendDigit(key)
                }
              }
              .testTag("converter_key_$key"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = key,
              fontSize = 20.sp,
              fontWeight = FontWeight.Medium,
              color = if (key == "C") MaterialTheme.colorScheme.onSecondaryContainer
              else MaterialTheme.colorScheme.onSurface
            )
          }
        }

        // Add backspace button to last row or beside
        if (rowIndex == 3) {
          Box(
            modifier = Modifier
              .weight(1f)
              .height(52.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .clickable { backspace() }
              .testTag("converter_backspace"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Backspace,
              contentDescription = "Backspace",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }
  }
}

private fun getCategoryIcon(category: UnitCategory): ImageVector {
  return when (category) {
    UnitCategory.LENGTH -> Icons.Default.Straighten
    UnitCategory.WEIGHT -> Icons.Default.Scale
    UnitCategory.TEMPERATURE -> Icons.Default.Thermostat
    UnitCategory.AREA -> Icons.Default.CropSquare
    UnitCategory.VOLUME -> Icons.Default.Opacity
    UnitCategory.DATA -> Icons.Default.Memory
    UnitCategory.SPEED -> Icons.Default.Speed
    UnitCategory.TIME -> Icons.Default.Schedule
  }
}
