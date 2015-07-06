package spass;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Locale;

import javax.swing.JTextField;

/**
 * A GUI element for numbers.
 * The mouse wheel is used here in a special way.  If the mouse wheel
 * is rotated and the cursor is behind a valid digit, that digit will
 * be increased or decreased.
 * If the users types 'pi' or an expression in the format of 'pi/n', then
 * that expression will be replaced by the value (pi or pi/n), where n
 * can be any floating point number.
 * 
 * @author Oliver Eickmeyer
 */
@SuppressWarnings("serial")
public class NumberField extends JTextField
implements ActionListener, MouseWheelListener{
	protected int fracDigits;
	protected String format;
	protected double number;
	protected NumberListener numberListener;
	protected Locale locale;
	
	/**
	 * Constructs a new <code>NumberField</code>.
	 *  
	 * @param fractionalDigits number of digits after the decimal point (will
	 * be filled up with zeros)
	 */
	public NumberField(int fractionalDigits){
		super(fractionalDigits + 5);
		locale = Locale.US;
		this.fracDigits = fractionalDigits;
		format = "%." + fracDigits + "f";
		addActionListener(this);
		addMouseWheelListener(this);
	}

	/**
	 * Handles <code>MouseWheelEvent</code>s.  If the cursor (or 'caret')
	 * is behind a valid digit, the latter will be increased or decreased.
	 */
	@Override
	public void mouseWheelMoved(MouseWheelEvent me) {
		String txtSummand = "";
		int cPos = getCaretPosition();
		int invCPos = getText().length() - cPos; // inverse caret position
		for(int i = 0; i < getText().length(); i++){
			if(getText().charAt(i) == '.'){
				txtSummand += '.';
			}
			else{
				if(i == cPos-1){
					txtSummand += '1';
				}
				else{
					txtSummand += '0';
				}
			}
		}
		
		double summand = Double.parseDouble(txtSummand);
		number -= summand * (double) me.getWheelRotation();
		setNumber(number);		
		setCaretPosition(getText().length() - invCPos);
		numberListener.numberChangedByUserInput(new NumberEvent(this));
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		String txt = getText().toLowerCase().trim();
		if(txt.equals("pi")){
			number = Math.PI;
		}
		else if(txt.startsWith("pi/")){
			double f = Double.parseDouble(txt.substring(3));
			number = Math.PI / f;
		}
		else{
			number = Double.parseDouble(txt);
		}
		setNumber(number);
		numberListener.numberChangedByUserInput(new NumberEvent(this));
	}
	
	/**
	 * Sets the number for this NumberField.
	 * @param number floating point number to be set for this NumberField
	 */
	public void setNumber(double number){
		this.number = number;
		setText(String.format(locale, format, number));
	}
	
	/**
	 * Returns the number that is stored in this NumberField.
	 * @return the stored floating point number
	 */
	public double getNumber(){ return number; }
	
	/**
	 * Sets (no add!) a listener for NumberEvents.  
	 * @param numberListener A NumberListener which will receive NumberEvents
	 */
	public void setNumberListener(NumberListener numberListener){
		this.numberListener = numberListener;
	}
	
	public String toString(){
		return String.format(locale, format, number);
	}
}
