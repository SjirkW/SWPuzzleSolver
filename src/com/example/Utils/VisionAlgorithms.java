package com.example.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;


import com.googlecode.tesseract.android.TessBaseAPI;

import android.graphics.Bitmap;
import android.os.Environment;

public class VisionAlgorithms
{
	/**
	 * pre-process the image to reduce noise and increase brightness
	 * 
	 * @param input
	 */
	public static void preProcess(Mat input)
	{
		// blur the image to reduce noise
		Imgproc.GaussianBlur( input , input , new Size( 5 , 5) , 0);
	}

	public static void increaseContrast(Mat input)
	{
		Mat closedImage = new Mat( );
		input.convertTo( input , CvType.CV_32FC1);

		Mat kernel = Imgproc.getStructuringElement( Imgproc.MORPH_ELLIPSE , new Size( 11 , 11));
		Imgproc.morphologyEx( input , closedImage , Imgproc.MORPH_CLOSE , kernel);
		Core.divide( input , closedImage , input);
		Core.normalize( input , input , 0 , 255 , Core.NORM_MINMAX);

		input.convertTo( input , CvType.CV_8UC1);
	}

	public static void CreateMask(Mat input)
	{

		Mat thresh = new Mat( input.rows( ) , input.cols( ) , CvType.CV_8UC1);
		Imgproc.adaptiveThreshold( input , thresh , 255 , 0 , 1 , 19 , 2);
		// create a list of points to store all the contours in
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>( 200);

		// find all the contours in the image and store them in a list
		Imgproc.findContours( thresh , contours , new Mat( ) , Imgproc.RETR_LIST ,
				Imgproc.CHAIN_APPROX_SIMPLE);

		// the biggest area in the contour list
		int maxArea = 0;
		// index of the biggest area in the contour list
		int maxAreaIndex = 0;
		// loop through the contour list and find the biggest contour
		for (int i = 0; i < contours.size( ); i++)
		{
			Mat contour = contours.get( i);
			double contourarea = Imgproc.contourArea( contour);

			if ( (contourarea > maxArea) && (contourarea > 1000))
			{
				maxArea = (int) contourarea;
				maxAreaIndex = i;
			}
		}

		// create a temporary mat and draw the biggest contour on it
		Mat temp = new Mat( input.rows( ) , input.cols( ) , CvType.CV_8UC1);
		Imgproc.drawContours( temp , contours , maxAreaIndex , new Scalar( 255 , 0 , 0 , 255) , -1);

		Core.bitwise_and( input , temp , input);

	}

	/**
	 * threshold an image by using the adaptive threshold image
	 * 
	 * @param input
	 */
	public static void thresholdImage(Mat input)
	{
		// threshold the image by calculating the mean over an area of 5x5
		// pixels, subtract 2 from it
		Imgproc.adaptiveThreshold( input , input , 255 , Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C ,
				Imgproc.THRESH_BINARY_INV , 5 , 2);
		// Imgproc.adaptiveThreshold( input , input , 255 , 0 , 1 , 19 , 2);

	}

	/**
	 * Find the contours of a puzzle by detecting the biggest blob
	 * 
	 * @param input
	 */
	public static void findPuzzleContours(Mat input)
	{
		// create a list of points to store all the contours in
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>( 200);

		// find all the contours in the image and store them in a list
		Imgproc.findContours( input , contours , new Mat( ) , Imgproc.RETR_LIST ,
				Imgproc.CHAIN_APPROX_SIMPLE);

		// the biggest area in the contour list
		int maxArea = 0;
		// index of the biggest area in the contour list
		int maxAreaIndex = 0;
		// loop through the contour list and find the biggest contour
		for (int i = 0; i < contours.size( ); i++)
		{
			Mat contour = contours.get( i);
			double contourarea = Imgproc.contourArea( contour);

			if (contourarea > maxArea)
			{
				maxArea = (int) contourarea;
				maxAreaIndex = i;
			}
		}

		// create a temporary mat and draw the biggest contour on it
		Mat temp = new Mat( input.rows( ) , input.cols( ) , CvType.CV_8UC1);
		Imgproc.drawContours( temp , contours , maxAreaIndex , new Scalar( 255 , 0 , 0 , 255) , 2);

		// assign contour image to the input
		temp.assignTo( input);

	}

