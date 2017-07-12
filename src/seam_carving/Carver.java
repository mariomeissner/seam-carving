package seam_carving;
import java.awt.Color;
import java.util.Arrays;

import edu.princeton.cs.algs4.Picture;
public class Carver {
	
	
	/**
	 * Main method, loads the image and calls the carvings in a loop.
	 * @param args
	 */
	public static void main(String[] args) {
		Picture photo = new Picture("atat.jpg");
		Carver carver = new Carver(photo);
		photo.show();
		for (int i = 0; i < 100; i++){
			//carver.removeHorizontalSeam(carver.horizontalSeam());
			carver.removeVerticalSeam(carver.verticalSeam());
		}
		carver.picture.show();
	}
	
	
	private Picture picture;
	private final int MAX_ENERGY = 99999999;
	public Carver(Picture picture){
		this.picture = picture;
	}
	public Picture picture(){
		return picture;
	}
	
	public int columns(){
		return picture.width();
	}
		
	public int rows(){
		return picture.height();
	}
	
	// devolver la energia del pixel en columna x, fila y
	public double energy(int x, int y){
		return gradientX(x, y) + gradientY(x, y);
	}
	
	/**
	 * Returns the gradient of the two neighbor pixels of the given pixel, in the X axis.
	 */
	private double gradientX(int col, int row){
		Color izq = picture.get(mod(col-1, columns()), row);
		Color der = picture.get(mod(col+1, columns()), row);
		return (izq.getRed() - der.getRed()) * (izq.getRed() - der.getRed()) + 
				(izq.getGreen() - der.getGreen()) * (izq.getGreen() - der.getGreen()) + 
				(izq.getBlue() - der.getBlue()) * (izq.getBlue() - der.getBlue());
	}
	
	/**
	 * Returns the gradient of the two neighbor pixels of the given pixel, in the Y axis.
	 */
	private double gradientY(int col, int row){
		Color up = picture.get(col, mod(row-1,rows()));
		Color down = picture.get(col, mod(row-1,rows()));
		return (up.getRed() - down.getRed()) * (up.getRed() - down.getRed()) + 
				(up.getGreen() - down.getGreen()) * (up.getGreen() - down.getGreen()) + 
				(up.getBlue() - down.getBlue()) * (up.getBlue() - down.getBlue());
		}
	
	/**
	 * Searches and returns an horizontal seam
	 * @return array corresponding to the row in which the seam pixel is in each column
	 */
	public int[] horizontalSeam(){
		int[] seam = new int[columns()];
		double[][] energies = new double[columns()][rows()];
		//initialize the first column
		for (int i = 0; i < rows(); i++){
			energies[0][i] = energy(0, i);
		}
		
		for (int i = 1; i < columns(); i++) { //Column
			for(int j = 0; j < rows(); j++){ //Row
				energies[i][j] = energy(i,j) + 
						min(energies[i-1][j], energies[i-1][mod(j-1, rows())], 
								energies[i-1][mod(j+1, rows())]);
			}
		}
		
		//We take the smallest accumulated energy of the last column
		int row = posMinArray(energies[columns()-1]);
		seam[columns()-1] = row;
		
		/* We have to stop the seam from crossing the edges of the image */
		for (int i = columns() - 2; i>= 0; i--){
			if(row >= rows()-1) {
				row += relativeMinPos(energies[i][row-1], 
						energies[i][row], MAX_ENERGY);
			} else if (row <= 0) {
				row += relativeMinPos(MAX_ENERGY, 
						energies[i][row], energies[i][row+1]);
			} else {
				row += relativeMinPos(energies[i][row-1], 
						energies[i][row], energies[i][row+1]);
			}
			seam[i] = row;
		}
		return seam;
	}
	/**
	 * Searches and returns a vertical seam
	 * @return array corresponding to the column in which the seam pixel is in each row
	 */
	public int[] verticalSeam(){
		int[] seam = new int[rows()];
		double[][] energies = new double[rows()][columns()];
		//we initialize the first row
		for (int i = 0; i < columns(); i++){
			energies[0][i] = energy(i, 0);
		}
		for (int i = 1; i < rows(); i++) { //Row
			for(int j = 0; j < columns(); j++){ //Column
				energies[i][j] = energy(j,i) + 
						min(energies[i-1][j], energies[i-1][mod(j-1, columns())], 
								energies[i-1][mod(j+1, columns())]);
			}
		}
		
		//We take the smallest accumulated energy of the last row
		int col = posMinArray(energies[rows()-1]);
		seam[rows()-1] = col;
		for (int i = rows() - 2; i>= 0; i--){
			if(col >= columns()-1) {
				col += relativeMinPos(energies[i][col-1], 
						energies[i][col], MAX_ENERGY);
			} else if (col <= 0) {
				col += relativeMinPos(MAX_ENERGY, 
						energies[i][col], energies[i][col+1]);
			} else {
				col += relativeMinPos(energies[i][col-1], 
						energies[i][col], energies[i][col+1]);
			}
			seam[i] = col;
		}
		return seam;
	}
	
	
	/*
	 * If the pixel is previous to the seam, we add it normally.
	 * If the pixel is part of the seam we dont add it.
	 * If the pixel is posterior to the seam, we add it shifted one position above.
	 */
	public void removeHorizontalSeam(int[] seam){
		Picture newPicture = new Picture(columns(), rows()-1);
		for (int i = 0; i < columns(); i++) {  //Columns
			for (int j = 0; j < rows(); j++) {  //Rows
				
				if(j < seam[i]){
					newPicture.set(i, j, picture.get(i, j));
				} else if(j > seam[i]){
					newPicture.set(i, j-1, picture.get(i, j));
				}
				
			}
		}
		picture = newPicture;
	}
	
