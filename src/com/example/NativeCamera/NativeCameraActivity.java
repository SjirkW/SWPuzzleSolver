package com.example.NativeCamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import com.example.Sudoku.SudokuSolveActivity;
import com.example.Utils.HelperMethods;
//import com.example.NativeCamera.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class NativeCameraActivity extends Activity 
{
	protected static final String TAG = "NativeCameraActivity";
	private Camera mCamera;
	private NativeCameraView mPreview;

	public static final int MEDIA_TYPE_IMAGE = 1;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate( savedInstanceState);
		setContentView( R.layout.native_camera);
	
		loadControlButtons();
		loadCamera();
	}
	
	//loads the control buttons and displays it on the screen
	private void loadControlButtons(){
		LayoutInflater mControlInflater = null;
		mControlInflater = LayoutInflater.from( getBaseContext( ));
		View viewControl = mControlInflater.inflate( R.layout.control_native , null);
		LayoutParams layoutParamsControl = new LayoutParams( LayoutParams.MATCH_PARENT ,
				LayoutParams.MATCH_PARENT);
		this.addContentView( viewControl , layoutParamsControl);
	}
	
	//creates a camera instance and shows the preview on the screen
	private void loadCamera(){
		
		// Create an instance of Camera
		mCamera = getCameraInstance( );
		// set Camera parameters
		Camera.Parameters params = mCamera.getParameters( );
		//find which focus modes the camera supports
		List<String> focusModes = params.getSupportedFocusModes();
		if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
			params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
		else if (focusModes.contains(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO))
			params.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		else if (focusModes.contains(Parameters.FOCUS_MODE_AUTO))
			params.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		
		// Create our Preview view and set it as the content of our activity.
		mPreview = new NativeCameraView( this , mCamera);
		FrameLayout preview = (FrameLayout) findViewById( R.id.camera_preview);
		preview.addView( mPreview);
	}
	
	//required for openCV to function
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback( this)
	{
		@Override
		public void onManagerConnected(int status)
		{
			switch (status)
			{
				case LoaderCallbackInterface.SUCCESS :
				{
					Log.i( TAG , "OpenCV loaded successfully");
				}
					break;
				default :
				{
					super.onManagerConnected( status);
				}
					break;
			}
		}
	};

	 //Turns the the camera flash on when it's off, turns it off when it's on
	private void EnableFlash()
	{
		mCamera.cancelAutoFocus( );
		Parameters p = mCamera.getParameters( );

		if (p.getFlashMode( ).equals( Parameters.FLASH_MODE_OFF))
		{
			p.setFlashMode( Parameters.FLASH_MODE_TORCH);
		}
		else
		{
			p.setFlashMode( Parameters.FLASH_MODE_OFF);
		}

		mCamera.setParameters( p);

		//let the camera adjust to the new light levels before autofocusing
		try
		{
			Thread.sleep( 1000);
			mCamera.autoFocus( null);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	// A safe way to get an instance of the Camera object. 
	private static Camera getCameraInstance()
	{
		Camera c = null;
		try
		{
			// attempt to get a Camera instance
			c = Camera.open( ); 
		}
		catch (Exception e)
		{
			// Camera is not available (in use or does not exist)
			Log.d( TAG , "no camera found");
		}
		// returns null if camera is unavailable
		return c; 
	}

	//method called when taking a picture. takes care of saving a picture
	private PictureCallback mPicture = new PictureCallback( )
	{

		@Override
		public void onPictureTaken(byte[] data , Camera camera)
		{

			File pictureFile = getOutputMediaFile( MEDIA_TYPE_IMAGE);
			if (pictureFile == null)
			{
				Log.d( TAG , "Error creating media file, check storage permissions: ");
				return;
			}

			String pictureFileName = HelperMethods.createFileName( );

			// Create options to help use less memory
			BitmapFactory.Options options = new BitmapFactory.Options( );
			//reduce the size of the picture by 4
			options.inSampleSize = 2;
			options.inPurgeable = true;
			options.inInputShareable = true;
			options.inPreferredConfig = Bitmap.Config.ARGB_8888;

			Bitmap picture = BitmapFactory.decodeByteArray( data , 0 , data.length , options);

			Matrix rotationMatrix = new Matrix( );
			// rotate the bitmap
			rotationMatrix.postRotate( 90);

			// recreate the bitmap
			Bitmap resizedBitmap = Bitmap.createBitmap( picture , 0 , 0 , picture.getWidth( ) ,
					picture.getHeight( ) , rotationMatrix , false);

			try
			{
				FileOutputStream out = new FileOutputStream( pictureFileName);

				// save the bitmap to a file
				resizedBitmap.compress( Bitmap.CompressFormat.JPEG , 100 , out);
				// clean up the bitmap data
				resizedBitmap.recycle( );

				// start the picture preview activity
				Intent intent = new Intent( getBaseContext( ) , SudokuSolveActivity.class);
				intent.putExtra( "filename" , pictureFileName);

				startActivity( intent);
				overridePendingTransition( R.anim.push_down_in , R.anim.push_down_out);
			}
			catch (FileNotFoundException e)
			{
				Log.d( TAG , "File not found: " + e.getMessage( ));
			}
		}
	};

	/** Create a File for saving an image or video */
	@SuppressLint("SimpleDateFormat" )
	private static File getOutputMediaFile(int type)
	{
		// Check if the SD card is mounted
		File mediaStorageDir = new File(
				Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES) ,
				"MyCameraApp");
		
		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists( ))
		{
			if (!mediaStorageDir.mkdirs( ))
			{
				Log.d( "MyCameraApp" , "failed to create directory");
				return null;
			}
		}

		// Create a media file name based on the current time
		String timeStamp = new SimpleDateFormat( "yyyyMMdd_HHmmss").format( new Date( ));
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE)
		{
			mediaFile = new File( mediaStorageDir.getPath( ) + File.separator + "IMG_" + timeStamp
					+ ".jpg");
		}
		else
		{
			return null;
		}

		return mediaFile;
	}

	 // release the camera for other applications
	private void releaseCamera()
	{
		if (mCamera != null)
		{
			mPreview.getHolder( ).removeCallback( mPreview);
			mCamera.stopPreview( );
			mCamera.release( );
			mCamera = null;
		}
	}
	
	public void onCaptureButtonClick(View view)
	{
		AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback( )
		{
			@Override
			public void onAutoFocus(boolean arg0 , Camera arg1)
			{
				mCamera.takePicture( null , null , mPicture);
			}
		};
		mCamera.autoFocus( myAutoFocusCallback);
	}

	public void onFlashButtonClick(View view)
	{
		EnableFlash( );
	}
	
	//called when the user taps on the screen
	public void onCameraPreviewClick(View view){
		mCamera.autoFocus( null);
	}
	

	@Override
	protected void onPause()
	{
		super.onPause( );
		releaseCamera( );

	}

	@Override
	public void onResume()
	{
		super.onResume( );
		releaseCamera( );
		loadCamera( );
		//initialize openCV
		OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_2_4_4 , this , mLoaderCallback);
	}

	public void onDestroy()
	{
		super.onDestroy( );
	}
	




}
