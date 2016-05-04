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
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

public class ChangePassword extends AppCompatActivity {
    private Context context;
    private static final String PREFS_NAME;
    private EditText oldPasswordEditText, newPasswordEditText, confirmPasswordEditText;
    private String oldPassword, newPassword, confirmPassword;
    private CheckInternet cd;
    private Boolean isInternetPresent;
    private Firebase ref;

    static {
        PREFS_NAME = Constants.LOGIN_PREFS;
    }

    public ChangePassword() {
        context = this;
        isInternetPresent = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(getString(R.string.color)));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String MID = settings.getString("MID", "");
        cd = new CheckInternet(getApplicationContext());
        Firebase.setAndroidContext(this);
        ref = new Firebase("https://torrid-fire-7819.firebaseio.com/admins/" + MID + "/password");
        Button submitPasswordButton = (Button) findViewById(R.id.submitPassword);
        submitPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
                oldPasswordEditText = (EditText) findViewById(R.id.oldpassword);
                newPasswordEditText = (EditText) findViewById(R.id.newpassword);
                confirmPasswordEditText = (EditText) findViewById(R.id.confirmpassword);
                oldPassword = oldPasswordEditText.getText().toString();
                newPassword = newPasswordEditText.getText().toString();
                confirmPassword = confirmPasswordEditText.getText().toString();
                oldPasswordEditText.setText("");
                newPasswordEditText.setText("");
                confirmPasswordEditText.setText("");
                if (oldPassword.equals("")) {
                    oldPasswordEditText.setError("Please enter your old password");
                } else if (newPassword.length() < 4) {
                    newPasswordEditText.setError("Please enter a new password of atleast 4 digits");
                } else if (!confirmPassword.equals(newPassword)) {
                    confirmPasswordEditText.setError("Confirm password should be same as new password");
                } else {

                    isInternetPresent = cd.isConnectingToInternet();
                    if (isInternetPresent) {
                        checkPassword();

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

    private void checkPassword() {
        final ProgressDialog progressDialog;
        progressDialog = ProgressDialog.show(context, Constants.PLEASE_WAIT, "Changing password...", true, true);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                progressDialog.dismiss();
                String currentPassword = snapshot.getValue().toString();
                Log.e("currentpassword", currentPassword);
                if (!currentPassword.equals(oldPassword)) {
                    Toast.makeText(getApplicationContext(), "Old Password is invalid", Toast.LENGTH_LONG).show();

                } else {
                    ref.setValue(newPassword, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            if (firebaseError != null) {
                                Toast.makeText(getApplicationContext(),"Data could not be saved. " + firebaseError.getMessage(), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "Password changed successfully!", Toast.LENGTH_LONG).show();
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


