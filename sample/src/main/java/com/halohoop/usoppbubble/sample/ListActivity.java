package com.halohoop.usoppbubble.sample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.halohoop.usoppbubble.widget.DraggableListener;
import com.halohoop.usoppbubble.widget.UsoppBubble;

import java.util.Random;

public class ListActivity extends AppCompatActivity implements DraggableListener {

    private RecyclerView recycler;
    private Random random;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setContentView(R.layout.activity_list);
        random = new Random();
        recycler = (RecyclerView) findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(new Adapter());
    }

    private final static String TAG = "Halohoop";

    @Override
    public void onBubbleDragStart(UsoppBubble view) {

    }

    @Override
    public void onOnBubbleReleaseWithLaunch(UsoppBubble view) {
        int pos = (int) view.getTag();
        showToast(this,"onOnBubbleReleaseWithLaunch: "+pos);
    }

    @Override
    public void onOnBubbleReleaseWithoutLaunch(UsoppBubble view) {
        int pos = (int) view.getTag();
        showToast(this,"onOnBubbleReleaseWithoutLaunch: "+pos);
    }


    class Adapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View inflate = View.inflate(ListActivity.this, R.layout.item, null);
            ViewHolder viewHolder = new ViewHolder(inflate);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.usoppBubble.setCount(random.nextInt(200));
            holder.tvName.setText(Cheeses.sCheeseStrings[position]);
            holder.usoppBubble.setDragListener(ListActivity.this);
            holder.usoppBubble.setVisibility(View.VISIBLE);
            holder.usoppBubble.setTag(position);
        }

        @Override
        public int getItemCount() {
            return Cheeses.sCheeseStrings.length;
        }
    }

    int mode = 0;
    class ViewHolder extends RecyclerView.ViewHolder {

        UsoppBubble usoppBubble;
        TextView tvName;

        public ViewHolder(View itemView) {
            super(itemView);
            usoppBubble = (UsoppBubble) itemView.findViewById(R.id.bubble);
            usoppBubble.setMode(mode++%2!=0?UsoppBubble.MODE_EMBOSS:UsoppBubble.MODE_GLOW);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
        }
    }
    /**
     * 单例吐司---start
     */
    private static Toast toast;

    public static void showToast(Context context, String string) {
        if(toast == null){
            toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
        }
        toast.setText(string);
        toast.show();
    }
}
