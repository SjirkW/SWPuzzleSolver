package com.example.OpenCVCamera;

import java.util.List;

import org.opencv.android.JavaCameraView;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;

public class CameraView extends JavaCameraView
{

	public boolean  mPictureTaken = false;

	public CameraView( Context context , AttributeSet attrs )
	{
		super( context , attrs);
	}

	public List<String> getEffectList( )
	{
		return mCamera.getParameters( ).getSupportedColorEffects( );
	}

	public boolean isEffectSupported( )
	{
		return ( mCamera.getParameters( ).getColorEffect( ) != null );
	}

	public String getEffect( )
	{
		return mCamera.getParameters( ).getColorEffect( );
	}

	public void setEffect( String effect )
	{
		Camera.Parameters params = mCamera.getParameters( );
		if ( params != null )
		{
			params.setColorEffect( effect);
			mCamera.setParameters( params);
		}
	}
	
	public List<String> getFocusModes(){
		return mCamera.getParameters( ).getSupportedFocusModes( );
	}
	
	public boolean isFocusModeSupported(){
		return (mCamera.getParameters( ).getFocusMode( ) != null);
	}
	
	public String getFocusMode(){
		return mCamera.getParameters( ).getFocusMode( );
	}
	
	public void setFocusMode( String focusMode)
	{
		Camera.Parameters params = mCamera.getParameters( );
		if (params != null){
			params.setFocusMode( Camera.Parameters.FOCUS_MODE_AUTO);
			mCamera.setParameters( params);
		}
	}
	
	public List<Size> getResolutionList( )
	{
		return mCamera.getParameters( ).getSupportedPreviewSizes( );
	}

	public void setResolution( Size resolution )
	{
		disconnectCamera( );
		mMaxHeight = resolution.height;
		mMaxWidth = resolution.width;
		connectCamera( getWidth( ) , getHeight( ));
	}

	public Size getResolution( )
	{
		return mCamera.getParameters( ).getPreviewSize( );
	}

	
	public void startCamera(){
		mCamera.startPreview( );
	}
	
	public void takePicture(Camera.PictureCallback callback){
		this.changeRes( );
		mCamera.takePicture( null , null , callback);
	}
	
	public void autoFocus(){
		mCamera.autoFocus( null);
	}
	
	public void focusType(){
		Camera.Parameters params = mCamera.getParameters( );
		if (params != null){
			params.setFocusMode( Camera.Parameters.FOCUS_MODE_MACRO);
			mCamera.setParameters( params);
		}
	}
	
	public void changeRes(){
		Camera.Parameters params = mCamera.getParameters( );
		params.setPictureSize( 480 , 800);
		mCamera.setParameters( params);
	}
	

	/**
	 * Turns the the camera flash on when it's off, turns it off when it's on
	 */
	public void EnableFlash( )
	{
		Parameters p = mCamera.getParameters( );

		if ( p.getFlashMode( ).equals( Parameters.FLASH_MODE_OFF) )
		{
			p.setFlashMode( Parameters.FLASH_MODE_TORCH);
		}
		else
		{
			p.setFlashMode( Parameters.FLASH_MODE_OFF);
		}

		mCamera.setParameters( p);
	}

}
