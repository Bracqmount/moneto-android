package com.fay.currencywidget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fay.currencywidget.data.repository.ExchangeRateRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPairDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val repository = remember { ExchangeRateRepository() }

    var currencies by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFrom by remember { mutableStateOf("EUR") }
    var selectedTo by remember { mutableStateOf("USD") }
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }
    var searchFrom by remember { mutableStateOf("") }
    var searchTo by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        scope.launch {
            currencies = repository.getCurrenciesWithNames()
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajouter une conversion") },
        text = {
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column {
                    // Devise source
                    Text("Devise source", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedFrom,
                        onExpandedChange = { expandedFrom = it }
                    ) {
                        OutlinedTextField(
                            value = if (expandedFrom) searchFrom
                            else "$selectedFrom — ${currencies[selectedFrom] ?: ""}",
                            onValueChange = { searchFrom = it },
                            readOnly = !expandedFrom,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFrom,
                            onDismissRequest = {
                                expandedFrom = false
                                searchFrom = ""
                            }
                        ) {
                            currencies
                                .filter { (code, name) ->
                                    searchFrom.isEmpty() ||
                                            code.contains(searchFrom, ignoreCase = true) ||
                                            name.contains(searchFrom, ignoreCase = true)
                                }
                                .forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(code)
                                                Text(
                                                    name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedFrom = code
                                            expandedFrom = false
                                            searchFrom = ""
                                        }
                                    )
                                }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Devise cible
                    Text("Devise cible", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    ExposedDropdownMenuBox(
                        expanded = expandedTo,
                        onExpandedChange = { expandedTo = it }
                    ) {
                        OutlinedTextField(
                            value = if (expandedTo) searchTo
                            else "$selectedTo — ${currencies[selectedTo] ?: ""}",
                            onValueChange = { searchTo = it },
                            readOnly = !expandedTo,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTo,
                            onDismissRequest = {
                                expandedTo = false
                                searchTo = ""
                            }
                        ) {
                            currencies
                                .filter { (code, name) ->
                                    searchTo.isEmpty() ||
                                            code.contains(searchTo, ignoreCase = true) ||
                                            name.contains(searchTo, ignoreCase = true)
                                }
                                .forEach { (code, name) ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(code)
                                                Text(
                                                    name,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedTo = code
                                            expandedTo = false
                                            searchTo = ""
                                        }
                                    )
                                }
                        }
                    }

                    if (selectedFrom == selectedTo) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Les deux devises doivent être différentes",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedFrom, selectedTo) },
                enabled = !isLoading && selectedFrom != selectedTo
            ) {
                Text("Ajouter")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}