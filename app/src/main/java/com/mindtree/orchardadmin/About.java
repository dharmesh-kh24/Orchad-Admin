package com.mindtree.orchardadmin;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.TextView;

public class About extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ColorDrawable colorDrawable = new ColorDrawable(Color.parseColor(getString(R.string.color)));
        getSupportActionBar().setBackgroundDrawable(colorDrawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        String message;
        String text = getString(R.string.webFormat);
        String data = getString(R.string.about);
        WebView webView = (WebView) findViewById(R.id.aboutView);
        webView.loadData(String.format(text, data), getString(R.string.format), getString(R.string.unicode));
        webView.setBackgroundColor(0x00000000);
        message = getString(R.string.leadsInfo);
        TextView leadView = (TextView) findViewById(R.id.leadView);
        leadView.setText(Html.fromHtml(message));
        leadView.setMovementMethod(LinkMovementMethod.getInstance());

        message = getString(R.string.teamInfo);
        TextView teamView = (TextView) findViewById(R.id.teamView);
        teamView.setText(Html.fromHtml(message));
        teamView.setMovementMethod(LinkMovementMethod.getInstance());


    }
@Override
    public boolean onOptionsItemSelected(MenuItem item){
    finish();

        return true;

    }
}
