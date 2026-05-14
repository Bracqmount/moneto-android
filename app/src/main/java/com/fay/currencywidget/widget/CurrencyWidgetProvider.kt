package com.fay.currencywidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.fay.currencywidget.R
import com.fay.currencywidget.ui.MainActivity
import com.fay.currencywidget.utils.TimeUtils
import java.util.concurrent.TimeUnit

class CurrencyWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, widgetId, appWidgetManager)
        }
        scheduleUpdate(context)
    }

    override fun onEnabled(context: Context) {
        scheduleUpdate(context)
    }

    override fun onDisabled(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork("currency_update_global")
    }

    // Intercepte le tap sur le header → rafraîchissement
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH) {
            // Affiche "Actualisation..." pendant le chargement
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, CurrencyWidgetProvider::class.java)
            )
            ids.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_currency)
                views.setTextViewText(R.id.tv_last_updated, "Actualisation...")
                manager.updateAppWidget(widgetId, views)
            }
            // Lance le Worker immédiatement
            fetchNow(context)
        }
    }

    companion object {

        const val ACTION_REFRESH = "com.fay.currencywidget.ACTION_REFRESH"

        fun updateWidget(
            context: Context,
            widgetId: Int,
            appWidgetManager: AppWidgetManager = AppWidgetManager.getInstance(context)
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_currency)

            // Date de dernière mise à jour
            val prefs = context.getSharedPreferences("widget_rates", Context.MODE_PRIVATE)
            val lastUpdated = prefs.getLong("last_updated", 0L)
            val lastUpdatedText = if (lastUpdated > 0L) {
                TimeUtils.getRelativeTime(lastUpdated)
            } else {
                "Jamais"
            }
            views.setTextViewText(R.id.tv_last_updated, lastUpdatedText)

            // Tap sur le header → rafraîchissement
            val refreshIntent = Intent(context, CurrencyWidgetProvider::class.java).apply {
                action = ACTION_REFRESH
            }
            val refreshPending = PendingIntent.getBroadcast(
                context,
                0,
                refreshIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.ll_header, refreshPending)

            // Intent template — ouvre le convertisseur pour la paire tapée
            val openAppIntent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            // Configure le ListView
            val serviceIntent = Intent(context, WidgetListService::class.java)
            views.setRemoteAdapter(R.id.lv_pairs, serviceIntent)
            views.setPendingIntentTemplate(R.id.lv_pairs, pendingIntent)
            views.setEmptyView(R.id.lv_pairs, R.id.tv_widget_title)

            appWidgetManager.updateAppWidget(widgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.lv_pairs)
        }

        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, CurrencyWidgetProvider::class.java)
            )
            ids.forEach { updateWidget(context, it, manager) }
        }

        fun fetchNow(context: Context) {
            val request = androidx.work.OneTimeWorkRequestBuilder<WidgetUpdateWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueue(request)
        }

        fun scheduleUpdate(context: Context) {
            val request = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(30, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "currency_update_global",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun showError(context: Context, message: String = "Erreur réseau") {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, CurrencyWidgetProvider::class.java)
            )
            ids.forEach { widgetId ->
                val views = RemoteViews(context.packageName, R.layout.widget_currency)
                views.setTextViewText(R.id.tv_last_updated, message)
                manager.updateAppWidget(widgetId, views)
            }
        }
    }
}