package me.remi.espie.brosignal;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.tabs.TabLayout;

import java.util.Random;

import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Fragment groupe de BROs
 */
public class BrolistFragment extends Fragment {

    private UserGroup userGroup;

    private View view;
    private View popupView;
    private ViewPagerAdapter adapter;

    private Button broButton;
    private TextView broDesc;
    private final Settings settings = Settings.getInstance();

//    public BrolistFragment() {
//    }

    /**
     * Création d'un fragment obligatoirement avec un groupe de BROs
     *
     * @param userGroup groupe de BROs du groupe
     */
    public BrolistFragment(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_brolist_fragment, container, false);

        popupView = inflater.inflate(R.layout.popup_settings, container);
        broButton = view.findViewById(R.id.addBroButton);
        broDesc = view.findViewById(R.id.broDesc);
        if (userGroup != null) {
            broButton.setBackgroundColor(userGroup.getColor());
            broButton.setTextColor(getContrastColor(userGroup.getColor()));
            broDesc.setText(userGroup.getDescription());
        }

        //Rempli la liste des BROs
        if (userGroup != null) {
            for (User u : userGroup.getUserList()) {
                addToDrawer(u, settings.isShowNumbers());
            }
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        broButton.setOnClickListener(this::addBro);
        view.findViewById(R.id.addGroup).setOnClickListener(this::createGroup);
        view.findViewById(R.id.groupSettings).setOnClickListener(this::changeSettings);
        view.findViewById(R.id.deleteGroup).setOnClickListener(this::alertDeleteSelf);
        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Ajoute un BRO à la fin de la liste des BROs
     *
     * @param user       BRO à ajouter
     * @param showNumber Le numéro de téléphone doit-il être affiché ?
     */
    private void addToDrawer(User user, boolean showNumber) {
        GridLayout brolist = view.findViewById(R.id.brolist);

        //création du layout principal et de ses param
        LinearLayout verticalLayout = new LinearLayout(getContext());
        verticalLayout.setBackgroundResource(R.drawable.card);
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams verticalParams = new LinearLayout.LayoutParams(
                300,
                300,
                1f
        );
        verticalParams.setMargins(25, 25, 25, 25);
        verticalLayout.setLayoutParams(verticalParams);
        verticalLayout.setGravity(Gravity.CENTER_HORIZONTAL);


        //set contact thumbnail
        ImageView thumbnail = new ImageView(getContext());
        if (!user.getContactThumbnails().equals("")) {
            thumbnail.setImageURI(Uri.parse(user.getContactThumbnails()));
        } else {
            thumbnail.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_person_24));
            thumbnail.setColorFilter(getResources().getColor(R.color.darkey), PorterDuff.Mode.SRC_IN);
        }
        thumbnail.setBackgroundResource(R.drawable.rounded_corner);
        thumbnail.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.2f
        ));
        verticalLayout.addView(thumbnail);


        //create contact name, set and add it
        TextView contactName = new TextView(getContext());
        contactName.setText(user.getContactName());
        contactName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
        ));
        contactName.setGravity(Gravity.CENTER_HORIZONTAL);
        contactName.setBackgroundColor(getResources().getColor(R.color.whitey));
        verticalLayout.addView(contactName);

        //*if relevant* create contact number, set and add it
        if (showNumber) {
            TextView contactNumber = new TextView(getContext());
            contactNumber.setText(user.getContactNumber());
            contactNumber.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    0.1f
            ));
            contactNumber.setGravity(Gravity.CENTER_HORIZONTAL);
            contactNumber.setBackgroundColor(getResources().getColor(R.color.whitey));
            verticalLayout.addView(contactNumber);
        }

        //create delete bin, set it and add it next to contact
        ImageView contactBin = new ImageView(getContext());
        contactBin.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_delete_forever_24));
        contactBin.setColorFilter(Color.RED, PorterDuff.Mode.SRC_IN);
        contactBin.setOnClickListener(view -> {
            userGroup.removeUser(user);
            brolist.removeView(verticalLayout);
        });
        contactBin.setBackgroundResource(R.drawable.rounded_corner);
        contactBin.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
        ));
        verticalLayout.addView(contactBin);

        //add data to drawer
        brolist.addView(verticalLayout);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 2) {
                Uri contentData = data.getData();
                Cursor phoneNumber;
                Cursor contactData = getContext().getContentResolver().query(contentData, null, null, null, null);
                if (contactData.moveToFirst()) {
                    //if contact data exists, create new contact
                    String contactID = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts._ID));
                    String contactName = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    String contactThumbnails = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    String idResult = contactData.getString(contactData.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                    int idResultHold = Integer.parseInt(idResult);

                    //check if user already exists
                    if (!userGroup.getUserList().isEmpty()) {
                        for (User u : userGroup.getUserList()) {
                            if (u.getContactID().equals(contactID)) {
                                Toast.makeText(getActivity(), R.string.bro_already_saved, Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }
                    }

                    if (idResultHold == 1) {
                        phoneNumber = getContext().getContentResolver().query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID,
                                null,
                                null
                        );

                        String contactNumber = "";

                        //get last phoneNumber
                        while (phoneNumber.moveToNext()) {
                            contactNumber = phoneNumber.getString(phoneNumber.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        contactNumber = contactNumber.replaceAll("\\s", "");

                        //create user depending of thumbnail presence
                        User user;
                        if (contactThumbnails != null)
                            user = new User(
                                    contactID,
                                    contactName,
                                    contactThumbnails,
                                    contactNumber
                            );
                        else user = new User(
                                contactID,
                                contactName,
                                "",
                                contactNumber
                        );

                        //add user to view and to file
                        userGroup.addUser(user);
                        addToDrawer(user, settings.isShowNumbers());

                        //close data
                        phoneNumber.close();

                    }
                    contactData.close();
                }

            }
        } else {
            Toast.makeText(getActivity(), R.string.select_bro, Toast.LENGTH_SHORT).show();
        }
    }

    private void changeSettings(View v) {

        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);
        popupWindow.setAnimationStyle(R.style.popup_window_fade);


        TextView editGroupName = popupView.findViewById(R.id.editGroupName);
        final String[] groupName = {userGroup.getName()};
        editGroupName.setText(groupName[0]);

        TextView editGroupDesc = popupView.findViewById(R.id.editGroupDesc);
        final String[] groupDesc = {userGroup.getDescription()};
        editGroupDesc.setText(groupDesc[0]);

        TextView editGroupCustomMessage = popupView.findViewById(R.id.editCustomMessage);
        final String[] groupCustomMessage = {userGroup.getCustomMessage()};
        editGroupCustomMessage.setText(groupCustomMessage[0]);

        View colorView = popupView.findViewById(R.id.colorView);
        final int[] groupColor = {userGroup.getColor()};
        colorView.setBackgroundColor(groupColor[0]);

        colorView.setOnClickListener((View view1) ->
                new AmbilWarnaDialog(this.getContext(), groupColor[0], new AmbilWarnaDialog.OnAmbilWarnaListener() {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color) {
                        groupColor[0] = color;
                        colorView.setBackgroundColor(color);
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog) {
                        // cancel was selected by the user
                    }
                }).show());

        editGroupName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                groupName[0] = editable.toString();
            }
        });

        editGroupCustomMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                groupCustomMessage[0] = editable.toString();
            }
        });

        editGroupDesc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                groupDesc[0] = editable.toString();
            }
        });

        popupView.findViewById(R.id.cancelButton).setOnClickListener((View vi) -> popupWindow.dismiss());

        popupView.findViewById(R.id.validateButton).setOnClickListener((View vi) -> {
            userGroup.setName(groupName[0]);
            TabLayout tabLayout = view.getRootView().findViewById(R.id.groupName);
            tabLayout.getTabAt(tabLayout.getSelectedTabPosition()).setText(groupName[0]);

            userGroup.setDescription(groupDesc[0]);
            broDesc.setText(groupDesc[0]);

            userGroup.setColor(groupColor[0]);
            broButton.setBackgroundColor(groupColor[0]);
            broButton.setTextColor(getContrastColor(groupColor[0]));

            userGroup.setCustomMessage(groupCustomMessage[0]);

            popupWindow.dismiss();
        });

        popupWindow.showAtLocation(view.getRootView(), Gravity.CENTER, 0, 0);
    }

    /**
     * Ajoute un groupe à l'adapter
     * @param v Vue globale
     */
    private void createGroup(View v) {
        UserGroup temp = new UserGroup("BROs", getString(R.string.new_bro), "", getRandomColor(), userGroup.getParentList());
        userGroup.getParentList().add(temp);
        getAdapter().addFragment(new BrolistFragment(temp));
    }

    /**
     * Check les permissions puis
     * Sélectionne un contact et l'ajoute à la vue
     * @param view
     */
    private void addBro(View view) {
        if (checkContactPerm(view)) {
            pickContact();
        } else {
            requestContactPerm();
        }
    }

    /**
     * Check les permissions de la view
     * @param view
     * @return permission READ_CONTACT
     */
    private boolean checkContactPerm(View view) {
        return ContextCompat.checkSelfPermission(
                view.getContext(),
                android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Request permission READ_CONTACT
     */
    private void requestContactPerm() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 1);
    }

    /**
     * Lance l'Intent pour sélectionner un contact
     */
    public void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 2);
    }

    /**
     * Retourne la couleur la plus contrasté (noir ou blanc) en fonction de @color
     * @param color couleur de fond
     * @return Color.BLACK ou Color.WHITE
     */
    private int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }

    /**
     * Retourne une couleur aléatoire
     * @return couleur aléatoire
     */
    private int getRandomColor() {
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    /**
     * Retourne l'adapter de la view
     * @return ViewPagerAdapter
     */
    private ViewPagerAdapter getAdapter() {
        if (adapter == null) {
            ViewPager2 viewPager2 = view.getRootView().findViewById(R.id.groupList);
            adapter = (ViewPagerAdapter) viewPager2.getAdapter();
        }
        return adapter;
    }

    /**
     * Supprime le groupe de la liste et le fragment associé
     * @param v view actuelle
     */
    private void alertDeleteSelf(View v) {
        //cree nouveau alertDialog et supprimer le groupe si validé
        new AlertDialog.Builder(v.getContext())
                .setTitle(R.string.delete_group_title)
                .setMessage(R.string.delete_group_message)
                .setPositiveButton(R.string.validate, (dialog, which) -> deleteSelf())
                .setNegativeButton(R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deleteSelf(){
        userGroup.deleteSelf();
        getAdapter().removeFragment(this);
    }


}