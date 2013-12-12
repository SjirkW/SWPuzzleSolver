package com.example.NativeCamera;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/** The Camera preview class */
@SuppressLint("ViewConstructor" )
public class NativeCameraView extends SurfaceView implements SurfaceHolder.Callback
{
	private SurfaceHolder mHolder;
	private Camera mCamera;

	private final String TAG = "NativeCameraPreview";

	public NativeCameraView(Context context , Camera camera)
	{
		super( context);
		mCamera = camera;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder( );
		mHolder.addCallback( this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder)
	{
		//tell the camera where to draw the preview
		try
		{
			mCamera.setPreviewDisplay( holder);
			mCamera.startPreview( );
		}
		catch (IOException e)
		{
			Log.d( TAG , "Error setting camera preview: " + e.getMessage( ));
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
		// empty. releasing the camera is being taken cared of in the activity
	}

	public void surfaceChanged(SurfaceHolder holder , int format , int w , int h)
	{
		
		if (mHolder.getSurface( ) == null)
		{
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try
		{
			mCamera.stopPreview( );
		}
		catch (Exception e)
		{
			// ignore: tried to stop a non-existent preview
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here

		// start preview with new settings
		try
		{

			Parameters parameters = mCamera.getParameters( );
			getContext( );
			Display display = ((WindowManager) getContext( ).getSystemService(
					Context.WINDOW_SERVICE)).getDefaultDisplay( );

			if (display.getRotation( ) == Surface.ROTATION_0)
			{
				parameters.setPreviewSize( h , w);
				mCamera.setDisplayOrientation( 90);
			}

			if (display.getRotation( ) == Surface.ROTATION_90)
			{
				parameters.setPreviewSize( w , h);
			}

			if (display.getRotation( ) == Surface.ROTATION_180)
			{
				parameters.setPreviewSize( h , w);
			}

			if (display.getRotation( ) == Surface.ROTATION_270)
			{
				parameters.setPreviewSize( w , h);
				mCamera.setDisplayOrientation( 180);
			}

			mCamera.setParameters( parameters);

			mCamera.setPreviewDisplay( mHolder);
			mCamera.startPreview( );

		}
		catch (Exception e)
		{
			Log.d( TAG , "Error starting camera preview: " + e.getMessage( ));
		}
	}

	
	
}

