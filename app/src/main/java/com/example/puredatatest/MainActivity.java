package com.example.puredatatest;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Debug;
import android.os.IBinder;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import org.puredata.android.io.AudioParameters;
import org.puredata.android.service.PdPreferences;
import org.puredata.android.service.PdService;
import org.puredata.core.PdBase;
import org.puredata.core.PdBaseLoader;
import org.puredata.core.PdReceiver;
import org.puredata.core.utils.IoUtils;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.puredata.core.PdBase.startAudio;

public class MainActivity extends Activity implements View.OnTouchListener{
    private static final String TAG = "Pd Test";

    private PdService pdService = null;

    private Toast toast = null;

    private void initGui() {
        setContentView(R.layout.activity_main);

        View layout = findViewById(R.id.layout);
        ArrayList<View> buttons = layout.getTouchables();
        for(View button : buttons){
            button.setOnTouchListener(this);
        }

    }

    private void toast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (toast == null) {
                    toast = Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT);
                }
                toast.setText(TAG + ": " + msg);
                toast.show();
            }
        });
    }

    private void post(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
            }
        });
    }

    private PdReceiver receiver = new PdReceiver() {

        private void pdPost(String msg) {
            toast("Pure Data says, \"" + msg + "\"");
        }

        @Override
        public void print(String s) {
            post(s);
        }

        @Override
        public void receiveBang(String source) {
            pdPost("bang");
        }

        @Override
        public void receiveFloat(String source, float x) {
            pdPost("float: " + x);
        }

        @Override
        public void receiveList(String source, Object... args) {
            pdPost("list: " + Arrays.toString(args));
        }

        @Override
        public void receiveMessage(String source, String symbol, Object... args) {
            pdPost("message: " + Arrays.toString(args));
        }

        @Override
        public void receiveSymbol(String source, String symbol) {
            pdPost("symbol: " + symbol);
        }
    };

    private void initPd() {
        Resources res = getResources();
        File patchFile = null;
        try {
            PdBase.setReceiver(receiver);
            PdBase.subscribe("android");
            InputStream in = res.openRawResource(R.raw.android_synth);
            patchFile = IoUtils.extractResource(in, "android_synth.pd", getCacheDir());
            PdBase.openPatch(patchFile);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            finish();
        } finally {
            if (patchFile != null) patchFile.delete();
        }
    }

    private final ServiceConnection pdConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            pdService = ((PdService.PdBinder)service).getService();
            initPd();
            if (recordAudioPermissionGranted()) {
                startAudio();
            } else {
                requestAudioPermission();
            }

            for(int i = 1; i<=12; i++){
                PdBase.sendBang("b"+i);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // this method will never be called
        }
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent m){
        boolean checkMotion =  true;
        double pressuref = m.getPressure();
        pressuref *= 127;
        int pressure = (int) Math.floor(pressuref);
        switch (m.getAction()){
            case MotionEvent.ACTION_HOVER_ENTER:
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_HOVER_EXIT:
            case MotionEvent.ACTION_UP:
                checkMotion = false;
                break;
        }
        if (checkMotion) return true;
        switch (v.getId()) {
            case R.id.button1:
                PdBase.sendBang("b1");
                break;
            case R.id.button2:
                PdBase.sendBang("b2");
                break;
            case R.id.button3:
                PdBase.sendBang("b3");
                break;
            case R.id.button4:
                PdBase.sendBang("b4");
               break;
            case R.id.button5:
                PdBase.sendBang("b5");
                break;
            case R.id.button6:
                PdBase.sendBang("b6");
                break;
            case R.id.button7:
                PdBase.sendBang("b7");
                break;
            case R.id.button8:
                PdBase.sendBang("b8");
                break;
            case R.id.button9:
                PdBase.sendBang("b9");
                break;
            case R.id.button10:
                PdBase.sendBang("b10");
                break;
            case R.id.button11:
                PdBase.sendBang("b11");
                break;
            case R.id.button12:
                PdBase.sendBang("b12");
                break;
            default:
                break;
        }
        return true;
    }


    private void startAudio() {
        String name = getResources().getString(R.string.app_name);
        try {
            pdService.initAudio(-1, -1, -1, -1);   // negative values will be replaced with defaults/preferences
            pdService.startAudio(new Intent(this, MainActivity.class), R.drawable.icon, name, "Return to " + name + ".");
        } catch (IOException e) {
            toast(e.toString());
        }
    }

    private void stopAudio() {
        pdService.stopAudio();
    }

    private boolean recordAudioPermissionGranted() {
        int permissionResult =
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return permissionResult == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initGui();
        bindService(new Intent(this, PdService.class), pdConnection, BIND_AUTO_CREATE);
    }
}
