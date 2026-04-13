package com.example.weatherforecast.presentation.view.fragments.cityselection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.weatherforecast.R
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.models.domain.RecentCities
import com.example.weatherforecast.presentation.PresentationUtils.formatFullCityName
import com.example.weatherforecast.presentation.PresentationUtils.toToolbarSubtitleFontSize
import com.example.weatherforecast.presentation.themeColor
import com.example.weatherforecast.presentation.view.composables.ProgressBar
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitySelectionEvent
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherUiState
import kotlinx.coroutines.FlowPreview

/**
 * Composable layout for the city selection screen.
 *
 * Provides a full UI for searching and selecting cities, featuring:
 * - A top app bar with dynamic title and subtitle from [AppBarViewModel]
 * - An auto-complete search field with real-time predictions
 * - A drop-down list of suggested cities based on user input
 * - "Recent" cities section shown when the query is empty
 * - Visual background for improved aesthetics
 *
 * The component supports keyboard dismissal, input clearing, and both manual and recent city selection.
 * It integrates with ViewModel(s) to observe state changes and emit user events via [onEvent].
 *
 * @param mainContentColor Color applied to text and icons (defaults to theme-based color)
 * @param citySelectionTitle Title displayed above the search input
 * @param queryLabel Hint text shown inside the search field
 * @param onEvent Callback to handle user interactions like city selection or navigation
 * @param appBarViewModel ViewModel providing title/subtitle and styling info for the top app bar
 * @param viewModel ViewModel managing city name suggestions, recent cities, and input state
 */
@Composable
@FlowPreview
@NonSkippableComposable
@ExperimentalMaterial3Api
fun CitySelectionLayout(
    mainContentColor: Color = themeColor(R.attr.colorMainText),
    citySelectionTitle: String,
    queryLabel: String,
    onEvent: (CitySelectionEvent) -> Unit,
    appBarViewModel: AppBarViewModel,
    viewModel: CitiesNamesViewModel
) {
    val cityUiState by viewModel.cityMaskStateFlow.collectAsStateWithLifecycle()
    val appbarUiState by appBarViewModel.appBarStateFlow.collectAsStateWithLifecycle()
    val cityPredictionsUiState by viewModel.cityPredictions.collectAsStateWithLifecycle()
    val recentCitiesNamesUiState by viewModel.recentCitiesNamesFlow.collectAsStateWithLifecycle()
    val fontSize = appbarUiState.subtitleSize.toToolbarSubtitleFontSize()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            modifier = Modifier
                                .padding(top = 4.dp),
                            text = appbarUiState.title,
                            color = mainContentColor,
                        )
                        Text(
                            text = appbarUiState.subtitle,
                            color = mainContentColor,
                            fontSize = fontSize,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(CitySelectionEvent.NavigateUp) }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "backIcon",
                            tint = mainContentColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        content = { innerPadding ->
            Image(
                painter = painterResource(id = R.drawable.background2),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize(),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = citySelectionTitle,
                        modifier = Modifier.padding(top = 16.dp),
                        fontSize = 16.sp,
                        color = mainContentColor
                    )
                    AddressEdit(
                        cityName = cityUiState,
                        queryLabel = queryLabel,
                        modifier = Modifier,
                        mainContentColor = mainContentColor,
                        cityMaskPredictions = cityPredictionsUiState,
                        recentCities = recentCitiesNamesUiState,
                        onEvent
                    )
                }
            }
        }
    )
}

/**
 * A customizable text input field with a clear ("X") button and keyboard action handling.
 *
 * Supports two styles: outlined (default) and filled. Emits events on text change, clear, and "Done" press.
 *
 * @param modifier Modifier to apply to the TextField
 * @param query Current text value in the field
 * @param label Placeholder/hint label displayed inside the field
 * @param useOutlined If true, renders as [OutlinedTextField]; otherwise as [TextField]
 * @param colors Optional custom [TextFieldColors] to override default theming
 * @param mainContentColor Base color for text, icons, and indicators
 * @param onDoneActionClick Called when the "Done" action is triggered on the keyboard
 * @param onClearClick Called when the user taps the clear icon
 * @param onQueryChanged Called whenever the input text changes
 * @param onFocusChanged Called when focus state changes (focused/unfocused)
 */
