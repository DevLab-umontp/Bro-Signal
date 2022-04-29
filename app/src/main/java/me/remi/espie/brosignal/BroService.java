package me.remi.espie.brosignal;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Implémentation d'un service
 */
public class BroService extends Service {

    public Settings settings = Settings.getInstance();

    private final SmsManager smsManager = SmsManager.getDefault();
    private final ArrayList<UserGroup> userGroups = new ArrayList<>();
    private final Gson gson = new Gson();
    private boolean sendDelay = false;
    private TabLayout tabLayout;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i("service", "service on bind");
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i("service", "service on create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("service", "service on start command");

        settings = readUserData();  // lecture et implémentation des parametre de l'utilisateur
        readUserGroups();           // lecture des groupes de contact
        launchBroSignal();          // lancement du signal

        return START_NOT_STICKY;
    }

    // lancement du BroSignal
    private void launchBroSignal() {
        if (checkSMSPerm()) sendBroSignal();
        else{
            Log.i("service", "Manque d'autorisation pour envoyer des messages");
        }
    }

    // Vérification des autorisations
    private boolean checkSMSPerm() {
        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    // suppression des fichier Json
    private void deleteFile() {
        File broname = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/broname.txt");
        broname.delete();
        File brolist = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json");
        brolist.delete();
        File customMessage = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/customMessage.txt");
        customMessage.delete();
        File setting = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/settings.json");
        setting.delete();
    }

    // lecture des Groupe de Bro
    private void readUserGroups() {
        File fileName = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/bros.json");
        if (fileName.isFile()) {
            long size = fileName.length();
            if (size != 0L) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                    String line = reader.readLine();
                    while (line != null) {
                        Log.i("service json read", line);
                        UserGroup userGroup = gson.fromJson(line, UserGroup.class);
                        userGroup.setParentList(userGroups);
                        userGroups.add(userGroup);
                        line = reader.readLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteFile();
                }
            } else Log.i("service", "empty user group file");
        } else Log.i("service", "not a user group file");
    }

    // lecture des parametre de l'utilisateur
    private Settings readUserData() {
        File fileName = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/settings.json");
        if (fileName.isFile()) {
            long size = fileName.length();
            if (size != 0L) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                    String line = reader.readLine();
                    Log.i("service json read", line);
                    Settings settings = gson.fromJson(line, Settings.class);
                    return settings.setInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteFile();
                }
            } else Log.i("service", "empty setting file");
        } else Log.i("service", "not a setting file");
        return Settings.getInstance("", "", false, false);
    }

    // envoi du BroSignal
    private void sendBroSignal() {

        Log.i("service", "début sendBroSignal");

        if (settings.isSpam() || (!settings.isSpam() && !sendDelay)) {
            if (!sendDelay) {
                sendDelay = true;

                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(750);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        sendDelay = false;
                    }
                }.start();
            }

            // on prend ici pour le widget automatiquement le premier groupe dans la liste
            int selectedGroup = 0;

            String name = settings.getBroName();

            if (!userGroups.isEmpty()) {

                String messageText;
                if (userGroups.get(selectedGroup).getCustomMessage().equals("")) {
                    messageText = settings.getCustomMessage();
                } else {
                    messageText = userGroups.get(selectedGroup).getCustomMessage();
                }

                if (name.length() != 0) {
                    if (messageText.length() == 0) {
                        messageText = "BRO !! Ton BRO " + name + " a besoin d'aide !";
                    } else {
                        messageText = messageText.replace("$nom", name);
                    }
                } else {
                    if (messageText.length() == 0) {
                        messageText = "BRO !! Ton BRO anonyme a besoin d'aide !";
                    }
                }

                if (!userGroups.get(selectedGroup).getUserList().isEmpty()) {
                    for (User u : userGroups.get(selectedGroup).getUserList()) {
                        smsManager.sendTextMessage(u.getContactNumber(), null, messageText, null, null);
                    }
                } else Toast.makeText(this, R.string.no_bro, Toast.LENGTH_LONG).show();
            } else Toast.makeText(this, R.string.no_bro, Toast.LENGTH_LONG).show();
        }
    }
}
