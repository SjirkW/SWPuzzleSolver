package com.example.OpenCVCamera;

import java.io.FileOutputStream;
import java.util.List;
import java.util.ListIterator;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import com.example.Sudoku.SudokuSolveActivity;
import com.example.Utils.HelperMethods;
import com.example.Utils.VisionAlgorithms;
import com.example.NativeCamera.R;


//import com.example.ocrtest.R;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

public class CameraActivity extends Activity implements CvCameraViewListener2
{

	protected static final String TAG = "HelloOpenCvActivity";

	private CameraView mCameraView;
	private List<Size> mResolutionList;
	private SubMenu mResolutionMenu;
	private MenuItem[ ] mResolutionMenuItems;

	private MenuItem[ ] mEffectMenuItems;
	private SubMenu mColorEffectsMenu;

	private MenuItem[ ] mFocusMenuItems;
	private SubMenu mFocusModesMenu;

	private Boolean mRealTime = false;

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback( this)
	{
		@Override
		public void onManagerConnected( int status )
		{
			switch ( status )
			{
				case LoaderCallbackInterface.SUCCESS :
				{
					Log.i( TAG , "OpenCV loaded successfully");
					Toast.makeText( getApplication( ) , "ok" , Toast.LENGTH_SHORT).show( );
					mCameraView.enableView( );
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

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		Log.i( TAG , "called onCreate");
		super.onCreate( savedInstanceState);
		getWindow( ).addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// Camera view
		setContentView( R.layout.camera);
		mCameraView = ( CameraView ) findViewById( R.id.HelloOpenCvView);
		mCameraView.setVisibility( SurfaceView.VISIBLE);
		mCameraView.setCvCameraViewListener( this);

		// control buttons
		LayoutInflater mControlInflater = null;
		mControlInflater = LayoutInflater.from( getBaseContext( ));
		View viewControl = mControlInflater.inflate( R.layout.control , null);

		LayoutParams layoutParamsControl = new LayoutParams( LayoutParams.MATCH_PARENT ,
				LayoutParams.MATCH_PARENT);
		this.addContentView( viewControl , layoutParamsControl);

	}

	@Override
	public void onPause( )
	{
		super.onPause( );
		if ( mCameraView != null )
			mCameraView.disableView( );
	}

	@Override
	public void onResume( )
	{
		super.onResume( );
		OpenCVLoader.initAsync( OpenCVLoader.OPENCV_VERSION_2_4_3 , this , mLoaderCallback);
	}

	public void onDestroy( )
	{
		super.onDestroy( );
		if ( mCameraView != null )
			mCameraView.disableView( );
	}

	public void onCaptureButtonClick( View view )
	{
		Log.i( TAG , "onTouch event");

		String fileName = HelperMethods.createFileName( );
		takePicture( fileName);

	}

	/**
	 * Take a picture of the current frame and save it on external storage
	 * 
	 * @param fileName
	 */
	private void takePicture( final String fileName )
	{
		Log.i( TAG , "Tacking picture");
		PictureCallback callback = new PictureCallback( )
		{

			private String mPictureFileName = fileName;

			@Override
			public synchronized void onPictureTaken( byte[ ] data , Camera camera )
			{
				Log.i( TAG , "Saving a bitmap to file");

				// Create options to help use less memory
				BitmapFactory.Options options = new BitmapFactory.Options( );
				options.inSampleSize = 4;
				options.inPurgeable = true;
				options.inInputShareable = true;
				options.inPreferredConfig = Bitmap.Config.ARGB_8888;

				Bitmap picture = BitmapFactory.decodeByteArray( data , 0 , data.length , options);

				Matrix matrix = new Matrix( );
				// rotate the bitmap
				matrix.postRotate( 90);

				// recreate the bitmap
				Bitmap resizedBitmap = Bitmap.createBitmap( picture , 0 , 0 , picture.getWidth( ) ,
						picture.getHeight( ) , matrix , false);

				try
				{
					FileOutputStream out = new FileOutputStream( mPictureFileName);

					// save the bitmap to a file
					resizedBitmap.compress( Bitmap.CompressFormat.JPEG , 90 , out);
					// clean up the bitmap data
					resizedBitmap.recycle( );

					mCameraView.startCamera( );

					Toast.makeText( getApplication( ) , "saved" , Toast.LENGTH_SHORT).show( );

					// start the picture preview activity
					Intent intent = new Intent( getBaseContext( ) , SudokuSolveActivity.class);
					intent.putExtra( "filename" , fileName);

					startActivity( intent);
					overridePendingTransition(R.anim.push_down_in,R.anim.push_down_out);
				}
				catch ( Exception e )
				{
					e.printStackTrace( );
				}
			}
		};

		mCameraView.takePicture( callback);
	}

	public void onFlashButtonClick( View view )
	{
		mCameraView.EnableFlash( );
	}

	public void onCameraViewStarted( int width , int height )
	{
	}

	public void onCameraViewStopped( )
	{
	}

	public Mat onCameraFrame( CvCameraViewFrame inputFrame )
	{

		if ( mRealTime )
		{
			VisionAlgorithms.thresholdImage( inputFrame.gray( ));
		}
		return inputFrame.rgba( );

	}

	

	@Override
	public boolean onCreateOptionsMenu( Menu menu )
	{
		// effects
		List<String> effects = mCameraView.getEffectList( );

		if ( effects == null )
		{
			Log.e( TAG , "Color effects are not supported by device!");
			return true;
		}

		mColorEffectsMenu = menu.addSubMenu( "Color Effect");
		mEffectMenuItems = new MenuItem[ effects.size( ) ];

		int idx = 0;
		ListIterator<String> effectItr = effects.listIterator( );
		while ( effectItr.hasNext( ) )
		{
			String element = effectItr.next( );
			mEffectMenuItems[ idx ] = mColorEffectsMenu.add( 1 , idx , Menu.NONE , element);
			idx++;
		}

		// resolution
		mResolutionMenu = menu.addSubMenu( "Resolution");
		mResolutionList = mCameraView.getResolutionList( );
		mResolutionMenuItems = new MenuItem[ mResolutionList.size( ) ];

		ListIterator<Size> resolutionItr = mResolutionList.listIterator( );
		idx = 0;
		while ( resolutionItr.hasNext( ) )
		{
			Size element = resolutionItr.next( );
			mResolutionMenuItems[ idx ] = mResolutionMenu.add( 2 , idx , Menu.NONE , Integer
					.valueOf( element.width).toString( )
					+ "x"
					+ Integer.valueOf( element.height).toString( ));
			idx++;
		}

		// focus modes
		List<String> focusModes = mCameraView.getFocusModes( );

		if ( focusModes == null )
		{
			Log.e( TAG , "Focus modes are not supported by device!");
			return true;
		}

		mFocusModesMenu = menu.addSubMenu( "Focus mode");
		mFocusMenuItems = new MenuItem[ focusModes.size( ) ];

		idx = 0;
		ListIterator<String> focusModeItr = focusModes.listIterator( );
		while ( focusModeItr.hasNext( ) )
		{
			String element = focusModeItr.next( );
			mFocusMenuItems[ idx ] = mFocusModesMenu.add( 1 , idx , Menu.NONE , element);
			idx++;
		}

		return true;
	}

	public boolean onOptionsItemSelected( MenuItem item )
	{
		Log.i( TAG , "called onOptionsItemSelected; selected item: " + item);
		//set color effect
		if ( item.getGroupId( ) == 1 )
		{
			mCameraView.setEffect( ( String ) item.getTitle( ));
			Toast.makeText( this , mCameraView.getEffect( ) , Toast.LENGTH_SHORT).show( );
		}
		//set resolution
		else if ( item.getGroupId( ) == 2 )
		{
			int id = item.getItemId( );
			Size resolution = mResolutionList.get( id);
			mCameraView.setResolution( resolution);
			resolution = mCameraView.getResolution( );
			String caption = Integer.valueOf( resolution.width).toString( ) + "x"
					+ Integer.valueOf( resolution.height).toString( );
			Toast.makeText( this , caption , Toast.LENGTH_SHORT).show( );
		}
		//set focus
		else if ( item.getGroupId( ) == 3 )
		{
			mCameraView.setFocusMode( ( String ) item.getTitle( ));
			Toast.makeText( this , mCameraView.getFocusMode( ) , Toast.LENGTH_SHORT).show( );
		}

		return true;
	}
	
	//show real time thresholding
	public void onRealTimeClick( View view )
	{
		mRealTime = !mRealTime;

	}
	
}
