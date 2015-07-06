package spass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

import javax.swing.JPanel;

/**
 * A GUI display that shows a graphical representation of an quadratic
 * array of floating point numbers.  There are no limitations for the
 * values in that array.  The ValueDisplay will normalize the values so
 * that the lowest value is mapped to zero, and the highest value is mapped
 * to 255.  The distribution of the values between the minimum and the maximum
 * can be either linear or logarithmic.
 * It is also possible to mask some values of the input array, so that they
 * will not be displayed (and not taken into account when determining the
 * minimum and maximum). 
 * 
 * @author Oliver Eickmeyer
 */
@SuppressWarnings("serial")
public class ValueDisplay extends JPanel{
	protected int size;
	protected double[] values;
	protected boolean[] mask;
	protected BufferedImage image;
	protected int zoom;
	protected int borderWidth;
	protected Color borderColor;
	protected Color maskColor;
	protected boolean log;

	/**
	 * Constructs display with no data.
	 */
	public ValueDisplay(){
		this(0, null);
	}
	
	/**
	 * Constructs display for a quadratic image of the given size.
	 * The size is the length of one dimension of the image.
	 * @param size size of one dimension of the quadratic image
	 * @param values quadratic array with values to be displayed 
	 */
	public ValueDisplay(int size, double[] values){
		this.size = size;
		this.values = values;
		log = false;
		zoom = 1;
		borderWidth = 1;
		borderColor = Color.GREEN;
		maskColor = new Color(0x008000);
		if(values != null) createImage();
	}
	
	/**
	 * Sets a new array which has to be displayed.
	 * @param size size of the quadratic array in one dimension
	 * @param values the array with the values to be displayed
	 * @param mask an optional boolean array of equal size; or
	 * <code>null</code>
	 */
	public void setValues(int size, double[] values, boolean[] mask){
		this.size = size;
		this.values = values;
		this.mask = mask;
		updateZoom();
		createImage();
	}
	
	/**
	 * Sets a new boolean quadratic array (has to be of same size as the
	 * value array) to mask the values.
	 * @param mask quadratic boolean array
	 */
	public void setMask(boolean[] mask){
		this.mask = mask;
		createImage();
	}
	
	/**
	 * Creates the <code>image</code> from the stored <code>values</code>.
	 * Will apply the <code>mask</code>, if latter is not <code>null</code>.
	 */
	protected void createImage(){
		image = doubleToImage(size, values, mask, log);
	}
	
	@Override
	public Dimension getPreferredSize(){
		int prefSize = Math.max(516+borderWidth*2, size+borderWidth*2);
		return new Dimension(prefSize, prefSize);
	}
	
