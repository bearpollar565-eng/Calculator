package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CalculationHistory
import com.example.data.CalculatorDatabase
import com.example.data.HistoryRepository
import com.example.model.CalculationEngine
import com.example.model.ConversionUnit
import com.example.model.EvaluationResult
import com.example.model.UnitCategory
import com.example.model.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CalculatorUiState(
  val expression: String = "",
  val liveResult: String = "",
  val finalResult: String? = null,
  val errorMessage: String? = null,
  val isRadMode: Boolean = false,
  val isSecondMode: Boolean = false,
  val memoryValue: Double? = null,
  val isScientificExpanded: Boolean = false,
  val isHistorySheetVisible: Boolean = false,
  val activeTab: Int = 0 // 0: Calculator, 1: Converter
)

data class UnitConverterUiState(
  val selectedCategory: UnitCategory = UnitCategory.LENGTH,
  val fromUnit: ConversionUnit = UnitConverter.getUnitsForCategory(UnitCategory.LENGTH)[0],
  val toUnit: ConversionUnit = UnitConverter.getUnitsForCategory(UnitCategory.LENGTH)[1],
  val inputAmount: String = "1",
  val outputAmount: String = "1000"
)

class CalculatorViewModel(application: Application) : AndroidViewModel(application) {

  private val repository: HistoryRepository

  private val _uiState = MutableStateFlow(CalculatorUiState())
  val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

  private val _converterState = MutableStateFlow(UnitConverterUiState())
  val converterState: StateFlow<UnitConverterUiState> = _converterState.asStateFlow()

  val historyList: StateFlow<List<CalculationHistory>>

  private var justCalculated = false

