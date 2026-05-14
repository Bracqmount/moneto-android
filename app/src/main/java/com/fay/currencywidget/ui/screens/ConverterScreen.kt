package com.fay.currencywidget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConverterScreen(
    from: String,
    to: String,
    rate: Double
) {
    var fromValue by remember { mutableStateOf("") }
    var toValue by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Taux affiché en haut
        Text(
            text = "1 $from = %.4f $to".format(rate),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Champ devise source → calcule la devise cible
        OutlinedTextField(
            value = fromValue,
            onValueChange = { input ->
                fromValue = input
                val amount = input.toDoubleOrNull()
                toValue = if (amount != null) "%.4f".format(amount * rate) else ""
            },
            label = { Text(from) },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                textAlign = TextAlign.End
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Icône de conversion
        Text(
            text = "⇅",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Champ devise cible → calcule la devise source en sens inverse
        OutlinedTextField(
            value = toValue,
            onValueChange = { input ->
                toValue = input
                val amount = input.toDoubleOrNull()
                fromValue = if (amount != null && rate != 0.0) {
                    "%.4f".format(amount / rate)
                } else ""
            },
            label = { Text(to) },
            placeholder = { Text("0.00") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(
                fontSize = 24.sp,
                textAlign = TextAlign.End
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Bouton reset
        OutlinedButton(
            onClick = {
                fromValue = ""
                toValue = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Réinitialiser")
        }
    }
}