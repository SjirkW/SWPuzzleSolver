package com.example.Sudoku;

import com.example.NativeCamera.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class SudokuView extends ViewGroup
{
	private static final String TAG = "Sudoku";
	private static int PUZZLE_SIZE = 9;
	private int cellAmount = PUZZLE_SIZE*PUZZLE_SIZE;
	private String mPuzzle;
	private String mOriginalPuzzle;
	private boolean mIsEditMode;
	private boolean mShowOriginal;
	private LinearLayout mButtonBar;
	private Bitmap mBackground;
	private Rect mBackgroundRect;

	// width of one tile
	private float width;
	// height of one tile
	private float height;
	// height of the puzzle
	private float totalHeight;
	// X index of selection
	private int selX;
	// Y index of selection
	private int selY;
	private final Rect selRect = new Rect( );

	private Paint background;
	private Paint dark;
	private Paint highlite;
	private Paint light;
	private Paint foreground;
	private Paint original;
	private Paint selected;

	private boolean mIsRectEnabled = false;

	public SudokuView(Context context , AttributeSet attrs)
	{
		super( context , attrs);
		setFocusable( true);
		setFocusableInTouchMode( true);

		setWillNotDraw( false);

		mPuzzle = "";
		mOriginalPuzzle = "";
		mIsEditMode = false;

		mButtonBar = (LinearLayout) findViewById( R.id.buttonRow);

		if (mButtonBar != null)
		{
			System.out.println( "succes");
		}

		init( );
	}

	public void init()
	{
		// background paint
		background = new Paint( );
		background.setColor( Color.WHITE);

		// major gridlines paint
		dark = new Paint( );
		dark.setColor( getResources( ).getColor( R.color.title));
		dark.setStrokeWidth( 4);

		// minor gridlines paint
		highlite = new Paint( );
		highlite.setColor( getResources( ).getColor( R.color.borders));

		// minor gridlines paint
		light = new Paint( );
		light.setColor( Color.LTGRAY);

		// paint for the selected rectangle
		selected = new Paint( );
		selected.setColor( getResources( ).getColor( R.color.blue));
		selected.setAlpha( 160);

		// paint for original numbers
		original = new Paint( Paint.ANTI_ALIAS_FLAG);
		original.setColor( getResources( ).getColor( R.color.red));
		original.setStyle( Style.FILL);
		original.setTextAlign( Paint.Align.CENTER);

		// paint for the found numbers
		foreground = new Paint( Paint.ANTI_ALIAS_FLAG);
		foreground.setColor( getResources( ).getColor( R.color.body));
		foreground.setStyle( Style.FILL);
		foreground.setTextAlign( Paint.Align.CENTER);
	}

	public void drawPuzzle(String puzzle , String originalPuzzle , boolean isEditMode)
	{
		mPuzzle = puzzle;
		mOriginalPuzzle = originalPuzzle;
		mIsEditMode = isEditMode;

		invalidate( );
		requestLayout( );
	}

	public void showOriginalSudoku(Bitmap original)
	{
		if (original != null)
		{
			mShowOriginal = true;

			mBackground = original;
			mBackgroundRect = new Rect( 0 , 0 , getWidth( ) , (int) totalHeight);

			invalidate( );
			requestLayout( );
		}
	}

	public void hideOriginalSudoku()
	{
		mShowOriginal = false;

		invalidate( );
		requestLayout( );
	}

	@Override
	protected void onSizeChanged(int w , int h , int oldw , int oldh)
	{
		width = w / PUZZLE_SIZE;
		height = w / PUZZLE_SIZE;
		totalHeight = w;
		getRect( selX , selY , selRect);
		Log.d( TAG , "onSizeChanged: width " + width + ", height " + height);
		super.onSizeChanged( w , h , oldw , oldh);
	}

	private void getRect(int x , int y , Rect rect)
	{
		rect.set( (int) (x * width) , (int) (y * height) , (int) (x * width + width) , (int) (y
				* height + height));
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw( canvas);

		if (mShowOriginal && mBackground != null && mBackgroundRect != null)
		{
			canvas.drawBitmap( mBackground , null , mBackgroundRect , background);
		}
		else
		{
			canvas.drawRect( 0 , 0 , getWidth( ) , totalHeight , background);

			// Draw the board...
			// Draw the minor grid lines
			for (int i = 1; i < PUZZLE_SIZE; i++)
			{
				canvas.drawLine( 0 , i * height , getWidth( ) , i * height , light);
				canvas.drawLine( i * width , 0 , i * width , totalHeight , light);
			}

			if (cellAmount == 81)
			{
			// Draw the major grid lines
			for (int i = 1; i < PUZZLE_SIZE; i++)
			{
				if (i % 3 != 0)
					continue;
				// horizontal
				canvas.drawLine( 0 , (i * height) - 2 , getWidth( ) , (i * height) - 2 , dark);
				// vertical
				canvas.drawLine( i * width , 0 , i * width , totalHeight , dark);

			}
			}
		}

		// only draw the selection when in edit mode
		if (mIsEditMode && mIsRectEnabled)
		{
			// Draw the selection...
			canvas.drawRect( selRect , selected);

		}

		foreground.setTextSize( height * 0.75f);
		foreground.setTextScaleX( width / height);

		original.setTextSize( height * 0.75f);
		original.setTextScaleX( width / height);

		// Draw the number in the center of the tile
		FontMetrics fm = foreground.getFontMetrics( );

		float x = 0;
		float y = 0;
		if (mShowOriginal)
		{
			// Centering in X: use alignment (and X at midpoint)
			x = width / 4;
			// Centering in Y: measure ascent/descent first
			y = height / 4 - (fm.ascent + fm.descent);
		}
		else
		{
			// Centering in X: use alignment (and X at midpoint)
			x = width / 2;
			// Centering in Y: measure ascent/descent first
			y = height / 2 - (fm.ascent + fm.descent) / 2;
		}

		
		if (mOriginalPuzzle.length( ) == cellAmount && mPuzzle.length( ) == cellAmount)
		{
			for (int i = 0; i < PUZZLE_SIZE; i++)
			{
				for (int j = 0; j < PUZZLE_SIZE; j++)
				{
					// if a original number, draw red
					if (Character.getNumericValue( mOriginalPuzzle.charAt( i + (j * PUZZLE_SIZE))) != 0)
					{
						String digitToDraw = String.valueOf( mPuzzle.charAt( i + (j * PUZZLE_SIZE)));
						canvas.drawText( digitToDraw , i * width + x , j * height + y , original);
					}
					// if new number, draw black
					else if (Character.getNumericValue( mPuzzle.charAt( i + (j * PUZZLE_SIZE))) != 0)
					{
						String digitToDraw = String.valueOf( mPuzzle.charAt( i + (j * PUZZLE_SIZE)));
						canvas.drawText( digitToDraw , i * width + x , j * height + y , foreground);
					}
				}
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec , int heightMeasureSpec)
	{

		// System.out.println("called" + getWidth() + " " + getHeight());
		int parentWidth = MeasureSpec.getSize( widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize( heightMeasureSpec);

		setMeasuredDimension( parentWidth , parentHeight);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction( ) != MotionEvent.ACTION_DOWN)
			return super.onTouchEvent( event);
		select( (int) (event.getX( ) / width) , (int) (event.getY( ) / height));
		// Log.d( TAG , "onTouchEvent: x " + selX + ", y " + selY);
		return true;
	}

	public void select(int x , int y)
	{
		invalidate( selRect);
		selX = Math.min( Math.max( x , 0) , 8);
		selY = Math.min( Math.max( y , 0) , 8);
		getRect( selX , selY , selRect);
		invalidate( selRect);
	}

	public int getPosition()
	{
		return ( (selY) * PUZZLE_SIZE + (selX + 1));
	}

	@Override
	protected void onLayout(boolean changed , int l , int t , int r , int b)
	{
		// TODO Auto-generated method stub

	}

	public int getFrameWidth()
	{
		return (int) width;
	}

	public int getFrameHeight()
	{
		return (int) height;
	}

	public int getBarWidth()
	{
		return 0;
	}

	public int getCellX()
	{
		return selX;
	}

	public int getCellY()
	{
		return selY;
	}

	public void enableRect()
	{
		mIsRectEnabled = true;
	}

	public void disableRect()
	{
		mIsRectEnabled = false;
	}

}