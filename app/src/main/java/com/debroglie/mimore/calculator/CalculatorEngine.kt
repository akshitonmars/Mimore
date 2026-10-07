package com.debroglie.mimore.calculator

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

sealed interface CalcResult {
    data class Success(val value: BigDecimal) : CalcResult
    data class Error(val message: String) : CalcResult
}

/**
 * Small, deterministic expression engine for the calculator UI.
 * Supported operators: +, -, ×, ÷ and postfix %.
 * Unary +/- are accepted and operator precedence is respected.
 */
object CalculatorEngine {
    private val mathContext = MathContext(40, RoundingMode.HALF_UP)

    fun evaluate(expression: String): CalcResult {
        val normalized = expression
            .replace('×', '*')
            .replace('÷', '/')
            .replace(" ", "")

        if (normalized.isBlank()) return CalcResult.Error("Nothing to calculate")
        return try {
            val tokens = tokenize(normalized)
            val rpn = toRpn(tokens)
            val value = evalRpn(rpn)
            CalcResult.Success(value.stripTrailingZeros())
        } catch (e: ArithmeticException) {
            CalcResult.Error("Cannot divide by zero")
        } catch (e: IllegalArgumentException) {
            CalcResult.Error(e.message ?: "Invalid expression")
        }
    }

    private sealed interface Token {
        data class Number(val value: BigDecimal, val percent: Boolean = false) : Token
        data class Op(val symbol: Char) : Token
        data object LeftParen : Token
        data object RightParen : Token
    }

    private fun tokenize(input: String): List<Token> {
        val out = mutableListOf<Token>()
        var i = 0
        var expectingValue = true

        while (i < input.length) {
            val ch = input[i]
            when {
                ch.isDigit() || ch == '.' -> {
                    val start = i
                    var dots = 0
                    var digits = 0
                    while (i < input.length && (input[i].isDigit() || input[i] == '.')) {
                        if (input[i] == '.') dots++ else digits++
                        i++
                    }
                    if (dots > 1 || digits == 0) throw IllegalArgumentException("Invalid number")
                    val raw = input.substring(start, i)
                    out += Token.Number(raw.toBigDecimal(mathContext))
                    expectingValue = false
                }
                ch == '(' -> {
                    out += Token.LeftParen
                    expectingValue = true
                    i++
                }
                ch == ')' -> {
                    if (expectingValue) throw IllegalArgumentException("Invalid expression")
                    out += Token.RightParen
                    expectingValue = false
                    i++
                }
                ch == '%' -> {
                    if (out.lastOrNull() !is Token.Number) throw IllegalArgumentException("Percent needs a number")
                    val last = out.removeLast() as Token.Number
                    out += last.copy(percent = true)
                    i++
                }
                ch == '+' || ch == '-' -> {
                    if (expectingValue) {
                        if (ch == '-') {
                            out += Token.Number(BigDecimal.ZERO)
                            out += Token.Op('-')
                        } else {
                            // Unary plus has no effect.
                        }
                    } else {
                        out += Token.Op(ch)
                        expectingValue = true
                    }
                    i++
                }
                ch == '*' || ch == '/' -> {
                    if (expectingValue) throw IllegalArgumentException("Operator needs a number")
                    out += Token.Op(ch)
                    expectingValue = true
                    i++
                }
                else -> throw IllegalArgumentException("Invalid character")
            }
        }

        if (expectingValue) throw IllegalArgumentException("Incomplete expression")
        return out
    }

    private fun precedence(op: Char): Int = when (op) {
        '+', '-' -> 1
        '*', '/' -> 2
        else -> 0
    }

    private fun toRpn(tokens: List<Token>): List<Token> {
        val out = mutableListOf<Token>()
        val ops = ArrayDeque<Token.Op>()

        for (token in tokens) {
            when (token) {
                is Token.Number -> out += token
                is Token.Op -> {
                    while (ops.isNotEmpty() && precedence(ops.last().symbol) >= precedence(token.symbol)) {
                        out += ops.removeLast()
                    }
                    ops += token
                }
                Token.LeftParen -> ops += Token.Op('(')
                Token.RightParen -> {
                    var found = false
                    while (ops.isNotEmpty()) {
                        val op = ops.removeLast()
                        if (op.symbol == '(') {
                            found = true
                            break
                        }
                        out += op
                    }
                    if (!found) throw IllegalArgumentException("Mismatched parentheses")
                }
            }
        }
        while (ops.isNotEmpty()) {
            val op = ops.removeLast()
            if (op.symbol == '(') throw IllegalArgumentException("Mismatched parentheses")
            out += op
        }
        return out
    }

    private data class Operand(val value: BigDecimal, val wasPercent: Boolean = false)

    private fun evalRpn(rpn: List<Token>): BigDecimal {
        val stack = ArrayDeque<Operand>()
        for (token in rpn) {
            when (token) {
                is Token.Number -> {
                    stack += Operand(
                        value = if (token.percent) token.value.divide(BigDecimal(100), mathContext) else token.value,
                        wasPercent = token.percent
                    )
                }
                is Token.Op -> {
                    if (stack.size < 2) throw IllegalArgumentException("Invalid expression")
                    val right = stack.removeLast()
                    val left = stack.removeLast()
                    val result = if (right.wasPercent && (token.symbol == '+' || token.symbol == '-')) {
                        val percentOfLeft = left.value.multiply(right.value, mathContext)
                        if (token.symbol == '+') left.value.add(percentOfLeft, mathContext)
                        else left.value.subtract(percentOfLeft, mathContext)
                    } else {
                        when (token.symbol) {
                            '+' -> left.value.add(right.value, mathContext)
                            '-' -> left.value.subtract(right.value, mathContext)
                            '*' -> left.value.multiply(right.value, mathContext)
                            '/' -> left.value.divide(right.value, mathContext)
                            else -> throw IllegalArgumentException("Invalid operator")
                        }
                    }
                    stack += Operand(result)
                }
                Token.LeftParen, Token.RightParen -> error("RPN cannot contain parentheses")
            }
        }
        if (stack.size != 1) throw IllegalArgumentException("Invalid expression")
        return stack.single().value
    }

    fun sanitizeExpression(display: String): String = display
        .replace('×', '*')
        .replace('÷', '/')
        .replace(" ", "")

    fun format(value: BigDecimal, useGrouping: Boolean = true): String {
        val clean = value.stripTrailingZeros()
        if (clean.compareTo(BigDecimal.ZERO) == 0) return "0"
        val abs = clean.abs()
        val needsScientific = abs >= BigDecimal("1000000000000000") || (abs > BigDecimal.ZERO && abs < BigDecimal("0.000000001"))
        if (needsScientific) return clean.round(MathContext(16, RoundingMode.HALF_UP)).toEngineeringString()

        val rounded = if (clean.scale() > 12) clean.setScale(12, RoundingMode.HALF_UP).stripTrailingZeros() else clean
        val plain = rounded.toPlainString()
        val sign = if (plain.startsWith("-")) "-" else ""
        val unsigned = plain.removePrefix("-")
        val pieces = unsigned.split('.')
        val integer = if (!useGrouping) pieces[0] else pieces[0].reversed().chunked(3).joinToString(",").reversed()
        return buildString {
            append(sign).append(integer)
            if (pieces.size == 2) append('.').append(pieces[1])
        }
    }
}