	public static void houghTransform(Mat input , Mat original)
	{
		Mat lines = new Mat( );

		Imgproc.HoughLines( input , lines , 1 , Math.PI / 180 , 200);

		mergeRelatedLines( input , lines);

		// Draw the lines
		for (int x = 0; x < lines.cols( ); x++)
		{
			double[] vec = lines.get( 0 , x);
			drawLine( input , vec);

		}

		findExtremes( original , lines);
		// drawGrid(original);
	}

	/**
	 * Draw a line on an Mat input by giving using a vector as an input
	 * 
	 * @param input
	 * @param vec
	 */
	private static void drawLine(Mat input , double[] vec)
	{

		Scalar rgb = new Scalar( 255 , 255 , 255);

		// draw non vertical lines
		if (vec[1] != 0)
		{
			float m = (float) (-1 / Math.tan( vec[1]));
			float c = (float) (vec[0] / Math.sin( vec[1]));

			Core.line( input , new Point( 0 , c) ,
					new Point( input.size( ).width , m * input.size( ).width + c) , rgb);

		}
		// draw vertical lines
		else
		{
			Core.line( input , new Point( vec[0] , 0) , new Point( vec[0] , input.size( ).height) ,
					rgb);
		}
	}

	/**
	 * Draw a sudoku grid over the puzzle
	 * 
	 * @param input
	 * @param vec
	 */
	@SuppressWarnings("unused" )
	private static void drawGrid(Mat input, int size)
	{
		int PUZZLE_SIZE = size;
		Scalar rgb = new Scalar( 255 , 255 , 255);

		int width = input.width( ) / PUZZLE_SIZE;
		int height = input.height( ) / PUZZLE_SIZE;

		// horizontal
		for (int x = 0; x < PUZZLE_SIZE; x++)
		{
			Core.line( input , new Point( 0 , x * height) ,
					new Point( input.width( ) , x * height) , rgb);
		}
		// vertical
		for (int y = 0; y < PUZZLE_SIZE; y++)
		{
			Core.line( input , new Point( y * width , 0) , new Point( y * width , input.height( )) ,
					rgb);
		}

	}

