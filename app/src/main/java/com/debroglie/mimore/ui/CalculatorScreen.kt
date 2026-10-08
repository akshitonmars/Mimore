package com.debroglie.mimore.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.debroglie.mimore.CalculatorViewModel
import com.debroglie.mimore.data.Accent
import com.debroglie.mimore.data.HistoryEntry
import com.debroglie.mimore.data.ThemeMode
import com.debroglie.mimore.haptics.HapticManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Sheet { HISTORY, SETTINGS, HELP }
private enum class KeyKind { NUMBER, UTILITY, OPERATOR, EQUAL }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(vm: CalculatorViewModel) {
    val palette = LocalMimorePalette.current
    var sheet by remember { mutableStateOf<Sheet?>(null) }
    var showClearHistoryConfirm by remember { mutableStateOf(false) }
    val haptics = remember { HapticManager() }
    val view = LocalView.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        palette.accent.copy(alpha = 0.07f),
                        Color.Transparent
                    ),
                    center = Offset(Float.POSITIVE_INFINITY, 0f),
                    radius = 850f
                )
            )
            .background(palette.background)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
        ) {
            TopBar(
                onHistory = {
                    haptics.selection(view, vm.settings.hapticsEnabled)
                    sheet = Sheet.HISTORY
                },
                onSettings = {
                    haptics.selection(view, vm.settings.hapticsEnabled)
                    sheet = Sheet.SETTINGS
                }
            )

            Spacer(Modifier.height(4.dp))

            DisplayArea(
                expression = vm.expression,
                display = vm.display,
                error = vm.error,
                palette = palette,
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.height(10.dp))

            Keypad(
                vm,
                haptics,
                compact = androidx.compose.ui.platform.LocalConfiguration
                    .current
                    .screenHeightDp < 600
            )
        }
    }

    sheet?.let { currentSheet ->
        ModalBottomSheet(
            onDismissRequest = { sheet = null },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            containerColor = palette.surface,
            contentColor = palette.textPrimary,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = palette.textTertiary.copy(alpha = 0.45f)
                )
            }
        ) {
            when (currentSheet) {
                Sheet.HISTORY -> HistorySheet(
                    history = vm.history,
                    palette = palette,
                    onUse = {
                        vm.useHistory(it)
                        sheet = null
                    },
                    onDelete = {
                        vm.deleteHistory(it.id)
                    },
                    onClear = {
                        showClearHistoryConfirm = true
                    }
                )

                Sheet.SETTINGS -> SettingsSheet(
                    vm,
                    palette,
                    onHelp = { sheet = Sheet.HELP }
                )

                Sheet.HELP -> HelpSheet(palette)
            }
        }
    }

    if (showClearHistoryConfirm) {
        AlertDialog(
            onDismissRequest = {
                showClearHistoryConfirm = false
            },
            title = {
                Text("Clear history?")
            },
            text = {
                Text(
                    "All saved calculations will be removed from this device."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.clearHistory()
                        showClearHistoryConfirm = false
                    }
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearHistoryConfirm = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TopBar(
    onHistory: () -> Unit,
    onSettings: () -> Unit
) {
    val p = LocalMimorePalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                "Mimore",
                style = MaterialTheme.typography.titleLarge,
                color = p.textPrimary
            )

            Text(
                "BY DEBROGLIE",
                style = MaterialTheme.typography.labelMedium,
                color = p.textTertiary,
                letterSpacing = 1.7.sp
            )
        }

        TopIconButton(
            icon = {
                androidx.compose.material3.Icon(
                    Icons.Rounded.History,
                    null,
                    tint = p.textSecondary
                )
            },
            description = "Calculation history",
            onClick = onHistory
        )

        Spacer(Modifier.width(2.dp))

        TopIconButton(
            icon = {
                androidx.compose.material3.Icon(
                    Icons.Rounded.Settings,
                    null,
                    tint = p.textSecondary
                )
            },
            description = "Settings",
            onClick = onSettings
        )
    }
}

