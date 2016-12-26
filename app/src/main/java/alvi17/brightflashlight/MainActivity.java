package alvi17.brightflashlight;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Build;

import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mBuilder;
    private CameraDevice mCameraDevice;
    private ImageButton flashLightButton;
    boolean isFlashLightOn = false;
    private Intent serviceIntent;
    FlashLightService flashLightService;
    CheckBox checkBox;

    private final ServiceConnection flashServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            FlashLightService.FleetBinder iFlashService = (FlashLightService.FleetBinder) iBinder;
            flashLightService = iFlashService.getFleetServiceReference();
            //flashLightService.setActivityRef(MainActivity.this);
            //bound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName){


        }
    };
    FrameLayout fm;
    AdView adView;

    //ad unit ca-app-pub-6508526601344465/6309563235
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        flashLightButton = (ImageButton)findViewById(R.id.flashlight_button);
        flashLightButton.setOnClickListener(new FlashOnOffListener());
        checkBox=(CheckBox)findViewById(R.id.checkBox);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.e("MainActivity","Checked: "+b);
                Util.saveInfo(getApplicationContext(),"background",b);
                if(b)
                {
                    serviceIntent.putExtra("background",b);
                    startService(serviceIntent);
//                    bindService(serviceIntent, flashServiceConnection, BIND_IMPORTANT);
                }
            }
        });

        fm=(FrameLayout)findViewById(R.id.mainFrame);
        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-6508526601344465/6309563235");
        adView.setAdSize(AdSize.BANNER);
        LinearLayout layout = (LinearLayout)findViewById(R.id.linearlayout);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        layout.addView(adView);
        fm.addView(layout);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(Util.getInfo(getApplicationContext(),"background"))
        {
            checkBox.setChecked(true);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        serviceIntent = new Intent(MainActivity.this, FlashLightService.class);
        startService(serviceIntent);
    }

    private class FlashOnOffListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            try {
            if(isFlashLightOn){
                flashLightButton.setImageResource(R.drawable.flashlight_off);
//                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
//                mSession.setRepeatingRequest(mBuilder.build(), null, null);
                serviceIntent = new Intent(MainActivity.this, FlashLightService.class);
                startService(serviceIntent);
                isFlashLightOn = false;
            }else{
                flashLightButton.setImageResource(R.drawable.flashlight_on);
//                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
//                mSession.setRepeatingRequest(mBuilder.build(), null, null);
                stopService(serviceIntent);
                isFlashLightOn = true;
            }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(!Util.getInfo(getApplicationContext(),"background"))
        {
            stopService(serviceIntent);
        }
        //close();
    }




}
