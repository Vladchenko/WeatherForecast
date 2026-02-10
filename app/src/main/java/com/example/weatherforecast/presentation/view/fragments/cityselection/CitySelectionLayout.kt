package com.example.weatherforecast.presentation.view.fragments.cityselection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.colorResource
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
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.PresentationUtils.getFullCityName
import com.example.weatherforecast.presentation.viewmodel.appBar.AppBarViewModel
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Layout for a city selection screen.
 *
 * Displays:
 * - A top app bar with title and subtitle from [AppBarViewModel]
 * - An auto-complete text field for entering a city name
 * - A list of predicted city names based on user input
 * - Background image for visual appeal
 *
 * The screen supports keyboard hiding, clearing input, and handling city selection.
 *
 * @param toolbarTitle Text to display in the toolbar (not currently used — title comes from [appBarViewModel])
 * @param citySelectionTitle Title shown above the search field
 * @param queryLabel Hint text for the city search input field
 * @param onBackClick Callback triggered when the back button is clicked
 * @param onCityClicked Callback invoked with the full city name when a city is selected
 * @param appBarViewModel ViewModel providing UI state for the app bar (title, subtitle, colors)
 * @param viewModel ViewModel managing city name search and suggestions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NonSkippableComposable
fun CitySelectionLayout(
    toolbarTitle: String,
    citySelectionTitle: String,
    queryLabel: String,
    onBackClick: () -> Unit,
    onCityClicked: (String) -> Unit,
    appBarViewModel: AppBarViewModel,
    viewModel: CitiesNamesViewModel
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val cityState by viewModel.cityMaskState.collectAsStateWithLifecycle()
    val appbarState by appBarViewModel.appBarState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = {
                    Column {
                        Text(
                            modifier = Modifier
                                .padding(top = 4.dp),
                            text = appbarState.title
                        )
                        Text(
                            text = appbarState.subtitle,
                            color = colorResource(appbarState.subtitleColorAttr),
                            fontSize = PresentationUtils.getToolbarSubtitleFontSize(appbarState.subtitle).sp,   //TODO Move to model
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "backIcon")
                    }
                },
            )
        },
        content = { innerPadding ->
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = citySelectionTitle,
                        modifier = Modifier.padding(top = 16.dp),
                        fontSize = 16.sp
                    )
                    AddressEdit(
                        cityName = cityState,
                        queryLabel = queryLabel,
                        modifier = Modifier,
                        cityMaskPredictions = viewModel.citiesNamesState.value?.cities.orEmpty()
                            .toPersistentList(),
                        cityMaskAction = cityMaskAction(
                            keyboardController,
                            { scope },
                            onCityClicked,
                            viewModel::clearCityMask,
                            viewModel::clearCitiesNames,
                            viewModel::getCitiesNamesForMask
                        )
                    )
                }
            }
        }
    )
}

/**
 * Returns an event handler for city mask (search query) actions.
 *
 * Handles various user interactions like typing, selecting a suggestion, clearing input, or pressing "Done".
 *
 * @param keyboardController Controller to hide the soft keyboard when needed
 * @param scope Provides a [CoroutineScope] for launching coroutines
 * @param onCityClicked Called when a city is selected; receives the full city name
 * @param onCityMaskEmptied Callback to clear the current city mask input
 * @param onCitiesNamesEmptied Callback to clear the list of suggested cities
 * @param getCitiesNamesForMask Loads city suggestions based on the current input mask
 *
 * @return A function that takes a [CityMaskAction] and performs the corresponding operation
 */
@Composable
private fun cityMaskAction(
    keyboardController: SoftwareKeyboardController?,
    scope: () -> CoroutineScope,
    onCityClicked: (String) -> Unit,
    onCityMaskEmptied: () -> Unit,
    onCitiesNamesEmptied: () -> Unit,
    getCitiesNamesForMask: (String) -> Unit
): (CityMaskAction) -> Unit =
    { action: CityMaskAction ->
        when (action) {
            is CityMaskAction.OnCitySelected -> {
                keyboardController?.hide()
                scope.invoke().launch {
                    onCityClicked(
                        getFullCityName(
                            action.selectedCity.name,
                            action.selectedCity.state,
                            action.selectedCity.country
                        )
                    )
                }
            }

            is CityMaskAction.OnCityMaskChange -> {
                scope.invoke().launch {
                    getCitiesNamesForMask(action.cityMask)
                }
            }

            is CityMaskAction.OnCityMaskAutoCompleteDone -> {
                keyboardController?.hide()
            }

            is CityMaskAction.OnCityMaskAutoCompleteClear -> {
                onCityMaskEmptied.invoke()
                onCitiesNamesEmptied.invoke()
            }

            is CityMaskAction.OnCitiesOptionsClear -> {
                onCitiesNamesEmptied.invoke()
            }
        }
    }