@Composable
private fun TopIconButton(
    icon: @Composable () -> Unit,
    description: String,
    onClick: () -> Unit
) {
    val p = LocalMimorePalette.current

    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(p.surface.copy(alpha = 0.75f))
            .clickable(onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
private fun DisplayArea(
    expression: String,
    display: String,
    error: String?,
    palette: MimorePalette,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        if (expression.isNotBlank()) {
            Text(
                text = styledExpression(expression, palette),
                style = MaterialTheme.typography.bodyLarge,
                color = palette.textSecondary,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.animateContentSize()
            )

            Spacer(Modifier.height(7.dp))
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth()
        ) {
            val size = when {
                display.length <= 8 -> 58.sp
                display.length <= 12 -> 48.sp
                display.length <= 16 -> 40.sp
                display.length <= 22 -> 32.sp
                else -> 26.sp
            }

            AnimatedContent(
                targetState = display,
                transitionSpec = {
                    (
                        fadeIn() +
                            slideInVertically { it / 3 }
                        ) togetherWith (
                        fadeOut() +
                            slideOutVertically { -it / 5 }
                        ) using SizeTransform(clip = false)
                },
                label = "result-transition",
                modifier = Modifier.fillMaxWidth()
            ) { target ->
                Text(
                    target,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = size
                    ),
                    color = if (error == null) {
                        palette.textPrimary
                    } else {
                        palette.danger
                    },
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        AnimatedVisibility(
            visible = error != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                error.orEmpty(),
                style = MaterialTheme.typography.labelMedium,
                color = palette.danger,
                modifier = Modifier.padding(top = 7.dp)
            )
        }
    }
}

private fun styledExpression(
    expression: String,
    palette: MimorePalette
): AnnotatedString =
    buildAnnotatedString {
        expression.forEach { c ->
            if (
                c == '+' ||
                c == '−' ||
                c == '×' ||
                c == '÷' ||
                c == '%'
            ) {
                withStyle(
                    SpanStyle(
                        color = palette.accent,
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(c)
                }
            } else {
                append(c)
            }
        }
    }

@Composable
private fun Keypad(
    vm: CalculatorViewModel,
    haptics: HapticManager,
    compact: Boolean
) {
    val hapticView = LocalView.current

    val rows = listOf(
        listOf(
            KeySpec("C", KeyKind.UTILITY),
            KeySpec("⌫", KeyKind.UTILITY),
            KeySpec("%", KeyKind.UTILITY),
            KeySpec("÷", KeyKind.OPERATOR)
        ),
        listOf(
            KeySpec("7", KeyKind.NUMBER),
            KeySpec("8", KeyKind.NUMBER),
            KeySpec("9", KeyKind.NUMBER),
            KeySpec("×", KeyKind.OPERATOR)
        ),
        listOf(
            KeySpec("4", KeyKind.NUMBER),
            KeySpec("5", KeyKind.NUMBER),
            KeySpec("6", KeyKind.NUMBER),
            KeySpec("−", KeyKind.OPERATOR)
        ),
        listOf(
            KeySpec("1", KeyKind.NUMBER),
            KeySpec("2", KeyKind.NUMBER),
            KeySpec("3", KeyKind.NUMBER),
            KeySpec("+", KeyKind.OPERATOR)
        ),
        listOf(
            KeySpec("0", KeyKind.NUMBER, span = 2),
            KeySpec(".", KeyKind.NUMBER),
            KeySpec("=", KeyKind.EQUAL)
        )
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { spec ->
                    KeyButton(
                        text = spec.label,
                        kind = spec.kind,
                        span = spec.span,
                        haptics = haptics,
                        hapticsEnabled = vm.settings.hapticsEnabled,
                        hapticLevel = vm.settings.hapticLevel,
                        onClick = {
                            when (spec.label) {
                                "C" -> vm.clear()
                                "⌫" -> vm.delete()
                                "%" -> vm.percent()

                                "÷", "×", "−", "+" ->
                                    vm.appendOperator(spec.label)

                                "=" -> {
                                    vm.equals()

                                    if (vm.error != null) {
                                        haptics.error(
                                            hapticView,
                                            vm.settings.hapticsEnabled
                                        )
                                    }
                                }

                                "." -> vm.appendDigit(".")

                                else -> vm.appendDigit(spec.label)
                            }
                        },
                        keyHeight = if (compact) 56.dp else 70.dp,
                        accessibility = when (spec.label) {
                            "÷" -> "divide"
                            "×" -> "multiply"
                            "−" -> "subtract"
                            "+" -> "add"
                            "=" -> "equals"
                            "⌫" -> "delete last digit"
                            "C" -> "clear"
                            else -> spec.label
                        }
                    )
                }
            }
        }
    }
}

private data class KeySpec(
    val label: String,
    val kind: KeyKind,
    val span: Int = 1
)

@Composable
private fun RowScope.KeyButton(
    text: String,
    kind: KeyKind,
    span: Int,
    haptics: HapticManager,
    hapticsEnabled: Boolean,
    hapticLevel: Int,
    keyHeight: Dp,
    accessibility: String,
    onClick: () -> Unit
) {
    val p = LocalMimorePalette.current
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsStateCompat()
    val hapticView = LocalView.current

    val scale by androidx.compose.animation.core.animateFloatAsState(
        if (pressed) 0.965f else 1f,
        label = "key-scale"
    )

    val background by animateColorAsState(
        when {
            kind == KeyKind.EQUAL -> p.accent
            pressed -> p.keyPressed
            kind == KeyKind.OPERATOR -> p.operator
            else -> p.key
        },
        label = "key-background"
    )

    LaunchedEffect(
        interaction,
        hapticsEnabled,
        hapticLevel
    ) {
        interaction.interactions.collect { signal ->
            if (signal is PressInteraction.Press) {
                when (text) {
                    "=" -> haptics.confirm(
                        hapticView,
                        hapticsEnabled,
                        hapticLevel
                    )

                    "C" -> haptics.clear(
                        hapticView,
                        hapticsEnabled
                    )

                    "⌫" -> haptics.delete(
                        hapticView,
                        hapticsEnabled
                    )

                    else -> haptics.tap(
                        hapticView,
                        hapticsEnabled,
                        hapticLevel
                    )
                }
            }
        }
    }

    Surface(
        modifier = Modifier
            .weight(span.toFloat())
            .height(keyHeight)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .semantics {
                contentDescription = accessibility
                role = Role.Button
            }
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        color = background,
        tonalElevation = if (kind == KeyKind.NUMBER) 0.dp else 1.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(
                Modifier.matchParentSize()
            ) {
                val highlight =
                    if (kind == KeyKind.EQUAL) {
                        Color.White.copy(alpha = 0.22f)
                    } else {
                        Color.White.copy(
                            alpha = if (pressed) 0.18f else 0.07f
                        )
                    }

                drawLine(
                    highlight,
                    Offset(18f, 1f),
                    Offset(size.width - 18f, 1f),
                    strokeWidth = 1.5f
                )

                if (pressed) {
                    drawCircle(
                        p.accent.copy(alpha = 0.12f),
                        radius = size.minDimension * 0.34f,
                        center = Offset(
                            size.width * 0.55f,
                            size.height * 0.25f
                        )
                    )
                }
            }

            if (text == "⌫") {
                Icon(
                    Icons.AutoMirrored.Rounded.Backspace,
                    contentDescription = null,
                    tint = p.textPrimary,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(24.dp)
                )
            } else {
                Text(
                    text,
                    modifier = Modifier.align(Alignment.Center),
                    color = if (kind == KeyKind.EQUAL) {
                        Color.White
                    } else {
                        p.textPrimary
                    },
                    style = when (kind) {
                        KeyKind.NUMBER ->
                            MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 27.sp,
                                fontWeight = FontWeight.Medium
                            )

                        KeyKind.OPERATOR,
                        KeyKind.EQUAL ->
                            MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Medium
                            )

                        KeyKind.UTILITY ->
                            MaterialTheme.typography.titleMedium.copy(
                                fontSize = 20.sp
                            )
                    }
                )
            }
        }
    }
}

