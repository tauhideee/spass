package spass;

import java.awt.Point;
import java.util.Locale;

import org.jtransforms.dht.DoubleDHT_2D;
import org.jtransforms.fft.DoubleFFT_2D;

/**
 * Holds all transform relevant data and methods.
 * 
 * @author Oliver Eickmeyer
 */
public class Trafo {
	public static enum Mode { FFT, DHT }
	protected Mode mode;
	protected double[] real;
	protected double[] imag;
	protected int size;
	protected Locale locale;
	
	/**
	 * Constructs a new Trafo, with no content.
	 */
	public Trafo(){
		mode = Mode.FFT;
		real = null;
		imag = null;
		size = 0;
		locale = Locale.US;
	}
	
	/**
	 * Calculates the transform of the <code>input</code>.
	 * @param input quadratic array
	 * @param size size of the input array in one dimension
	 * @param mode kind of transform
	 */
	public void transform(double[] input, int size, Mode mode){
		this.size = size;
		this.mode = mode;
		real = new double[size*size];
		switch(mode){
		case FFT:
			imag = new double[size*size];
			double[] complex = new double[size*size*2];
			for(int i=0; i<size*size; i++){
				complex[i*2] = input[i];
				complex[i*2+1] = 0.0;
			}
			DoubleFFT_2D transformerFFT = new DoubleFFT_2D(size, size);
			transformerFFT.complexForward(complex);
			for(int i=0; i<size*size; i++){
				real[i] = complex[i*2];
				imag[i] = complex[i*2+1];
			}
			break;
		case DHT:
			imag = null;
			System.arraycopy(input, 0, real, 0, size*size);
			DoubleDHT_2D transformerDHT = new DoubleDHT_2D(size, size);
			transformerDHT.forward(real);
			break;
		}
	}
	
	/**
	 * Returns an array with the real parts of the transform.
	 * @return array of the real parts
	 */
	public double[] getRealArray(){ return real; }
	
	/**
	 * Returns an array with the imaginary parts of the transform.
	 * @return array of the imaginary parts
	 */
	public double[] getImagArray(){ return imag; }
	
	/**
	 * Returns an array with with the absolute value of the transform - only
	 * allowed if there are imaginary parts.
	 * @return array of absolute values
	 */
	public double[] getAbsArray(){
		double[] abs = new double[size*size];
		for(int i=0; i<size*size; i++){
			abs[i] = Math.sqrt(real[i]*real[i] + imag[i]*imag[i]);
		}
		return abs;
	}
	
	public double[] getPhaseArray(){
		double[] phase = new double[size*size];
		for(int i=0; i<size*size; i++){
			phase[i] = getPhasePix(i);
		}
		return phase;
	}
	
	public double getReal(int index){
		return real[index];
	}
	
	public double getImag(int index){
		return imag[index];
	}
	
	public double getAbs(int index){
		return Math.sqrt(real[index]*real[index] + imag[index]*imag[index]);
	}
	
	/**
	 * Calculates the coordinates from the <code>index</code>.
	 * If the coordinates are not in the fourth quadrant, they
	 * will be modified, so that they can be interpreted as to be
	 * relative to (0, 0), for easier calculation of angle and
	 * wavelength.
	 * @param index index of the value in the arrays
	 * @return coordinates relative to (0, 0)
	 */
	public Point getCoords(int index){
		int x = index % size;
		int y = index / size;
		if(x > size/2) x -= size;
		if(y > size/2) y -= size;
		return new Point(x, y);
	}
	
	public double getAngle(int index){
		return Math.atan2(getCoords(index).y, getCoords(index).x);
	}
	
	/**
	 * Gets the phase in radians.
	 * @param index
	 * @return
	 */
	public double getPhaseRad(int index){
		return Math.atan2(imag[index], real[index]) + Math.PI/2.0;
	}
	
	/**
	 * Gets the phase in pixels.
	 * @param index
	 * @return
	 */
	public double getPhasePix(int index){
		return getPhaseRad(index) / Math.PI / 2.0 * getWavelength(index);
	}
	
	/**
	 * Calculates the SI-parameters for the given index.
	 * 
	 * @param index index of the position in the arrays
	 * @return <code>SIParam</code>s of the given index
	 */
	public SIParams getSIParams(int index){
		// calculate angle:
		double angle = getAngle(index);
		
		// calculate wavelength:
		double wvlen = getWavelength(index);
		
		// calculate phase:		
		double phase = getPhasePix(index);
		
		return new SIParams(angle, phase, wvlen);
	}
	
	public double getWavelength(int index){
		return (double) size / getCoords(index).distance(0, 0);
	}
	
	/**
	 * Size of the quadratic arrays in one dimension 
	 * @return size of the quadratic arrays in one dimension
	 */
	public int getSize(){ return size; }
	
	public Mode getMode(){ return mode; }

	/**
	 * Sets the Locale for locale-sensitive operations.
	 * 
	 * @param locale The Locale to be used.
	 */
	public void setLocate(Locale locale){
		this.locale = locale;
	}
	
	/**
	 * Creates a string representing some values of the given index.
	 * It is a locale-sensitive operation.
	 * 
	 * @param index index of the position in the array
	 * @return string of the transform-values
	 */
	public String toString(int index){
		switch(mode){
		case DHT:
			String formatDHT = "%.4f";
			return String.format(locale, formatDHT, getReal(index));
		case FFT:
			String formatFFT = "%.4f%+.4fi, abs %.4f, phase %.4f";
			return String.format(locale, formatFFT, getReal(index), getImag(index), getAbs(index), getPhasePix(index));
		default:
			return "?";
		}
	}
}
