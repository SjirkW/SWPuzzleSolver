package com.example.Sudoku;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.opencv.android.*;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import com.example.Utils.StopWatch;
import com.example.Utils.VisionAlgorithms;
import com.example.NativeCamera.R;

/**
 * activity to preview the sudoku
 * 
 * @author Sjirk
 * 
 */
public class SudokuSolveActivity extends Activity implements OnClickListener
{
	private static final String TAG = "PreviewActivity";

	private String mFileName = "empty";

	Mat mSudoku = new Mat( );
	Mat mImgToProcess = new Mat( );
	Mat mOriginal = new Mat( );
	Mat mBackgroundSudoku = new Mat( );

	private int mStepToShow = 0;
	StopWatch s = new StopWatch( );
	StopWatch t = new StopWatch( );

	private SudokuView puzzleView;

	private RelativeLayout buttonPanel;
	private LinearLayout buttonBar;
	private Button buttonSolve;
	private Button button1;
	private Button button2;
	private Button button3;
	private Button button4;
	private Button button5;
	private Button button6;
	private Button button7;
	private Button button8;
	private Button button9;
	private Button buttonBack;

	private CheckBox chbxShowOriginal;

	LinearLayout mLinearLayoutMain;

	private String mSudokuPuzzle;

	private boolean mPuzzleSolved = false;

	private boolean mContrastStretched = false;
	private Bitmap bmpOriginalSudoku = null;

	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	// time when the solve button was last clicked
	long mLastClickTime = 0;
	long mFirstClickTime = 0;

	private static final int PUZZLE_SIZE = 9;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i( TAG , "called onCreate");
		super.onCreate( savedInstanceState);

		setContentView( R.layout.sudoku_view);

		// create touch listeners
		initTouchListeners( );

		// Create the interface
		initInterface( );

		// get the filename of the last photo from the intent
		Intent intent = getIntent( );
		mFileName = intent.getExtras( ).getString( "filename");

		Mat mImgToProcess = Highgui.imread( mFileName , 1);
		Imgproc.cvtColor( mImgToProcess , mImgToProcess , Imgproc.COLOR_RGB2GRAY);

