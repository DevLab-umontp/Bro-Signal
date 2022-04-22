package me.remi.espie.brosignal;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
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


public class BrolistFragment extends Fragment {

    private UserGroup userGroup;

    private View view;
    private View popupView;
    private ViewPagerAdapter adapter;

    private Button broButton;
    private TextView broDesc;

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    public BrolistFragment(UserGroup userGroup) {
        this.userGroup = userGroup;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_brolist_fragment, container, false);
        popupView = inflater.inflate(R.layout.popup_settings, null);
        broButton = view.findViewById(R.id.addBroButton);
        broDesc = view.findViewById(R.id.broDesc);
        if (userGroup !=null) {
            broButton.setBackgroundColor(userGroup.getColor());
            broButton.setTextColor(getContrastColor(userGroup.getColor()));
            broDesc.setText(userGroup.getDescription());
        }

        broButton.setOnClickListener(this::addBro);
        view.findViewById(R.id.addGroup).setOnClickListener(this::createGroup);
        view.findViewById(R.id.groupSettings).setOnClickListener(this::changeSettings);
        view.findViewById(R.id.deleteGroup).setOnClickListener(this::deleteSelf);

        for (User u: userGroup.getUserList()) {
            addToDrawer(u);
        }

        int dp1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1,
                view.getResources().getDisplayMetrics());

        Log.i("dp1", String.valueOf(dp1));
        return view;
    }

    private void addToDrawer(User user) {
        GridLayout brolist = view.findViewById(R.id.brolist);
        //create layout
        LinearLayout verticalLayout = new LinearLayout(getContext());
        verticalLayout.setOrientation(LinearLayout.VERTICAL);
        verticalLayout.setBackgroundColor(Color.WHITE);
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
            thumbnail.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        }
        thumbnail.setBackgroundColor(Color.LTGRAY);
        thumbnail.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.2f
        ));

        //set other contact data
        TextView contactName = new TextView(getContext());
        contactName.setText(user.getContactName());
        contactName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
        ));
        contactName.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView contactNumber = new TextView(getContext());
        contactNumber.setText(user.getContactNumber());
        contactNumber.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
        ));
        contactNumber.setGravity(Gravity.CENTER_HORIZONTAL);

        //add delete bin next to contact
        ImageView contactBin = new ImageView(getContext());
        contactBin.setImageURI(Uri.parse("android.resource://me.remi.espie.brosignal/" + R.drawable.ic_baseline_delete_forever_24));
        contactBin.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_IN);
        contactBin.setOnClickListener(view -> {
            userGroup.removeUser(user);
            brolist.removeView(verticalLayout);
        });
        contactBin.setBackgroundColor(Color.RED);
        contactBin.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                0.1f
        ));

        //add data to drawer
        verticalLayout.addView(thumbnail);
        verticalLayout.addView(contactName);
        verticalLayout.addView(contactNumber);
        verticalLayout.addView(contactBin);

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
                    //readUserGroups();
                            if (!userGroup.getUserList().isEmpty()) {
                                for (User u : userGroup.getUserList()) {
                                    if (u.getContactID().equals(contactID)) {
                                        Toast.makeText(getActivity(), "BRO déjà enregistré", Toast.LENGTH_LONG).show();
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
                                //writeUserToFile(user);
                                userGroup.addUser(user);
                                addToDrawer(user);

                                //close data
                                phoneNumber.close();

                    }
                    contactData.close();
                }

            }
        } else {
            Toast.makeText(getActivity(), "Veuillez sélectionner un BRO", Toast.LENGTH_LONG).show();
        }
    }

    private boolean checkContactPerm(View view) {
        return ContextCompat.checkSelfPermission(
                view.getContext(),
                android.Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestContactPerm() {
        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 5);
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, 2);
    }

    private void addBro(View view) {
        if (checkContactPerm(view)) {
            pickContact();
        } else {
            requestContactPerm();
        }
    }

    private int getContrastColor(int color) {
        double y = (299 * Color.red(color) + 587 * Color.green(color) + 114 * Color.blue(color)) / 1000;
        return y >= 128 ? Color.BLACK : Color.WHITE;
    }

    private void deleteSelf(View v){
        Log.e("delete", "group " + userGroup.getName() + " deleted ? " + userGroup.deleteSelf());
        getAdapter().removeFragment(this);
    }

    private void createGroup(View v){
        UserGroup temp = new UserGroup("BROs", "Mes nouveaux BROs !", "", getRandomColor(), userGroup.getParentList());
        userGroup.getParentList().add(temp);
        getAdapter().addFragment(new BrolistFragment(temp));
    }

    private void changeSettings(View v){

        PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, true);

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

        popupView.findViewById(R.id.cancelButton).setOnClickListener((View vi)->popupWindow.dismiss());

        popupView.findViewById(R.id.validateButton).setOnClickListener((View vi)-> {
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

    private int getRandomColor(){
        Random rnd = new Random();
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }

    private ViewPagerAdapter getAdapter(){
        if (adapter == null){
            ViewPager2 viewPager2 = (ViewPager2) view.getRootView().findViewById(R.id.groupList);
            adapter = (ViewPagerAdapter) viewPager2.getAdapter();
        }
        return adapter;
    }


}