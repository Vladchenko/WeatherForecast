package com.example.weatherforecast.presentation.fragments.forecast

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NonSkippableComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.R
import com.example.weatherforecast.models.domain.WeatherForecastDomainModel
import com.example.weatherforecast.presentation.PresentationUtils
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel

/**
 * Layout for a main screen fragment
 */
@Composable
@NonSkippableComposable
fun CurrentTimeForecastLayout(
    toolbarTitle: String,
    currentDate: String,
    mainContentTextColor: Color,
    @DrawableRes weatherImageId: Int,
    onCityClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: WeatherForecastViewModel
) {
    val toolbarState = viewModel.toolbarSubtitleMessageState.collectAsState()
    val toolbarSubtitle:String = toolbarState.value.stringId?.let {
        LocalContext.current.getString(
            it,
            toolbarState.value.valueForStringId.orEmpty()
        )
    } ?: toolbarState.value.valueForStringId.orEmpty()

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
            AnimatedVisibility(
                visible = viewModel.showProgressBarState.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ShowProgressBar()
            }
            if (!viewModel.showProgressBarState.value) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = Modifier.padding(top = 56.dp),
                        text = currentDate,
                        fontSize = 14.sp,
                        color = mainContentTextColor
                    )
                    Text(
                        modifier = Modifier
                            .padding(start = 32.dp, top = 24.dp, end = 32.dp)
                            .clickable {
                                onCityClick()
                            },
                        text = viewModel.dataModelState.value?.city.orEmpty(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = mainContentTextColor,
                        fontSize = 36.sp,
                    )
                    Row(
                        modifier = Modifier
                            .padding(top = 24.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = viewModel.dataModelState.value?.temperature.orEmpty(),
                            fontSize = 60.sp,
                            color = mainContentTextColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = viewModel.dataModelState.value?.temperatureType.orEmpty(),
                            color = mainContentTextColor,
                            fontSize = 30.sp,
                        )
                        Image(
                            modifier = Modifier.padding(start = 8.dp),
                            painter = painterResource(id = weatherImageId),
                            contentDescription = viewModel.dataModelState.value?.weatherType
                        )
                    }
                    Text(
                        modifier = Modifier
                            .padding(top = 16.dp),
                        text = viewModel.dataModelState.value?.weatherType.orEmpty(),
                        color = mainContentTextColor
                    )
                }
            }
        }
    )
}

@Composable
fun ShowProgressBar() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(0.6f)
            .background(color = Color.White),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
