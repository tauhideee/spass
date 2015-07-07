package spass;

/**
 * Represents a set of Structured Illumination parameters.
 * 
 * @author Oliver Eickmeyer
 *
 */
public class SIParams {
	protected double angle;
	protected double phase;
	protected double wvlen;
	protected String format;
	
	public SIParams(){
		this(0.0, 0.0, 0.0);
	}
	
	public SIParams(double angle, double phase, double wvlen){
		setAngle(angle);
		setPhase(phase);
		setWvlen(wvlen);
		format = "angle %.4f, phase %.4f, wavelength %.4f";
	}
	
	public double getAngle() {
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
	}

	public double getPhase() {
		return phase;
	}

	public void setPhase(double phase) {
		this.phase = phase;
	}

	public double getWvlen() {
		return wvlen;
	}

	public void setWvlen(double wvlen) {
		this.wvlen = wvlen;
	}
	
	/**
	 * Sets the format for a String representation of this object.
	 * The format must have placeholders for three floating point numbers,
	 * in the following order:
	 * <ul>
	 * <li>angle</li>
	 * <li>phase</li>
	 * <li>wavelength</li>
	 * </ul>
	 * @param format format for String representation
	 * (floating point numbers for angle, phase, wavelength)
	 */
	public void setFormat(String format){
		this.format = format;
	}
	
	public String toString(){
		return String.format(format, angle, phase, wvlen);
	}
}
