package com.halohoop.usoppbubble.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.halohoop.usoppbubble.utils.Utils;
import com.halohoop.usoppbubble.widget.DraggableListener;
import com.halohoop.usoppbubble.widget.UsoppBubble;

public class MainActivity extends AppCompatActivity implements DraggableListener, View.OnClickListener {

    private UsoppBubble bubble0;
    private UsoppBubble bubble1;
    private UsoppBubble bubble2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        bubble0 = (UsoppBubble) findViewById(R.id.bubble0);
        bubble0.setMode(UsoppBubble.MODE_GLOW);
        bubble1 = (UsoppBubble) findViewById(R.id.bubble1);
        bubble1.setMode(UsoppBubble.MODE_GLOW);
        bubble2 = (UsoppBubble) findViewById(R.id.bubble2);
        bubble2.setMode(UsoppBubble.MODE_GLOW);

        bubble0.setDragListener(this);
        bubble1.setDragListener(this);
        bubble2.setDragListener(this);
        bubble0.setCount(1);
        bubble1.setCount(1);
        bubble2.setCount(1);
        findViewById(R.id.btn0).setOnClickListener(this);
        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    public void openListAct(MenuItem item) {
        Intent intent = new Intent(this, ListActivity.class);
        startActivity(intent);
    }

    @Override
    public void onBubbleDragStart(UsoppBubble view) {
        switch (view.getId()) {
            case R.id.bubble0:
                Utils.l("start");
                break;
            case R.id.bubble1:
                break;
            case R.id.bubble2:
                break;
        }
    }

    @Override
    public void onOnBubbleReleaseWithLaunch(UsoppBubble view) {

    }

    @Override
    public void onOnBubbleReleaseWithoutLaunch(UsoppBubble view) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn0:
                bubble0.setCount(bubble0.getCount() + 1);
                break;
            case R.id.btn1:
                bubble1.setCount(bubble1.getCount() + 1);
                break;
            case R.id.btn2:
                bubble2.setCount(bubble2.getCount() + 1);
                break;
        }
    }
}
