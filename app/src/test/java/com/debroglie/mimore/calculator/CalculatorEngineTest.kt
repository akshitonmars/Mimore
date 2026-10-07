package com.debroglie.mimore.calculator

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CalculatorEngineTest {
    private fun value(expression: String): BigDecimal =
        (CalculatorEngine.evaluate(expression) as CalcResult.Success).value

    @Test fun precedence() = assertEquals(BigDecimal("14"), value("2+3*4"))
    @Test fun decimals() = assertEquals(BigDecimal("3.5"), value("1.25+2.25"))
    @Test fun negative() = assertEquals(BigDecimal("-10"), value("-5*2"))
    @Test fun percentage() = assertEquals(BigDecimal("0.1"), value("10%"))
    @Test fun divideByZero() = assert(CalculatorEngine.evaluate("1/0") is CalcResult.Error)
    @Test fun formatting() = assertEquals("1,234.5", CalculatorEngine.format(BigDecimal("1234.5000")))
    @Test fun recurringDecimalFormatsCleanly() = assertEquals("0.333333333333", CalculatorEngine.format(value("1/3")))
    @Test fun conventionalPercentAddition() = assertEquals(BigDecimal("110"), value("100+10%"))
    @Test fun conventionalPercentMultiplication() = assertEquals(BigDecimal("5"), value("50*10%"))
}
