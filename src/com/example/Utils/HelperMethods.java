package com.example.Utils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

/**
 * A class that contains general helping methods for the project
 * @author Sjirk
 *
 */
public class HelperMethods
{
	private final static String TAG = "HelperMethods";

	/**
	 * create a new filename based on the current time
	 * @return
	 */
	@SuppressLint("SimpleDateFormat" )
	public static String createFileName( )
	{
		File pictureFileDir = getAppDir( );

		// display and log an error when a directory cant be found or created
		if ( !pictureFileDir.exists( ) && !pictureFileDir.mkdirs( ) )
		{
			Log.d( TAG , "Can't create directory to save image.");
			return "";
		}

		// create a name for the picture based on the current date
		SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMdd_HHmmss" , Locale.US);
		String date = dateFormat.format( new Date( ));
		String photoFile = "Picture_" + date + ".jpg";

		String fileName = pictureFileDir.getPath( ) + File.separator + photoFile;

		return fileName;
	}

	/**
	 * get the directory to save photos in
	 * @return
	 */
	private static File getAppDir( )
	{
		File sdDir = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES);
		return new File( sdDir , "SjirkSudoku");
	}
	
	/**
	 * get a bitmap by giving a file location
	 * @param fileName
	 * @return
	 */
	public static Bitmap getImgFromLocation(String fileName){
		File imgFile = new File( fileName);
		
		Bitmap myBitmap = null;
		
		//shows an image on the screen
		if ( imgFile.exists( ) )
		{

			myBitmap = BitmapFactory.decodeFile( imgFile.getAbsolutePath( ));
			Log.i( TAG , "file saved");

		}
		else 
			Log.i( TAG , "file not found");
		
		return myBitmap;
	}
}
