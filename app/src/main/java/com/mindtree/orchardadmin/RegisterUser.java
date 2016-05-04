package com.mindtree.orchardadmin;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class RegisterUser extends AppCompatActivity {
    private static Context context;
    private static final String PREFS_NAME;
    private CheckInternet cd;
    private Boolean isInternetPresent;
    private static Firebase ref;
    private static final int FILE_SELECT_CODE = 0;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    static int excelCount;
    static String message = "";
    ScrollView scrollView;
    static TextView messageContent,messageHeading;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static {
        PREFS_NAME = Constants.LOGIN_PREFS;
    }

    public RegisterUser() {
        context = this;
        isInternetPresent = false;
    }

    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(getString(R.string.color)));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        cd = new CheckInternet(getApplicationContext());
        Firebase.setAndroidContext(this);
        message="";
        scrollView=(ScrollView)findViewById(R.id.scrollView);
        messageContent=(TextView)findViewById(R.id.message);
        messageContent.setText("");
        messageHeading=(TextView)findViewById(R.id.messageHeading);
        messageHeading.setVisibility(View.GONE);
        Button submitRegisterButton = (Button) findViewById(R.id.submitRegister);
        submitRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isInternetPresent = cd.isConnectingToInternet();
                if (isInternetPresent) {
                    verifyStoragePermissions(RegisterUser.this);
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
        });
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(
                    Intent.createChooser(intent, "Select a File to Upload"),
                    FILE_SELECT_CODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case FILE_SELECT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    Log.d("TAG", "File Uri: " + uri.toString());
                    try {
                        path = FileUtils.getPath(this, uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.d("TAG", "File Path: " + path);

                    readExcel(path);


                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private static void readExcel(String path) {
        try {
            Log.e("path", path);
            message = "";
            messageContent.setText("");
            messageHeading.setVisibility(View.GONE);
            Workbook m_workBook = Workbook.getWorkbook(new File(path));
            Sheet sheet = m_workBook.getSheet(0);
            String excelMID;
            String excelName;
            excelCount = sheet.getRows();

            final ArrayList<String> MIDList = new ArrayList<>();
            final ArrayList<String> NameList = new ArrayList<>();
            for (int j = 0; j < sheet.getRows(); j++) {
                message += "(" + (j + 1) + ") ";
                for (int k = 0; k < sheet.getColumns(); k++) {
                    Cell column1_cell = sheet.getCell(k, j);
                    if (k == 0) {
                        if (column1_cell.getContents().length() == 8 && (column1_cell.getContents().substring(0, 1).equals(Constants.M) || column1_cell.getContents().substring(0, 1).equals(Constants.SMALL_M))) {
                            if (column1_cell.getContents().substring(1, 8).matches("[0-9]+")) {
                                if (column1_cell.getContents().substring(0, 1).equals(Constants.SMALL_M)) {
                                    excelMID = Constants.M + column1_cell.getContents().substring(1, 8);
                                } else {
                                    excelMID = column1_cell.getContents();
                                }
                                MIDList.add(excelMID);
                                message += excelMID + ", ";
                            }
                        }
                    }
                    if (k == 1) {
                        excelName = column1_cell.getContents();
                        NameList.add(excelName);
                        message += excelName + "\n";
                    }
                }
            }
            Log.e("message", message);
            registerNewUser(MIDList, NameList);

        } catch (BiffException | IOException e) {
            e.printStackTrace();
        }
    }

    public void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            showFileChooser();

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showFileChooser();
                } else {
                    Toast.makeText(RegisterUser.this, "Permission Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }


    private static void registerNewUser(final ArrayList<String> MIDList, final ArrayList<String> nameList) {
        final ProgressDialog progressDialog;
        progressDialog = ProgressDialog.show(context, Constants.PLEASE_WAIT, "Registering " + MIDList.size() + " Campus Minds...", true, true);
        for (int i = 0; i != MIDList.size(); i++) {
            ref = new Firebase("https://torrid-fire-7819.firebaseio.com/users/" + MIDList.get(i));
            final int finalI = i;
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (finalI == MIDList.size() - 1) {
                        progressDialog.dismiss();
                        Toast.makeText(context, "Campus minds registered successfully!", Toast.LENGTH_LONG).show();
                        messageContent.setText(message);
                        messageHeading.setVisibility(View.VISIBLE);
                    }
                    if (!snapshot.exists()) {
                        ref = new Firebase("https://torrid-fire-7819.firebaseio.com/users/");
                        ref.child(MIDList.get(finalI)).child("username").setValue(nameList.get(finalI));
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e("username", "The read failed. Reason: " + firebaseError.getMessage());
                }
            });
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


