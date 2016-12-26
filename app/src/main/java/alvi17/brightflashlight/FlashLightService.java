package alvi17.brightflashlight;

import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 12/25/2016.
 */

public class FlashLightService extends Service {

    private final IBinder bindedService = new FleetBinder();
    boolean on_off;
    private CameraCaptureSession mSession;
    private CaptureRequest.Builder mBuilder;
    private CameraDevice mCameraDevice;

    public class FleetBinder extends Binder
    {
        public FlashLightService getFleetServiceReference() {
            return FlashLightService.this;
        }
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("FlashService", "onBind" + "called");
        return bindedService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.e("FlashService","OnStartCommand");

        if(Build.VERSION.SDK_INT>=21) {
            try {
                init();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else
        {
            try {
                initOld();
            }catch (Exception e)
            {
                e.printStackTrace();
            }
        }


        return Service.START_STICKY;
    }



    private Camera camera;
    private Camera.Parameters parameters;

    private void initOld()
    {
        if (isFlashSupported()) {
            camera = Camera.open();
            parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            camera.startPreview();
        } else {
            showNoFlashAlert();
        }
    }
    private void showNoFlashAlert() {
        new AlertDialog.Builder(this)
                .setMessage("Your device hardware does not support flashlight!")
                .setIcon(android.R.drawable.ic_dialog_alert).setTitle("Error")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        stopSelf();
                    }
                }).show();
    }

    private boolean isFlashSupported() {
        PackageManager pm = getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }


    private CameraManager mCameraManager;

    @SuppressWarnings("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void init() throws CameraAccessException {
        mCameraManager = (CameraManager) getApplicationContext().getSystemService(Context.CAMERA_SERVICE);
        //here to judge if flash is available
        CameraCharacteristics cameraCharacteristics = mCameraManager.getCameraCharacteristics("0");
        boolean flashAvailable = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
        if (flashAvailable) {
            mCameraManager.openCamera("0", new MyCameraDeviceStateCallback(), null);
        } else {
            Toast.makeText(getApplicationContext(), "Flash not available", Toast.LENGTH_SHORT).show();
            //todo: throw Exception
        }
        mCameraManager.openCamera("0", new MyCameraDeviceStateCallback(), null);
    }

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;

    /**
     * camera device callback
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class MyCameraDeviceStateCallback extends CameraDevice.StateCallback {

        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            //get builder
            try {
                mBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                //flash on, default is on
                mBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                mBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                List<Surface> list = new ArrayList<Surface>();
                mSurfaceTexture = new SurfaceTexture(1);
                Size size = getSmallestSize(mCameraDevice.getId());
                mSurfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
                mSurface = new Surface(mSurfaceTexture);
                list.add(mSurface);
                mBuilder.addTarget(mSurface);
                camera.createCaptureSession(list, new MyCameraCaptureSessionStateCallback(), null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
            Log.e("MainActivity","Onopened");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

            Log.e("MainActivity","OnDisconnected");

        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.e("MainActivity","OnError");
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private Size getSmallestSize(String cameraId) throws CameraAccessException {
        Size[] outputSizes = mCameraManager.getCameraCharacteristics(cameraId)
                .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                .getOutputSizes(SurfaceTexture.class);
        if (outputSizes == null || outputSizes.length == 0) {
            throw new IllegalStateException(
                    "Camera " + cameraId + "doesn't support any outputSize.");
        }
        Size chosen = outputSizes[0];
        for (Size s : outputSizes) {
            if (chosen.getWidth() >= s.getWidth() && chosen.getHeight() >= s.getHeight()) {
                chosen = s;
            }
        }
        return chosen;
    }

    /**
     * session callback
     */  @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    class MyCameraCaptureSessionStateCallback extends CameraCaptureSession.StateCallback {

        @Override
        public void onConfigured(CameraCaptureSession session) {
            mSession = session;
            try {
                mSession.setRepeatingRequest(mBuilder.build(), null, null);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            Log.e("MainActivity","Configured");
        }

        @Override
        public void onConfigureFailed(CameraCaptureSession session) {
            Log.e("MainActivity","fialedConfigured");
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void close() {
        if (mCameraDevice == null || mSession == null) {
            return;
        }
        mSession.close();
        mCameraDevice.close();
        mCameraDevice = null;
        mSession = null;
    }

    private void closeOld()
    {
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("flashservice","ondestroy");
        if(Build.VERSION.SDK_INT>=21) {
            close();
        }else{
            closeOld();
        }
    }
}