@Composable
private fun HistorySheet(
    history: List<HistoryEntry>,
    palette: MimorePalette,
    onUse: (HistoryEntry) -> Unit,
    onDelete: (HistoryEntry) -> Unit,
    onClear: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    "History",
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    "Saved only on this device",
                    color = palette.textTertiary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (history.isNotEmpty()) {
                TextButton(onClick = onClear) {
                    Text("Clear all")
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        if (history.isEmpty()) {
            EmptyHistory(palette)
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(420.dp)
            ) {
                items(
                    history,
                    key = { it.id }
                ) { entry ->
                    HistoryRow(
                        entry,
                        palette,
                        onUse,
                        onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyHistory(
    palette: MimorePalette
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(palette.accentSoft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.History,
                null,
                tint = palette.accent,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Nothing here yet",
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            "Your calculations will appear here.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary
        )
    }
}

@Composable
private fun HistoryRow(
    entry: HistoryEntry,
    palette: MimorePalette,
    onUse: (HistoryEntry) -> Unit,
    onDelete: (HistoryEntry) -> Unit
) {
    Surface(
        color = palette.surfaceRaised,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onUse(entry)
            }
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    entry.expression,
                    color = palette.textSecondary,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    entry.result,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary
                )
            }

            Text(
                timeLabel(entry.timestamp),
                style = MaterialTheme.typography.labelMedium,
                color = palette.textTertiary
            )

            Spacer(Modifier.width(7.dp))

            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable {
                        onDelete(entry)
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete calculation",
                    tint = palette.textTertiary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun timeLabel(timestamp: Long): String =
    SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    ).format(Date(timestamp))

@Composable
private fun SettingsSheet(
    vm: CalculatorViewModel,
    palette: MimorePalette,
    onHelp: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 28.dp)
    ) {
        Text(
            "Settings",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(16.dp))

        SettingSection("Appearance")

        SettingChoiceRow(
            "Theme",
            themeLabel(vm.settings.themeMode),
            Icons.Rounded.Tune
        ) {
            CycleTheme(vm)
        }

        AccentChooser(vm)

        Spacer(Modifier.height(8.dp))

        SettingSection("Tactile")

        SettingSwitchRow(
            "Haptics",
            "Small feedback on interaction",
            vm.settings.hapticsEnabled
        ) {
            vm.updateSettings {
                it.copy(
                    hapticsEnabled = !it.hapticsEnabled
                )
            }
        }

        if (vm.settings.hapticsEnabled) {
            SettingSliderRow(vm)
        }

        Spacer(Modifier.height(8.dp))

        SettingSection("Calculator")

        SettingSwitchRow(
            "Digit grouping",
            "Show separators on longer results",
            vm.settings.grouping
        ) {
            vm.updateSettings {
                it.copy(
                    grouping = !it.grouping
                )
            }
        }

        SettingSwitchRow(
            "Keep history",
            "Store up to 100 recent calculations",
            vm.settings.keepHistory
        ) {
            vm.updateSettings {
                it.copy(
                    keepHistory = !it.keepHistory
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        SettingSection("About")

        SettingChoiceRow(
            "Help",
            "How Mimore works",
            Icons.Rounded.Info,
            onHelp
        )

        Text(
            "Mimore 1.0.0",
            color = palette.textTertiary,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 4.dp
            )
        )

        Text(
            "By DeBroglie",
            color = palette.textTertiary,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

@Composable
private fun SettingSection(title: String) {
    Text(
        title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = LocalMimorePalette.current.textTertiary,
        letterSpacing = 1.1.sp,
        modifier = Modifier.padding(
            bottom = 6.dp,
            top = 5.dp
        )
    )
}

@Composable
private fun SettingChoiceRow(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val p = LocalMimorePalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            tint = p.textSecondary,
            modifier = Modifier.size(20.dp)
        )

        Spacer(Modifier.width(14.dp))

        Column(
            Modifier.weight(1f)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                color = p.textSecondary
            )
        }

        Icon(
            Icons.Rounded.KeyboardArrowRight,
            null,
            tint = p.textTertiary
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    val p = LocalMimorePalette.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier.weight(1f)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = p.textSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = {
                onToggle()
            }
        )
    }
}

@Composable
private fun SettingSliderRow(
    vm: CalculatorViewModel
) {
    val p = LocalMimorePalette.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier.weight(1f)
            ) {
                Text(
                    "Haptic intensity",
                    style = MaterialTheme.typography.bodyLarge
                )

                Text(
                    "Subtle → firm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = p.textSecondary
                )
            }

            Text(
                vm.settings.hapticLevel.toString(),
                color = p.accent,
                style = MaterialTheme.typography.labelLarge
            )
        }

        Slider(
            value = vm.settings.hapticLevel.toFloat(),
            onValueChange = {
                vm.updateSettings { s ->
                    s.copy(
                        hapticLevel = it
                            .toInt()
                            .coerceIn(1, 3)
                    )
                }
            },
            valueRange = 1f..3f,
            steps = 1
        )
    }
}

@Composable
private fun AccentChooser(
    vm: CalculatorViewModel
) {
    val p = LocalMimorePalette.current

    Column(
        Modifier.padding(vertical = 8.dp)
    ) {
        Text(
            "Accent",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.height(7.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Accent.entries.forEach { accent ->
                val selected = vm.settings.accent == accent

                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.color)
                        .clickable {
                            vm.updateSettings {
                                it.copy(accent = accent)
                            }
                        }
                        .semantics {
                            contentDescription =
                                "${accent.label} accent"
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Canvas(
                            Modifier.size(10.dp)
                        ) {
                            drawCircle(Color.White)
                        }
                    }
                }
            }
        }

        Text(
            "${vm.settings.accent.label} accent",
            color = p.textSecondary,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

private fun CycleTheme(
    vm: CalculatorViewModel
) {
    val next = when (vm.settings.themeMode) {
        ThemeMode.SYSTEM -> ThemeMode.LIGHT
        ThemeMode.LIGHT -> ThemeMode.DARK
        ThemeMode.DARK -> ThemeMode.SYSTEM
    }

    vm.updateSettings {
        it.copy(themeMode = next)
    }
}

private fun themeLabel(
    mode: ThemeMode
) = when (mode) {
    ThemeMode.SYSTEM -> "System"
    ThemeMode.LIGHT -> "Light"
    ThemeMode.DARK -> "Dark"
}

@Composable
private fun HelpSheet(
    palette: MimorePalette
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 30.dp)
    ) {
        Text(
            "How Mimore works",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(14.dp))

        HelpItem(
            "Calculate",
            "Tap numbers and operators exactly as you would on a physical calculator. Mimore respects operator precedence."
        )

        HelpItem(
            "Percent",
            "10% is 0.1 on its own. In addition and subtraction, Mimore treats it as a percentage of the left side."
        )

        HelpItem(
            "History",
            "Tap the history button to reuse a previous result, or remove individual entries."
        )

        HelpItem(
            "Themes",
            "Choose the system, light or dark appearance and a restrained accent color."
        )

        HelpItem(
            "Haptics",
            "Haptics are native Android feedback and can be disabled or softened at any time."
        )

        Surface(
            color = palette.accentSoft,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Row(
                Modifier.padding(15.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.AccessibilityNew,
                    null,
                    tint = palette.accent
                )

                Spacer(Modifier.width(10.dp))

                Text(
                    "Every primary control uses accessible labels and touch targets.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun HelpItem(
    title: String,
    body: String
) {
    val p = LocalMimorePalette.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(Modifier.height(2.dp))

        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = p.textSecondary
        )
    }
}

@Composable
private fun MutableInteractionSource.collectIsPressedAsStateCompat():
    androidx.compose.runtime.State<Boolean> {
    val state = remember {
        mutableStateOf(false)
    }

    LaunchedEffect(this) {
        interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press ->
                    state.value = true

                is PressInteraction.Release,
                is PressInteraction.Cancel ->
                    state.value = false
            }
        }
    }

    return state
}