@Composable
private fun QuerySearch(
    modifier: Modifier = Modifier,
    query: String,
    label: String,
    useOutlined: Boolean = false,
    colors: TextFieldColors? = null,
    mainContentColor: Color,
    onDoneActionClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onQueryChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    var showClearButton by remember { mutableStateOf(false) }
    if (useOutlined) {
        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    showClearButton = (focusState.isFocused)
                    onFocusChanged(focusState.isFocused)
                },
            value = query,
            onValueChange = onQueryChanged,
            label = { Text(text = label) },
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true,
            trailingIcon = {
                if (showClearButton) {
                    IconButton(onClick = {
                        onClearClick()
                    }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear")
                    }
                }

            },
            keyboardActions = KeyboardActions(onDone = {
                onDoneActionClick()
            }),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            colors = TextFieldDefaults.colors(
                focusedTextColor = mainContentColor,
                unfocusedTextColor = mainContentColor,
                disabledTextColor = mainContentColor.copy(alpha = 0.38f),
                errorTextColor = Color.Red,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                cursorColor = mainContentColor,
                errorCursorColor = Color.Red,
                selectionColors = LocalTextSelectionColors.current,
                focusedIndicatorColor = mainContentColor,
                unfocusedIndicatorColor = mainContentColor.copy(alpha = 0.6f),
                disabledIndicatorColor = mainContentColor.copy(alpha = 0.38f),
                errorIndicatorColor = Color.Red,
                focusedLeadingIconColor = mainContentColor.copy(alpha = 0.6f),
                unfocusedLeadingIconColor = mainContentColor.copy(alpha = 0.6f),
                disabledLeadingIconColor = mainContentColor.copy(alpha = 0.38f),
                errorLeadingIconColor = Color.Red,
                focusedTrailingIconColor = mainContentColor.copy(alpha = 0.6f),
                unfocusedTrailingIconColor = mainContentColor.copy(alpha = 0.6f),
                disabledTrailingIconColor = mainContentColor.copy(alpha = 0.38f),
                errorTrailingIconColor = Color.Red,
                focusedLabelColor = mainContentColor,
                unfocusedLabelColor = mainContentColor.copy(alpha = 0.6f),
                disabledLabelColor = mainContentColor.copy(alpha = 0.38f),
                errorLabelColor = Color.Red,
                focusedPlaceholderColor = mainContentColor.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = mainContentColor.copy(alpha = 0.5f),
                disabledPlaceholderColor = mainContentColor.copy(alpha = 0.38f),
                errorPlaceholderColor = Color.Red,
                focusedSupportingTextColor = mainContentColor.copy(alpha = 0.6f),
                unfocusedSupportingTextColor = mainContentColor.copy(alpha = 0.6f),
                disabledSupportingTextColor = mainContentColor.copy(alpha = 0.38f),
                errorSupportingTextColor = Color.Red,
                focusedPrefixColor = mainContentColor,
                unfocusedPrefixColor = mainContentColor.copy(alpha = 0.6f),
                disabledPrefixColor = mainContentColor.copy(alpha = 0.38f),
                errorPrefixColor = Color.Red,
                focusedSuffixColor = mainContentColor,
                unfocusedSuffixColor = mainContentColor.copy(alpha = 0.6f),
                disabledSuffixColor = mainContentColor.copy(alpha = 0.38f),
                errorSuffixColor = Color.Red
            )
        )
    } else {
        TextField(
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    showClearButton = (focusState.isFocused)
                },
            value = query,
            onValueChange = onQueryChanged,
            label = { Text(text = label) },
            textStyle = MaterialTheme.typography.bodySmall,
            singleLine = true,
            trailingIcon = {
                if (showClearButton) {
                    IconButton(onClick = { onClearClick() }) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            keyboardActions = KeyboardActions(onDone = {
                onDoneActionClick()
            }),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Text
            ),
            colors = colors ?: TextFieldDefaults.colors()
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * Generic auto-complete UI with dynamic suggestion list.
 *
 * Displays a text field followed by a scrollable list of suggestions. Behavior changes based on input:
 * - When query is not blank: shows city predictions from [predictions]
 * - When query is blank: shows "Recent" cities from [recentCities]
 *
 * Handles loading and error states for both data sources.
 *
 * @param T Type of prediction items (e.g., [CityDomainModel])
 * @param modifier Modifier for the outer container
 * @param query Current user input text
 * @param queryLabel Hint text for the input field
 * @param useOutlined Whether to use outlined style for the text field
 * @param colors Optional custom colors for styling the text field
 * @param mainContentColor Base color for text and icons
 * @param onQueryChanged Called when the user types into the field
 * @param predictions Current state (Loading/Success/Error) of city predictions
 * @param recentCities Current state (Loading/Success/Error) of recently used cities
 * @param onDoneActionClick Triggered when "Done" is pressed on the keyboard
 * @param onClearClick Triggered when the clear ("X") button is clicked
 * @param onItemClick Called when a suggestion is tapped
 * @param onFocusChanged Called when focus enters or leaves the input field
 * @param itemContent Composable lambda defining how each suggestion item is rendered
 */
@Composable
private fun <T> AutoCompleteUI(
    modifier: Modifier,
    query: String,
    queryLabel: String,
    useOutlined: Boolean = false,
    colors: TextFieldColors? = null,
    mainContentColor: Color,
    onQueryChanged: (String) -> Unit = {},
    predictions: WeatherUiState<List<CityDomainModel>>?,
    recentCities: WeatherUiState<RecentCities>?,
    onDoneActionClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onItemClick: (CityDomainModel) -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    itemContent: @Composable (CityDomainModel) -> Unit = {},
) {
    val view = LocalView.current
    val lazyListState = rememberLazyListState()

    QuerySearch(
        query = query,
        label = queryLabel,
        useOutlined = useOutlined,
        colors = colors,
        mainContentColor = mainContentColor,
        onQueryChanged = onQueryChanged,
        onDoneActionClick = {
            view.clearFocus()
            onDoneActionClick()
        },
        onClearClick = {
            onClearClick()
        },
        onFocusChanged = onFocusChanged
    )

    if (query.isBlank()) {
        when (recentCities) {
            is WeatherUiState.Success -> {
                if (recentCities.data.cities.isNotEmpty()) {
                    Text(
                        text = "Recent",
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                        color = mainContentColor.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            else -> {}
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .heightIn(
                min = TextFieldDefaults.MinHeight,
                max = (TextFieldDefaults.MinHeight * 6)
            )
    ) {
        // Show either predictions or recent cities depending on query
        if (query.isNotBlank()) {
            // Search mode: display prediction results
            when (predictions) {
                is WeatherUiState.Loading -> {
                    item { ProgressBar() }
                }

                is WeatherUiState.Error -> {
                    item {
                        Text(
                            text = "Error loading cities",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }

                is WeatherUiState.Success -> {
                    if (predictions.data.isNotEmpty()) {
                        items(
                            items = predictions.data,
                            key = { city -> city.id }
                        ) { city ->
                            Row(
                                Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        view.clearFocus()
                                        onItemClick(city)
                                    }
                            ) {
                                itemContent(city)
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "No cities found",
                                modifier = Modifier.padding(16.dp),
                                color = mainContentColor.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                null -> {} // no-op
            }
        } else {
            // Recent mode: display recently searched cities
            when (recentCities) {
                is WeatherUiState.Loading -> {
                    item { ProgressBar() }
                }

                is WeatherUiState.Error -> {
                    item {
                        Text(
                            text = "Error loading recent cities",
                            modifier = Modifier.padding(16.dp),
                            color = Color.Red,
                            fontSize = 14.sp
                        )
                    }
                }

                is WeatherUiState.Success -> {
                    if (recentCities.data.cities.isNotEmpty()) {
                        items(
                            items = recentCities.data.cities,
                            key = { city -> city.id }
                        ) { city ->
                            Row(
                                Modifier
                                    .padding(4.dp)
                                    .fillMaxWidth()
                                    .clickable {
                                        view.clearFocus()
                                        onItemClick(city)
                                    }
                            ) {
                                itemContent(city)
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = "No recent cities",
                                modifier = Modifier.padding(16.dp),
                                color = mainContentColor.copy(alpha = 0.6f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                null -> {} // no-op
            }
        }
    }
}

/**
 * City name input field with integrated auto-completion and recent cities support.
 *
 * Combines [QuerySearch] and [AutoCompleteUI] to provide a complete city search experience.
 * On first focus, triggers loading of recent cities. On selection, formats and emits the chosen city.
 *
 * @param cityName Current value of the city input field
 * @param queryLabel Hint text for the search input
 * @param modifier Modifier for layout customization
 * @param mainContentColor Color used for text and UI elements
 * @param cityMaskPredictions Current state of city prediction results
 * @param recentCities Current state of recently used cities
 * @param onEvent Callback to emit user actions (e.g., select city, clear query)
 * @param keyboardController Optional software keyboard controller to hide the keyboard on selection
 */
@Composable
private fun AddressEdit(
    cityName: String,
    queryLabel: String,
    modifier: Modifier,
    mainContentColor: Color,
    cityMaskPredictions: WeatherUiState<List<CityDomainModel>>?,
    recentCities: WeatherUiState<RecentCities>?,
    onEvent: (CitySelectionEvent) -> Unit,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current
) {
    var isFirstFocus by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AutoCompleteUI<CityDomainModel>(
            modifier = Modifier.fillMaxWidth(),
            query = cityName,
            queryLabel = queryLabel,
            useOutlined = true,
            mainContentColor = mainContentColor,
            onQueryChanged = { updatedCityMask ->
                if (updatedCityMask.isNotBlank()) {
                    onEvent(CitySelectionEvent.UpdateQuery(updatedCityMask))
                }
            },
            predictions = cityMaskPredictions,
            recentCities = recentCities,
            onClearClick = {
                onEvent(CitySelectionEvent.ClearQuery)
            },
            onDoneActionClick = {
                keyboardController?.hide()
            },
            onItemClick = { selectedCity ->
                onEvent(
                    CitySelectionEvent.SelectCity(selectedCity)
                )
                onEvent(CitySelectionEvent.ClearQuery)
                keyboardController?.hide()
            },
            onFocusChanged = { hasFocus ->
                if (hasFocus && isFirstFocus) {
                    isFirstFocus = false
                    onEvent(CitySelectionEvent.LoadRecentCities)
                }
            }
        ) { city: CityDomainModel ->
            Text(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                color = mainContentColor,
                text = formatFullCityName(city.name, city.state, city.country),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}