package spass;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.TransferHandler;

import org.jtransforms.fft.DoubleFFT_2D;

import spass.Trafo.Mode;

/**
 * Structured illumination Pattern Analyzer (and Stripe Slicer).
 * 
 * Analyzes images of Structured Illumination Microscopy (SIM), to
 * find the parameters of the structured illumination.
 *
 * TODO:
 * Cut stripes out of the images and compose them to an
 * resolution enhanced image.
 * (my Bachelor-Thesis)
 * 
 * @author Oliver Eickmeyer
 */
@SuppressWarnings("serial")
public class Spass extends JFrame
implements ActionListener,
		   NumberListener,
		   MouseListener,
		   MouseMotionListener,
		   MouseWheelListener,
		   KeyListener{
	
	/**
	 * Digits after decimal point for
	 * <code>NumberField</code>s.
	 */
	public final static int PARAM_DIGITS = 4;
	
	/**
	 * Frames per second for animations.
	 */
	public final static int animFPS = 20;
	
	/**
	 * Minimum size for the quadratic arrays in one dimension.
	 */
	public final static int MINSIZE = 4;
	
	/**
	 * Transform mode: Discrete Hartley Transform
	 */
	public final static int TRAFOMODE_DHT = 0;
	
	/**
	 * Transform mode: Fast Fourier Transform (absolute values)
	 */
	public final static int TRAFOMODE_FFT_ABS = 1;
	
	/**
	 * Transform mode: Fast Fourier Transform (real part)
	 */
	public final static int TRAFOMODE_FFT_RE = 2;
	
	/**
	 * Transform mode: Fast Fourier Transform (imaginary part)
	 */
	public final static int TRAFOMODE_FFT_IM = 3;
	
	/**
	 * Transform mode: Fast Fourier Transform (phase)
	 */
	public final static int TRAFOMODE_FFT_PHASE = 4;
	
	/**
	 * What to display for the input side:
	 * <ul>
	 * <li><code>SI</code> - Structured Illumination pattern</li>
	 * <li><code>IMAGE</code> - Image loaded from file</li> 
	 * <li><code>MUL</code> - element-wise multiplication of image and SI-pattern</li>
	 * </ul> 
	 *
	 */
	public static enum ValueMode {SI, IMAGE, MUL};
	
	protected ValueMode valueMode;
	protected ValueDisplay inValueDisp;
	protected ValueDisplay outValueDisp;
	protected Timer timer;
	protected int size;
	protected double[] valSIP; // values of SI-pattern
	protected double[] valImg; // values of image file
	protected double[] valMul; // values of multiplication
	protected double sumMul; // pixelsum of multiplication
//	protected double[] trafos; // values of transform
	protected Trafo trafo;
	protected JPanel imagePanel;
	protected JPanel sipPanel;
	protected JLabel lblCursorValue;
	protected JLabel lblCursorTrafo;
	protected JLabel lblSize;
	protected JLabel lblSumMul;
	protected JButton btnFindSIP;
	protected NumberField angle, phase, wvlen;
	protected Locale locale;
	protected JComboBox<String> trafoMode;
	protected JCheckBox mask;
	protected JTextField maskRange;
	protected JCheckBox log;

	/**
	 * Starts the application.
	 * 
	 * @param args so far no command line arguments available
	 */
	public static void main(String[] args) {
		new Spass();
	}
	
	/**
	 * Constructs the main class of this application.
	 */
	public Spass(){
		super("Spass");
		locale = Locale.US;
		valueMode = ValueMode.SI;
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setLayout(new BorderLayout());
		trafo = new Trafo();
		
		// Image Panel (SI-Pattern and Transformation)
		
		imagePanel = new JPanel();
		add(imagePanel, BorderLayout.CENTER);
		imagePanel.setLayout(new GridLayout(1,2));		
		inValueDisp = new ValueDisplay();
		inValueDisp.setName("inDisp");
		inValueDisp.addMouseMotionListener(this);
		inValueDisp.addMouseListener(this);
		inValueDisp.setTransferHandler(new TransferHandler(){
			public boolean canImport(TransferHandler.TransferSupport support){
				if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
	                return false;
	            }
				else{
					return true;
				}
			}
			public boolean importData(TransferHandler.TransferSupport support) {
				if (!canImport(support)) {
	                return false;
	            }
				Transferable t = support.getTransferable();
				try {
	                java.util.List<File> l =
	                    (java.util.List<File>)t.getTransferData(DataFlavor.javaFileListFlavor);
	                loadImage(l.get(0));
	            } catch (UnsupportedFlavorException e) {
	                return false;
	            } catch (IOException e) {
	                return false;
	            }
				return true;
			}
		});
		imagePanel.add(inValueDisp);
		outValueDisp = new ValueDisplay();
		outValueDisp.setName("outDisp");
		outValueDisp.addMouseMotionListener(this);
		outValueDisp.addMouseListener(this);
		imagePanel.add(outValueDisp);
		setFocusable(true);
		
		
		// Data Panel
		
		JPanel dataPanel = new JPanel();
		add(dataPanel, BorderLayout.SOUTH);
		dataPanel.setLayout(new GridLayout(3,2));
		lblCursorValue = new JLabel("?");
		dataPanel.add(lblCursorValue);
		lblCursorTrafo = new JLabel("?");
		dataPanel.add(lblCursorTrafo);
		JPanel infoPanel1 = new JPanel();
		dataPanel.add(infoPanel1);
		infoPanel1.setLayout(new GridLayout(1, 2));
		lblSize = new JLabel("?");
		infoPanel1.add(lblSize);
		lblSumMul = new JLabel("sum: ?");
		infoPanel1.add(lblSumMul);
		
		JPanel optionsPanel = new JPanel();
		optionsPanel.setLayout(new FlowLayout());
		String[] trafoModeStrings = {
				"DHT", 
				"FFT, abs",
				"FFT, re",
				"FFT, im",
				"FFT, phase"};
		trafoMode = new JComboBox<>(trafoModeStrings);
		trafoMode.addActionListener(this);
		optionsPanel.add(trafoMode);
		mask = new JCheckBox("Mask");
		mask.setSelected(true);
		optionsPanel.add(mask);
		mask.addActionListener(this);
		maskRange = new JTextField(3);
		maskRange.setText("20");
		maskRange.addActionListener(this);
		optionsPanel.add(maskRange);
		log = new JCheckBox("log");
		log.setSelected(false);
		optionsPanel.add(log);
		log.addActionListener(this);
		dataPanel.add(optionsPanel);
		
		sipPanel = new JPanel();
		dataPanel.add(sipPanel);
		sipPanel.setLayout(new FlowLayout());
		sipPanel.add(new JLabel("Angle"));
		angle = new NumberField(PARAM_DIGITS);
		angle.setNumberListener(this);
		sipPanel.add(angle);
		sipPanel.add(new JLabel("Phase"));
		phase = new NumberField(PARAM_DIGITS);
		phase.setNumberListener(this);
		sipPanel.add(phase);
		sipPanel.add(new JLabel("Wvlen"));
		wvlen = new NumberField(PARAM_DIGITS);
		wvlen.setNumberListener(this);
		sipPanel.add(wvlen);
		sipPanel.addMouseWheelListener(this);
		
		btnFindSIP = new JButton("find SI params");
		btnFindSIP.addActionListener(this);
		dataPanel.add(btnFindSIP);
		
		// Go Live
		
		size = 256;
		angle.setNumber(Math.PI/4.0);
		phase.setNumber(0.0);
		wvlen.setNumber(8);
		calculateValues();
		if(mask.isSelected()){
			double r = Double.parseDouble(maskRange.getText());
			outValueDisp.setMask(createMask(size, r));
		}
		addKeyListener(this);
		addMouseListener(this);
		pack();
		setVisible(true);
		trafoMode.setSelectedIndex(TRAFOMODE_FFT_ABS);
//		timer = new Timer(animFPS, this);
	}
		
	/**
	 * Loads image file and converts to <code>double</code>s.
	 * So far only gray values are supported.
	 * Images must be quadratic in size and the width and height
	 * must be integer powers of 2.
	 * 
	 * @param file
	 */
	protected void loadImage(File file){
		BufferedImage image = null;
		try {
			image = ImageIO.read(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(image == null){
			System.err.println("Loading of image failed: "+file);
			return;
		}
		if(image.getWidth() != image.getHeight()){
			System.err.println("Error: Width and height must be equal.");
			return;
		}
		double logPrecise = Math.log(image.getWidth()) / Math.log(2);
		double logRounded = (double)((int)logPrecise);
		if(logPrecise != logRounded){
			System.err.println("Width and height must be integer powers of 2");
		}
		size = image.getWidth();
		Raster r = image.getRaster();
		SampleModel sm = r.getSampleModel();
		DataBuffer db = r.getDataBuffer();
		int w = sm.getWidth();
		int h = sm.getHeight();
		byte[] bytes = (byte[]) sm.getDataElements(0, 0, w, h, null, db);
		valImg = new double[size*size];
		for(int i=0; i<bytes.length; i++){
			valImg[i] = Byte.toUnsignedInt(bytes[i]);
		}
		valueMode = ValueMode.IMAGE;
		updateValueDisplay();
		transform();
		updateTrafoDisplay();
		updateMultiplication();
	}
	
	/**
	 * Does the multiplication and updates the data shown in the info
	 * panel regarding the multiplication results.
	 */
	private void updateMultiplication(){
		if(valMul == null) valMul = new double[size*size];
		sumMul = multiply(valMul, valImg, valSIP);
		lblSumMul.setText(String.format(locale, "sum: %.3f", sumMul));
	}
	
	/**
	 * Multiplies two <code>double</code> arrays element-wise and stores
	 * the result in <code>target</code>.
	 *
	 * @param target will receive the result
	 * @param val1 first array
	 * @param val2 second array
	 * @return element-wise multiplication of first and second array
	 */
	public static double multiply(double[] target, double[] val1, double[] val2){
		double sum = 0.0;
		for(int i=0; i<val1.length; i++){
			target[i] = val1[i] * val2[i];
			sum += target[i];
		}
		return sum;
	}
	
	/**
	 * Updates the display showing the input values (the image, the SI-pattern
	 * or the multiplication).
	 */
	public void updateValueDisplay(){
		switch(valueMode){
		case SI:
			inValueDisp.setValues(size, valSIP, null);
			break;
		case IMAGE:
			inValueDisp.setValues(size, valImg, null);
			break;
		case MUL:
			inValueDisp.setValues(size, valMul, null);
			break;
		}
		repaint();
	}
	
	/**
	 * Updates the display for the transform result.
	 */
	public void updateTrafoDisplay(){
		switch(trafoMode.getSelectedIndex()){
		case TRAFOMODE_DHT:
			outValueDisp.setValues(size, trafo.getRealArray());
			break;
		case TRAFOMODE_FFT_ABS:
			outValueDisp.setValues(size, trafo.getAbsArray());
			break;
		case TRAFOMODE_FFT_RE:
			outValueDisp.setValues(size, trafo.getRealArray());
			break;
		case TRAFOMODE_FFT_IM:
			outValueDisp.setValues(size, trafo.getImagArray());
			break;
		case TRAFOMODE_FFT_PHASE:
			outValueDisp.setValues(size, trafo.getPhaseArray());
			break;
		}
		repaint();
	}
	
	/**
	 * Calculates the SI-pattern values.  The parameters will be read from
	 * the GUI input elements.	 */
	protected void calculateValues(){
		if(valueMode == ValueMode.IMAGE){
			if(valMul == null) valueMode = ValueMode.SI;
			else valueMode = ValueMode.MUL;
		}
		lblSize.setText(String.format(locale, "size: %d", size));
		valSIP = Spass.createSIPattern(size, angle.getNumber(), phase.getNumber(), wvlen.getNumber());
		if(valImg != null){
			updateMultiplication();
		}
		transform();
		updateValueDisplay();
		updateTrafoDisplay();
		repaint();
	}
	
	protected void transform(){
		if(trafoMode.getSelectedIndex() == TRAFOMODE_DHT){
			trafo.transform(getInputArray(), size, Mode.DHT);
		}
		else{
			trafo.transform(getInputArray(), size, Mode.FFT);
		}
		
	}
		
	/**
	 * Generates a quadratic SI pattern of the given size, and with the given
	 * SI parameters.  The values will be stored in an one-dimensional array,
	 * index of each element calculated with
	 * <code>i = y * size + x</code>.
	 * 
	 * @param size - size of the quadratic pattern in one dimension
	 * @param angle - angle of the SI pattern (in radians)
	 * @param phase - phase of the SI pattern (in pixels)
	 * @param wavelength - wavelength of the SI pattern (in pixels)
	 * @return array containing the values of the SI pattern
	 */
	public static double[] createSIPattern(int size, double angle, double phase, double wavelength){
		double[] pattern = new double[size*size];
		int xm = size / 2, ym = size / 2;
		for(int y=0; y<size; y++){
			for(int x=0; x<size; x++){
				double r = Math.sqrt( (x-xm)*(x-xm) + (y-ym)*(y-ym) );
				double x2 = xm + r * Math.cos(Math.atan2(y-ym, x-xm) - angle);
				pattern[y*size+x] = (1.0 + Math.sin(2.0 * Math.PI * (x2+phase) / wavelength)) / 2.0;
			}
		}
		return pattern;
	}
	
	/**
	 * Creates a boolean array for the purpose of masking the transform
	 * array.
	 * This is helpful to make the interesting parts of the spectrum more
	 * visible.
	 * So far only the point <code>0, 0</code> is masked (because this is
	 * in every transform usually the 'brightest').
	 *  
	 * @param size size of the quadratic array (in one dimension)
	 * @return boolean array
	 */
	public static boolean[] createMask(int size){
		boolean[] mask = new boolean[size*size];
		mask[0] = false;
		for(int i=1; i<size*size; i++){
			mask[i] = true;
		}
		return mask;
	}
	
	/**
	 * Creates a boolean array for the purpose of masking the transform
	 * array.  All values within the distance of <code>r</code> from the
	 * origin (0, 0) will be masked.
	 *  
	 * @param size size of the quadratic array
	 * @param r radius of sector mask
	 * @return quadratic boolean array
	 */
	public static boolean[] createMask(int size, double r){
		boolean[] mask = new boolean[size*size];
		for(int row=0; row<size; row++){
			for(int col=0; col<size; col++){
				int i = row*size+col;
				if( Point.distance(col, row, 0, 0) <= r ||
					Point.distance(col, row, size-1, 0) <= r ||
					Point.distance(col, row, 0, size-1) <= r ||
					Point.distance(col, row, size-1, size-1) <= r )
				{
					mask[i] = false;
				}
				else
					mask[i] = true;
			}
		}
		return mask;
	}

	/**
	 * Handles <code>ActionEvent</code>s from the GUI.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == timer){
			repaint();
		}
		else if(e.getSource() == trafoMode){
			transform();
			updateTrafoDisplay();
			repaint();
		}
		else if(e.getSource() == mask){
			if( ((JCheckBox) e.getSource()).isSelected() ){
				double r = Double.parseDouble(maskRange.getText());
				outValueDisp.setMask(createMask(size, r));
			}
			else{
				outValueDisp.setMask(null);
			}
			repaint();
		}
		else if(e.getSource() == maskRange){
			if( ((JCheckBox) mask).isSelected() ){
				double r = Double.parseDouble(maskRange.getText());
				outValueDisp.setMask(createMask(size, r));
				repaint();
			}
		}
		else if(e.getSource() == log){
			outValueDisp.setLog(log.isSelected());
			updateTrafoDisplay();
		}
		else if(e.getSource() == btnFindSIP){
			findSIP();
		}
	}
	
	protected SIParams estimateSIP(){
		boolean[] searchMask = outValueDisp.getMask();
		
		int iMax = findMaxInFirstHalf(trafo.getAbsArray(), searchMask);	
		
		DoubleFFT_2D transformerFFT = new DoubleFFT_2D(size, size);
		double[] complex = new double[size*size*2];
		double[] inputValues = getInputArray();
		for(int i=0; i<size*size; i++){
			complex[i*2] = inputValues[i];
			complex[i*2+1] = 0.0;
		}
		transformerFFT.complexForward(complex);
		
		SIParams params = trafo.getSIParams(iMax);
		System.out.println(params);
		
		System.out.println("max: "+iMax+" (coords "+(iMax % size)+", "+(iMax / size)+
		") complex: "+trafo.getReal(iMax)+", "+trafo.getImag(iMax)+"i");
		
		return params;
	}
	
	/**
	 * Finds SI-parameters automatically.
	 * So far it just locates the first-order maximum in the spectrum.
	 * It uses the actual trafo- and mask-setting.
	 */
	protected void findSIP(){
		if(trafo.getMode() != Trafo.Mode.FFT) return;
		
		SIParams params = estimateSIP();
		
		// TODO: fit the SI-pattern
		
		angle.setNumber(params.angle);
		phase.setNumber(params.phase);
		wvlen.setNumber(params.wvlen);
		
		// update:
		calculateValues();
	}
	
	/**
	 * Returns the array for the actual shown values.
	 * @return array for the actual shown values
	 */
	protected double[] getInputArray(){
		switch(valueMode){
		case SI:
			return valSIP;
		case IMAGE:
			return valImg;
		case MUL:
			return valMul;
		default:
			return null;
		}
	}
	
	/**
	 * Finds the largest value in the first half of the array
	 * <code>trafos</code>, ignoring the values indicated by the optional
	 * <code>mask</code>.
	 * 
	 * @param trafos where to search for the largest value
	 * @param mask determines which values are valid
	 * @return index of the largest value
	 */
	public static int findMaxInFirstHalf(double[] trafos, boolean[] mask){
		double max = Double.MIN_VALUE;
		int iMax = 0;
		for(int i=0; i<trafos.length/2; i++){
			if(trafos[i] > max)
				if(mask == null || mask[i]){
					max = trafos[i];
					iMax = i;
				}
		}
		return iMax;
	}
	
	/**
	 * Handles <code>NumberEvent</code>s, generated by <code>
	 * NumberField</code>s.  In this case, those events get
	 * created, when the user changes the number by using the GUI.
	 */
	@Override
	public void numberChangedByUserInput(NumberEvent e){
		calculateValues();
	}

	@Override
	public void mouseDragged(MouseEvent e) {	}

	@Override
	public void mouseMoved(MouseEvent e) {
		if(e.getSource() == outValueDisp || e.getSource() == inValueDisp){
			Point p = outValueDisp.getCoordinatesOf(e.getX(), e.getY());
			String textV = "?", textT = "?";
			if(p != null){
				int index = outValueDisp.getIndexOf(e.getX(), e.getY());
				textV = String.format(locale, "(%d, %d) value: %.3f", p.x, p.y, valSIP[index]);
				textT = String.format(locale, "(%d, %d) trafo: %s", p.x, p.y, trafo.toString(index));
			}
			lblCursorValue.setText(textV);
			lblCursorTrafo.setText(textT);
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {}

	@Override
	public void keyTyped(KeyEvent e) {
		
		switch(e.getKeyChar()){
		case '*':
			size *= 2;
			calculateValues();
			break;
		case '/':
			if(size > MINSIZE){
				size /= 2;
				calculateValues();
			}
			break;
		case '1':
			changeValueMode(ValueMode.SI);
			break;
		case '2':
			changeValueMode(ValueMode.IMAGE);
			break;
		case '3':
			changeValueMode(ValueMode.MUL);
			break;
		}
	}
	
	/**
	 * Change the mode for the value-display (image, SI-Pattern, or
	 * multiplication results).
	 * @param newMode new mode for the value display
	 */
	public void changeValueMode(ValueMode newMode){
		if(newMode == ValueMode.IMAGE && valImg == null) return;
		if(newMode == ValueMode.MUL && valImg == null) return;
		valueMode = newMode;
		updateValueDisplay();
		transform();
		updateTrafoDisplay();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getSource() == inValueDisp || e.getSource() == outValueDisp){
			requestFocus();
		}
		if(e.getSource() == inValueDisp && e.getButton()>1){
			switch(valueMode){
			case IMAGE:
				changeValueMode(ValueMode.SI);
				break;
			case SI:
				changeValueMode(ValueMode.MUL);
				break;
			case MUL:
				changeValueMode(ValueMode.IMAGE);
				break;
			}
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}

	@Override
	public void mousePressed(MouseEvent e) {}

	@Override
	public void mouseReleased(MouseEvent e) {}

	@Override
	public void mouseWheelMoved(MouseWheelEvent me) {
		if(me.getSource() == sipPanel)
			getFocusOwner().dispatchEvent(me);
	}

}
