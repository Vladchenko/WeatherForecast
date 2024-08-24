package com.example.weatherforecast.presentation.fragments.cityselection

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
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
import com.example.weatherforecast.R
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.presentation.PresentationUtils.getFullCityName
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Layout for a city selection screen.
 *
 * @param toolbarTitle          text for a toolbar title
 * @param citySelectionTitle    title text for city selection
 * @param queryLabel            mask typing text field hint text
 * @param onBackClick           back button click callback
 * @param onCityClicked         city name click callback
 * @param viewModel             viewModel for city selection ops
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CitySelectionLayout(
    toolbarTitle: String,
    citySelectionTitle: String,
    queryLabel: String,
    onBackClick: () -> Unit,
    onCityClicked: (String) -> Unit,
    viewModel: CitiesNamesViewModel
) {
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val cityItem by viewModel.cityMaskState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            modifier = Modifier.offset((-16).dp),
                            text = toolbarTitle
                        )
                        Text(
                            modifier = Modifier.offset((-16).dp),
                            text = viewModel.toolbarSubtitleTextState.value,
                            color = viewModel.toolbarSubtitleColorState.value,
                            fontSize = (viewModel.toolbarSubtitleFontSizeState.value).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "backIcon")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                elevation = 10.dp
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
            Box(modifier = Modifier.fillMaxSize()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = citySelectionTitle,
                        modifier = Modifier.padding(top = 16.dp),
                        fontSize = 16.sp
                    )
                    AddressEdit(
                        cityName = cityItem,
                        queryLabel = queryLabel,
                        modifier = Modifier,
                        cityMaskPredictions = viewModel.citiesNamesState.value?.cities.orEmpty(),
                        cityMaskAction = cityMaskAction(
                            keyboardController,
                            scope,
                            viewModel,
                            onCityClicked
                        )
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun cityMaskAction(
    keyboardController: SoftwareKeyboardController?,
    scope: CoroutineScope,
    viewModel: CitiesNamesViewModel,
    onCityClicked: (String) -> Unit,
): (CityMaskAction) -> Unit =
    { action: CityMaskAction ->
        when (action) {
            is CityMaskAction.OnCitySelected -> {
                keyboardController?.hide()
                scope.launch {
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
                scope.launch {
                    viewModel.getCitiesNamesForMask(action.cityMask)
                }
            }

            is CityMaskAction.OnCityMaskAutoCompleteDone -> {
                keyboardController?.hide()
            }

            is CityMaskAction.OnCityMaskAutoCompleteClear -> {
                viewModel.emptyCityMask()
                viewModel.emptyCitiesNames()
            }

            is CityMaskAction.OnCitiesOptionsClear -> {
                viewModel.emptyCitiesNames()
            }
        }
    }

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
            textStyle = MaterialTheme.typography.subtitle1,
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
            colors = colors ?: TextFieldDefaults.outlinedTextFieldColors()
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
            textStyle = MaterialTheme.typography.subtitle1,
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
            colors = colors ?: TextFieldDefaults.textFieldColors()
        )
    }
}

@Composable
private fun <T> AutoCompleteUI(
    modifier: Modifier,
    query: String,
    queryLabel: String,
    useOutlined: Boolean = false,
    colors: TextFieldColors? = null,
    onQueryChanged: (String) -> Unit = {},
    predictions: List<T>,
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

@Composable
private fun AddressEdit(
    cityName: CityItem,
    queryLabel: String,
    modifier: Modifier,
    cityMaskPredictions: List<CityDomainModel>,
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
                cityName.cityMask = updatedCityMask
                cityMaskAction(CityMaskAction.OnCityMaskChange(cityName.cityMask))
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
                    .background(MaterialTheme.colors.primary)
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
