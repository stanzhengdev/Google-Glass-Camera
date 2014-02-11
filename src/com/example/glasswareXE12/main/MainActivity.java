package com.example.glasswareXE12.main;

import java.io.File;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.CameraManager;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.view.KeyEvent;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.os.Bundle;
import android.os.FileObserver;
import android.provider.MediaStore;

public class MainActivity extends Activity {
	
//declare privates
	
	
	protected void onCreate(Bundle savedInstanceState){	
		super.onCreate(savedInstanceState);
		//keeps the camera on 
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    
		
		Card card1 = new Card(this);
		card1.setText("Nice Picture!");
		card1.setFootnote("Cool! ...");
		View card1View = card1.toView();
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

	@Override
	protected void onResume() {
	    super.onResume();
	    // Re-acquire the camera and start the preview.
	}
	private static final int TAKE_PICTURE_REQUEST = 1;

	private void takePicture() {
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
	/*
	 * end of boiler plate 
	 */
}
