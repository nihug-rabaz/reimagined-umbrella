package io.github.gustavlindberg99.weather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class DefconWidget : AppWidgetProvider() {
    //Refreshes the widget with cached data first and then live data.
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        val repository = DefconIntelRepository(context.applicationContext)
        val cachedIntel = repository.getCachedIntel()
        if (cachedIntel != null) {
            renderIntel(context, appWidgetManager, appWidgetIds, cachedIntel)
        }
        else {
            renderMessage(
                context,
                appWidgetManager,
                appWidgetIds,
                context.getString(R.string.defcon_widget_loading)
            )
        }
        repository.refreshIntel(
            { intel -> renderIntel(context, appWidgetManager, appWidgetIds, intel) },
            {
                if (cachedIntel == null) {
                    renderMessage(
                        context,
                        appWidgetManager,
                        appWidgetIds,
                        context.getString(R.string.defcon_widget_error)
                    )
                }
            }
        )
    }

    //Fills the widget layout with intel data.
    private fun renderIntel(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        intel: DefconIntel
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = createBaseViews(context)
            val levelColor = resolveLevelColor(context, intel.level)
            views.setViewVisibility(R.id.defcon_widget_content, android.view.View.VISIBLE)
            views.setViewVisibility(R.id.defcon_widget_message, android.view.View.GONE)
            views.setTextViewText(
                R.id.defcon_widget_level_value,
                context.getString(R.string.defcon_widget_level_value, intel.level)
            )
            views.setTextViewText(R.id.defcon_widget_description, intel.description)
            views.setTextViewText(
                R.id.defcon_widget_updated,
                formatTimestamp(context, intel.timestamp)
            )
            views.setInt(R.id.defcon_widget_level_value, "setTextColor", levelColor)
            views.setInt(R.id.defcon_widget_indicator, "setBackgroundColor", levelColor)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    //Shows status messages while loading or on failure.
    private fun renderMessage(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        message: String
    ) {
        for (appWidgetId in appWidgetIds) {
            val views = createBaseViews(context)
            views.setViewVisibility(R.id.defcon_widget_content, android.view.View.GONE)
            views.setViewVisibility(R.id.defcon_widget_message, android.view.View.VISIBLE)
            views.setTextViewText(R.id.defcon_widget_message, message)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    //Creates RemoteViews with shared configuration.
    private fun createBaseViews(context: Context): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_defcon)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.defcon_widget_root, pendingIntent)
        return views
    }

    //Formats the last updated timestamp for display.
    private fun formatTimestamp(context: Context, timestamp: Long): String {
        val formatter = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.getDefault())
        val time = formatter.format(Date(timestamp))
        return context.getString(R.string.defcon_widget_updated, time)
    }

    //Maps DEFCON levels to color resources.
    @ColorInt
    private fun resolveLevelColor(context: Context, level: Int): Int {
        @ColorRes val colorRes = when (level) {
            1 -> R.color.defconLevel1
            2 -> R.color.defconLevel2
            3 -> R.color.defconLevel3
            4 -> R.color.defconLevel4
            else -> R.color.defconLevel5
        }
        return ContextCompat.getColor(context, colorRes)
    }
}