	/*
	 * If the pixel is previous to the seam, we add it normally.
	 * If the pixel is part of the seam we dont add it.
	 * If the pixel is posterior to the seam, we add it shifted one position to the left.
	 */
	public void removeVerticalSeam(int[] seam){
		Picture newPicture = new Picture(columns()-1, rows());
		for (int i = 0; i < rows(); i++) { //Rows
			for (int j = 0; j < columns(); j++) { //Columns
				if(j < seam[i]){
					newPicture.set(j, i, picture.get(j, i));
				} else if(j > seam[i]){
					newPicture.set(j-1, i, picture.get(j, i));
				}
			}
		}
		picture = newPicture;
	}
	
	/* draw a vertical seam on the canvas */
	public void drawVerticalSeam(int[] seam){
		Picture newPicture = new Picture(columns(), rows());
		for (int i = 0; i < rows(); i++) { //Rows
			for (int j = 0; j < columns(); j++) { //Columns
				if(j == seam[i]){
					newPicture.set(j, i, new Color(200, 10, 10));
				} else {
					newPicture.set(j, i, picture.get(j, i));
				}
			}
		}
		picture = newPicture;
	}
	
	/* draw an horizontal seam on the canvas */
	public void drawHorizontalSeam(int[] seam){
		Picture newPicture = new Picture(columns(), rows());
		for (int i = 0; i < columns(); i++) { //Columns
			for (int j = 0; j < rows(); j++) { //Rows
				if(j == seam[i]){
					newPicture.set(i, j, new Color(200, 10, 10));
				} else {
					newPicture.set(i, j, picture.get(i, j));
				}
			}
		}
		picture = newPicture;
	}
	
	
	
	/**
	 * Returns the relative position of the smallest of three numbers
	 * -1 if the smallest is the first number, 0 if its the second and 1 if it's the last
	 * @param one
	 * @param two
	 * @param three
	 * @return relative position of the smallest between the three indicated numbers.
	 */
	private int relativeMinPos (double one, double two, double three){
		double min;
		min = Math.min(Math.min(one, two), three);
		if (min == one){
			return -1;
		} else if (min == two){
			return 0;
		} else {
			return 1;
		}
	}
	
	/* Returns the smallest of the three indicated numbers */
	private double min (double one, double two, double three){
		return Math.min(Math.min(one, two), three);
	}
	
	/* Returns the position of the smallest number in the array */
	private int posMinArray (double[] array){
		double min = array[0];
		int posMin = 0;
		for (int i = 1; i < array.length; i++){
			if (min > array[i]){
				min = array[i];
				posMin = i;
			}
		}
		return posMin;
	}
	
	/* Performs modulo operation  */
	private int mod(int num, int mod){
		return (num+mod)%mod;
	}
}
