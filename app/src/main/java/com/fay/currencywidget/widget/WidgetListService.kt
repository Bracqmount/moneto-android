package com.fay.currencywidget.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.fay.currencywidget.R
import com.fay.currencywidget.data.store.CurrencyPairStore
import com.fay.currencywidget.domain.model.CurrencyPair

class WidgetListService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return WidgetListFactory(applicationContext)
    }
}

class WidgetListFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var pairs: List<CurrencyPair> = emptyList()
    private var rates: Map<Int, Double> = emptyMap()
    private var variations: Map<Int, Double> = emptyMap()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        val store = CurrencyPairStore(context)
        pairs = store.getAllPairs()

        val prefs = context.getSharedPreferences("widget_rates", Context.MODE_PRIVATE)
        val newRates = mutableMapOf<Int, Double>()
        val newVariations = mutableMapOf<Int, Double>()

        pairs.forEach { pair ->
            val rate = prefs.getFloat("rate_${pair.id}", -1f)
            val variation = prefs.getFloat("variation_${pair.id}", Float.MAX_VALUE)
            if (rate != -1f) newRates[pair.id] = rate.toDouble()
            if (variation != Float.MAX_VALUE) newVariations[pair.id] = variation.toDouble()
        }

        rates = newRates
        variations = newVariations
    }

    override fun onDestroy() {}
    override fun getCount() = pairs.size

    override fun getViewAt(position: Int): RemoteViews {
        val pair = pairs[position]
        val rate = rates[pair.id]
        val variation = variations[pair.id]

        val views = RemoteViews(context.packageName, R.layout.widget_row)

        // Paire
        views.setTextViewText(R.id.tv_row_pair, "${pair.fromCurrency} → ${pair.toCurrency}")

        // Taux
        views.setTextViewText(
            R.id.tv_row_rate,
            if (rate != null) "%.4f".format(rate) else "..."
        )

        // Variation + couleur bordure
        val borderColor: Int
        if (variation != null) {
            val variationText = when {
                variation > 0 -> "▲ +${"%.2f".format(variation)}%"
                variation < 0 -> "▼ ${"%.2f".format(variation)}%"
                else -> "= 0.00%"
            }
            val variationColor = when {
                variation > 0 -> Color.parseColor("#10B981")   // vert
                variation < 0 -> Color.parseColor("#EF4444")   // rouge
                else -> Color.parseColor("#6B7280")             // gris
            }
            borderColor = variationColor
            views.setTextViewText(R.id.tv_row_variation, variationText)
            views.setTextColor(R.id.tv_row_variation, variationColor)
        } else {
            borderColor = Color.parseColor("#6B7280")
            views.setTextViewText(R.id.tv_row_variation, "")
        }

        // Couleur de la bordure gauche
        views.setInt(R.id.tv_row_border, "setBackgroundColor", borderColor)

        // Intent tap sur la ligne
        val fillIntent = Intent().apply {
            putExtra("pair_id", pair.id)
        }
        views.setOnClickFillInIntent(R.id.tv_row_pair, fillIntent)
        views.setOnClickFillInIntent(R.id.tv_row_rate, fillIntent)
        views.setOnClickFillInIntent(R.id.tv_row_variation, fillIntent)
        views.setOnClickFillInIntent(R.id.tv_row_arrow, fillIntent)
        views.setOnClickFillInIntent(R.id.tv_row_border, fillIntent)

        return views
    }

    override fun getLoadingView() = null
    override fun getViewTypeCount() = 1
    override fun getItemId(position: Int) = pairs[position].id.toLong()
    override fun hasStableIds() = true
}