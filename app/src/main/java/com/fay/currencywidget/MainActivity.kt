package com.fay.currencywidget.ui

import android.appwidget.AppWidgetManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fay.currencywidget.data.repository.ExchangeRateRepository
import com.fay.currencywidget.data.store.CurrencyPairStore
import com.fay.currencywidget.domain.model.CurrencyPair
import com.fay.currencywidget.ui.screens.ConverterScreen
import com.fay.currencywidget.ui.theme.CurrencyWidgetTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pairId = intent.getIntExtra("pair_id", -1)
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        )

        setContent {
            CurrencyWidgetTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        pairId != -1 -> {
                            // Ouvert depuis une ligne du widget → convertisseur direct
                            PairConverterScreen(
                                pairId = pairId,
                                store = CurrencyPairStore(this)
                            )
                        }
                        else -> {
                            // Ouvert directement → liste des conversions
                            com.fay.currencywidget.ui.screens.HomeScreen(
                                store = CurrencyPairStore(this)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Écran ouvert depuis le widget — charge les devises du widget tapé
@Composable
fun WidgetConverterScreen(
    widgetId: Int,
    store: CurrencyPairStore
) {
    val scope = rememberCoroutineScope()
    var from by remember { mutableStateOf("EUR") }
    var to by remember { mutableStateOf("GBP") }
    var rate by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(widgetId) {
        scope.launch {
            try {
                val pair = store.getPair(widgetId)
                if (pair != null) {
                    from = pair.fromCurrency
                    to = pair.toCurrency
                }
                val repository = ExchangeRateRepository()
                val fetchedRate = repository.getRate(from, to)
                if (fetchedRate != null) {
                    rate = fetchedRate
                } else {
                    errorMessage = "Impossible de récupérer le taux"
                }
            } catch (e: Exception) {
                errorMessage = "Erreur réseau"
            } finally {
                isLoading = false
            }
        }
    }

    ConverterScaffold(
        title = "$from / $to",
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRetry = {
            isLoading = true
            errorMessage = ""
            scope.launch {
                val repository = ExchangeRateRepository()
                val fetchedRate = repository.getRate(from, to)
                rate = fetchedRate ?: 0.0
                isLoading = false
                errorMessage = if (fetchedRate == null) "Erreur réseau" else ""
            }
        },
        content = { ConverterScreen(from = from, to = to, rate = rate) }
    )
}

// Scaffold partagé entre les deux écrans
@Composable
fun ConverterScaffold(
    title: String,
    isLoading: Boolean,
    errorMessage: String,
    onRetry: () -> Unit,
    onBack: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(8.dp)
            ) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                }
                Text(
                    text = "Convertisseur $title",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }
            errorMessage.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) { Text("Réessayer") }
                    }
                }
            }
            else -> content()
        }
    }
}

@Composable
fun PairConverterScreen(pairId: Int, store: CurrencyPairStore) {
    val scope = rememberCoroutineScope()
    var from by remember { mutableStateOf("") }
    var to by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(pairId) {
        scope.launch {
            val pair = store.getPairById(pairId)
            if (pair != null) {
                from = pair.fromCurrency
                to = pair.toCurrency
                val repository = ExchangeRateRepository()
                val fetchedRate = repository.getRate(from, to)
                rate = fetchedRate ?: 0.0
                errorMessage = if (fetchedRate == null) "Erreur réseau" else ""
            } else {
                errorMessage = "Paire introuvable"
            }
            isLoading = false
        }
    }

    ConverterScaffold(
        title = if (from.isNotEmpty()) "$from / $to" else "Chargement",
        isLoading = isLoading,
        errorMessage = errorMessage,
        onRetry = {
            isLoading = true
            errorMessage = ""
            scope.launch {
                val repository = ExchangeRateRepository()
                val fetchedRate = repository.getRate(from, to)
                rate = fetchedRate ?: 0.0
                isLoading = false
                errorMessage = if (fetchedRate == null) "Erreur réseau" else ""
            }
        },
        content = {
            ConverterScreen(from = from, to = to, rate = rate)
        }
    )
}