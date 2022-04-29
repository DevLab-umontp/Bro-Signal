package me.remi.espie.brosignal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private final SmsManager smsManager = SmsManager.getDefault();
    private FusedLocationProviderClient fusedLocationClient;

    private final Gson gson = new Gson();
    private boolean sendDelay = false;
    private boolean emergency = false;
    private TransitionDrawable transitionSignal;
    private TransitionDrawable transitionSignalEmergency;
    private final ArrayList<UserGroup> userGroups = new ArrayList<>();
    private TabLayout tabLayout;

    Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //deleteFile();
        readUserGroups();
        settings = readUserData();
        SettingsFragment settingsFragment = new SettingsFragment();

        tabLayout = findViewById(R.id.groupName);
        ViewPager2 viewPager2 = findViewById(R.id.groupList);
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager2.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager2, this::setTabText).attach();

        adapter.addFragment(0, settingsFragment);

        //Si il n'existe aucun groupe, crée 3 groupes vides
        if (userGroups.isEmpty()) {
            userGroups.add(new UserGroup("BRO 1", "Tous mes BROs réunis !", "", Color.RED, userGroups));
            userGroups.add(new UserGroup("BRO 2", "Mes autres maxi BROs !", "", Color.GREEN, userGroups));
            userGroups.add(new UserGroup("BRO 3", "Et mes giga maxi BROs !", "", Color.BLUE, userGroups));
        }
        //ajoute tous les groupes à l'adapter
        for (int i = 0; i < userGroups.size(); i++) {
            adapter.addFragment(new BrolistFragment(userGroups.get(i)));
        }

        //sélectionne le 1er groupe de BRO
        tabLayout.getTabAt(1).select();

        //create bro-sognal button transition
        BitmapDrawable[] drawables = new BitmapDrawable[2];
        drawables[0] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal, null));
        drawables[1] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal_color, null));
        transitionSignal = new TransitionDrawable(drawables);

        //create bro-sognal emergency button transition
        BitmapDrawable[] drawablesEmergency = new BitmapDrawable[2];
        drawablesEmergency[0] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal, null));
        drawablesEmergency[1] = ((BitmapDrawable) ResourcesCompat.getDrawable(getResources(), R.drawable.brosignal_emergency_color, null));
        transitionSignalEmergency = new TransitionDrawable(drawablesEmergency);

        ImageView callbros = findViewById(R.id.callBros);
        callbros.setImageDrawable(transitionSignal);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        //          create listener
        //for buttons
        findViewById(R.id.callBros).setOnClickListener(view -> launchBroSignal());

        // intent for widget
        if (getIntent() != null && getIntent().getAction().equals("callbros")) {
            sendBroSignal(userGroups.get(0));
        }
        if (getIntent() != null && getIntent().getAction().equals("mybroname")) {

        }
        findViewById(R.id.panicButton1).setOnClickListener(view -> launchBroGPSAlert());
        findViewById(R.id.panicButton2).setOnClickListener(view -> launchBroGPSAlert());
    }

    /**
     * Set le titre des tab
     * Si @position = 0, titre = "Paramètres"
     * Sinon, titre = titre du groupe
     * @param tab
     * @param position
     */
    private void setTabText(TabLayout.Tab tab, int position) {
        if (position == 0) {
            tab.setText("Paramètres");
        } else {
            tab.setText(userGroups.get(position - 1).getName());
        }
    }

    @Override
    protected void onStop() {
        saveData();
        super.onStop();
    }

    @Override
    protected void onPause() {
        saveData();
        super.onPause();
    }

    private void saveData() {
        writeUserGroups();
        writeUserData();
    }

    /**
     * Supprime les fichiers de configuration
     */
    private void deleteFile() {
        File brolist = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/bros.json");
        brolist.delete();
        File setting = new File(getApplicationContext().getFilesDir().getAbsolutePath() + "/settings.json");
        setting.delete();
    }

    /**
     * Ajoute la configuration utilisateur sauvegardé à au singleton Settings, et le retourne
     * @return singleton Settings
     */
    private Settings readUserData() {
        File fileName = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/settings.json");
        if (fileName.isFile()) {
            long size = fileName.length();
            if (size != 0L) {
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(fileName.getAbsolutePath()));
                    String line = reader.readLine();
                    Log.i("json read", line);
                    Settings settings = gson.fromJson(line, Settings.class);
                    return settings.setInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteFile();
                }
            } else System.out.println("empty setting file");
        } else System.out.println("not a setting file");
        return Settings.getInstance("", "", false, false);
    }

    /**
     * Sauvegarde les paramètres utilisateurs dans settings.json
     */
    private void writeUserData() {
        File file = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/settings.json");
        FileWriter out;
        try {
            out = new FileWriter(file);
            out.write(gson.toJson(settings));
            Log.i("json write", gson.toJson(settings));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ajoute les différents groupes de BRO à la liste de groupes de BROs
     */
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
                        Log.i("json read", line);
                        UserGroup userGroup = gson.fromJson(line, UserGroup.class);
                        userGroup.setParentList(userGroups);
                        userGroups.add(userGroup);
                        line = reader.readLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    deleteFile();
                }
            } else System.out.println("empty user group file");
        } else System.out.println("not a user group file");
    }

    /**
     * Enregistre la liste de groupes de BROs dans bros.json
     */
    private void writeUserGroups() {
        File file = new File(getApplicationContext().getFilesDir().getAbsoluteFile() + "/bros.json");
        FileWriter out;
        try {
            out = new FileWriter(file);
            for (UserGroup u : userGroups) {
                out.write(gson.toJson(u) + "\n");
                Log.i("json write", gson.toJson(u));
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Ecrit le texte @text dans le fichier @filepath
     * @param filePath chemin de fichier
     * @param text texte à sauvegarder
     */
    private void writeToFile(String filePath, String text) {
        File file = new File(filePath);
        FileWriter out;
        if (file.length() == 0L) {
            try {
                out = new FileWriter(file);
                out.write(text);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                out = new FileWriter(file, true);
                out.write("\n" + text);
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Check la permission de SMS
     * @return permission.SEND_SMS
     */
    private boolean checkSMSPerm() {
        return ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Demande la permission.SEND_SMS
     */
    private void requestSMSPerm() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 5);
    }

    /**
     * Check les permissions puis
     * Les demande si nécessaire
     * Envoi les messages aux groupe sélectionné sinon
     */
    private void launchBroSignal() {
        if (!checkSMSPerm()) requestSMSPerm();
        else {
            UserGroup userGroup = checkValidGroup();
            if (userGroup != null) sendBroSignal(userGroup);
        }
    }

    /**
     * Envoi le message au groupe @userGroup
     * @param userGroup groupe d'utilisateur à qui envoyer les messages
     */
    private void sendBroSignal(UserGroup userGroup) {
        ImageView callBros = findViewById(R.id.callBros);
        if (emergency) {
            emergency = false;
            callbrosAnimation(callBros);
        } else if (settings.isSpam() || (!settings.isSpam() && !sendDelay)) {
            if (!sendDelay) {
                sendDelay = true;
                callBros.setImageDrawable(transitionSignal);
                transitionSignal.startTransition(50);
                callbrosAnimation(callBros);

                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(750);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        transitionSignal.reverseTransition(150);
                        sendDelay = false;
                    }
                }.start();
            }

            String name = settings.getBroName();

            String messageText;
            if (userGroup.getCustomMessage().equals("")) {
                messageText = settings.getCustomMessage();
            } else {
                messageText = userGroup.getCustomMessage();
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


            for (User u : userGroup.getUserList()) {
                smsManager.sendTextMessage(u.getContactNumber(), null, messageText, null, null);
            }
            Toast.makeText(this, R.string.contacted, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Check les Manifest.permission.ACCESS_FINE_LOCATION et Manifest.permission.ACCESS_COARSE_LOCATION
     * @return Manifest.permission.ACCESS_FINE_LOCATION && Manifest.permission.ACCESS_COARSE_LOCATION
     */
    private boolean checkLocationPerm() {
        return ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Demande les permissions Manifest.permission.ACCESS_FINE_LOCATION et Manifest.permission.ACCESS_COARSE_LOCATION
     */
    private void requestLocationPerm() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 7);
    }

    /**
     * Si emergency, désactive l'urgence
     * Sinon, demande les permissions, check le groupe, demande la localisation, démarre l'animation et envoi un message
     */
    private void launchBroGPSAlert() {
        ImageView callBros = this.findViewById(R.id.callBros);
        if (!emergency) {
            if (!checkLocationPerm()) {
                requestLocationPerm();
            } else {
                UserGroup userGroup = checkValidGroup();
                if (userGroup != null) {

                    emergency = true;
                    callBros.setImageDrawable(transitionSignalEmergency);
                    transitionSignalEmergency.startTransition(250);

                    new Thread() {
                        public void run() {
                            boolean layer = true;
                            while (emergency) {
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                layer = !layer;
                                transitionSignalEmergency.reverseTransition(250);
                            }
                            if (layer) transitionSignalEmergency.reverseTransition(250);
                        }
                    }.start();


                    LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        getLocation(userGroup);
                    } else {
                        LocationRequest locationRequest = LocationRequest.create();
                        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        locationRequest.setExpirationTime(10000);
                        locationRequest.setFastestInterval(5000);

                        LocationSettingsRequest.Builder locationSettingRequest = new LocationSettingsRequest.Builder();
                        locationSettingRequest.addLocationRequest(locationRequest);
                        locationSettingRequest.setAlwaysShow(true);
                        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
                        settingsClient.checkLocationSettings(locationSettingRequest.build()).addOnSuccessListener(this, locationSettingsResponse -> getLocation(userGroup))
                                .addOnFailureListener(this, e -> {
                                    if (e instanceof ResolvableApiException) {
                                        try {
                                            ((ResolvableApiException) e).startResolutionForResult(MainActivity.this, 101);
                                        } catch (IntentSender.SendIntentException sendIntentException) {
                                            sendIntentException.printStackTrace();
                                        }
                                    }
                                });
                    }
                }
            }
        } else {
            emergency = false;
        }
    }

    /**
     * Animation simple de l'ImageView callbros
     * @param callBros ImageView à animer
     */
    private void callbrosAnimation(ImageView callBros) {
        callBros.animate().setDuration(250).scaleXBy(-0.1f).scaleYBy(-0.1f)
                .withEndAction(() -> callBros.animate().setDuration(250).scaleXBy(0.1f).scaleYBy(0.1f));
    }

    /**
     * Récupère la localisation et démarre l'envoi d'un message au groupe @userGroup ensuite
     * @param userGroup groupe à qui envoyer un message
     */
    @SuppressLint("MissingPermission")
    private void getLocation(UserGroup userGroup) {
        fusedLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(this, location -> sendBroAlert(location, userGroup));
    }

    /**
     * Envoi un message d'alerte au groupe @userGroup avec la localisation location si no, nulle
     * @param location localisation de l'appareil
     * @param userGroup groupe à qui envoyer le message
     */
    private void sendBroAlert(Location location, UserGroup userGroup) {

        String name = settings.getBroName();

        String messageText;

        if (name.length() != 0) {
            if (location != null) {
                Date d = new Date(location.getTime());
                DateFormat format = new SimpleDateFormat("HH'h'mm:ss", Locale.getDefault());
                String date = format.format(d);
                messageText = getString(R.string.BRO) + name + getString(R.string.emergency_location) + location.getLatitude() + "," + location.getLongitude() + getString(R.string.at) + date + " " + TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
            } else {
                messageText = getString(R.string.BRO) + name + getString(R.string.emergency);
            }
        } else {
            if (location != null) {
                Date d = new Date(location.getTime());
                DateFormat format = new SimpleDateFormat("HH'h'mm:ss", Locale.getDefault());

                String date = format.format(d);
                messageText = getString(R.string.BRO) + getString(R.string.emergency_location) + location.getLatitude() + "," + location.getLongitude() + getString(R.string.at) + date + " " + TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
            } else {
                messageText = getString(R.string.BRO) + getString(R.string.emergency);
            }
        }


        for (User u : userGroup.getUserList()) {
            smsManager.sendTextMessage(u.getContactNumber(), null, messageText, null, null);
        }
        Toast.makeText(this, R.string.contacted, Toast.LENGTH_SHORT).show();

    }

    /**
     * Check si la sélection de groupe est valide
     * @return groupe valide ou null si groupe invalide
     */
    private UserGroup checkValidGroup() {
        int selectedGroup = tabLayout.getSelectedTabPosition() - 1;
        if (selectedGroup < 0) {
            Toast.makeText(this, R.string.select_bro_group, Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!userGroups.isEmpty()) {
            if (userGroups.size() > selectedGroup) {
                if (!userGroups.get(selectedGroup).getUserList().isEmpty()) {
                    return userGroups.get(selectedGroup);
                } else
                    Toast.makeText(this, R.string.no_bro, Toast.LENGTH_SHORT).show();
            } else Log.e("sms", "bad group selection");
        } else {
            Toast.makeText(this, R.string.no_bro, Toast.LENGTH_SHORT).show();
        }
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        //Check si les permissions ont été données et lance les fonctions résultantes
        if (requestCode == 1 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//Ajoute un contact au fragment en cours
            ViewPager2 viewPager2 = findViewById(R.id.groupList);
            ViewPagerAdapter adapter = (ViewPagerAdapter) viewPager2.getAdapter();
            BrolistFragment fragment = (BrolistFragment) adapter.getFragment(tabLayout.getSelectedTabPosition());
            fragment.pickContact();
        } else if (requestCode == 5 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {//envoi un SMS au groupe
            UserGroup userGroup = checkValidGroup();
            if (userGroup != null) sendBroSignal(userGroup);
        } else if (requestCode == 7 && grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //envoi un SMS d'urgence au groupe
            UserGroup userGroup = checkValidGroup();
            if (userGroup != null) getLocation(userGroup);
        } else {
            Toast.makeText(this, R.string.unauthorized, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == 101) {
                UserGroup userGroup = checkValidGroup();
                if (userGroup != null) getLocation(userGroup);
            }
        } else if (requestCode == 101) emergency = false;
    }

}