	/**
	 * Merge lines that are within a certain distance from each other
	 * 
	 * @param lines
	 *            a matrix containt the lines
	 * @param input
	 *            the input image
	 */
	public static void mergeRelatedLines(Mat input , Mat lines)
	{
		// a threshold for the total distance between 2 points
		final int POINT_THRESHOLD = 64 * 64;
		// a threshold for certain distance between 2 points
		final int LINE_DISTANCE_THRESHOLD = 20;
		double[] current;

		double horizontalLeftThreshold = Math.PI * 45 / 180;
		double horizontalRightThreshold = Math.PI * 135 / 180;

		for (int x = 0; x < lines.cols( ); x++)
		{
			// store the current line in an array
			current = lines.get( 0 , x);

			// skip the lines that have been marked as fused
			if (current[0] == 0 && current[1] == -100)
				continue;

			// store rho and theta for the current line in variables
			float p1 = (float) current[0];
			float theta1 = (float) current[1];

			// 2 temporary points to store coords in
			Point pt1current = new Point( );
			Point pt2current = new Point( );
			// if theta is horizontal (around 90 degrees), find a point at the
			// extreme right and extreme left
			if (theta1 > horizontalLeftThreshold && theta1 < horizontalRightThreshold)
			{
				pt1current.x = 0;
				pt1current.y = p1 / Math.sin( theta1);

				pt2current.x = input.size( ).width;
				pt2current.y = -pt2current.x / Math.tan( theta1) + p1 / Math.sin( theta1);
			}
			// find a point at the extreme top and extreme bottom when a point
			// isnt horizontal
			else
			{
				pt1current.y = 0;
				pt1current.x = p1 / Math.cos( theta1);

				pt2current.y = input.size( ).height;
				pt2current.x = -pt2current.y / Math.tan( theta1) + p1 / Math.cos( theta1);
			}

			double[] pos;
			// compare every line with every other line
			for (int y = 0; y < lines.cols( ); y++)
			{
				pos = lines.get( 0 , y);

				// continue when the lines are the same
				if (current == pos)
					continue;

				// check whether the lines are within a certain distance of each
				// other
				if (Math.abs( pos[0] - current[0]) < LINE_DISTANCE_THRESHOLD
						&& Math.abs( pos[1] - current[1]) < Math.PI * 10 / 180)
				{
					// save rho and theta for theline position
					float p = (float) pos[0];
					float theta = (float) pos[1];

					Point pt1 = new Point( );
					Point pt2 = new Point( );
					// if theta is horizontal (around 90 degrees), find a point
					// at the
					// extreme right and extreme left
					if (pos[1] > horizontalLeftThreshold && pos[1] < horizontalRightThreshold)
					{
						pt1.x = 0;
						pt1.y = p / Math.sin( theta);

						pt2.x = input.size( ).width;
						pt2.y = -pt2.x / Math.tan( theta) + p / Math.sin( theta);
					}
					// find a point at the extreme top and extreme bottom
					// otherwise
					else
					{
						pt1.y = 0;
						pt1.x = p / Math.cos( theta);

						pt2.y = input.size( ).height;
						pt2.x = -pt2.y / Math.tan( theta) + p / Math.cos( theta);
					}

					// fuse pos and current if the end lines are close to each
					// other
					double totalPoint1Distance = (double) (pt1.x - pt1current.x)
							* (pt1.x - pt1current.x) + (pt1.y - pt1current.y)
							* (pt1.y - pt1current.y);
					boolean isPoint1Close = (totalPoint1Distance < POINT_THRESHOLD);

					double totalPoint2Distance = (double) (pt2.x - pt2current.x)
							* (pt2.x - pt2current.x) + (pt2.y - pt2current.y)
							* (pt2.y - pt2current.y);
					boolean isPoint2Close = (totalPoint2Distance < POINT_THRESHOLD);
					if (isPoint1Close && isPoint2Close)
					{

						// Merge the two lines
						current[0] = (current[0] + pos[0]) / 2;
						current[1] = (current[1] + pos[1]) / 2;

						pos[0] = 0;
						pos[1] = -100;

						// save the lines in the lines matrix
						lines.put( 0 , y , pos);
						lines.put( 0 , x , current);
					}
				}
			}
		}

	}

