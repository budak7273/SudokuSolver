import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

public class Sudoku {

	private static int boardSize = 0;
	private static int partitionSize = 0;
	private static final String line = " +----------+----------+----------+";
	//hash maps to keep track of what's in each row/col/box
	private static HashMap<Integer, HashSet<Integer>> columnContents = new HashMap<Integer, HashSet<Integer>>();
	private static HashMap<Integer, HashSet<Integer>> rowContents = new HashMap<Integer, HashSet<Integer>>();
	private static HashMap<Integer, HashSet<Integer>> boxContents = new HashMap<Integer, HashSet<Integer>>();
	
	public static void main(String[] args){
		String filename = args[0];
		File inputFile = new File(filename);
		File outputFile = new File(filename.replace(".txt", "Solution.txt"));
		PrintWriter pw = null;
		Scanner input = null;
		int[][] vals = null;
		

		int temp = 0;
    	int count = 0;
    	
	    try {
			input = new Scanner(inputFile);
			pw = new PrintWriter(outputFile);
			temp = input.nextInt();
			boardSize = temp;
			partitionSize = (int) Math.sqrt(boardSize);
			System.out.println("Boardsize: " + temp + "x" + temp + " (Chunks " + partitionSize + ")");
			vals = new int[boardSize][boardSize];	
			for (int i = 0; i < boardSize; i++) { //init records hashsets
				columnContents.put(i, new HashSet<Integer>());
				rowContents.put(i, new HashSet<Integer>());
				boxContents.put(i, new HashSet<Integer>());
			}
			
			System.out.println("Input:");
	    	int row = 0;
	    	int col = 0;
	    	System.out.println(line);
	    	while (input.hasNext()){
	    		boolean printRowDivider = false;
	    		temp = input.nextInt();
	    		count++;
	    		if (col % partitionSize == 0) // on a col box boundary
	    			System.out.print(" |");
	    		System.out.printf("%3d", temp);
	    		if ((row + 1) % partitionSize == 0 && col == boardSize - 1) // on a row box boundary
	    			printRowDivider = true;
	    		
	    		vals[row][col] = temp;
				if (temp == 0) { 
					//cell is empty and needs to be solved
				} else {
					addNumToRecords(temp, row, col);
				}
				col++;
				if (col == boardSize) {
					col = 0;
					row++;
					System.out.println(" |");
				}
				if(printRowDivider)
					System.out.println(line);
				if (col == boardSize) {
					break;
				}
	    	}
	    	input.close();
	    } catch (FileNotFoundException exception) {
	    	System.out.println("Input file not found: " + filename);
	    }
	    if (count != boardSize*boardSize) throw new RuntimeException("Incorrect number of inputs.");
	    
		boolean solved = solve(vals);
		
		// Output
		if (!solved) {
			System.out.println("No solution found.");
			pw.print(-1); // If no solution exists, then you should write -1 to the file.
		} else {
			//printContents();
			
			//Output to screen and file
			System.out.println("Outputting to " + outputFile);
			System.out.println("\nOutput\n");
			for (int row = 0; row < boardSize; row++) {
				for (int col = 0; col < boardSize; col++) {
					if (col % partitionSize == 0) // on a col box boundary
		    			System.out.print(" |");
					System.out.printf("%3d", vals[row][col]); // output to screen
					pw.printf("%3d", vals[row][col]); // output to file
					if ((row + 1) % partitionSize == 0 && col == boardSize - 1) // on a row box boundary
		    			System.out.print("\n" + line);
					
				}
				System.out.println();
				pw.println();
			}
		}
		pw.close();
	}
	
