package com.example.weatherforecast.presentation.view.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherforecast.models.domain.CityDomainModel
import com.example.weatherforecast.presentation.PresentationUtils.formatFullCityName

@Composable
fun CitySuggestionItem(
    city: CityDomainModel,
    mainContentColor: Color,
    onItemClick: (CityDomainModel) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .clickable { onItemClick(city) }
    ) {
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

@Preview(showBackground = true)
@Composable
private fun CitySuggestionItemPreview() {
    val city = CityDomainModel(
        name = "San Francisco",
        state = "CA",
        country = "US",
        lat = 37.7749,
        lon = -122.4194
    )
    CitySuggestionItem(
        city = city,
        mainContentColor = Color.White,
        onItemClick = {}
    )
}