		t.start( );
		// process the image
		ProcessImage( mImgToProcess);
		t.stop( );
		System.out.println( "Time taken: " + t.getElapsedTime( ));
	}

	private void initTouchListeners()
	{
		// gesture detector
		class MyGestureDetector extends SimpleOnGestureListener
		{
			@Override
			public boolean onDown(MotionEvent e)
			{
				return false;
			}

			@Override
			public boolean onDoubleTap(MotionEvent e)
			{
				vibrate( );
				deleteNumber( );
				return true;
			}

			@Override
			public void onLongPress(MotionEvent e)
			{
				vibrate( );
				deleteNumber( );
			}
		}

		gestureDetector = new GestureDetector( this , new MyGestureDetector( ));
		gestureListener = new View.OnTouchListener( )
		{
			public boolean onTouch(View v , MotionEvent event)
			{
				setBarheight( v , event);
				return gestureDetector.onTouchEvent( event);
			}
		};
	}

	// the bar height of the button bar
	private void setBarheight(View v , MotionEvent event)
	{
		if (!sudokuIsSolved( ))
		{
			buttonBar.setVisibility( View.VISIBLE);
			puzzleView.enableRect( );
		}

		puzzleView.select( (int) (event.getX( ) / puzzleView.getFrameWidth( )) ,
				(int) (event.getY( ) / puzzleView.getFrameHeight( )));

		int cellY = puzzleView.getCellY( );

		// place the buttonbar below the selected square when at the top two
		// rows
		if (cellY < 2)
		{
			buttonBar.setY( (puzzleView.getCellY( ) * puzzleView.getFrameHeight( ))
					+ puzzleView.getFrameHeight( ) * 2);
		}
		else
		{
			buttonBar.setY( (puzzleView.getCellY( ) * puzzleView.getFrameHeight( ))
					- puzzleView.getFrameHeight( ) * 1.5f);
		}
	}

	private void initInterface()
	{
		// get reference to the custom view
		puzzleView = (SudokuView) findViewById( R.id.puzzleView);
		// set touch listeners
		puzzleView.setOnClickListener( SudokuSolveActivity.this);
		puzzleView.setOnTouchListener( gestureListener);

		// solve button
		buttonSolve = (Button) findViewById( R.id.buttonSolve);
		buttonPanel = (RelativeLayout) findViewById( R.id.buttonPanel);

		// back button
		buttonBack = (Button) findViewById( R.id.buttonBack);
		// the bar with 9 buttons
		buttonBar = (LinearLayout) findViewById( R.id.buttonRow);
		buttonBar.setVisibility( View.INVISIBLE);
		buttonBar.setOnClickListener( SudokuSolveActivity.this);
		buttonBar.setOnTouchListener( gestureListener);
		// number buttons
		final int buttonAlpha = 160;
		button1 = (Button) findViewById( R.id.one);
		button1.getBackground( ).setAlpha( buttonAlpha);
		button2 = (Button) findViewById( R.id.two);
		button2.getBackground( ).setAlpha( buttonAlpha);
		button3 = (Button) findViewById( R.id.three);
		button3.getBackground( ).setAlpha( buttonAlpha);
		button4 = (Button) findViewById( R.id.four);
		button4.getBackground( ).setAlpha( buttonAlpha);
		button5 = (Button) findViewById( R.id.five);
		button5.getBackground( ).setAlpha( buttonAlpha);
		button6 = (Button) findViewById( R.id.six);
		button6.getBackground( ).setAlpha( buttonAlpha);
		button7 = (Button) findViewById( R.id.seven);
		button7.getBackground( ).setAlpha( buttonAlpha);
		button8 = (Button) findViewById( R.id.eight);
		button8.getBackground( ).setAlpha( buttonAlpha);
		button9 = (Button) findViewById( R.id.nine);
		button9.getBackground( ).setAlpha( buttonAlpha);

		chbxShowOriginal = (CheckBox) findViewById( R.id.checkBox1);
	}

	private void ProcessImage(Mat input)
	{
		s.start( );

		VisionAlgorithms.preProcess( input);
		getTime( "preProcess");

		Mat sudoku = new Mat( );
		input.copyTo( sudoku);

		VisionAlgorithms.thresholdImage( input);
		getTime( "thresholdImage");

		VisionAlgorithms.findPuzzleContours( input);
		getTime( "findPuzzleContours");

		VisionAlgorithms.houghTransform( input , sudoku);
		getTime( "houghTransform");

		VisionAlgorithms.increaseContrast( sudoku);

		sudoku.copyTo( mBackgroundSudoku);
		mSudokuPuzzle = VisionAlgorithms.getDigits( sudoku , PUZZLE_SIZE);
		getTime( "findContourInCell and get didigts");

		// showImage( sudoku);

		drawOriginalSudoku( );
	}

	/**
	 * Attempt to improve the puzzle detection by warping the perspective of
	 * each cell individually
	 * 
	 * @param input
	 */
	@SuppressWarnings("unused" )
	private void processImageImproved(Mat input)
	{
		s.start( );

		VisionAlgorithms.preProcess( input);
		getTime( "preProcess");

		VisionAlgorithms.increaseContrast( input);

		VisionAlgorithms.CreateMask( input);

		Mat closex = new Mat( );
		VisionAlgorithms.findVerticalLines( input , closex);

		Mat closey = new Mat( );
		VisionAlgorithms.findHorizontalLines( input , closey);

		Mat dst = new Mat( );
		Core.bitwise_and( closex , closey , dst);

		// create a list of points to store all the contours in
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>( 2000);

		// find all the contours in the image and store them in a list
		Imgproc.findContours( dst , contours , new Mat( ) , Imgproc.RETR_LIST ,
				Imgproc.CHAIN_APPROX_SIMPLE);

		// get the middle point of each contour and draw it on the original
		double fontScale = 2;
		Point[] myPoints = new Point[contours.size( )];
		for (int i = 0; i < contours.size( ); i++)
		{
			Moments mom = Imgproc.moments( contours.get( i));
			Point m = new Point( (mom.get_m10( ) / mom.get_m00( )) ,
					(mom.get_m01( ) / mom.get_m00( )));
			Core.circle( input , m , 6 , new Scalar( 255 , 255 , 255) , -1);
			Core.putText( input , Integer.toString( i) , m , 1 , 3 , new Scalar( 0 , 0 , 0));
			myPoints[i] = m;
		}

		showImage( input);
		System.out.println( myPoints.length);
	}

	private void showImage(Mat input)
	{
		input.convertTo( input , CvType.CV_8UC1);
		ImageView myImage = (ImageView) findViewById( R.id.preview);

		Bitmap bmp = Bitmap.createBitmap( input.cols( ) , input.rows( ) , Bitmap.Config.ARGB_8888);
		Utils.matToBitmap( input , bmp);
		myImage.setImageBitmap( bmp);
	}

	Mat mClosey = new Mat( );
	Mat mClosex = new Mat( );
	Mat mDest = new Mat( );

	public void NextStepClick(View view)
	{
		regularSudokuSegment();
	}

	//segment a sudoku step by step using the regular method
	private void regularSudokuSegment()
	{
		mStepToShow++;
		switch (mStepToShow)
		{
			case 1 :
				mImgToProcess = Highgui.imread( mFileName , 1);
				Imgproc.cvtColor( mImgToProcess , mImgToProcess , Imgproc.COLOR_RGB2GRAY);
				showImage( mImgToProcess);
				break;
			case 2 :
				VisionAlgorithms.preProcess( mImgToProcess);
				mImgToProcess.copyTo( mSudoku);
				showImage( mImgToProcess);
				break;
			case 3 :
				VisionAlgorithms.thresholdImage( mImgToProcess);
				showImage( mImgToProcess);
				break;
			case 4 :
				VisionAlgorithms.findPuzzleContours( mImgToProcess);
				showImage( mImgToProcess);
				break;
			case 5 :
				VisionAlgorithms.houghTransform( mImgToProcess , mSudoku);
				showImage( mImgToProcess);
				break;
			case 6 :
				showImage( mSudoku);
				break;
			case 7 :
				VisionAlgorithms.getDigits( mSudoku , PUZZLE_SIZE);
				showImage( mSudoku);
				break;
			default :
				mStepToShow = 0;
				// recursive call to go the step 1 again
				NextStepClick( null);
				break;
		}
	}

	//segment a sudoku step by step using a method that tries to warp 
	//each cell seperately by crossing all the gridlines and using the 
	//cross points as corner points for the cells
	private void AdvancedWarpSudokuSegment()
	{
		mStepToShow++;
		switch (mStepToShow)
		{
			case 1 :
				mImgToProcess = Highgui.imread( mFileName , 1);
				Imgproc.cvtColor( mImgToProcess , mImgToProcess , Imgproc.COLOR_RGB2GRAY);
				showImage( mImgToProcess);
				break;
			case 2 :
				VisionAlgorithms.preProcess( mImgToProcess);
				mImgToProcess.copyTo( mSudoku);
				showImage( mImgToProcess);
				break;
			case 3 :
				VisionAlgorithms.increaseContrast( mImgToProcess);
				showImage( mImgToProcess);
				break;
			case 4 :
				VisionAlgorithms.CreateMask( mImgToProcess);
				showImage( mImgToProcess);
				break;
			case 5 :
				VisionAlgorithms.findHorizontalLines( mImgToProcess , mClosex);
				showImage( mClosex);
				break;
			case 6 :
				VisionAlgorithms.findVerticalLines( mImgToProcess , mClosey);
				showImage( mClosey);
				break;
			case 7 :
				Core.bitwise_and( mClosex , mClosey , mDest);
				showImage( mDest);
				break;
			case 8 :
				// create a list of points to store all the contours in
				List<MatOfPoint> contours = new ArrayList<MatOfPoint>( 2000);

				// find all the contours in the image and store them in a list
				Imgproc.findContours( mDest , contours , new Mat( ) , Imgproc.RETR_LIST ,
						Imgproc.CHAIN_APPROX_SIMPLE);

				// get the middle point of each contour and draw it on the
				// original
				double fontScale = 2;
				Point[] myPoints = new Point[contours.size( )];
				for (int i = 0; i < contours.size( ); i++)
				{
					Moments mom = Imgproc.moments( contours.get( i));
					Point m = new Point( (mom.get_m10( ) / mom.get_m00( )) ,
							(mom.get_m01( ) / mom.get_m00( )));
					Core.circle( mImgToProcess , m , 6 , new Scalar( 255 , 255 , 255) , -1);
					Core.putText( mImgToProcess , Integer.toString( i) , m , 1 , 5 , new Scalar(
							255 , 255 , 255));
					myPoints[i] = m;
				}
				Core.putText( mImgToProcess , Integer.toString( contours.size( )) , new Point( 100 ,
						100) , 1 , 5 , new Scalar( 255 , 255 , 255));
				showImage( mImgToProcess);
				break;
			default :
				mStepToShow = 0;
				// recursive call to go the step 1 again
				NextStepClick( null);
				break;
		}
	}

	private void getTime(String method)
	{
		s.stop( );
		System.out.println( method + " " + s.getElapsedTime( ));
		s.start( );
	}

	@SuppressWarnings("unused" )
	private static void printSudoku(String sudoku)
	{
		for (int i = 1; i <= sudoku.length( ); i++)
		{
			System.out.print( sudoku.charAt( i - 1) + " ");

			if ( (i % 9) == 0)
			{
				System.out.print( '\n');
			}
		}
		System.out.print( '\n');
	}

	public void solveSudokuClick(View view)
	{

		mFirstClickTime = System.currentTimeMillis( );

		// block the button click for 1000ms
		if (canPressAgain( ))
		{
			setLastClick( );

			slideSolveButton( );
			slideNumberButtons( );

			SolveSudoku solver = new SolveSudoku( mSudokuPuzzle);
			try
			{
				solver.solve( 0 , 0);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace( );
			}

			String solvedPuzzle = solver.getPuzzle( );

			puzzleView.hideOriginalSudoku( );
			chbxShowOriginal.setChecked( false);
			puzzleView.drawPuzzle( solvedPuzzle , mSudokuPuzzle , false);

			mPuzzleSolved = true;

		}

	}

	// make sure a user doesnt double press a button during the animation
	private boolean canPressAgain()
	{
		return (mFirstClickTime > mLastClickTime);
	}

	private void setLastClick()
	{
		// animationTime of the button
		final long animationTime = 1000;

		// set the time of the last button click
		mLastClickTime = mFirstClickTime + animationTime;
	}

	public void changeNumberClick(View view)
	{
		vibrate( );

		Button b = (Button) view;
		String numberToInsert = b.getText( ).toString( );

		if (!isNumeric( numberToInsert))
		{
			numberToInsert = "0";
		}

		StringBuffer charInserter = new StringBuffer( mSudokuPuzzle);
		charInserter.replace( puzzleView.getPosition( ) - 1 , puzzleView.getPosition( ) ,
				numberToInsert);
		mSudokuPuzzle = charInserter.toString( );

		drawOriginalSudoku( );

		System.out.println( mSudokuPuzzle);
	}

	private void deleteNumber()
	{
		String numberToInsert = "0";
		StringBuffer charInserter = new StringBuffer( mSudokuPuzzle);
		charInserter.replace( puzzleView.getPosition( ) - 1 , puzzleView.getPosition( ) ,
				numberToInsert);

		mSudokuPuzzle = charInserter.toString( );

		drawOriginalSudoku( );
	}

	public boolean isNumeric(String input)
	{
		try
		{
			Integer.parseInt( input);
			return true;
		}
		catch (NumberFormatException e)
		{
			// input is not numeric
			return false;
		}
	}

	@Override
	public void onBackPressed()
	{
		// show the orginal sudoku when it has been solved
		if (sudokuIsSolved( ))
		{
			onBack( );
		}
		// go back to the previous activity otherwise
		else
		{
			super.onBackPressed( );
			overridePendingTransition( R.anim.push_down_out , R.anim.push_down_in);
		}
	}

	public void onBackClick(View view)
	{
		onBack( );
	}

	private void onBack()
	{
		mFirstClickTime = System.currentTimeMillis( );

		if (canPressAgain( ))
		{
			setLastClick( );

			slideSolveButton( );
			slideNumberButtons( );
			drawOriginalSudoku( );
		}
	}

	public boolean onKeyUp(int keyCode , KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_MENU)
		{
			// show the step screen
			if (this.findViewById( R.id.preview) == null)
			{
				setContentView( R.layout.preview);
			}
			else
			{
				setContentView( R.layout.sudoku_view);
			}
			return true;
		}
		return super.onKeyUp( keyCode , event);
	}

	private void slideSolveButton()
	{

		// slide button out
		if (!sudokuIsSolved( ))
		{
			Animation slideOut = AnimationUtils.loadAnimation( this , R.anim.slide_out_left);
			Animation slideIn = AnimationUtils.loadAnimation( this , R.anim.slide_in_right);

			buttonSolve.startAnimation( slideOut);
			buttonSolve.setVisibility( View.GONE);

			buttonBack.startAnimation( slideIn);
			buttonBack.setVisibility( View.VISIBLE);

		}
		// slide button in
		else
		{
			Animation slideIn = AnimationUtils.loadAnimation( this , R.anim.slide_in_left);
			Animation slideOut = AnimationUtils.loadAnimation( this , R.anim.slide_out_right);

			buttonSolve.startAnimation( slideIn);
			buttonSolve.setVisibility( View.VISIBLE);

			buttonBack.startAnimation( slideOut);
			buttonBack.setVisibility( View.GONE);
		}
		buttonSolve.setEnabled( true);
	}

	private void slideNumberButtons()
	{
		// slide button out
		if (!sudokuIsSolved( ))
		{
			Animation slideLeft = AnimationUtils.loadAnimation( this , R.anim.slide_out_left_top);
			Animation slideRight = AnimationUtils.loadAnimation( this , R.anim.slide_out_right);
			Animation slideDiagLeft = AnimationUtils.loadAnimation( this ,
					R.anim.slide_out_diag_left);
			Animation slideDiagRight = AnimationUtils.loadAnimation( this ,
					R.anim.slide_out_diag_right);
			Animation slideBottom = AnimationUtils.loadAnimation( this , R.anim.slide_out_bottom);
			Animation slideTop = AnimationUtils.loadAnimation( this , R.anim.slide_out_top);

			slideDiagRight.setAnimationListener( new AnimationListener( )
			{

				public void onAnimationEnd(Animation anim)
				{
					buttonPanel.setVisibility( View.INVISIBLE);
					buttonBar.setVisibility( View.INVISIBLE);
					puzzleView.disableRect( );
				}

				public void onAnimationRepeat(Animation arg0)
				{
				}

				public void onAnimationStart(Animation arg0)
				{
				}

			});

			button1.startAnimation( slideLeft);
			button2.startAnimation( slideLeft);
			button3.startAnimation( slideTop);
			button5.startAnimation( slideRight);
			button4.startAnimation( slideRight);
			button6.startAnimation( slideDiagLeft);
			button7.startAnimation( slideDiagLeft);
			button8.startAnimation( slideBottom);
			button9.startAnimation( slideDiagRight);
		}
		// slide button in
		else
		{
			Animation slideLeft = AnimationUtils.loadAnimation( this , R.anim.slide_in_left_top);
			Animation slideRight = AnimationUtils.loadAnimation( this , R.anim.slide_in_right);
			Animation slideDiagLeft = AnimationUtils.loadAnimation( this ,
					R.anim.slide_in_diag_left);
			Animation slideDiagRight = AnimationUtils.loadAnimation( this ,
					R.anim.slide_in_diag_right);
			Animation slideBottom = AnimationUtils.loadAnimation( this , R.anim.slide_in_bottom);
			Animation slideTop = AnimationUtils.loadAnimation( this , R.anim.slide_in_top);

			button1.startAnimation( slideLeft);
			button2.startAnimation( slideLeft);
			button3.startAnimation( slideTop);
			button4.startAnimation( slideRight);
			button5.startAnimation( slideRight);
			button6.startAnimation( slideDiagLeft);
			button7.startAnimation( slideDiagLeft);
			button8.startAnimation( slideBottom);
			button9.startAnimation( slideDiagRight);

			buttonPanel.setVisibility( View.VISIBLE);
		}
	}

	private boolean sudokuIsSolved()
	{
		return mPuzzleSolved;
	}

	private void drawOriginalSudoku()
	{
		puzzleView.drawPuzzle( mSudokuPuzzle , mSudokuPuzzle , true);
		mPuzzleSolved = false;
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
	}

	private void vibrate()
	{
		Vibrator v = (Vibrator) getSystemService( Context.VIBRATOR_SERVICE);
		// Vibrate for 500 milliseconds
		v.vibrate( 50);
	}

	public void onCheckboxClick(View view)
	{
		// show the original puzzle behind the drawn puzzle
		if (chbxShowOriginal.isChecked( ) && mBackgroundSudoku != null)
		{
			if (!mContrastStretched)
			{
				mBackgroundSudoku.convertTo( mBackgroundSudoku , CvType.CV_8UC1);
				bmpOriginalSudoku = Bitmap.createBitmap( mBackgroundSudoku.cols( ) ,
						mBackgroundSudoku.rows( ) , Bitmap.Config.ARGB_8888);
				Utils.matToBitmap( mBackgroundSudoku , bmpOriginalSudoku);
			}

			mContrastStretched = true;
			puzzleView.showOriginalSudoku( bmpOriginalSudoku);
		}
		// remove the original puzzle from the sudoku grid
		else
		{
			puzzleView.hideOriginalSudoku( );
		}
	}

	public void onOutsideClick(View view)
	{
		buttonBar.setVisibility( View.INVISIBLE);
		puzzleView.disableRect( );
	}

}
