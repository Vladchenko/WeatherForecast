package com.example.weatherforecast.presentation.fragments.cityselection

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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
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
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.PresentationUtils.getFullCityName
import com.example.weatherforecast.presentation.viewmodel.cityselection.CitiesNamesViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
@NonSkippableComposable
fun CitySelectionLayout(
    toolbarTitle: String,
    citySelectionTitle: String,
    queryLabel: String,
    onBackClick: () -> Unit,
    onCityClicked: (String) -> Unit,
    viewModel: CitiesNamesViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current
    val cityState by viewModel.cityMaskState.collectAsState()
    val toolbarState = viewModel.toolbarSubtitleMessageState.collectAsState()
    val toolbarSubtitle = toolbarState.value.stringId?.let {
        context.getString(
            it,
            toolbarState.value.valueForStringId.orEmpty()
        )
    } ?: toolbarState.value.valueForStringId.orEmpty()

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
                            text = toolbarTitle
                        )
                        Text(
                            text = toolbarSubtitle,
                            color = PresentationUtils.getToolbarSubtitleColor(toolbarState.value.messageType),
                            fontSize = PresentationUtils.getToolbarSubtitleFontSize(toolbarSubtitle).sp,
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
            Box(modifier = Modifier.fillMaxSize()
                .padding(innerPadding)) {
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
                            viewModel::emptyCitiesNames,
                            viewModel::getCitiesNamesForMask
                        )
                    )
                }
            }
        }
    )
}

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
