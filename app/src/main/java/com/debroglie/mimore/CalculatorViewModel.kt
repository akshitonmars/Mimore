package com.debroglie.mimore

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.debroglie.mimore.calculator.CalcResult
import com.debroglie.mimore.calculator.CalculatorEngine
import com.debroglie.mimore.data.AppSettings
import com.debroglie.mimore.data.HistoryEntry
import com.debroglie.mimore.data.LocalStore

class CalculatorViewModel(app: Application) : AndroidViewModel(app) {
    private val store = LocalStore(app)

    var expression by mutableStateOf("")
        private set
    var display by mutableStateOf("0")
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var justEvaluated by mutableStateOf(false)
        private set
    var history by mutableStateOf(store.loadHistory())
        private set
    var settings by mutableStateOf(store.loadSettings())
        private set

    private var lastOperand: String? = null
    private var lastOperator: String? = null

    fun appendDigit(digit: String) {
        if (error != null) {
            expression = ""
            display = "0"
            justEvaluated = false
        }
        error = null
        if (justEvaluated) {
            expression = ""
            display = "0"
            justEvaluated = false
            lastOperand = null
            lastOperator = null
        }
        val current = currentNumber()
        if (digit == ".") {
            if (current.contains('.')) return
            replaceCurrentNumber(if (current.isBlank()) "0." else "$current.")
        } else {
            val clean = current.trimStart('0')
            val next = if (clean.isEmpty()) digit else clean + digit
            replaceCurrentNumber(next)
        }
        display = currentNumber().ifEmpty { "0" }
    }

    fun appendOperator(op: String) {
        if (error != null) {
            clear()
        }
        error = null
        if (expression.isBlank()) {
            if (op == "−") {
                expression = "−"
                display = "−"
            }
            return
        }
        if (justEvaluated) {
            expression = display
            justEvaluated = false
        }
        val normalized = expression.trimEnd()
        if (normalized.lastOrNull()?.let { it == '+' || it == '−' || it == '×' || it == '÷' } == true) {
            expression = normalized.dropLast(1).trimEnd() + " $op "
        } else {
            expression = "$normalized $op "
        }
        display = "0"
    }

    fun toggleSign() {
        error = null
        if (justEvaluated) {
            val normalized = if (display.startsWith("-") || display.startsWith("−")) display.removePrefix("-").removePrefix("−") else "-$display"
            expression = normalized
            display = normalized
            justEvaluated = false
            return
        }
        val current = currentNumber()
        if (current.isBlank()) {
            expression = if (expression.isBlank()) "−" else expression + "−"
            display = "−"
            return
        }
        val next = when {
            current == "−" -> ""
            current.startsWith("−") -> current.removePrefix("−")
            current.startsWith("-") -> current.removePrefix("-")
            else -> "−$current"
        }
        replaceCurrentNumber(next)
        display = next.ifBlank { "0" }
    }

    fun percent() {
        error = null
        if (justEvaluated) return
        val current = currentNumber()
        if (current.isBlank() || current == "−") return
        replaceCurrentNumber("$current%")
        display = current.removeSuffix("%")
    }

    fun delete() {
        if (error != null) {
            clear()
            return
        }
        error = null
        if (justEvaluated) {
            expression = ""
            display = "0"
            justEvaluated = false
            return
        }
        expression = expression.trimEnd()
        if (expression.isBlank()) {
            display = "0"
            return
        }
        expression = expression.dropLast(1).trimEnd()
        display = currentNumber().ifBlank { "0" }
    }

    fun clear() {
        expression = ""
        display = "0"
        error = null
        justEvaluated = false
        lastOperand = null
        lastOperator = null
    }

    fun equals() {
        error = null
        val targetExpression = when {
            expression.isBlank() && lastOperator != null && lastOperand != null -> "$display ${lastOperator!!.trim()} $lastOperand"
            justEvaluated && lastOperator != null && lastOperand != null -> "$display ${lastOperator!!.trim()} $lastOperand"
            else -> expression
        }
        if (targetExpression.isBlank() || targetExpression == "−") return

        val result = CalculatorEngine.evaluate(targetExpression.replace('−', '-'))
        when (result) {
            is CalcResult.Success -> {
                val formatted = CalculatorEngine.format(result.value, settings.grouping)
                val binary = extractLastBinary(targetExpression)
                if (binary != null) {
                    lastOperator = binary.first
                    lastOperand = binary.second
                }
                val entry = HistoryEntry(System.currentTimeMillis(), prettyExpression(targetExpression), formatted, System.currentTimeMillis())
                history = listOf(entry) + history.filterNot { it.expression == entry.expression && it.result == entry.result }.take(99)
                if (settings.keepHistory) store.saveHistory(history)
                expression = formatted
                display = formatted
                justEvaluated = true
            }
            is CalcResult.Error -> {
                error = result.message
                display = "Error"
            }
        }
    }

    fun useHistory(entry: HistoryEntry) {
        expression = entry.result
        display = entry.result
        error = null
        justEvaluated = true
    }

    fun deleteHistory(id: Long) {
        history = history.filterNot { it.id == id }
        store.saveHistory(history)
    }

    fun clearHistory() {
        history = emptyList()
        store.saveHistory(history)
    }

    fun updateSettings(transform: (AppSettings) -> AppSettings) {
        settings = transform(settings)
        store.saveSettings(settings)
        if (!settings.keepHistory) {
            history = emptyList()
            store.saveHistory(emptyList())
        }
    }

    private fun currentNumber(): String {
        val segment = expression.substringAfterLast(' ').ifBlank { expression }
        return segment
    }

    private fun replaceCurrentNumber(value: String) {
        val trimmed = expression.trimEnd()
        val lastSpace = trimmed.lastIndexOf(' ')
        expression = if (lastSpace >= 0) trimmed.substring(0, lastSpace + 1) + value else value
    }

    private fun extractLastBinary(raw: String): Pair<String, String>? {
        val m = Regex("^(.+)\\s([+−×÷])\\s([−]?[0-9.]+%?)$").find(raw.trim()) ?: return null
        return m.groupValues[2] to m.groupValues[3]
    }

    private fun prettyExpression(raw: String): String = raw
        .replace("+", " + ")
        .replace("−", " − ")
        .replace("*", " × ")
        .replace("/", " ÷ ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
