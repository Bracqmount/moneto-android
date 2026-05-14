package com.fay.currencywidget.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fay.currencywidget.data.repository.ExchangeRateRepository
import com.fay.currencywidget.data.store.CurrencyPairStore
import com.fay.currencywidget.domain.model.CurrencyPair
import kotlinx.coroutines.launch
import com.fay.currencywidget.utils.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(store: CurrencyPairStore) {
    val scope = rememberCoroutineScope()
    val repository = remember { ExchangeRateRepository() }

    var pairs by remember { mutableStateOf<List<CurrencyPair>>(emptyList()) }
    var rates by remember { mutableStateOf<Map<Int, Double>>(emptyMap()) }
    var selectedPair by remember { mutableStateOf<CurrencyPair?>(null) }
    var selectedRate by remember { mutableStateOf(0.0) }
    var isLoadingConverter by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var ratesWithVariation by remember {
        mutableStateOf<Map<Int, Pair<Double, Double>>>(emptyMap())
    }
    var lastUpdated by remember { mutableStateOf(0L) }

    fun refreshPairs() {
        pairs = store.getAllPairs()
    }

    fun loadRates(pairList: List<CurrencyPair>) {
        scope.launch {
            val newRates = mutableMapOf<Int, Pair<Double, Double>>()
            pairList.forEach { pair ->
                val result = repository.getRateWithVariation(pair.fromCurrency, pair.toCurrency)
                if (result != null) {
                    newRates[pair.id] = Pair(result.rate, result.variationPercent)
                }
            }
            ratesWithVariation = newRates
            lastUpdated = System.currentTimeMillis()
        }
    }

    LaunchedEffect(Unit) {
        refreshPairs()
        loadRates(pairs)
    }

    LaunchedEffect(pairs) {
        loadRates(pairs)
    }

    fun openConverter(pair: CurrencyPair) {
        selectedPair = pair
        isLoadingConverter = true
        errorMessage = ""
        scope.launch {
            val rate = repository.getRate(pair.fromCurrency, pair.toCurrency)
            selectedRate = rate ?: 0.0
            isLoadingConverter = false
            errorMessage = if (rate == null) "Erreur réseau" else ""
        }
    }

    if (showAddDialog) {
        AddPairDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { from, to ->
                store.createPair(from, to)
                refreshPairs()
                showAddDialog = false
            }
        )
    }

    if (selectedPair != null) {
        ConverterScaffold(
            title = "${selectedPair!!.fromCurrency} / ${selectedPair!!.toCurrency}",
            isLoading = isLoadingConverter,
            errorMessage = errorMessage,
            onRetry = { openConverter(selectedPair!!) },
            onBack = { selectedPair = null },
            content = {
                ConverterScreen(
                    from = selectedPair!!.fromCurrency,
                    to = selectedPair!!.toCurrency,
                    rate = selectedRate
                )
            }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Moneto",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary  // doré
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter")
                }
            }
        ) { padding ->
            if (pairs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Aucune conversion configurée",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Appuie sur + pour ajouter une paire",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(top = 8.dp)
                ) {
                    items(pairs) { pair ->
                        PairCard(
                            pair = pair,
                            rate = ratesWithVariation[pair.id]?.first,
                            variation = ratesWithVariation[pair.id]?.second,
                            lastUpdated = lastUpdated,
                            onClick = { openConverter(pair) },
                            onDelete = {
                                store.deletePair(pair.id)
                                refreshPairs()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PairCard(
    pair: CurrencyPair,
    rate: Double?,
    variation: Double?,
    lastUpdated: Long,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${pair.fromCurrency} → ${pair.toCurrency}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (rate != null) {
                            Text(
                                text = "1 ${pair.fromCurrency} = ${"%.4f".format(rate)} ${pair.toCurrency}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Chargement...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (variation != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            val (variationText, variationColor) = when {
                                variation > 0 -> "▲ +${"%.2f".format(variation)}%" to
                                        androidx.compose.ui.graphics.Color(0xFF4CAF50)
                                variation < 0 -> "▼ ${"%.2f".format(variation)}%" to
                                        androidx.compose.ui.graphics.Color(0xFFF44336)
                                else -> "=" to
                                        androidx.compose.ui.graphics.Color(0xFF888888)
                            }
                            Text(
                                text = variationText,
                                style = MaterialTheme.typography.bodySmall,
                                color = variationColor
                            )
                        }
                    }

                    // Date de dernière mise à jour
                    if (lastUpdated > 0L) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = TimeUtils.getRelativeTime(lastUpdated),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Supprimer",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            if (pair.widgetId != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "📱 Widget actif",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}