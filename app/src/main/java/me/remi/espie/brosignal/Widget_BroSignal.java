package me.remi.espie.brosignal;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Implémentation d'une App Widget
 */
public class Widget_BroSignal extends AppWidgetProvider {

    public RemoteViews views;
    public Settings settings;

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construire les objets RemoteViews
        views = new RemoteViews(context.getPackageName(), R.layout.widget__bro_signal);
        views.setTextViewText(R.id.widget_myname, settings.getBroName());

        // construire le service BroService en PendingIntent
        Intent intent = new Intent(context ,BroService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);

        // Au clic sur l'image, pendingIntent est lancée
        views.setOnClickPendingIntent(R.id.widget_callBros, pendingIntent);

        // Dire au widget manager de mettre à jour le widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Il peut y avoir plusieurs widget actif, on les met tous à jours
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Entrer une fonctionnalité quand le premier widget est créé
    }

    @Override
    public void onDisabled(Context context) {
        // Entrer une fonctionnalité quand le dernier widget est désactivé
    }
}