	// find lines that are the nearest towards the edges
	private static void findExtremes(Mat input , Mat lines)
	{

		double[] topEdge = {1000 , 1000};
		double[] bottomEdge = {-1000 , -1000};
		double[] leftEdge = {1000 , 1000};
		double leftXIntercept = 100000;

		double[] rightEdge = {-1000 , -1000};
		double rightXIntercept = 0;

		double LeftThreshold = Math.PI * 45 / 180;
		double rightThreshold = Math.PI * 135 / 180;

		// loop over all lines
		for (int i = 0; i < lines.cols( ); i++)
		{
			double[] current = lines.get( 0 , i);

			float p = (float) current[0];
			float theta = (float) current[1];

			// skip the line when it has been merged
			if (p == 0 && theta == -100)
				continue;

			// use the normal form of a line to calculate intercepts
			double intercept = p / Math.cos( theta);

			// find what edge the line belongs to
			// when a line is nearly horizontal
			if (theta > LeftThreshold && theta < rightThreshold)
			{
				if (p < topEdge[0])
					topEdge = current;

				if (p > bottomEdge[0])
					bottomEdge = current;

			}
			// or when a line is nearly vertical
			else if (theta < LeftThreshold || theta > rightThreshold)
			{
				if (intercept > rightXIntercept)
				{
					rightEdge = current;
					rightXIntercept = intercept;
				}
				else if (intercept <= leftXIntercept)
				{
					leftEdge = current;
					leftXIntercept = intercept;
				}
			}
		}

		drawLine( input , topEdge);
		drawLine( input , bottomEdge);
		drawLine( input , leftEdge);
		drawLine( input , rightEdge);

		Point left1 = new Point( );
		Point left2 = new Point( );
		Point right1 = new Point( );
		Point right2 = new Point( );
		Point bottom1 = new Point( );
		Point bottom2 = new Point( );
		Point top1 = new Point( );
		Point top2 = new Point( );

		int height = (int) input.size( ).height;
		int width = (int) input.size( ).width;

		// calculate intersections of the four edge lines
		if (leftEdge[1] != 0)
		{
			left1.x = 0;
			left1.y = leftEdge[0] / Math.sin( leftEdge[1]);
			left2.x = width;
			left2.y = -left2.x / Math.tan( leftEdge[1]) + left1.y;
		}
		else
		{
			left1.y = 0;
			left1.x = leftEdge[0] / Math.cos( leftEdge[1]);
			left2.y = height;
			left2.x = left1.x - height * Math.tan( leftEdge[1]);
		}

		if (rightEdge[1] != 0)
		{
			right1.x = 0;
			right1.y = rightEdge[0] / Math.sin( rightEdge[1]);
			right2.x = width;
			right2.y = -right2.x / Math.tan( rightEdge[1]) + right1.y;
		}
		else
		{
			right1.y = 0;
			right1.x = rightEdge[0] / Math.cos( rightEdge[1]);
			right2.y = height;
			right2.x = right1.x - height * Math.tan( rightEdge[1]);
		}

		bottom1.x = 0;
		bottom1.y = bottomEdge[0] / Math.sin( bottomEdge[1]);
		bottom2.x = width;
		bottom2.y = -bottom2.x / Math.tan( bottomEdge[1]) + bottom1.y;

		top1.x = 0;
		top1.y = topEdge[0] / Math.sin( topEdge[1]);
		top2.x = width;
		top2.y = -top2.x / Math.tan( topEdge[1]) + top1.y;

		// find the intersection of these four lines
		double leftA = left2.y - left1.y;
		double leftB = left1.x - left2.x;
		double leftC = leftA * left1.x + leftB * left1.y;

		double rightA = right2.y - right1.y;
		double rightB = right1.x - right2.x;
		double rightC = rightA * right1.x + rightB * right1.y;

		double topA = top2.y - top1.y;
		double topB = top1.x - top2.x;
		double topC = topA * top1.x + topB * top1.y;

		double bottomA = bottom2.y - bottom1.y;
		double bottomB = bottom1.x - bottom2.x;
		double bottomC = bottomA * bottom1.x + bottomB * bottom1.y;

		// Intersection of left and top
		double detTopLeft = leftA * topB - leftB * topA;
		Point ptTopLeft = new Point( (topB * leftC - leftB * topC) / detTopLeft ,
				(leftA * topC - topA * leftC) / detTopLeft);

		// Intersection of top and right
		double detTopRight = rightA * topB - rightB * topA;
		Point ptTopRight = new Point( (topB * rightC - rightB * topC) / detTopRight , (rightA
				* topC - topA * rightC)
				/ detTopRight);

		// Intersection of right and bottom
		double detBottomRight = rightA * bottomB - rightB * bottomA;
		Point ptBottomRight = new Point( (bottomB * rightC - rightB * bottomC) / detBottomRight ,
				(rightA * bottomC - bottomA * rightC) / detBottomRight);

		// Intersection of bottom and left
		double detBottomLeft = leftA * bottomB - leftB * bottomA;
		Point ptBottomLeft = new Point( (bottomB * leftC - leftB * bottomC) / detBottomLeft ,
				(leftA * bottomC - bottomA * leftC) / detBottomLeft);

		// find the longest edge of a puzzle
		int maxLength = (int) ( (ptBottomLeft.x - ptBottomRight.x)
				* (ptBottomLeft.x - ptBottomRight.x) + (ptBottomLeft.y - ptBottomRight.y)
				* (ptBottomLeft.y - ptBottomRight.y));
		int temp = (int) ( (ptTopRight.x - ptBottomRight.x) * (ptTopRight.x - ptBottomRight.x) + (ptTopRight.y - ptBottomRight.y)
				* (ptTopRight.y - ptBottomRight.y));
		if (temp > maxLength)
			maxLength = temp;

		temp = (int) ( (ptTopRight.x - ptTopLeft.x) * (ptTopRight.x - ptTopLeft.x) + (ptTopRight.y - ptTopLeft.y)
				* (ptTopRight.y - ptTopLeft.y));
		if (temp > maxLength)
			maxLength = temp;

		temp = (int) ( (ptBottomLeft.x - ptTopLeft.x) * (ptBottomLeft.x - ptTopLeft.x) + (ptBottomLeft.y - ptTopLeft.y)
				* (ptBottomLeft.y - ptTopLeft.y));
		if (temp > maxLength)
			maxLength = temp;

		maxLength = (int) Math.sqrt( maxLength);

		// create source and destionation points
		List<Point> src = new ArrayList<Point>( );
		List<Point> dst = new ArrayList<Point>( );

		src.add( ptTopLeft);
		dst.add( new Point( 0 , 0));

		src.add( ptTopRight);
		dst.add( new Point( maxLength - 1 , 0));

		src.add( ptBottomRight);
		dst.add( new Point( maxLength - 1 , maxLength - 1));

		src.add( ptBottomLeft);
		dst.add( new Point( 0 , maxLength - 1));

		// convert to a float matrix required for
		// Imgproc.getPerspectiveTransform
		Mat source = Converters.vector_Point_to_Mat( src);
		source.convertTo( source , CvType.CV_32FC1);
		Mat destination = Converters.vector_Point_to_Mat( dst);
		destination.convertTo( destination , CvType.CV_32FC1);

		// do the perspective transform
		Imgproc.warpPerspective( input , input , Imgproc.getPerspectiveTransform( source ,
				destination) , new Size( maxLength , maxLength));
		
		
	}

