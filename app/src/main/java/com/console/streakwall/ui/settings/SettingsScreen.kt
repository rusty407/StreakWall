package com.console.streakwall.ui.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.console.streakwall.R
import com.console.streakwall.data.WallpaperTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit
) {
    val preferences by viewModel.preferences.collectAsState()
    val saveConfirmed by viewModel.saveConfirmed.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val savedMessage = stringResource(R.string.settings_saved)

    var habitNameInput by remember(preferences?.habitName) {
        mutableStateOf(preferences?.habitName ?: "")
    }

    LaunchedEffect(saveConfirmed) {
        if (saveConfirmed) {
            snackbarHostState.showSnackbar(savedMessage)
            viewModel.consumeSaveConfirmation()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.settings_back_cd))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        val current = preferences ?: return@Scaffold

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column {
                    OutlinedTextField(
                        value = habitNameInput,
                        onValueChange = { habitNameInput = it },
                        label = { Text(stringResource(R.string.settings_habit_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton(
                        onClick = { viewModel.saveHabitName(habitNameInput) },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.settings_save))
                    }
                }
            }

            item {
                Column {
                    Text(
                        text = stringResource(R.string.settings_theme_label),
                        style = MaterialTheme.typography.labelLarge
                    )
                    WallpaperTheme.entries.forEach { theme ->
                        Card(
                            onClick = { viewModel.selectTheme(theme) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (theme == current.theme) {
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
                                RadioButton(selected = theme == current.theme, onClick = { viewModel.selectTheme(theme) })
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

            item {
                OutlinedButton(
                    onClick = viewModel::reapplyWallpaperNow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.settings_reapply_wallpaper))
                }
            }

            item {
                TextButton(onClick = onOpenPrivacyPolicy) {
                    Text(stringResource(R.string.settings_privacy_policy))
                }
            }
        }
    }
}
