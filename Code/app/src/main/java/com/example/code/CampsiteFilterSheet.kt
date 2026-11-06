package com.example.code

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val MAX_GUESTS = 50

/**
 * Bottom sheet for the location and number of guests filters. Changes only apply when the
 * entrant taps Apply
 *
 * @param current the filters currently in use, used to fill in the sheet
 * @param onApply called with the new filters when Apply or Clear is tapped
 * @param onDismiss called when the sheet is closed without applying
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteFilterSheet(
    current: CampsiteFilters,
    onApply: (CampsiteFilters) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var location by remember { mutableStateOf(current.location) }
    var guests by remember { mutableStateOf(current.minGuests) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = col.BottomSheetBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text("Filters", color = col.CardText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = col.UnfocusedInputContainer,
                    focusedContainerColor = col.FocusedInputContainer,
                    focusedLabelColor = col.CardText,
                    unfocusedLabelColor = col.CardText,
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text("Number of guests", color = col.CardText, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StepButton("−", enabled = guests > 0) { guests-- }
                Text(
                    text = if (guests == 0) "Any" else guests.toString(),
                    color = col.CardText,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.width(72.dp)
                )
                StepButton("+", enabled = guests < MAX_GUESTS) { guests++ }
            }
            Spacer(modifier = Modifier.height(28.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button( //filter clear without date
                    onClick = { onApply(current.copy(location = "", minGuests = 0)) },
                    colors = ButtonDefaults.buttonColors(containerColor = col.FilterButton),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear", color = col.CardText)
                }
                Button(
                    onClick = { onApply(current.copy(location = location.trim(), minGuests = guests)) },
                    colors = ButtonDefaults.buttonColors(containerColor = col.SubmitButton),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
        }
    }
}

/**
 * A round − or + button for the guests counter
 */
@Composable
private fun StepButton(label: String, enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = col.SubmitButton),
        modifier = Modifier.size(40.dp)
    ) {
        Text(label, fontSize = 20.sp)
    }
}

/**
 * Pop up calendar for picking the first and last night of a stay. Past dates can't be picked
 *
 * @param startDate the currently picked first date, or null
 * @param endDate the currently picked last date, or null
 * @param onConfirm called with the picked dates when OK is tapped
 * @param onDismiss called when the dialog is closed without picking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampsiteDateRangeDialog(
    startDate: Long?,
    endDate: Long?,
    onConfirm: (Long?, Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val today = remember { todayUtcMillis() }
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate,
        initialSelectedEndDateMillis = endDate,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis >= today
        }
    )
    val colors = DatePickerDefaults.colors(
        containerColor = col.BottomSheetBackground,
        selectedDayContainerColor = col.SubmitButton,
        dayInSelectionRangeContainerColor = col.CampsiteCardOpen,
        todayDateBorderColor = col.SubmitButton
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = colors,
        confirmButton = {
            TextButton(
                onClick = { onConfirm(state.selectedStartDateMillis, state.selectedEndDateMillis) },
                enabled = state.selectedStartDateMillis != null
            ) {
                Text("OK", color = col.CardText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = col.CardText)
            }
        }
    ) {
        DateRangePicker(
            state = state,
            colors = colors,
            title = {
                Text("Select your dates", modifier = Modifier.padding(start = 24.dp, top = 16.dp))
            },
            modifier = Modifier.weight(1f)
        )
    }
}