/**
 * A reusable text field with a clear button and "Done" action support.
 *
 * Can be rendered as outlined or filled style.
 *
 * @param modifier Modifier to apply to the text field
 * @param query Current input text
 * @param label Label/hint displayed inside the text field
 * @param useOutlined If true, uses [OutlinedTextField]; otherwise, uses [TextField]
 * @param colors Custom colors for the text field (optional)
 * @param onDoneActionClick Called when the "Done" action is triggered on the keyboard
 * @param onClearClick Called when the clear ("X") icon is clicked
 * @param onQueryChanged Called whenever the text input changes
 */
@Composable
private fun QuerySearch(
    modifier: Modifier = Modifier,
    query: String,
    label: String,
    useOutlined: Boolean = false,
    colors: TextFieldColors? = null,
    onDoneActionClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onQueryChanged: (String) -> Unit
) {
    var showClearButton by remember { mutableStateOf(false) }
    if (useOutlined) {
        OutlinedTextField(
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
            colors = colors ?: TextFieldDefaults.colors()
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
}

/**
 * Generic auto-complete UI component with drop-down suggestions.
 *
 * Displays a text field and a scrollable list of predictions below it.
 *
 * @param T Type of prediction items (e.g., [CityDomainModel])
 * @param modifier Modifier for the container
 * @param query Current user input
 * @param queryLabel Hint text for the input field
 * @param useOutlined Whether to use outlined or filled text field style
 * @param colors Optional custom colors for the text field
 * @param onQueryChanged Called when input changes
 * @param predictions List of available suggestions
 * @param onDoneActionClick Triggered when "Done" is pressed
 * @param onClearClick Triggered when the clear button is clicked
 * @param onItemClick Called when a suggestion is clicked
 * @param itemContent Composable lambda defining how each suggestion is rendered
 */
@Composable
private fun <T> AutoCompleteUI(
    modifier: Modifier,
    query: String,
    queryLabel: String,
    useOutlined: Boolean = false,
    colors: TextFieldColors? = null,
    onQueryChanged: (String) -> Unit = {},
    predictions: ImmutableList<T>,
    onDoneActionClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onItemClick: (T) -> Unit = {},
    itemContent: @Composable (T) -> Unit = {}
) {
    val view = LocalView.current
    val lazyListState = rememberLazyListState()

    LazyColumn(
        state = lazyListState,
        modifier = modifier.heightIn(max = TextFieldDefaults.MinHeight * 6)
    ) {

        item {
            QuerySearch(
                query = query,
                label = queryLabel,
                useOutlined = useOutlined,
                colors = colors,
                onQueryChanged = onQueryChanged,
                onDoneActionClick = {
                    view.clearFocus()
                    onDoneActionClick()
                },
                onClearClick = {
                    onClearClick()
                }
            )
        }

        if (predictions.isNotEmpty()) {
            items(predictions.size, null, { null }) { prediction ->
                Row(
                    Modifier
                        .padding(4.dp)
                        .fillMaxWidth()
                        .clickable {
                            view.clearFocus()
                            onItemClick(predictions[prediction])
                        }
                ) {
                    itemContent(predictions[prediction])
                }
            }
        }
    }
}

/**
 * Input field for city name with auto-completion and prediction display.
 *
 * Wraps [AutoCompleteUI] with specific logic for city name search.
 *
 * @param cityName Current city input state (wrapper with mutable [CityItem.cityMask])
 * @param queryLabel Hint text for the search field
 * @param modifier Modifier for layout customization
 * @param cityMaskPredictions List of matching cities to display as suggestions
 * @param cityMaskAction Handler for user actions (typing, selection, etc.)
 */
@Composable
private fun AddressEdit(
    cityName: CityItem,
    queryLabel: String,
    modifier: Modifier,
    cityMaskPredictions: ImmutableList<CityDomainModel>,
    cityMaskAction: (CityMaskAction) -> Unit
) {
    Column(
        modifier = modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        AutoCompleteUI(
            modifier = Modifier.fillMaxWidth(),
            query = cityName.cityMask,
            queryLabel = queryLabel,
            useOutlined = true,
            onQueryChanged = { updatedCityMask ->
                if (updatedCityMask.isNotBlank()) {
                    cityName.cityMask = updatedCityMask
                    cityMaskAction(CityMaskAction.OnCityMaskChange(cityName.cityMask))
                }
            },
            predictions = cityMaskPredictions,
            onClearClick = {
                cityMaskAction(CityMaskAction.OnCityMaskAutoCompleteClear)
            },
            onDoneActionClick = {
                cityMaskAction(CityMaskAction.OnCityMaskAutoCompleteDone)
            },
            onItemClick = { selectedCity ->
                cityMaskAction(
                    CityMaskAction.OnCitySelected(
                        selectedCity
                    )
                )
                cityMaskAction(
                    CityMaskAction.OnCityMaskAutoCompleteClear
                )
                cityMaskAction(
                    CityMaskAction.OnCitiesOptionsClear
                )
            }
        ) {
            Text(
                modifier = Modifier
                    .clip(shape = RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.onPrimary)
                    .padding(8.dp),
                color = Color.Black,
                text = with(it) {
                    getFullCityName(name, state, country)
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
