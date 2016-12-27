package alvi17.brightflashlight;

import android.Manifest;
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
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements  ActivityCompat.OnRequestPermissionsResultCallback{

    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mBuilder;
    private CameraDevice mCameraDevice;
    private ImageButton flashLightButton;
    boolean isFlashLightOn = false;
    private Intent serviceIntent;
    FlashLightService flashLightService;
    public  CheckBox checkBox;
    TextView trun_on_off;

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

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA

    };
    private static final int REQUEST_APP_PERMISSIONS = 0;
    private final RequestPermissionDialog.OnClickListenerHandlerPermission m_onClickListenerHandlerPerm = new RequestPermissionDialog.OnClickListenerHandlerPermission() {

        @Override
        public void onClickListenerOk() {
            ActivityCompat
                    .requestPermissions(MainActivity.this, PERMISSIONS,
                            REQUEST_APP_PERMISSIONS);
        }
    };
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
                Util.saveInfo(MainActivity.this,"background",b);
                if(b)
                {
//                    serviceIntent.putExtra("background",b);
//                    startService(serviceIntent);
 //                  bindService(serviceIntent, flashServiceConnection, BIND_IMPORTANT);
                }
            }
        });
        trun_on_off=(TextView)findViewById(R.id.turn_on_off);

        if(checkPermission(Manifest.permission.CAMERA)) {
            if (Util.getInfo(this, "background")) {
                checkBox.setChecked(true);
                serviceIntent = new Intent(MainActivity.this, FlashLightService.class);
                startService(serviceIntent);
                trun_on_off.setText(getResources().getString(R.string.turn_off));
                flashLightButton.setImageResource(R.drawable.flashlight_off);
                isFlashLightOn = false;
                // bindService(serviceIntent, flashServiceConnection, BIND_IMPORTANT);
            } else {
                serviceIntent = new Intent(MainActivity.this, FlashLightService.class);
                startService(serviceIntent);
                trun_on_off.setText(getResources().getString(R.string.turn_off));
                flashLightButton.setImageResource(R.drawable.flashlight_off);
                isFlashLightOn = false;
            }
        }else
        {
            requestlocationPermissions();
        }

        fm=(FrameLayout)findViewById(R.id.mainFrame);
        adView = new AdView(this);
        adView.setAdUnitId("ca-app-pub-6508526601344465/6309563235");
        adView.setAdSize(AdSize.BANNER);
        LinearLayout layout = (LinearLayout)findViewById(R.id.linearlayout);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        layout.addView(adView);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStart() {
        super.onStart();

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
                trun_on_off.setText(getResources().getString(R.string.turn_off));
            }else{
                flashLightButton.setImageResource(R.drawable.flashlight_on);
//                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
//                mSession.setRepeatingRequest(mBuilder.build(), null, null);
                stopService(serviceIntent);
                isFlashLightOn = true;
                trun_on_off.setText(getResources().getString(R.string.turn_on));
            }
            } catch (Exception e) {
                Log.e("MainActivity","onClick: "+e);
                e.printStackTrace();
            }

        }

    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (!Util.getInfo(this, "background")) {
                stopService(serviceIntent);
            }
            //unbindService(flashServiceConnection);
        }catch (Exception e)
        {

        }
        //close();
    }
    private boolean checkPermission(String perm)
    {
        return(PackageManager.PERMISSION_GRANTED == ActivityCompat.checkSelfPermission(this, perm));
    }

    private void requestlocationPermissions() {

        // BEGIN_INCLUDE(contacts_permission_request)
        Log.e(this.getClass().getSimpleName(), "Requesting location permission");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {

            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            Log.e(getClass().getSimpleName(),
                    "Displaying contacts permission rationale to provide additional context.");
            RequestPermissionDialog permissionDialog = new RequestPermissionDialog(
                    MainActivity.this, m_onClickListenerHandlerPerm);

            permissionDialog.setTitle(getResources().getString(R.string.grant));
            permissionDialog.setMessage(getResources().getString(R.string.details));
            WindowManager.LayoutParams wmlp = permissionDialog.getWindow()
                    .getAttributes();
            wmlp.gravity = Gravity.CENTER;
            permissionDialog.show();

        } else {
            // Contact permissions have not been granted yet. Request them directly.
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_APP_PERMISSIONS);
        }
        // END_INCLUDE(contacts_permission_request)
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_APP_PERMISSIONS) {
            boolean permissionGranted = true;
            int index = 0;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    permissionGranted = false;
                    Log.e(this.getClass().getSimpleName(), "Permission failed :" + permissions[index]);
                    break;
                }
                index++;
            }
            if (permissionGranted) {

             if (Util.getInfo(this, "background")) {
                    checkBox.setChecked(true);
                    serviceIntent = new Intent(MainActivity.this, FlashLightService.class);
                    startService(serviceIntent);
                    trun_on_off.setText(getResources().getString(R.string.turn_off));
                    flashLightButton.setImageResource(R.drawable.flashlight_off);
                    isFlashLightOn = false;
                    // bindService(serviceIntent, flashServiceConnection, BIND_IMPORTANT);
                } else {
                    serviceIntent = new Intent(MainActivity.this, FlashLightService.class);
                    startService(serviceIntent);
                    trun_on_off.setText(getResources().getString(R.string.turn_off));
                    flashLightButton.setImageResource(R.drawable.flashlight_off);
                    isFlashLightOn = false;
                }

            }
            else {

                finish();
            }
        }
        else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }



}
