package io.github.vladchenko.weatherforecast.presentation.view.composables

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vladchenko.weatherforecast.R
import io.github.vladchenko.weatherforecast.presentation.themeColor

@Composable
fun QuerySearch(
    modifier: Modifier = Modifier,
    query: String,
    label: String,
    useOutlined: Boolean = false,
    mainContentColor: Color = themeColor(R.attr.colorMainText),
    onDoneActionClick: () -> Unit = {},
    onClearClick: () -> Unit = {},
    onQueryChanged: (String) -> Unit,
    onFocusChanged: (Boolean) -> Unit = {}
) {
    var showClearButton by remember { mutableStateOf(false) }

    val textFieldColors = TextFieldDefaults.colors(
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
        focusedTrailingIconColor = mainContentColor.copy(alpha = 0.6f),
        unfocusedTrailingIconColor = mainContentColor.copy(alpha = 0.6f),
        disabledTrailingIconColor = mainContentColor.copy(alpha = 0.38f),
        errorTrailingIconColor = Color.Red
    )

    if (useOutlined) {
        OutlinedTextField(
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    showClearButton = focusState.isFocused
                    onFocusChanged(focusState.isFocused)
                },
            value = query,
            onValueChange = onQueryChanged,
            label = { Text(text = label) },
            singleLine = true,
            trailingIcon = {
                if (showClearButton && query.isNotBlank()) {
                    IconButton(onClick = onClearClick) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            keyboardActions = KeyboardActions(onDone = {onDoneActionClick()}),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Text),
            colors = textFieldColors
        )
    } else {
        TextField(
            modifier = modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    showClearButton = focusState.isFocused
                },
            value = query,
            onValueChange = onQueryChanged,
            label = { Text(text = label) },
            singleLine = true,
            trailingIcon = {
                if (showClearButton && query.isNotBlank()) {
                    IconButton(onClick = onClearClick) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            keyboardActions = KeyboardActions(onDone = {onDoneActionClick()}),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Text),
            colors = textFieldColors
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Preview(showBackground = true)
@Composable
private fun QuerySearchPreview() {
    QuerySearch(
        query = "New York",
        label = "Enter city name",
        useOutlined = true,
        mainContentColor = Color.White,
        onQueryChanged = {},
        onClearClick = {},
        onDoneActionClick = {}
    )
}