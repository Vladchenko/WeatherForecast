package io.github.vladchenko.weatherforecast.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.core.domain.model.TemperatureUnit
import io.github.vladchenko.weatherforecast.core.navigation.NavigationEventBus
import io.github.vladchenko.weatherforecast.core.preferences.PreferencesManager
import io.github.vladchenko.weatherforecast.core.ui.component.BackgroundImage
import io.github.vladchenko.weatherforecast.core.ui.status.TextType
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.rememberResolvedColorAttr
import io.github.vladchenko.weatherforecast.core.ui.utils.UiUtils.toToolbarSubtitleFontSize
import io.github.vladchenko.weatherforecast.models.presentation.AppBarUiState
import io.github.vladchenko.weatherforecast.presentation.navigation.NavigationEvent

/**
 * Settings screen layout.
 *
 * Displays the settings UI with current preferences (temperature unit)
 * and allows the user to modify them.
 *
 * ## Supported Features
 * - Temperature unit selection (Celsius/Fahrenheit)
 * - Top app bar with back navigation
 *
 * @param appBarUiState The app bar UI state (title, subtitle, colors, visibility)
 * @param navigationEventBus The event bus for dispatching navigation events
 * @param preferencesManager Manages user preferences (e.g., temperature unit)
 */
@ExperimentalMaterial3Api
@Composable
fun SettingsLayout(
    appBarUiState: AppBarUiState,
    navigationEventBus: NavigationEventBus,
    preferencesManager: PreferencesManager,
) {
    val statusColor = rememberResolvedColorAttr(appBarUiState.subtitleColorAttr)
    val temperatureUnit by preferencesManager.temperatureUnit.collectAsStateWithLifecycle()
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            modifier = Modifier.padding(top = 4.dp),
                            text = stringResource(appBarUiState.titleResId),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = when (appBarUiState.subtitle) {
                                is TextType.Text -> appBarUiState.subtitle.value
                                is TextType.ResId -> {
                                    stringResource(
                                        appBarUiState.subtitle.id,
                                        *appBarUiState.subtitle.args
                                    )
                                }

                                is TextType.Empty -> ""
                            },
                            color = statusColor,
                            fontSize = appBarUiState.subtitleSize.toToolbarSubtitleFontSize(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navigationEventBus.send(NavigationEvent.NavigateUp) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            stringResource(R.string.back_button),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        content = { innerPadding ->
            BackgroundImage()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            modifier = Modifier.padding(end = 16.dp),
                            text = stringResource(R.string.temperature_unit),
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        SettingsDropdownMenu(
                            selectedItem = temperatureUnit.name,
                            itemsNamesList = TemperatureUnit.entries.map { it.name },
                            clickedItem = {
                                preferencesManager.setTemperatureUnit(
                                    TemperatureUnit.valueOf(it)
                                )
                            })
                    }
                }
            }
        }
    )
}

@ExperimentalMaterial3Api
@Composable
fun SettingsDropdownMenu(
    selectedItem: String,
    itemsNamesList: List<String>,
    clickedItem: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        modifier = Modifier
            .background(Color.Transparent)
            .width(180.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
            readOnly = true,
            value = selectedItem,
            onValueChange = {},
            label = {
                Text(
                    text = stringResource(R.string.choose_unit),
                    fontSize = 8.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                disabledContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSurface,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
            )
        )

        ExposedDropdownMenu(
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = null,
            expanded = expanded,
            containerColor = Color.Transparent,
            onDismissRequest = { expanded = false },
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.onSurface)
        ) {
            itemsNamesList.forEach { selectionOption ->
                DropdownMenuItem(
                    text = {
                        Text(
                            modifier = Modifier.padding(start = 12.dp),
                            text = selectionOption,
                        )
                    },
                    onClick = {
                        clickedItem.invoke(selectionOption)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                )
            }
        }
    }
}
