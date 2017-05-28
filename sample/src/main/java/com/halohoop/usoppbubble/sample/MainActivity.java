package com.halohoop.usoppbubble.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

import com.halohoop.usoppbubble.widget.UsoppBubble;

public class MainActivity extends AppCompatActivity {

    private UsoppBubble bubble0;
    private UsoppBubble bubble1;
    private UsoppBubble bubble2;
    private UsoppBubble bubble3;
    private UsoppBubble bubble4;
    private UsoppBubble bubble5;
    private UsoppBubble bubble6;
    private UsoppBubble bubble7;
    private UsoppBubble bubble8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        bubble0 = (UsoppBubble) findViewById(R.id.bubble0);
        bubble1 = (UsoppBubble) findViewById(R.id.bubble1);
        bubble2 = (UsoppBubble) findViewById(R.id.bubble2);
        bubble3 = (UsoppBubble) findViewById(R.id.bubble3);
        bubble4 = (UsoppBubble) findViewById(R.id.bubble4);
        bubble5 = (UsoppBubble) findViewById(R.id.bubble5);
        bubble6 = (UsoppBubble) findViewById(R.id.bubble6);
        bubble7 = (UsoppBubble) findViewById(R.id.bubble7);
        bubble8 = (UsoppBubble) findViewById(R.id.bubble8);

        bubble1.setmMode(UsoppBubble.MODE_EMBOSS);
        bubble2.setmMode(UsoppBubble.MODE_GLOW);
        bubble4.setmMode(UsoppBubble.MODE_EMBOSS);
        bubble5.setmMode(UsoppBubble.MODE_GLOW);
        bubble7.setmMode(UsoppBubble.MODE_EMBOSS);
        bubble8.setmMode(UsoppBubble.MODE_GLOW);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void openListAct(MenuItem item){
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }
}
