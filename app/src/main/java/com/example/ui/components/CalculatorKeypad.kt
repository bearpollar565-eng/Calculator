package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.DarkEqualBackground
import com.example.ui.theme.DarkEqualText
import com.example.ui.theme.DarkKeyAction
import com.example.ui.theme.DarkKeyActionText
import com.example.ui.theme.DarkKeyNum
import com.example.ui.theme.DarkKeyNumText
import com.example.ui.theme.DarkKeyOp
import com.example.ui.theme.DarkKeyOpText
import com.example.ui.theme.LightEqualBackground
import com.example.ui.theme.LightEqualText
import com.example.ui.theme.LightKeyAction
import com.example.ui.theme.LightKeyActionText
import com.example.ui.theme.LightKeyNum
import com.example.ui.theme.LightKeyNumText
import com.example.ui.theme.LightKeyOp
import com.example.ui.theme.LightKeyOpText

enum class KeyType {
  NUMBER,
  OPERATOR,
  ACTION,
  EQUAL
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalculatorKeypad(
  isScientificExpanded: Boolean,
  onToggleScientific: () -> Unit,
  onDigitClick: (String) -> Unit,
  onOperatorClick: (String) -> Unit,
  onDecimalClick: () -> Unit,
  onEqualsClick: () -> Unit,
  onClearClick: () -> Unit,
  onBackspaceClick: () -> Unit,
  onParenthesesClick: () -> Unit,
  onPercentClick: () -> Unit,
  onToggleSignClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val haptic = LocalHapticFeedback.current

  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 12.dp, vertical = 6.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    // Top Control Bar: Scientific Toggle & Backspace
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 4.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Toggle scientific panel pill
      Surface(
        modifier = Modifier
          .clip(RoundedCornerShape(16.dp))
          .combinedClickable(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onToggleScientific()
            }
          )
          .testTag("toggle_scientific_button"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = "fx",
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Icon(
            imageVector = if (isScientificExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isScientificExpanded) "Collapse scientific" else "Expand scientific",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // Backspace button with combined clickable (long-press to clear all)
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .combinedClickable(
            onClick = {
              haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
              onBackspaceClick()
            },
            onLongClick = {
              haptic.performHapticFeedback(HapticFeedbackType.LongPress)
              onClearClick()
            }
          )
          .testTag("backspace_button"),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Backspace,
          contentDescription = "Delete",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(22.dp)
        )
      }
    }

    // Row 1: AC, ( ), %, ÷
    KeypadRow {
      CalcKey(
        text = "AC",
        keyType = KeyType.ACTION,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onClearClick()
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "( )",
        keyType = KeyType.ACTION,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onParenthesesClick()
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "%",
        keyType = KeyType.ACTION,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onPercentClick()
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "÷",
        keyType = KeyType.OPERATOR,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onOperatorClick("÷")
        },
        modifier = Modifier.weight(1f)
      )
    }

    // Row 2: 7, 8, 9, ×
    KeypadRow {
      CalcKey(
        text = "7",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("7")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "8",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("8")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "9",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("9")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "×",
        keyType = KeyType.OPERATOR,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onOperatorClick("×")
        },
        modifier = Modifier.weight(1f)
      )
    }

    // Row 3: 4, 5, 6, −
    KeypadRow {
      CalcKey(
        text = "4",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("4")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "5",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("5")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "6",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("6")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "−",
        keyType = KeyType.OPERATOR,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onOperatorClick("−")
        },
        modifier = Modifier.weight(1f)
      )
    }

    // Row 4: 1, 2, 3, +
    KeypadRow {
      CalcKey(
        text = "1",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("1")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "2",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("2")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "3",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("3")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "+",
        keyType = KeyType.OPERATOR,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onOperatorClick("+")
        },
        modifier = Modifier.weight(1f)
      )
    }

    // Row 5: ±, 0, ., =
    KeypadRow {
      CalcKey(
        text = "±",
        keyType = KeyType.ACTION,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onToggleSignClick()
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "0",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDigitClick("0")
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = ".",
        keyType = KeyType.NUMBER,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
          onDecimalClick()
        },
        modifier = Modifier.weight(1f)
      )
      CalcKey(
        text = "=",
        keyType = KeyType.EQUAL,
        onClick = {
          haptic.performHapticFeedback(HapticFeedbackType.LongPress)
          onEqualsClick()
        },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun KeypadRow(content: @Composable () -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    content()
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CalcKey(
  text: String,
  keyType: KeyType,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onLongClick: (() -> Unit)? = null
) {
  val shape = RoundedCornerShape(22.dp)

  val (bgColor, textColor) = when (keyType) {
    KeyType.NUMBER -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurface
    KeyType.OPERATOR -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    KeyType.ACTION -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    KeyType.EQUAL -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
  }

  Box(
    modifier = modifier
      .height(60.dp)
      .clip(shape)
      .background(bgColor)
      .combinedClickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(bounded = true, color = textColor),
        onClick = onClick,
        onLongClick = onLongClick
      )
      .testTag("key_$text"),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      color = textColor,
      fontSize = when {
        keyType == KeyType.OPERATOR || keyType == KeyType.EQUAL -> 26.sp
        text.length > 2 -> 18.sp
        else -> 24.sp
      },
      fontWeight = if (keyType == KeyType.NUMBER) FontWeight.Normal else FontWeight.SemiBold
    )
  }
}