	public static String getDigits(Mat input, int size)
	{
		int PUZZLE_SIZE = size;
		TessBaseAPI baseApi = new TessBaseAPI( );

		String FILE_PATH = Environment.getExternalStorageDirectory( ).getPath( );

		baseApi.init( FILE_PATH , "eng");
		baseApi.setVariable( "tessedit_char_whitelist" , "123456789");
		//baseApi.setVariable( "tessedit_char_whitelist" , "01");

		int widthCell = input.cols( ) / PUZZLE_SIZE;
		int heightCell = input.rows( ) / PUZZLE_SIZE;

		input.convertTo( input , CvType.CV_8UC1);

		Imgproc.threshold( input , input , 0 , 255 , Imgproc.THRESH_BINARY_INV
				+ Imgproc.THRESH_OTSU);

		// Actual cell
		Mat cell = new Mat( );
		Imgproc.resize( input , cell , new Size( widthCell , heightCell));

		//result string, represents a 1D sudoku puzzle
		String sudokuString = "";
		//Height of a character
		double[][] characterHeight = new double[9][9];		

		// set size limits for numbers within the cell
		int sizeLimit = (widthCell * heightCell * 85) / 100;
		int heightLimit = (heightCell * 85) / 100;
		int widthLimit = (widthCell * 85) / 100;
		int areaLowerLimit = (widthCell * heightCell * 5) / 100;
		int offset = widthCell / 8;

		// loop through the puzzle
		for (int row = 0; row < PUZZLE_SIZE; row++)
		{
			for (int col = 0; col < PUZZLE_SIZE; col++)
			{
				// the rectangle that binds a number
				Rect boundingRect = null;
				// an idividual cell in the puzzle
				cell = input.submat( row * heightCell , (row + 1) * heightCell , col * widthCell ,
						(col + 1) * widthCell);

				Mat tempCell = new Mat( );
				cell.copyTo( tempCell);

				// Preparing for finding bounds
				List<MatOfPoint> contours = new ArrayList<MatOfPoint>( );

				// find all the contours in a cell
				Imgproc.findContours( tempCell , contours , new Mat( ) , Imgproc.RETR_EXTERNAL ,
						Imgproc.CHAIN_APPROX_SIMPLE , new org.opencv.core.Point( 0 , 0));

				Point topLeftCorner = new Point( );
				Point bottomRightCorner = new Point( );

				double biggestContourOfCell = 0;
				boolean cellHasDigit = false;

				String outputText = "0";

				// loop through all the contours of a cell
				for (int i = 0; i < contours.size( ); i++)
				{
					boundingRect = Imgproc.boundingRect( contours.get( i));

					// a number cant be bigger than 80% of the cell area
					boolean isSmallerThanCell = boundingRect.area( ) < sizeLimit;
					// a number has to be bigger than 15% of the cell area
					boolean isBigEnough = boundingRect.area( ) > areaLowerLimit;
					// a number has to be shorter than 80% of the cell height
					boolean isShorterThanCell = boundingRect.height < heightLimit;
					// a number has to be slimmer than 80% of the cell width
					boolean isSlimmerThanCell = boundingRect.width < widthLimit;
					// the width of a number cant be bigger than twice the
					// height
					boolean isNotWidth = boundingRect.width < (boundingRect.height * 2);
					// has to be the biggest contour in the cell
					boolean isBiggestContour = boundingRect.area( ) > biggestContourOfCell;

					if (isSmallerThanCell && isShorterThanCell && isSlimmerThanCell && isBigEnough
							&& isNotWidth && isBiggestContour)
					{
						biggestContourOfCell = boundingRect.area( );
						cellHasDigit = true;
												
					
						boundingRect.x = boundingRect.x - offset;
						boundingRect.y = boundingRect.y - offset;
						boundingRect.width = boundingRect.width + (2 * offset);
						boundingRect.height = boundingRect.height + (2 * offset);

						topLeftCorner = new Point( boundingRect.x , boundingRect.y);
						bottomRightCorner = new Point( boundingRect.x + boundingRect.width ,
								boundingRect.y + boundingRect.height);

						try
						{
							// recognize the digit
							//get a matrix of sudoku cell
							Mat temp = cell.submat( boundingRect);
							//convert it to the required type
							temp.convertTo( temp , CvType.CV_8UC1);
							//create an empty bitmap
							Bitmap bmp = Bitmap.createBitmap( temp.cols( ) , temp.rows( ) ,
									Bitmap.Config.ARGB_8888);
							//convert the matrix to a bitmap
							Utils.matToBitmap( temp , bmp);
							// find the digit
							baseApi.setImage( bmp);
							outputText = baseApi.getUTF8Text( );
						}
						catch (Exception ex)
						{

						}
					}
				}

				if (cellHasDigit)
				{
					if (outputText.length( ) > 0)
					{
						sudokuString = sudokuString.concat( String.valueOf( outputText.charAt( 0)));
					}
					else
					{
						sudokuString = sudokuString.concat( "0");
					}
					// System.out.println( baseApi.getUTF8Text());
				}
				else
				{
					sudokuString = sudokuString.concat( "0");
				}
				
				// draw a rectangle around the boundingbox
				Core.rectangle( cell , new Point(topLeftCorner.x + offset,topLeftCorner.y + offset)
				, new Point(bottomRightCorner.x - offset,bottomRightCorner.y - offset) , new Scalar( 255 ,
						255 , 255));
				
				characterHeight[row][col] = (bottomRightCorner.y - offset)-(topLeftCorner.y + offset);
			}
		}

		//DEBUG, print the height of every character
		for (int x = 0; x < 9; x++)
		{
			for (int y = 0; y < 9; y++)
			{
				System.out.print( characterHeight[x][y] + " ");
				
			}
			System.out.print( '\n');
		}
		System.out.println(sudokuString);
		return sudokuString;
		
	}

