package com.example.Sudoku;

import java.util.BitSet;

/**
 * Solves a sudoku puzzle by recursion and backtracking
 */
public class SolveSudoku
{
	String mPuzzle = "";
	String solvedSudoku = "";

	public SolveSudoku(String puzzle)
	{
		this.mPuzzle = puzzle;
		createModel( );

	}

	/** The model */
	protected int model[][];

	/** Creates the model and sets up the initial situation */
	protected void createModel()
	{
		model = new int[9][9];

		// Clear all cells
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				model[row][col] = 0;

		// fill the model
		for (int i = 0; i < 81; i++)
		{
			model[i % 9][i / 9] = Character.getNumericValue( mPuzzle.charAt( i));
		}

	}

	/** Checks if num is an acceptable value for the given row */
	protected boolean checkRow(int row , int num)
	{
		for (int col = 0; col < 9; col++)
			if (model[row][col] == num)
				return false;

		return true;
	}

	/** Checks if num is an acceptable value for the given column */
	protected boolean checkCol(int col , int num)
	{
		for (int row = 0; row < 9; row++)
			if (model[row][col] == num)
				return false;

		return true;
	}

	/** Checks if num is an acceptable value for the box around row and col */
	protected boolean checkBox(int row , int col , int num)
	{
		row = (row / 3) * 3;
		col = (col / 3) * 3;

		for (int r = 0; r < 3; r++)
			for (int c = 0; c < 3; c++)
				if (model[row + r][col + c] == num)
					return false;

		return true;
	}

	/** Recursive function to find a valid number for one single cell */
	public void solve(int row , int col) throws Exception
	{
		// Throw an exception to stop the process if the puzzle is solved
		if (row > 8)
		{
			if (isValid( model))
			{
				for (int tempRow = 0; tempRow < 9; tempRow++)
				{
					for (int tempCol = 0; tempCol < 9; tempCol++)
					{
						solvedSudoku = solvedSudoku.concat( Integer
								.toString( model[tempCol][tempRow]));
					}
				}
			}
			else
			{
				solvedSudoku = "";
			}
		}

		// If the cell is not empty, continue with the next cell
		if (model[row][col] != 0)
			next( row , col);
		else
		{
			// Find a valid number for the empty cell
			for (int num = 1; num < 10; num++)
			{
				if (checkRow( row , num) && checkCol( col , num) && checkBox( row , col , num))
				{
					model[row][col] = num;

					// Delegate work on the next cell to a recursive call
					next( row , col);
				}
			}

			// No valid number was found, clean up and return to caller
			model[row][col] = 0;
		}
	}

	/** Calls solve for the next cell */
	public void next(int row , int col) throws Exception
	{
		if (col < 8)
			solve( row , col + 1);
		else
			solve( row + 1 , 0);
	}

	public String getPuzzle()
	{
		return solvedSudoku;
	}

	/**
	 * check if a sudoku is valid 
	 * source
	 * http://stackoverflow.com/questions/5484629
	 * 
	 * 
	 * @param board
	 * @return
	 */
	private static boolean isValid(int[][] board)
	{
		// Check rows and columns
		for (int i = 0; i < board.length; i++)
		{
			BitSet bsRow = new BitSet( 9);
			BitSet bsColumn = new BitSet( 9);
			for (int j = 0; j < board[i].length; j++)
			{
				if (board[i][j] == 0 || board[j][i] == 0)
					continue;
				if (bsRow.get( board[i][j] - 1) || bsColumn.get( board[j][i] - 1))
					return false;
				else
				{
					bsRow.set( board[i][j] - 1);
					bsColumn.set( board[j][i] - 1);
				}
			}
		}
		// Check within 3 x 3 grid
		for (int rowOffset = 0; rowOffset < 9; rowOffset += 3)
		{
			for (int columnOffset = 0; columnOffset < 9; columnOffset += 3)
			{
				BitSet threeByThree = new BitSet( 9);
				for (int i = rowOffset; i < rowOffset + 3; i++)
				{
					for (int j = columnOffset; j < columnOffset + 3; j++)
					{
						if (board[i][j] == 0)
							continue;
						if (threeByThree.get( board[i][j] - 1))
							return false;
						else
							threeByThree.set( board[i][j] - 1);
					}
				}
			}
		}
		return true;
	}
}