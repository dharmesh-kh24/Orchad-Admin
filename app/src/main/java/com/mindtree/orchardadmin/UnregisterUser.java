package com.mindtree.orchardadmin;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.ArrayList;

public class UnregisterUser extends AppCompatActivity {

    private Context context;
    private static final String PREFS_NAME;
    private EditText MIDStartEditText, MIDEndEditText, SingleMIDEditText;
    private String MIDStart, MIDEnd, SingleMID;
    private CheckInternet cd;
    private Boolean isInternetPresent;
    private Firebase ref;

    static {
        PREFS_NAME = Constants.LOGIN_PREFS;
    }

    public UnregisterUser() {
        context = this;
        isInternetPresent = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unregister_user);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(getString(R.string.color)));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        cd = new CheckInternet(getApplicationContext());
        Firebase.setAndroidContext(this);
        Button submitMultipleButton = (Button) findViewById(R.id.submitMultiple);
        Button submitSingleButton = (Button) findViewById(R.id.submitSingle);
        submitMultipleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                MIDStartEditText = (EditText) findViewById(R.id.enterStart);
                MIDEndEditText = (EditText) findViewById(R.id.enterEnd);
                MIDStart = MIDStartEditText.getText().toString();
                MIDEnd = MIDEndEditText.getText().toString();
                MIDStartEditText.setText("");
                MIDEndEditText.setText("");
                boolean isValidMID = false;
                if (MIDStart.length() == 8 && (MIDStart.substring(0, 1).equals(Constants.M) || MIDStart.substring(0, 1).equals(Constants.SMALL_M))) {
                    if (MIDStart.substring(1, 8).matches("[0-9]+")) {
                        isValidMID = true;
                    }
                }

                if (!isValidMID)
                    MIDStartEditText.setError(Constants.INVALID_MID_VALID_MID_EXAMPLES_M1234567_M1234567);
                else {
                    isValidMID = false;
                    if (MIDEnd.length() == 8 && (MIDEnd.substring(0, 1).equals(Constants.M) || MIDEnd.substring(0, 1).equals(Constants.SMALL_M))) {
                        if (MIDEnd.substring(1, 8).matches("[0-9]+")) {
                            isValidMID = true;
                        }
                    }

                    if (!isValidMID)
                        MIDEndEditText.setError(Constants.INVALID_MID_VALID_MID_EXAMPLES_M1234567_M1234567);
                    else {

                        if (MIDStart.substring(0, 1).equals(Constants.SMALL_M))
                            MIDStart = Constants.M + MIDStart.substring(1, 8);
                        if (MIDEnd.substring(0, 1).equals(Constants.SMALL_M))
                            MIDEnd = Constants.M + MIDEnd.substring(1, 8);
                        isInternetPresent = cd.isConnectingToInternet();
                        if (isInternetPresent) {
                            unregisterMultipleUsers();
                        } else {
                            AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                            alertDialog.setTitle(Constants.NO_INTERNET_CONNECTION);
                            alertDialog.setMessage(Constants.PLEASE_CONNECT_TO_INTERNET);
                            alertDialog.setButton(Constants.OK, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                            alertDialog.show();
                        }
                    }
                }
            }
        });
        submitSingleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                SingleMIDEditText = (EditText) findViewById(R.id.singleMID);
                SingleMID = SingleMIDEditText.getText().toString();
                SingleMIDEditText.setText("");
                boolean isValidMID = false;
                if (SingleMID.length() == 8 && (SingleMID.substring(0, 1).equals(Constants.M) || SingleMID.substring(0, 1).equals(Constants.SMALL_M))) {
                    if (SingleMID.substring(1, 8).matches("[0-9]+")) {
                        isValidMID = true;
                    }
                }
                if (!isValidMID)
                    SingleMIDEditText.setError(Constants.INVALID_MID_VALID_MID_EXAMPLES_M1234567_M1234567);
                else {

                    if (SingleMID.substring(0, 1).equals(Constants.SMALL_M))
                        SingleMID = Constants.M + SingleMID.substring(1, 8);
                    isInternetPresent = cd.isConnectingToInternet();
                    if (isInternetPresent) {
                        unregisterSingleUsers();
                    } else {
                        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
                        alertDialog.setTitle(Constants.NO_INTERNET_CONNECTION);
                        alertDialog.setMessage(Constants.PLEASE_CONNECT_TO_INTERNET);
                        alertDialog.setButton(Constants.OK, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                        alertDialog.show();
                    }
                }
            }
        });
    }

    private void unregisterMultipleUsers() {
        final ArrayList<String> MIDList= new ArrayList<>();
        long first= Integer.parseInt(MIDStart.substring(1, 8));
        long last= Integer.parseInt(MIDEnd.substring(1, 8));
        if(first<last) {
            for (long i = first;i<=last;i++){
                MIDList.add(Constants.M + i);
            }
        }
        else{
            for (long i = last;i<=first;i++){
                MIDList.add(Constants.M + i);
            }
        }
        final ProgressDialog progressDialog;
        progressDialog = ProgressDialog.show(context, Constants.PLEASE_WAIT, "Unregistering "+MIDList.size()+" Campus Minds...", true, true);
        final int[] i = {0};
        for(final String st:MIDList){
            ref = new Firebase("https://torrid-fire-7819.firebaseio.com/users/"+st);
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    i[0]++;
                    Log.e("MID is:",st);
                    if (i[0]==MIDList.size()) {
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Campus minds unregistered successfully!", Toast.LENGTH_LONG).show();
                    }
                    if (snapshot.exists()) {
                        ref = new Firebase("https://torrid-fire-7819.firebaseio.com/users/");
                        ref.child(st).removeValue();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("username", "The read failed. Reason: " + firebaseError.getMessage());
                }
            });
        }
    }
    private void unregisterSingleUsers() {
        final ProgressDialog progressDialog;
        progressDialog = ProgressDialog.show(context, Constants.PLEASE_WAIT, "Unregistering Campus Mind...", true, true);
        ref = new Firebase("https://torrid-fire-7819.firebaseio.com/users/"+SingleMID);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressDialog.dismiss();
                if (!snapshot.exists()) {
                    Toast.makeText(getApplicationContext(), SingleMID + " is not registered", Toast.LENGTH_LONG).show();
                } else {
                    ref = new Firebase("https://torrid-fire-7819.firebaseio.com/users/");
                    ref.child(SingleMID).removeValue(new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError != null) {
                                Toast.makeText(getApplicationContext(),"Data could not be removed. " + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),  SingleMID+ " successfully removed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e("username", "The read failed. Reason: " + firebaseError.getMessage());
            }
        });
    }

    public void onRadioButtonClicked(View view) {

        boolean checked = ((RadioButton) view).isChecked();
        switch (view.getId()) {
            case R.id.singleRadio:
                if (checked) {

                    RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.multipleRelativeLayout);
                    relativeLayout.setVisibility(View.GONE);
                    relativeLayout = (RelativeLayout) findViewById(R.id.singleRelativeLayout);
                    relativeLayout.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.multipleRadio:
                if (checked) {

                    RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.singleRelativeLayout);
                    relativeLayout.setVisibility(View.GONE);
                    relativeLayout = (RelativeLayout) findViewById(R.id.multipleRelativeLayout);
                    relativeLayout.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_select, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar wilZ
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(this, About.class);
            startActivity(i);
            return true;
        }
        if (id == R.id.exit) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setTitle(Constants.EXIT_APPLICATION);

            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton(Constants.EXIT_NOW,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    moveTaskToBack(true);
                                    android.os.Process.killProcess(android.os.Process.myPid());
                                    System.exit(1);

                                }
                            })

                    .setNegativeButton(Constants.GO_BACK, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            dialog.cancel();
                        }
                    });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

        }
        if (id == R.id.switch_user) {
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove(Constants.LOGGED);
            editor.remove(Constants.USER_NAME);
            editor.remove(Constants.MID2);
            editor.apply();
            finish();
            Intent i = new Intent(this, Login.class);
            startActivity(i);
            return true;
        }
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}



