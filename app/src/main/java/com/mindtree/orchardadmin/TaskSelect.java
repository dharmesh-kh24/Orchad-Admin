package com.mindtree.orchardadmin;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TaskSelect extends AppCompatActivity {
    private static final String PREFS_NAME;

    static {
        PREFS_NAME = "AdminLoginPrefs";
    }

    private Context context;
    public TaskSelect() {

        context = this;

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_select);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(Constants.EA));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.logowhite_50);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String user_name = settings.getString(Constants.USER_NAME, "");
        Log.e(Constants.TEST, user_name);
        TextView userName = (TextView) findViewById(R.id.user_name);
        userName.setText(Constants.HELLO + user_name + "!");
        userName.setTypeface(null, Typeface.ITALIC);
        Button changePassword,registerUser,unregisterUser;
        changePassword=(Button) findViewById(R.id.change_password);
        registerUser=(Button) findViewById(R.id.register);
        unregisterUser=(Button) findViewById(R.id.unregister);
        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskSelect.this, ChangePassword.class);

                startActivity(intent);
            }
        });
        registerUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskSelect.this, RegisterUser.class);

                startActivity(intent);
            }
        });
        unregisterUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TaskSelect.this, UnregisterUser.class);

                startActivity(intent);
            }
        });
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
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
        return super.onOptionsItemSelected(item);
    }
}

