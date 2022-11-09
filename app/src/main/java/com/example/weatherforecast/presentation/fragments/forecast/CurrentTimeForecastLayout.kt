package com.example.weatherforecast.presentation.fragments.forecast

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.R
import com.example.weatherforecast.presentation.viewmodel.forecast.WeatherForecastViewModel
import kotlin.system.exitProcess

/**
 * Layout for a main screen fragment
 */
@Composable
fun CurrentTimeForecastLayout(
    toolbarTitle: String,
    currentDate: String,
    mainContentTextColor: Color,
    @DrawableRes weatherImageId: Int,
    onCityClick: (() -> Unit),
    viewModel: WeatherForecastViewModel
) {
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
                            text = viewModel.toolbarSubtitleState.value,
                            color = viewModel.toolbarSubtitleColorState.value,
                            fontSize = (viewModel.toolbarSubtitleFontSizeState.value).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { exitProcess(0) }) {
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
