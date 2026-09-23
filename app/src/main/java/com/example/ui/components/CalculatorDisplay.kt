package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CalculatorUiState

@Composable
fun CalculatorDisplay(
  uiState: CalculatorUiState,
  onToggleRad: () -> Unit,
  onMemoryRecall: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val exprScrollState = rememberScrollState()

  // Auto-scroll to end of expression when it changes
  LaunchedEffect(uiState.expression, uiState.finalResult) {
    exprScrollState.animateScrollTo(exprScrollState.maxValue)
  }

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 8.dp)
      .testTag("calculator_display"),
    shape = RoundedCornerShape(24.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
    tonalElevation = 2.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
        .animateContentSize(),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.Bottom
    ) {
      // Top status badges bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // DEG/RAD Toggle Chip
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .clickable { onToggleRad() }
              .testTag("deg_rad_chip"),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            shape = RoundedCornerShape(8.dp)
          ) {
            Text(
              text = if (uiState.isRadMode) "RAD" else "DEG",
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            )
          }

          // Memory indicator
          AnimatedVisibility(
            visible = uiState.memoryValue != null,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Surface(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onMemoryRecall() }
                .testTag("memory_active_chip"),
              color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "M",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.secondary
                )
              )
            }
          }

          // INV Mode indicator
          AnimatedVisibility(
            visible = uiState.isSecondMode,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Surface(
              color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
              shape = RoundedCornerShape(8.dp)
            ) {
              Text(
                text = "2nd",
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.tertiary
                )
              )
            }
          }
        }

        // Copy button
        val textToCopy = uiState.finalResult ?: uiState.expression
        if (textToCopy.isNotEmpty()) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .clickable {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Calculation", textToCopy)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
              }
              .testTag("copy_button"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy result",
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Main Expression Display
      val expressionText = when {
        uiState.finalResult != null -> uiState.expression
        uiState.expression.isEmpty() -> "0"
        else -> uiState.expression
      }

      val isHistoryState = uiState.finalResult != null

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(exprScrollState),
        horizontalArrangement = Arrangement.End
      ) {
        Text(
          text = expressionText,
          style = if (isHistoryState) {
            MaterialTheme.typography.titleLarge.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Normal,
              fontSize = 22.sp
            )
          } else {
            MaterialTheme.typography.headlineLarge.copy(
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Light,
              fontSize = if (expressionText.length > 12) 32.sp else 44.sp,
              fontFamily = FontFamily.SansSerif
            )
          },
          textAlign = TextAlign.End,
          maxLines = 1,
          modifier = Modifier.testTag("expression_text")
        )
      }

      // Result or Preview Row
      Spacer(modifier = Modifier.height(4.dp))

      when {
        uiState.errorMessage != null -> {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(
              imageVector = Icons.Default.ErrorOutline,
              contentDescription = "Calculation Error",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = uiState.errorMessage,
              style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.SemiBold
              ),
              modifier = Modifier.testTag("error_text")
            )
          }
        }

        uiState.finalResult != null -> {
          Text(
            text = "= ${uiState.finalResult}",
            style = MaterialTheme.typography.headlineLarge.copy(
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
              fontSize = if (uiState.finalResult.length > 10) 36.sp else 48.sp
            ),
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.testTag("final_result_text")
          )
        }

        uiState.liveResult.isNotEmpty() -> {
          Text(
            text = "= ${uiState.liveResult}",
            style = MaterialTheme.typography.titleMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              fontWeight = FontWeight.Medium,
              fontSize = 20.sp
            ),
            textAlign = TextAlign.End,
            maxLines = 1,
            modifier = Modifier.testTag("live_result_text")
          )
        }

        else -> {
          // Subtle placeholder line to maintain height stability
          Spacer(modifier = Modifier.height(28.dp))
        }
      }
    }
  }
}
