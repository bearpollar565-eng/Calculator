package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CalculatorViewModel
import com.example.ui.components.CalculatorDisplay
import com.example.ui.components.CalculatorKeypad
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.ScientificKeypad
import com.example.ui.components.UnitConverterView
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        CalculatorApp()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorApp(
  viewModel: CalculatorViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val converterState by viewModel.converterState.collectAsStateWithLifecycle()
  val historyList by viewModel.historyList.collectAsStateWithLifecycle()

  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

  var showMenu by remember { mutableStateOf(false) }

  Scaffold(
    modifier = Modifier.fillMaxSize(),
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      CenterAlignedTopAppBar(
        title = {
          PrimaryTabRow(
            selectedTabIndex = uiState.activeTab,
            modifier = Modifier
              .fillMaxWidth(0.7f)
              .clip(RoundedCornerShape(12.dp)),
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ) {
            Tab(
              selected = uiState.activeTab == 0,
              onClick = { viewModel.setActiveTab(0) },
              text = { Text("Calculator", fontWeight = FontWeight.SemiBold) },
              icon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) },
              modifier = Modifier.testTag("tab_calculator")
            )
            Tab(
              selected = uiState.activeTab == 1,
              onClick = { viewModel.setActiveTab(1) },
              text = { Text("Convert", fontWeight = FontWeight.SemiBold) },
              icon = { Icon(Icons.Default.Straighten, contentDescription = null, modifier = Modifier.size(18.dp)) },
              modifier = Modifier.testTag("tab_convert")
            )
          }
        },
        actions = {
          // History Button with Badge
          IconButton(
            onClick = { viewModel.setHistorySheetVisible(true) },
            modifier = Modifier.testTag("history_button")
          ) {
            BadgedBox(
              badge = {
                if (historyList.isNotEmpty()) {
                  Badge {
                    Text(
                      text = if (historyList.size > 99) "99+" else historyList.size.toString(),
                      modifier = Modifier.testTag("history_badge_count")
                    )
                  }
                }
              }
            ) {
              Icon(
                imageVector = Icons.Default.History,
                contentDescription = "Calculation History",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }
          }

          // Overflow menu
          Box {
            IconButton(
              onClick = { showMenu = true },
              modifier = Modifier.testTag("overflow_menu_button")
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More options",
                tint = MaterialTheme.colorScheme.onSurface
              )
            }

            DropdownMenu(
              expanded = showMenu,
              onDismissRequest = { showMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text(if (uiState.isRadMode) "Switch to Degrees (DEG)" else "Switch to Radians (RAD)") },
                onClick = {
                  viewModel.toggleRadMode()
                  showMenu = false
                }
              )
              if (uiState.memoryValue != null) {
                DropdownMenuItem(
                  text = { Text("Clear Memory (MC)") },
                  onClick = {
                    viewModel.onMemoryClear()
                    showMenu = false
                  }
                )
              }
              DropdownMenuItem(
                text = { Text("Clear All History") },
                onClick = {
                  viewModel.clearAllHistory()
                  showMenu = false
                }
              )
            }
          }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(innerPadding)
    ) {
      if (uiState.activeTab == 0) {
        // Calculator Tab
        if (isLandscape) {
          // Landscape Layout: Split Scientific + Keypad side-by-side
          Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.weight(1f)) {
              CalculatorDisplay(
                uiState = uiState,
                onToggleRad = { viewModel.toggleRadMode() },
                onMemoryRecall = { viewModel.onMemoryRecall() },
                modifier = Modifier.weight(1f)
              )
              ScientificKeypad(
                isExpanded = true,
                isSecondMode = uiState.isSecondMode,
                isRadMode = uiState.isRadMode,
                onSecondToggle = { viewModel.toggleSecondMode() },
                onRadToggle = { viewModel.toggleRadMode() },
                onFunctionClick = { viewModel.onFunctionClick(it) },
                onMemoryClear = { viewModel.onMemoryClear() },
                onMemoryRecall = { viewModel.onMemoryRecall() },
                onMemoryAdd = { viewModel.onMemoryAdd() },
                onMemorySubtract = { viewModel.onMemorySubtract() }
              )
            }
            CalculatorKeypad(
              isScientificExpanded = false,
              onToggleScientific = { viewModel.toggleScientificExpanded() },
              onDigitClick = { viewModel.onDigitClick(it) },
              onOperatorClick = { viewModel.onOperatorClick(it) },
              onDecimalClick = { viewModel.onDecimalClick() },
              onEqualsClick = { viewModel.onEqualsClick() },
              onClearClick = { viewModel.onClearClick() },
              onBackspaceClick = { viewModel.onBackspaceClick() },
              onParenthesesClick = { viewModel.onParenthesesClick() },
              onPercentClick = { viewModel.onPercentClick() },
              onToggleSignClick = { viewModel.onToggleSignClick() },
              modifier = Modifier.weight(1f)
            )
          }
        } else {
          // Portrait Layout: Stacked Display, Optional Scientific panel, Main Keypad
          Column(modifier = Modifier.fillMaxSize()) {
            CalculatorDisplay(
              uiState = uiState,
              onToggleRad = { viewModel.toggleRadMode() },
              onMemoryRecall = { viewModel.onMemoryRecall() },
              modifier = Modifier.weight(1f)
            )

            // Scientific keys (expandable)
            ScientificKeypad(
              isExpanded = uiState.isScientificExpanded,
              isSecondMode = uiState.isSecondMode,
              isRadMode = uiState.isRadMode,
              onSecondToggle = { viewModel.toggleSecondMode() },
              onRadToggle = { viewModel.toggleRadMode() },
              onFunctionClick = { viewModel.onFunctionClick(it) },
              onMemoryClear = { viewModel.onMemoryClear() },
              onMemoryRecall = { viewModel.onMemoryRecall() },
              onMemoryAdd = { viewModel.onMemoryAdd() },
              onMemorySubtract = { viewModel.onMemorySubtract() }
            )

            // Primary Keypad
            CalculatorKeypad(
              isScientificExpanded = uiState.isScientificExpanded,
              onToggleScientific = { viewModel.toggleScientificExpanded() },
              onDigitClick = { viewModel.onDigitClick(it) },
              onOperatorClick = { viewModel.onOperatorClick(it) },
              onDecimalClick = { viewModel.onDecimalClick() },
              onEqualsClick = { viewModel.onEqualsClick() },
              onClearClick = { viewModel.onClearClick() },
              onBackspaceClick = { viewModel.onBackspaceClick() },
              onParenthesesClick = { viewModel.onParenthesesClick() },
              onPercentClick = { viewModel.onPercentClick() },
              onToggleSignClick = { viewModel.onToggleSignClick() }
            )

            Spacer(modifier = Modifier.height(8.dp))
          }
        }
      } else {
        // Unit Converter Tab
        UnitConverterView(
          converterState = converterState,
          onCategorySelected = { viewModel.setConverterCategory(it) },
          onFromUnitSelected = { viewModel.setConverterFromUnit(it) },
          onToUnitSelected = { viewModel.setConverterToUnit(it) },
          onSwapUnits = { viewModel.swapConverterUnits() },
          onInputChanged = { viewModel.onConverterInputChanged(it) }
        )
      }

      // History Modal Bottom Sheet
      if (uiState.isHistorySheetVisible) {
        HistoryBottomSheet(
          historyList = historyList,
          onDismiss = { viewModel.setHistorySheetVisible(false) },
          onSelectEntry = { entry ->
            viewModel.loadHistoryEntry(entry, loadResultOnly = false)
          },
          onDeleteItem = { id ->
            viewModel.deleteHistoryItem(id)
          },
          onClearAll = {
            viewModel.clearAllHistory()
          }
        )
      }
    }
  }
}
