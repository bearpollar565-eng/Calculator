package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkKeySci
import com.example.ui.theme.DarkKeySciText

@Composable
fun ScientificKeypad(
  isExpanded: Boolean,
  isSecondMode: Boolean,
  isRadMode: Boolean,
  onSecondToggle: () -> Unit,
  onRadToggle: () -> Unit,
  onFunctionClick: (String) -> Unit,
  onMemoryClear: () -> Unit,
  onMemoryRecall: () -> Unit,
  onMemoryAdd: () -> Unit,
  onMemorySubtract: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current

  AnimatedVisibility(
    visible = isExpanded,
    enter = expandVertically() + fadeIn(),
    exit = shrinkVertically() + fadeOut(),
    modifier = modifier.fillMaxWidth()
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 4.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Row 1: 2nd, RAD/DEG, sin, cos, tan
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SciKey(
          label = "2nd",
          isActive = isSecondMode,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onSecondToggle()
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = if (isRadMode) "RAD" else "DEG",
          isActive = isRadMode,
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onRadToggle()
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = if (isSecondMode) "sin⁻¹" else "sin",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick(if (isSecondMode) "sin⁻¹" else "sin")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = if (isSecondMode) "cos⁻¹" else "cos",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick(if (isSecondMode) "cos⁻¹" else "cos")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = if (isSecondMode) "tan⁻¹" else "tan",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick(if (isSecondMode) "tan⁻¹" else "tan")
          },
          modifier = Modifier.weight(1f)
        )
      }

      // Row 2: Powers, Roots, Logs
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SciKey(
          label = "x^y",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick("x^y")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = "x²",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick("x²")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = if (isSecondMode) "∛" else "√",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick(if (isSecondMode) "∛" else "√")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = if (isSecondMode) "e^x" else "ln",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick(if (isSecondMode) "exp" else "ln")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = if (isSecondMode) "10^x" else "log",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (isSecondMode) {
              onFunctionClick("10^")
            } else {
              onFunctionClick("log")
            }
          },
          modifier = Modifier.weight(1f)
        )
      }

      // Row 3: Constants & Functions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SciKey(
          label = "π",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick("π")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = "e",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick("e")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = "n!",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick("n!")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = "1/x",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick("1/x")
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = "|x|",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onFunctionClick("|x|")
          },
          modifier = Modifier.weight(1f)
        )
      }

      // Row 4: Memory Operations
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        SciKey(
          label = "MC",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onMemoryClear()
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = "MR",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onMemoryRecall()
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = "M+",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onMemoryAdd()
          },
          modifier = Modifier.weight(1f)
        )
        SciKey(
          label = "M-",
          onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onMemorySubtract()
          },
          modifier = Modifier.weight(1f)
        )
      }
    }
  }
}

@Composable
fun SciKey(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isActive: Boolean = false
) {
  val shape = RoundedCornerShape(12.dp)
  val backgroundColor = if (isActive) {
    MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
  } else {
    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
  }
  val textColor = if (isActive) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }

  Box(
    modifier = modifier
      .height(42.dp)
      .clip(shape)
      .background(backgroundColor)
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = MaterialTheme.colorScheme.primary),
        onClick = onClick
      )
      .testTag("key_$label"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 14.sp,
      fontWeight = FontWeight.Medium
    )
  }
}
