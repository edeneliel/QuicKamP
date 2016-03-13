package com.example.eden.quickamp;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lge.qcircle.template.*;

import com.lge.qcircle.utils.QCircleFeature;
import com.maxmpz.poweramp.player.PowerampAPI;

public class MainActivity extends Activity {

    //region Valuables
    private QCircleTemplate template;
    private QCircleBackButton backButton;

    private ImageView titleboarder;
    private TextView title;

    private Button play_toggle_btn;
    private Button next_btn;
    private Button prev_btn;

    private BroadcastReceiver mReceiver;
    private BroadcastReceiver aaReceiver;
    private BroadcastReceiver statusReceiver;

    private Intent launchIntent;
    private Intent pwramp_play_toggle;
    private Intent next_song;
    private Intent prev_song;

    private boolean isPlayed;
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        template = new QCircleTemplate(this, TemplateType.CIRCLE_EMPTY);

        setContentView(template.getView());

        //region Launch Poweramp When Case Open
        launchIntent = getPackageManager().getLaunchIntentForPackage("com.maxmpz.audioplayer");
        launchIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        template.setFullscreenIntent(launchIntent);
        //endregion

        //region Poweramp Controls
        pwramp_play_toggle = MakePowerampIntent(PowerampAPI.Commands.TOGGLE_PLAY_PAUSE);
        next_song = MakePowerampIntent(PowerampAPI.Commands.NEXT);
        prev_song = MakePowerampIntent(PowerampAPI.Commands.PREVIOUS);
        //endregion

        //region Buttons
        backButton = new QCircleBackButton(this);

        title = new TextView(this);

        play_toggle_btn = new Button(getApplicationContext());
        play_toggle_btn.setBackgroundResource(R.drawable.play);

        next_btn = new Button(getApplicationContext());
        next_btn.setBackgroundResource(R.drawable.next);

        prev_btn = new Button(getApplicationContext());
        prev_btn.setBackgroundResource(R.drawable.prev);

        titleboarder = new ImageView(this);
        titleboarder.setImageResource(R.drawable.titleboarder);
        //endregion

        //region Costumize Buttons
        backButton.setTheme(ButtonTheme.TRANSPARENT);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        params.topMargin = QCircleFeature.getRelativePixelValue(this, template.getDiameter() / 2 - 200);
        params.leftMargin = QCircleFeature.getRelativePixelValue(this, template.getDiameter() / 2 - 100);
        params.width = QCircleFeature.getRelativePixelValue(this, 200);
        params.height = QCircleFeature.getRelativePixelValue(this, 200);
        play_toggle_btn.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = QCircleFeature.getRelativePixelValue(this, template.getDiameter() / 2 - 200);
        params.leftMargin = QCircleFeature.getRelativePixelValue(this, template.getDiameter() / 2 + 200);
        params.width = QCircleFeature.getRelativePixelValue(this, 200);
        params.height = QCircleFeature.getRelativePixelValue(this, 200);
        next_btn.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = QCircleFeature.getRelativePixelValue(this, template.getDiameter() / 2 - 200);
        params.leftMargin = QCircleFeature.getRelativePixelValue(this, template.getDiameter() / 2 - 400);
        params.width = QCircleFeature.getRelativePixelValue(this, 200);
        params.height = QCircleFeature.getRelativePixelValue(this, 200);
        prev_btn.setLayoutParams(params);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        title.setLayoutParams(params);
        title.setTextColor(Color.argb(80, 255, 255, 255));
        title.setTypeface(null, Typeface.ITALIC);

        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = QCircleFeature.getRelativePixelValue(this, 0);
        params.leftMargin = QCircleFeature.getRelativePixelValue(this, 0);
        params.width = QCircleFeature.getRelativePixelValue(this, 1000);
        params.height = QCircleFeature.getRelativePixelValue(this, 80);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        titleboarder.setLayoutParams(params);
        //endregion

        //region Add Buttons
        template.addElement(backButton);

        RelativeLayout main = template.getLayoutById(TemplateTag.CONTENT_MAIN);
        main.addView(play_toggle_btn);
        main.addView(next_btn);
        main.addView(prev_btn);
        main.addView(titleboarder);
        main.addView(title);
        //endregion

        //region Listeners
        play_toggle_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {startService(pwramp_play_toggle);}});

        next_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(next_song);
            }
        });

        prev_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(prev_song);
            }
        });
        //endregions
    }

    @Override
    protected void onPause() {
        super.onPause();
        template.unregisterReceiver();
        unregisterReceiver(mReceiver);
        unregisterReceiver(aaReceiver);
        unregisterReceiver(statusReceiver);
    }

    protected void onResume(){
        super.onResume();
        setReceivers();
    }

    public void setReceivers() {
        template.registerIntentReceiver();

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle track = intent.getBundleExtra("track");
                title.setText(track.getString("title"));
            }
        };
        IntentFilter trackIntent = new IntentFilter();
        trackIntent.addAction(PowerampAPI.ACTION_TRACK_CHANGED);
        registerReceiver(mReceiver, trackIntent);

        aaReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bitmap album = intent.getParcelableExtra("aaBitmap");
                Drawable album_art = new BitmapDrawable(getResources(), album);
                template.setBackgroundDrawable(album_art);
            }
        };
        IntentFilter albumIntent = new IntentFilter();
        albumIntent.addAction(PowerampAPI.ACTION_AA_CHANGED);
        registerReceiver(aaReceiver, albumIntent);

        statusReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isPlayed = !(intent.getBooleanExtra(PowerampAPI.PAUSED, true));
                System.out.println(isPlayed);
                if (isPlayed == false)
                    play_toggle_btn.setBackgroundResource(R.drawable.play);
                else
                    play_toggle_btn.setBackgroundResource(R.drawable.pause);
            }
        };
        IntentFilter statusIntent = new IntentFilter();
        statusIntent.addAction(PowerampAPI.ACTION_STATUS_CHANGED);
        registerReceiver(statusReceiver, statusIntent);
    }

    private Intent MakePowerampIntent(int cmd) {
        Intent intent = new Intent(PowerampAPI.ACTION_API_COMMAND);
        intent.setPackage(PowerampAPI.PACKAGE_NAME);
        intent.putExtra(PowerampAPI.COMMAND, cmd);
        return intent;
    }
}