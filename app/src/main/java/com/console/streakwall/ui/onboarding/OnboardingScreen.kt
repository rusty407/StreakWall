package com.console.streakwall.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.console.streakwall.R
import com.console.streakwall.data.WallpaperTheme
import com.console.streakwall.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onOnboardingComplete: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDatePicker by remember { mutableStateOf(false) }

    // Navigation is a side effect: run it once when completion flips, not on every recomposition.
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onOnboardingComplete()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.onboarding_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = uiState.habitName,
                onValueChange = viewModel::onHabitNameChanged,
                label = { Text(stringResource(R.string.onboarding_habit_label)) },
                placeholder = { Text(stringResource(R.string.onboarding_habit_placeholder)) },
                isError = uiState.showValidationError,
                supportingText = {
                    if (uiState.showValidationError) {
                        Text(stringResource(R.string.onboarding_habit_error))
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column {
                Text(
                    text = stringResource(R.string.onboarding_start_date_label),
                    style = MaterialTheme.typography.labelLarge
                )
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(DateUtils.formatForDisplay(uiState.startDateMillis))
                }
            }
        }

        item {
            Column {
                Text(
                    text = stringResource(R.string.onboarding_theme_label),
                    style = MaterialTheme.typography.labelLarge
                )
                ThemePicker(
                    selected = uiState.theme,
                    onThemeSelected = viewModel::onThemeChanged,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        item {
            Button(
                onClick = viewModel::onContinueClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Text(stringResource(R.string.onboarding_cta))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = uiState.startDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { picked ->
                        viewModel.onStartDateChanged(DateUtils.startOfDayMillis(DateUtils.toLocalDate(picked)))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun ThemePicker(
    selected: WallpaperTheme,
    onThemeSelected: (WallpaperTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        WallpaperTheme.entries.forEach { theme ->
            Card(
                onClick = { onThemeSelected(theme) },
                colors = CardDefaults.cardColors(
                    containerColor = if (theme == selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    RadioButton(selected = theme == selected, onClick = { onThemeSelected(theme) })
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(28.dp)
                            .background(theme.background, CircleShape)
                            .border(2.dp, theme.accent, CircleShape)
                    )
                    Text(theme.displayName, modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}