	public static void findVerticalLines(Mat input , Mat output)
	{
		Mat kernelx = Imgproc.getStructuringElement( Imgproc.MORPH_RECT , new Size( 2 , 10));

		Mat dx = new Mat( );
		Imgproc.Sobel( input , dx , CvType.CV_16S , 1 , 0);
		Core.convertScaleAbs( dx , dx);

		Core.normalize( dx , dx , 0 , 255 , Core.NORM_MINMAX);

		Mat close = new Mat( );
		Imgproc.threshold( dx , close , 0 , 255 , Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
		Imgproc.morphologyEx( close , close , Imgproc.MORPH_DILATE , kernelx , new Point( -1 , -1) ,
				1);

		// create a list of points to store all the contours in
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>( 2000);

		// find all the contours in the image and store them in a list
		Imgproc.findContours( close , contours , new Mat( ) , Imgproc.RETR_EXTERNAL ,
				Imgproc.CHAIN_APPROX_SIMPLE);

		double values[] = new double[contours.size( )];
		Rect areaRect = null;
		for (int i = 0; i < contours.size( ); i++)
		{
			areaRect = Imgproc.boundingRect( contours.get( i));
			double size = areaRect.height;

			values[i] = size;
		}
		Arrays.sort( values);
		double valuesDesc[] = new double[contours.size( )];
		for (int i = 0; i < values.length; i++)
		{
			valuesDesc[i] = values[ (values.length - 1) - i];
		}

		// the rectangle that binds a number
		Rect boundingRect = null;
		for (int i = 0; i < contours.size( ); i++)
		{
			boundingRect = Imgproc.boundingRect( contours.get( i));

			if ( ( (boundingRect.height / boundingRect.width) > 5)
					&& (boundingRect.height >= valuesDesc[10]))
			{
				Imgproc.drawContours( close , contours , i , new Scalar( 255 , 255) , -1);
			}
			else
			{
				Imgproc.drawContours( close , contours , i , new Scalar( 0 , 0) , -1);
			}

		}

		Imgproc.morphologyEx( close , close , Imgproc.MORPH_DILATE , new Mat( ) , new Point( -1 ,
				-1) , 2);

		close.assignTo( output);
	}

	public static void findHorizontalLines(Mat input , Mat output)
	{

		Mat kernelx = Imgproc.getStructuringElement( Imgproc.MORPH_RECT , new Size( 10 , 2));

		Mat dx = new Mat( );
		Imgproc.Sobel( input , dx , CvType.CV_16S , 0 , 1);
		Core.convertScaleAbs( dx , dx);

		Core.normalize( dx , dx , 0 , 255 , Core.NORM_MINMAX);

		Mat close = new Mat( );
		Imgproc.threshold( dx , close , 0 , 255 , Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
		Imgproc.morphologyEx( close , close , Imgproc.MORPH_DILATE , kernelx , new Point( -1 , -1) ,
				1);

		// create a list of points to store all the contours in
		List<MatOfPoint> contours = new ArrayList<MatOfPoint>( 2000);

		// find all the contours in the image and store them in a list
		Imgproc.findContours( close , contours , new Mat( ) , Imgproc.RETR_EXTERNAL ,
				Imgproc.CHAIN_APPROX_SIMPLE);

		// for every contour in contours get area size, store in array
		// sort array
		// get 10th biggest
		// use it in if check

		double values[] = new double[contours.size( )];
		Rect areaRect = null;
		for (int i = 0; i < contours.size( ); i++)
		{
			areaRect = Imgproc.boundingRect( contours.get( i));
			double size = areaRect.width;

			values[i] = size;
		}
		Arrays.sort( values);
		double valuesDesc[] = new double[contours.size( )];
		for (int i = 0; i < values.length; i++)
		{
			valuesDesc[i] = values[ (values.length - 1) - i];
		}

		// the rectangle that binds a number
		Rect boundingRect = null;
		for (int i = 0; i < contours.size( ); i++)
		{
			boundingRect = Imgproc.boundingRect( contours.get( i));

			if ( ( (boundingRect.width / boundingRect.height) > 5)
					&& (boundingRect.width >= valuesDesc[10]))
			{
				Imgproc.drawContours( close , contours , i , new Scalar( 255 , 255) , -1);
			}
			else
			{
				Imgproc.drawContours( close , contours , i , new Scalar( 0 , 0) , -1);
			}

		}

		Imgproc.morphologyEx( close , close , Imgproc.MORPH_DILATE , new Mat( ) , new Point( -1 ,
				-1) , 2);

		close.assignTo( output);
	}

}
