package com.example.glasswareXE12.main;

import java.io.File;
import java.io.IOException;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.CameraManager;
import com.google.android.glass.touchpad.*;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.gesture.Gesture;
import android.hardware.*;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Menu;
import android.net.Uri;
import android.view.WindowManager;
import android.os.Bundle;
import android.util.*;
import android.hardware.Camera.PictureCallback;
import android.content.Intent;
import android.os.FileObserver;
import android.provider.MediaStore;


public class MainActivity extends Activity {
	
// create variable instances
	
	private static final String TAG = MainActivity.class.getSimpleName();

	private GestureDetector mGestureDetector;

	private Camera mCamera;
	private CameraPreview mPreview;
    PictureCallback mPicture = null;

	
	protected void onCreate(Bundle savedInstanceState){	
		super.onCreate(savedInstanceState);
		//keeps the camera on 
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
		
		Card card1 = new Card(this);
		card1.setText("Nice Picture!");
		card1.setFootnote("Cool! ...");
		View card1View = card1.getView();
	    //Calling the intent
		setContentView(card1View);
		takePicture();
//	    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
//	    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, false);       
//	    startActivityForResult(cameraIntent, 1);
		setContentView(card1View);
		takePicture();
		
	}
/**
 * Boiler plate google code 
 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    if (keyCode == KeyEvent.KEYCODE_CAMERA) {
	        // Stop the preview and release the camera.
	        // Execute your logic as quickly as possible
	        // so the capture happens quickly.
	        return false;
	    } else {
	        return super.onKeyDown(keyCode, event);
	    }
	}

	
	
	/*** boiler plate google code 
	 * https://developers.google.com/glass/develop/gdk/media-camera/camera
	 */
	@Override
	protected void onResume() {
	    super.onResume();
	    // Re-acquire the camera and start the preview.
	}
	private static final int TAKE_PICTURE_REQUEST = 1;
	
	public void takePicture() {
	    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    startActivityForResult(intent, TAKE_PICTURE_REQUEST);
	    
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == TAKE_PICTURE_REQUEST && resultCode == RESULT_OK) {
	        String picturePath = data.getStringExtra(
	                CameraManager.EXTRA_PICTURE_FILE_PATH);
	        processPictureWhenReady(picturePath);
	    }

	    super.onActivityResult(requestCode, resultCode, data);
	}

	private void processPictureWhenReady(final String picturePath) {
	    final File pictureFile = new File(picturePath);

	    if (pictureFile.exists()) {
	        // The picture is ready; process it.
	    } else {
	        // The file does not exist yet. Before starting the file observer, you
	        // can update your UI to let the user know that the application is
	        // waiting for the picture (for example, by displaying the thumbnail
	        // image and a progress indicator).

	        final File parentDirectory = pictureFile.getParentFile();
	        FileObserver observer = new FileObserver(parentDirectory.getPath()) {
	            // Protect against additional pending events after CLOSE_WRITE is
	            // handled.
	            private boolean isFileWritten;

	            @Override
	            public void onEvent(int event, String path) {
	                if (!isFileWritten) {
	                    // For safety, make sure that the file that was created in
	                    // the directory is actually the one that we're expecting.
	                    File affectedFile = new File(parentDirectory, path);
	                    isFileWritten = (event == FileObserver.CLOSE_WRITE
	                            && affectedFile.equals(pictureFile));

	                    if (isFileWritten) {
	                        stopWatching();

	                        // Now that the file is ready, recursively call
	                        // processPictureWhenReady again (on the UI thread).
	                        runOnUiThread(new Runnable() {
	                            @Override
	                            public void run() {
	                                processPictureWhenReady(picturePath);
	                            }
	                        });
	                    }
	                }
	            }
	        };
	        observer.startWatching();
	    }
	}
	/**
	 * end of boiler plate 
	 */
	private class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
	    private SurfaceHolder mHolder;
	    private Camera mCamera;

	    public CameraPreview(Context context, Camera camera) {
	        super(context);
	        mCamera = camera;

	        Log.v(TAG,"In CameraPreview");

	        // Install a SurfaceHolder.Callback so we get notified when the
	        // underlying surface is created and destroyed.
	        mHolder = getHolder();

	        Log.v(TAG,"Got holder");

	        mHolder.addCallback(this);

	        Log.v(TAG,"Added callback");

	        // deprecated setting, but required on Android versions prior to 3.0
	        //mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	    }

	    public void surfaceCreated(SurfaceHolder holder) {
	        // The Surface has been created, now tell the camera where to draw the preview.
	        try {
	            Log.v(TAG,"in surface created");
	            mCamera.setPreviewDisplay(holder);
	            Log.v(TAG,"set preview display");
	            mCamera.startPreview();
	            Log.v(TAG,"preview started");
	        } catch (IOException e) {
	            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
	        }
	    }

	    public void surfaceDestroyed(SurfaceHolder holder) {
	        // empty. Take care of releasing the Camera preview in your activity.
	        mCamera.stopPreview();
	        mCamera.release();
	        mCamera = null;
	    }

	    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
	        // If your preview can change or rotate, take care of those events here.
	        // Make sure to stop the preview before resizing or reformatting it.

	        Log.v(TAG,"in surface changted");

	        if (mHolder.getSurface() == null){
	          // preview surface does not exist
	            Log.v(TAG,"surface don't exist");
	          return;
	        }

	        // stop preview before making changes
	        try {
	            mCamera.stopPreview();
	            Log.v(TAG,"stopped preview");
	        } catch (Exception e){
	          // ignore: tried to stop a non-existent preview
	            Log.v(TAG,"preview e");
	        }

	        // start preview with new settings
	        try {
	            Log.v(TAG,"startpreview");
	            mCamera.setPreviewDisplay(mHolder);
	            mCamera.startPreview();

	        } catch (Exception e){
	            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
	        }
	    }
	}

	public void takePic() {
	    // get an image from the camera
		mCamera.takePicture(null, null, mPicture);
	}
	/*
	 * end of boiler plate 
	 */
}