  init {
    val db = CalculatorDatabase.getDatabase(application)
    repository = HistoryRepository(db.historyDao())
    historyList = repository.allHistory.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )
    updateConverterResult()
  }

  // MARK: - Calculator Input Handling

  fun onDigitClick(digit: String) {
    _uiState.update { state ->
      val newExpr = if (justCalculated) {
        justCalculated = false
        digit
      } else {
        if (state.expression == "0") digit else state.expression + digit
      }
      state.copy(
        expression = newExpr,
        errorMessage = null,
        finalResult = null
      )
    }
    updateLiveResult()
  }

  fun onDecimalClick() {
    _uiState.update { state ->
      val expr = if (justCalculated) {
        justCalculated = false
        "0"
      } else {
        state.expression
      }

      val lastNumberSegment = getLastNumberSegment(expr)
      val newExpr = if (lastNumberSegment.contains(".")) {
        expr // already contains decimal
      } else if (expr.isEmpty() || isLastCharOperator(expr) || expr.endsWith("(")) {
        expr + "0."
      } else {
        expr + "."
      }
      state.copy(
        expression = newExpr,
        errorMessage = null,
        finalResult = null
      )
    }
    updateLiveResult()
  }

  fun onOperatorClick(op: String) {
    justCalculated = false
    _uiState.update { state ->
      val expr = state.expression.trim()
      val newExpr = if (expr.isEmpty()) {
        if (op == "−") "−" else ""
      } else if (isLastCharOperator(expr)) {
        // Replace previous operator
        expr.dropLast(1).trim() + " " + op + " "
      } else {
        expr + " " + op + " "
      }
      state.copy(
        expression = newExpr,
        errorMessage = null,
        finalResult = null
      )
    }
    updateLiveResult()
  }

  fun onFunctionClick(funcName: String) {
    justCalculated = false
    _uiState.update { state ->
      val expr = state.expression.trim()
      val toAppend = when (funcName) {
        "x²" -> "^2"
        "x^y" -> "^"
        "√" -> "√("
        "∛" -> "∛("
        "1/x" -> "^(-1)"
        "n!" -> "!"
        "π" -> "π"
        "e" -> "e"
        "|x|" -> "abs("
        else -> "$funcName("
      }

      // Check if we need implicit multiplication before function/constant
      val newExpr = if (expr.isNotEmpty() && (expr.last().isDigit() || expr.endsWith(")") || expr.endsWith("π") || expr.endsWith("e"))) {
        if (toAppend.startsWith("^") || toAppend == "!") {
          expr + toAppend
        } else {
          expr + " × " + toAppend
        }
      } else {
        expr + toAppend
      }

      state.copy(
        expression = newExpr,
        errorMessage = null,
        finalResult = null
      )
    }
    updateLiveResult()
  }

  fun onParenthesesClick() {
    justCalculated = false
    _uiState.update { state ->
      val expr = state.expression
      val openCount = expr.count { it == '(' }
      val closeCount = expr.count { it == ')' }

      val newExpr = if (openCount > closeCount && expr.isNotEmpty() && (expr.last().isDigit() || expr.endsWith(")") || expr.endsWith("π") || expr.endsWith("e"))) {
        expr + ")"
      } else if (expr.isNotEmpty() && (expr.last().isDigit() || expr.endsWith(")"))) {
        expr + " × ("
      } else {
        expr + "("
      }

      state.copy(
        expression = newExpr,
        errorMessage = null,
        finalResult = null
      )
    }
    updateLiveResult()
  }

  fun onPercentClick() {
    justCalculated = false
    _uiState.update { state ->
      val expr = state.expression
      if (expr.isNotEmpty() && !isLastCharOperator(expr)) {
        state.copy(
          expression = expr + "%",
          errorMessage = null,
          finalResult = null
        )
      } else {
        state
      }
    }
    updateLiveResult()
  }

  fun onToggleSignClick() {
    justCalculated = false
    _uiState.update { state ->
      val expr = state.expression.trim()
      if (expr.isEmpty()) return@update state

      val newExpr = if (expr.startsWith("−(") && expr.endsWith(")")) {
        expr.substring(2, expr.length - 1)
      } else if (expr.startsWith("−")) {
        expr.substring(1)
      } else {
        "−($expr)"
      }

      state.copy(
        expression = newExpr,
        errorMessage = null,
        finalResult = null
      )
    }
    updateLiveResult()
  }

  fun onBackspaceClick() {
    justCalculated = false
    _uiState.update { state ->
      var expr = state.expression.trimEnd()
      if (expr.isEmpty()) return@update state

      // If ends with a space and operator, strip both
      if (expr.length >= 3 && expr[expr.length - 2] == ' ' && isLastCharOperator(expr.trim())) {
        expr = expr.substring(0, expr.lastIndexOf(' ')).trim()
      } else {
        // Check for multi-character functions at the end
        val multiCharTokens = listOf("sin⁻¹(", "cos⁻¹(", "tan⁻¹(", "sin(", "cos(", "tan(", "log(", "ln(", "abs(", "√(", "∛(")
        val matchedToken = multiCharTokens.firstOrNull { expr.endsWith(it) }
        expr = if (matchedToken != null) {
          expr.dropLast(matchedToken.length)
        } else {
          expr.dropLast(1)
        }
      }

      state.copy(
        expression = expr,
        errorMessage = null,
        finalResult = null
      )
    }
    updateLiveResult()
  }

  fun onClearClick() {
    justCalculated = false
    _uiState.update {
      it.copy(
        expression = "",
        liveResult = "",
        finalResult = null,
        errorMessage = null
      )
    }
  }

  fun onEqualsClick() {
    val state = _uiState.value
    val expr = state.expression.trim()
    if (expr.isEmpty()) return

    when (val result = CalculationEngine.evaluate(expr, state.isRadMode)) {
      is EvaluationResult.Success -> {
        val formatted = result.formattedText
        justCalculated = true
        _uiState.update {
          it.copy(
            finalResult = formatted,
            liveResult = "",
            errorMessage = null
          )
        }
        // Save to Room DB
        viewModelScope.launch {
          repository.insert(expr, formatted)
        }
      }
      is EvaluationResult.Error -> {
        _uiState.update {
          it.copy(
            errorMessage = if (result.message.isNotEmpty()) result.message else "Error",
            finalResult = null
          )
        }
      }
    }
  }

  private fun updateLiveResult() {
    val state = _uiState.value
    val expr = state.expression.trim()
    if (expr.isEmpty() || isLastCharOperator(expr) || expr.endsWith("(") || expr.length < 2) {
      _uiState.update { it.copy(liveResult = "") }
      return
    }

    when (val result = CalculationEngine.evaluate(expr, state.isRadMode)) {
      is EvaluationResult.Success -> {
        _uiState.update { it.copy(liveResult = result.formattedText) }
      }
      is EvaluationResult.Error -> {
        _uiState.update { it.copy(liveResult = "") }
      }
    }
  }

  // MARK: - Scientific & Memory Controls

  fun toggleRadMode() {
    _uiState.update { it.copy(isRadMode = !it.isRadMode) }
    updateLiveResult()
  }

  fun toggleSecondMode() {
    _uiState.update { it.copy(isSecondMode = !it.isSecondMode) }
  }

  fun toggleScientificExpanded() {
    _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
  }

  fun setHistorySheetVisible(visible: Boolean) {
    _uiState.update { it.copy(isHistorySheetVisible = visible) }
  }

  fun setActiveTab(tab: Int) {
    _uiState.update { it.copy(activeTab = tab) }
  }

  fun onMemoryClear() {
    _uiState.update { it.copy(memoryValue = null) }
  }

  fun onMemoryRecall() {
    val mem = _uiState.value.memoryValue ?: return
    val formatted = CalculationEngine.formatNumber(mem)
    onDigitClick(formatted)
  }

  fun onMemoryAdd() {
    evaluateCurrentDouble { current ->
      _uiState.update {
        val newMem = (it.memoryValue ?: 0.0) + current
        it.copy(memoryValue = newMem)
      }
    }
  }

  fun onMemorySubtract() {
    evaluateCurrentDouble { current ->
      _uiState.update {
        val newMem = (it.memoryValue ?: 0.0) - current
        it.copy(memoryValue = newMem)
      }
    }
  }

  private fun evaluateCurrentDouble(block: (Double) -> Unit) {
    val state = _uiState.value
    val expr = state.finalResult ?: state.expression
    if (expr.isNotEmpty()) {
      when (val res = CalculationEngine.evaluate(expr, state.isRadMode)) {
        is EvaluationResult.Success -> block(res.value)
        else -> Unit
      }
    }
  }

  // MARK: - History Actions

  fun loadHistoryEntry(history: CalculationHistory, loadResultOnly: Boolean = false) {
    justCalculated = loadResultOnly
    _uiState.update {
      it.copy(
        expression = if (loadResultOnly) history.result else history.expression,
        finalResult = if (loadResultOnly) history.result else null,
        liveResult = "",
        errorMessage = null,
        isHistorySheetVisible = false
      )
    }
    if (!loadResultOnly) updateLiveResult()
  }

  fun deleteHistoryItem(id: Long) {
    viewModelScope.launch {
      repository.deleteById(id)
    }
  }

  fun clearAllHistory() {
    viewModelScope.launch {
      repository.clearAll()
    }
  }

  // MARK: - Unit Converter Logic

  fun setConverterCategory(category: UnitCategory) {
    val units = UnitConverter.getUnitsForCategory(category)
    _converterState.update {
      it.copy(
        selectedCategory = category,
        fromUnit = units[0],
        toUnit = if (units.size > 1) units[1] else units[0]
      )
    }
    updateConverterResult()
  }

  fun setConverterFromUnit(unit: ConversionUnit) {
    _converterState.update { it.copy(fromUnit = unit) }
    updateConverterResult()
  }

  fun setConverterToUnit(unit: ConversionUnit) {
    _converterState.update { it.copy(toUnit = unit) }
    updateConverterResult()
  }

  fun swapConverterUnits() {
    _converterState.update {
      it.copy(fromUnit = it.toUnit, toUnit = it.fromUnit)
    }
    updateConverterResult()
  }

  fun onConverterInputChanged(input: String) {
    _converterState.update { it.copy(inputAmount = input) }
    updateConverterResult()
  }

  private fun updateConverterResult() {
    val state = _converterState.value
    val num = state.inputAmount.toDoubleOrNull() ?: 0.0
    val converted = UnitConverter.convert(num, state.fromUnit, state.toUnit)
    val formatted = UnitConverter.formatConvertedValue(converted)
    _converterState.update { it.copy(outputAmount = formatted) }
  }

  // MARK: - Helpers

  private fun getLastNumberSegment(expr: String): String {
    val delims = charArrayOf('+', '−', '-', '×', '*', '÷', '/', '(', ')', '^')
    val lastIndex = expr.lastIndexOfAny(delims)
    return if (lastIndex == -1) expr else expr.substring(lastIndex + 1)
  }

  private fun isLastCharOperator(expr: String): Boolean {
    val trimmed = expr.trimEnd()
    if (trimmed.isEmpty()) return false
    val last = trimmed.last()
    return last == '+' || last == '−' || last == '-' || last == '×' || last == '*' || last == '÷' || last == '/' || last == '^'
  }
}