	private static boolean solve(int[][] vals) { 
		//backtracing solve approach learned from geeksforgeeks.org/sudoku-backtracking-7/
		//find the first 0 spot to start solving at
        int row = -1; 
        int col = -1; 
        boolean isEmpty = true; 
        for (int rowSearch = 0; rowSearch < boardSize; rowSearch++) { 
            for (int colSearch = 0; colSearch < boardSize; colSearch++) { 
                if (vals[rowSearch][colSearch] == 0) { //if is empty...
                	//set this as the spot to try
                    row = rowSearch; 
                    col = colSearch; 
                    isEmpty = false; 
                    break; //stop searching
                } 
            } 
            if (!isEmpty) { 
                break; //stop searching
            } 
        } 
        //no empty space left -> solved
        if (isEmpty) { 
            return true; 
        }
        //else has empty space, still need to solve further
        for (int numToTry = 1; numToTry <= boardSize; numToTry++) { 
        	//try a number if it's not already in the row/col/box
        	if (isSafeToAdd(numToTry, row, col)) {
                vals[row][col] = numToTry; 
                addNumToRecords(numToTry, row, col);
                if (solve(vals)) { //recursively check if this number will lead to a solution or not
                    return true; 
                } else {
                    //go back to 0, it didn't work
                    vals[row][col] = 0;
                    //remove it from records
                    //the 'Dancing Links' thing you linked us info on sounds very useful for a case like this
                    removeNumFromRecords(numToTry, row, col);
                } 
            } 
        } 
        //no valid numbers for this blank; not solveable
        return false; 
    }
	
	private static void addNumToRecords(int numToAdd, int row, int col) {
		columnContents.get(col).add(numToAdd);
		rowContents.get(row).add(numToAdd);
		boxContents.get(getBoxIndexFromRowCol(row, col)).add(numToAdd);
	}
	
	private static void removeNumFromRecords(int numToDel, int row, int col) {
		columnContents.get(col).remove(numToDel);
		rowContents.get(row).remove(numToDel);
		boxContents.get(getBoxIndexFromRowCol(row, col)).remove(numToDel);
	}
	
	// no longer needed; was used to revert before I realized I could just make removeNumFromRecords
//	@SuppressWarnings("unchecked")
//	private static HashMap<Integer, HashSet<Integer>> deepCopyContentsMap(HashMap<Integer, HashSet<Integer>> contentsMap) {
//		HashMap<Integer, HashSet<Integer>> newContentsMap = new HashMap<Integer, HashSet<Integer>>();
//		for (Integer thisKey : contentsMap.keySet()) {
//			newContentsMap.put(thisKey, (HashSet<Integer>) contentsMap.get(thisKey).clone());
//		}
//		return newContentsMap;
//	}
	
	private static boolean isRowSafeToAdd(int numToAdd, int row) {
		return !rowContents.get(row).contains(numToAdd);
	}
	
	private static boolean isColSafeToAdd(int numToAdd, int col) {
		return !columnContents.get(col).contains(numToAdd);
	}
	
	private static boolean isBoxSafeToAdd(int numToAdd, int row, int col) {
		return !boxContents.get(getBoxIndexFromRowCol(row,col)).contains(numToAdd);
	}
	
	private static boolean isSafeToAdd(int numToAdd, int row, int col) {
		return isRowSafeToAdd(numToAdd, row) &&
				isColSafeToAdd(numToAdd, col) &&
				isBoxSafeToAdd(numToAdd, row, col);
	}
	
	private static void printContentsMap(HashSet<Integer> contentsMap) {
		for (Integer thisInteger: contentsMap) {
			System.out.print(thisInteger + ", ");
		}
	}
	
	private static void printContents() {
		//debug method
		System.out.println("Printing column contents:");
		for (int i = 0; i < boardSize; i++) {
			System.out.print("c" + i + ": ");
			printContentsMap(columnContents.get(i));
			System.out.println();
		}
		
		System.out.println("Printing row contents:");
		for (int i = 0; i < boardSize; i++) {
			System.out.print("r" + i + ": ");
			printContentsMap(rowContents.get(i));
			System.out.println();
		}
		
		System.out.println("Printing box contents:");
		for (int i = 0; i < boardSize; i++) {
			System.out.print("b" + i + ": ");
			printContentsMap(boxContents.get(i));
			System.out.println();
		}
	}

	private static int getBoxIndexFromRowCol(int row, int col) {
		int rowBoxIndex = (row) / partitionSize;
		int rowColIndex = (col) / partitionSize;
		int rVal = rowBoxIndex * partitionSize + rowColIndex;
		//System.out.println("Calculated box index " + rVal + " from (r" + row + ",c" + col + ")");
		return rVal;
	}
		
}