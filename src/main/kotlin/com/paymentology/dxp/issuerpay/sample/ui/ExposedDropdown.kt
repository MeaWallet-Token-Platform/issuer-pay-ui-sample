package com.paymentology.dxp.issuerpay.sample.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T> M3ExposedDropdown(
    label: String,
    options: List<T>,
    selected: T?,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = MaterialTheme.shapes.small,
    isError: Boolean = false,
    supportingText: String? = null,
    optionLabel: (T) -> String = { it.toString() },
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected?.let(optionLabel).orEmpty(),
            onValueChange = {}, // read-only
            readOnly = true,
            enabled = enabled,
            isError = isError,
            label = { Text(label) },
            leadingIcon = leadingIcon,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            supportingText = supportingText?.let { { Text(it) } },
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(),
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                val text = optionLabel(option)
                DropdownMenuItem(
                    text = { Text(text, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}