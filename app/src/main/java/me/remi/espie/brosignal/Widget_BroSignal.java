package me.remi.espie.brosignal;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * Implémentation d'une App Widget
 */
public class Widget_BroSignal extends AppWidgetProvider {

    public RemoteViews views;
    public Settings settings;
    public Context context;
    public AppWidgetManager appWidgetManager;
    public int broWidgetId;
    public static final String ACTION_NOTIF_TOAST = "widget.NOTIF_TOAST";
    private final Gson gson = new Gson();

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // initialisation des attributs de classe
        this.context = context;
        settings = readUserData();
        this.appWidgetManager = appWidgetManager;
        broWidgetId = appWidgetId;

        // Construire les objets RemoteViews
        views = new RemoteViews(context.getPackageName(), R.layout.widget__bro_signal);
        views.setTextViewText(R.id.widget_myname, settings.getBroName());

        // Au clic sur l'image, pendingIntent est lancée
        views.setOnClickPendingIntent(R.id.widget_callBros, getPendingIntentBroSignal(context));
        views.setOnClickPendingIntent(R.id.widget_callBros, getPendingIntentTransition(context));

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

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // reception de l'intent NOTIF_TOAST
        if (ACTION_NOTIF_TOAST.equals(intent.getAction())) {
            Log.i("widget", "toast reçu");
            CharSequence message = "Message envoyé à ton 1er groupe de BRO";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, message, duration);
            toast.show();
        }
    }

    // création du service via une pendingintent (seule intent a fonctionner dans les widget personnalisé)
    public PendingIntent getPendingIntentBroSignal(Context context){

        // construire le service BroService en PendingIntent
        Intent intent = new Intent(context ,BroService.class);

        return PendingIntent.getService(context, 0, intent, 0);
    }

    // création du service via une pendingintent (seule intent a fonctionner dans les widget personnalisé)
    public PendingIntent getPendingIntentTransition(Context context){

        // construire le service BroService en PendingIntent
        Intent intent = new Intent(context ,Widget_BroSignal.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, broWidgetId);
        intent.setAction(ACTION_NOTIF_TOAST);

        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    // lecture des fichier JSON dedié au parametre de l'utilisateur
    private Settings readUserData() {
        File fileName = new File(context.getFilesDir().getAbsoluteFile() + "/settings.json");
        if (fileName.isFile()) {
            long size = fileName.length();
            if (size != 0L) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                    String line = reader.readLine();
                    Log.i("widget json read", line);
                    Settings settings = gson.fromJson(line, Settings.class);
                    return settings.setInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteFile();
                }
            } else Log.i("widget", "empty setting file");
        } else Log.i("widget", "not a setting file");
        return Settings.getInstance("", "", false, false);
    }

    // suppression des fichier Json
    private void deleteFile() {
        File broname = new File(context.getFilesDir().getAbsolutePath() + "/broname.txt");
        broname.delete();
        File brolist = new File(context.getFilesDir().getAbsolutePath() + "/bros.json");
        brolist.delete();
        File customMessage = new File(context.getFilesDir().getAbsolutePath() + "/customMessage.txt");
        customMessage.delete();
        File setting = new File(context.getFilesDir().getAbsolutePath() + "/settings.json");
        setting.delete();
    }
}