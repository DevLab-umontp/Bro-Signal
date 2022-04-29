package me.remi.espie.brosignal;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.widget.ImageView;
import android.widget.RemoteViews;

import androidx.core.content.res.ResourcesCompat;

/**
 * Implementation of App Widget functionality.
 */
public class Widget_BroSignal extends AppWidgetProvider {

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {


        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget__bro_signal);
        //views.setTextViewText(R.id.widget_myname, getMyBroName());

        // OnClick event
        views.setOnClickPendingIntent(R.id.widget_callBros, getPendingIntentCallBros(context,1));

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    // recupere la pending intent pour la fonction callBros
    private PendingIntent getPendingIntentCallBros(Context context, int value){

        Intent intent = new Intent(context ,MainActivity.class);
        intent.setAction("callbros");

        return PendingIntent.getActivity(context, value, intent, 0);
    }

    // recupere notre Bro's name
    private CharSequence getMyBroName(){

        return "gab";
    }
}