	@Override
	protected void paintComponent(Graphics g){
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, getWidth(), getHeight());
		if(values != null){
			updateZoom();
			g.setColor(borderColor);
			g.drawRect(0, 0,
					size*zoom + borderWidth*2 - 1,
					size*zoom + borderWidth*2 - 1);
			g.drawImage(image,
					borderWidth, borderWidth, 
					image.getWidth()*zoom, image.getHeight()*zoom,
					Color.BLACK, null);
		}
		if(mask != null){
			g.setColor(maskColor);
			for(int row=0; row<size; row++){
				for(int col=0; col<size; col++){
					int i = row*size+col;
					if(!mask[i]){
						int x = borderWidth + col*zoom;
						int y = borderWidth + row*zoom;
						int s = zoom;
						g.fillRect(x, y, s, s);
					}
				}
			}
		}
	}
	
	/**
	 * Calculates a new zoom, so that the image will fit in the available
	 * space, including the border.
	 */
	public void updateZoom(){
		if(size != 0)
			zoom = (Math.min(getWidth(), getHeight())-borderWidth*2) / size;
		else
			zoom = 0;
	}
	
	/**
	 * Creates an image of gray values out of the given array.
	 * The array values will be normalized into a range from
	 * 0 to 1.
	 * If <code>mask</code> is not <code>null</code>, the values
	 * will be masked before.  A value will be set to zero if
	 * the corresponding mask entry is <code>false</code>.
	 * 
	 * @param size size of one dimension of the quadratic arrays
	 * @param values array of gray values
	 * @param mask a masking array to be applied to the values before
	 * normalizing
	 * @param log <code>true</code> activates logarithmic mode
	 * @return image created out of the array
	 */
	public static BufferedImage doubleToImage(int size, double[] values, boolean[] mask, boolean log){
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
		Raster raster = image.getRaster();
		SampleModel sm = raster.getSampleModel();
		DataBuffer db = raster.getDataBuffer();
		sm.setDataElements(0, 0, size, size, doubleToByteArray(values, mask, log), db);
		return image;
	}
	
	/**
	 * Converts an quadratic two-dimensional array of floating point values
	 * into an equally sized array of unsigned bytes.
	 * It will also be normalized (lowest value maps to zero and highest
	 * value maps to <code>255</code>).
	 * If <code>log</code> is <code>true</code>, the values between 0 and 255
	 * will be distributed logarithmically; otherwise linearly. 
	 * If <code>mask</code> is not <code>null</code>, the values
	 * will be masked before.  A value will be set to zero if
	 * the corresponding mask entry is <code>false</code>. 
	 * @param d array of double precision values to be converted to bytes
	 * @param mask a masking array to be applied to the array before
	 * @param log <code>true</code> activates logarithmic mode
	 * @return quadratic byte array
	 */
	public static byte[] doubleToByteArray(double[] d, boolean[] mask, boolean log){
		if(log) return doubleToByteArrayLog(d, mask);
		else return doubleToByteArrayLin(d, mask);
	}
	
	/**
	 * Converts an quadratic two-dimensional array of floating point values
	 * into an equally sized array of unsigned bytes.
	 * It will also be normalized (lowest value maps to zero and highest
	 * value maps to <code>255</code>).
	 * If <code>mask</code> is not <code>null</code>, the values
	 * will be masked before.  A value will be set to zero if
	 * the corresponding mask entry is <code>false</code>. 
	 * @param d array of double precision values to be converted to bytes
	 * @param mask a masking array to be applied to the array before
	 * @return quadratic byte array
	 */
	public static byte[] doubleToByteArrayLin(double[] d, boolean[] mask){
		double max = Double.MIN_VALUE;
		double min = Double.MAX_VALUE;
		
		for(int i=0; i<d.length; i++){
			if(mask == null || mask[i]){
				if(d[i] < min) min = d[i];
				if(d[i] > max) max = d[i];
			}
		}
		double scale = 255.0 / (max-min);

		byte[] b = new byte[d.length];
		for(int i=0; i<b.length; i++){
			if(mask != null && !mask[i])
				b[i] = 0;
			else{
				b[i] = (byte) ((d[i]-min) * scale);
			}
				
		}
		return b;
	}
	
	/**
	 * Converts an quadratic two-dimensional array of floating point values
	 * into an equally sized array of unsigned bytes.
	 * It will also be normalized (lowest value maps to zero and highest
	 * value maps to <code>255</code>).
	 * The values between 0 and 255 will be distributed logarithmically. 
	 * If <code>mask</code> is not <code>null</code>, the values
	 * will be masked before.  A value will be set to zero if
	 * the corresponding mask entry is <code>false</code>. 
	 * @param d array of double precision values to be converted to bytes
	 * @param mask a masking array to be applied to the array before
	 * @return quadratic byte array
	 */
	public static byte[] doubleToByteArrayLog(double[] d, boolean[] mask){
		double dMin = Double.MAX_VALUE;
		for(int i=0; i<d.length; i++){
			if(mask == null || mask[i])
				if(d[i] < dMin) dMin = d[i];
		}
		
		double[] g = new double[d.length];
		double gMax = Double.MIN_VALUE;
		for(int i=0; i<d.length; i++){
			g[i] = Math.log(d[i] - dMin + Math.E) - 1.0;
			if(mask == null || mask[i])
				if(g[i] > gMax) gMax = g[i];
		}
		byte[] b = new byte[d.length];
		for(int i=0; i<d.length; i++){
			if(mask == null || mask[i])
				b[i] = (byte) (g[i] / gMax * 255.0);
			else
				b[i] = 0;
		}
		return b;
	}
	
	public void setBorderWidth(int borderWidth){
		this.borderWidth = borderWidth;
	}
	
	public int getZoom(){ return zoom; }
	
	/**
	 * Calculates the image-related coordinates of the given pixel-position
	 * in this ValueDisplay.
	 * @param x - x-position in this ValueDisplay
	 * @param y - y-position in this ValueDisplay
	 * @return Point containing the image-related coordinates; or
	 * <code>null</code> if position is outside the image
	 */
	public Point getCoordinatesOf(int x, int y){
		if(x < borderWidth || x >= size*zoom+borderWidth ||
		   y < borderWidth || y >= size*zoom+borderWidth){
			return null;
		}
		Point p = new Point();
		p.x = (x - borderWidth) / zoom;
		p.y = (y - borderWidth) / zoom;
		return p;
	}
	
	/**
	 * Calculates the index of the value related to the given
	 * pixel-position in this ValueDisplay.
	 * @param x - x-position in this ValueDisplay
	 * @param y - y-position in this ValueDisplay
	 * @return index of the given position; or
	 * <code>-1</code> if the index would be out of bounds
	 */
	public int getIndexOf(int x, int y){		
		Point p = getCoordinatesOf(x, y);
		if(p == null) return -1;
		int i =  p.y * size + p.x;
		
		return i;
	}

	/**
	 * Checks if logarithmic mode is active.
	 * @return <code>true</code> if logarithmic mode is active; <code>
	 * false</code> otherwise
	 */
	public boolean isLog() {
		return log;
	}

	/**
	 * Sets the logarithmic mode.
	 * @param log <code>true</code> activates the logarithmic mode;
	 * <code>false</code> deactivates it
	 */
	public void setLog(boolean log) {
		this.log = log;
	}
}
