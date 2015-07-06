package spass;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;

import org.junit.Test;

import spass.Spass;
import spass.ValueDisplay;

/**
 * Unit tests for the class <code>ValueDisplay</code>.
 * 
 * @author Oliver Eickmeyer
 *
 */
public class ValueDisplayTest {

	@Test
	public void testDoubleToByteArray(){
		int size = 2;
		double[] input = new double[size*size];
		byte[] output;
		
		// first, test range 0.0 to 1.0:
		
		input[0] = 0.0;
		input[1] = 1.0;
		input[2] = 0.5;
		input[3] = 1.0 / 3.0;
		output = ValueDisplay.doubleToByteArray(input, null, false);
		
		assertEquals("a-outbut byte 0", 0, output[0]);
		assertEquals("a-outbut byte 1", (byte)0xff, output[1]);
		assertEquals("a-outbut byte 2", (byte)0x7f, output[2]);
		assertEquals("a-outbut byte 3", (byte)0x55, output[3]);
		
		
		// second, test range -2.0 to 10.0:
		
		input[0] = -2.0;
		input[1] = 0.0;
		input[2] = 1.0;
		input[3] = 10.0;
		output = ValueDisplay.doubleToByteArray(input, null, false);

		assertEquals("b-outbut byte 0", 0, output[0]);
		assertEquals("b-outbut byte 1", (byte)(255.0*2.0/12.0), output[1]);
		assertEquals("b-outbut byte 2", (byte)(255.0*3.0/12.0), output[2]);
		assertEquals("b-outbut byte 3", (byte)0xff, output[3]);

	}
	
	@Test
	public void testDoubleToByteArrayLog(){
		int size = 2;
		double[] input = new double[size*size];
		byte[] output;
		
		// test A (some easy values)
		
		for(int i=0; i<size*size; i++){
			input[i] = Math.pow(Math.E, i);
		}
		
		output = ValueDisplay.doubleToByteArray(input, null, true);
		
		double min = input[0];
		double max = input[3];
		double maxLog = Math.log(max-min+Math.E)-1;
		double scale = 255.0 / maxLog;
		byte v0 = 0;
		byte v1 = (byte) (scale * (Math.log(input[1]-min+Math.E)-1));
		byte v2 = (byte) (scale * (Math.log(input[2]-min+Math.E)-1));
		byte v3 = (byte) 255;
		
		assertEquals("(A) output byte 0", v0, output[0]);
		assertEquals("(A) output byte 1", v1, output[1]);
		assertEquals("(A) output byte 2", v2, output[2]);
		assertEquals("(A) output byte 3", v3, output[3]);
		
		// test B (some more extreme values, w/o log)
		
		input[0] = -3000.0;
		input[1] = 6000.0;
		input[2] = 6000.0;
		input[3] = -10000.0;
		
		output = ValueDisplay.doubleToByteArray(input, null, false);
		
		scale = 255.0 / 16000.0;
		v0 = (byte) (scale * 7000.0);
		v1 = (byte) (scale * 16000.0);
		v2 = (byte) (scale * 16000.0);
		v3 = (byte) (scale * 0.0);
		
		assertEquals("(B) output byte 0", v0, output[0]);
		assertEquals("(B) output byte 1", v1, output[1]);
		assertEquals("(B) output byte 2", v2, output[2]);
		assertEquals("(B) output byte 3", v3, output[3]);

		// test C (same values as B, but with log)
		
		output = ValueDisplay.doubleToByteArray(input, null, true);
		
		scale = 255.0 / (Math.log(16000.0+Math.E)-1);
		v0 = (byte) (scale * (Math.log(7000.0+Math.E)-1));
		v1 = (byte) (scale * (Math.log(16000.0+Math.E)-1));
		v2 = v1;
		v3 = 0;
		
		assertEquals("(C) output byte 0", v0, output[0]);
		assertEquals("(C) output byte 1", v1, output[1]);
		assertEquals("(C) output byte 2", v2, output[2]);
		assertEquals("(C) output byte 3", v3, output[3]);
		
		// test D
		
		input[0] = 32765.025;
		input[1] = 10000.0;
		input[2] = 1.211;
		input[3] = 1.198;
		
		output = ValueDisplay.doubleToByteArray(input, null, true);
		
		min = input[3];
		max = input[0];
		scale = 255.0 / (Math.log(max-min+Math.E)-1);
		v0 = (byte) (scale * (Math.log(input[0]-min+Math.E)-1));
		v1 = (byte) (scale * (Math.log(input[1]-min+Math.E)-1));
		v2 = (byte) (scale * (Math.log(input[2]-min+Math.E)-1));
		v3 = (byte) (scale * (Math.log(input[3]-min+Math.E)-1));
		
		assertEquals("(D) output byte 0", v0, output[0]);
		assertEquals("(D) output byte 1", v1, output[1]);
		assertEquals("(D) output byte 2", v2, output[2]);
		assertEquals("(D) output byte 3", v3, output[3]);
	}
	
	@Test
	public void testDoubleToImage(){
		int size = 2;
		double[] input = new double[size*size];
		input[0] = 0.0;
		input[1] = 1.0;
		input[2] = 0.5;
		input[3] = 1.0 / 3.0;
		BufferedImage image = ValueDisplay.doubleToImage(size, input, null, false);
		assertEquals("image width", size, image.getWidth());
		assertEquals("image height", size, image.getHeight());
		Raster raster = image.getRaster();
		SampleModel sm = raster.getSampleModel();
		DataBuffer db = raster.getDataBuffer();
		byte[] bytes = (byte[]) sm.getDataElements(0, 0, size, size, null, db);
		assertEquals("image byte 0", 0, bytes[0]);
		assertEquals("image byte 1", (byte)0xff, bytes[1]);
		assertEquals("image byte 2", (byte)0x7f, bytes[2]);
		assertEquals("image byte 3", (byte)0x55, bytes[3]);
	}
	
	@Test
	public void testCreateSIPattern(){
		double angle = 0.0;
		double phase = 0.0;
		double wavelength = 4.0;
		int size = 4;
		double[] pattern = Spass.createSIPattern(size, angle, phase, wavelength);
		assertEquals("pattern length", size*size, pattern.length);
	}
	
	@Test
	public void testGetIndexOf(){
		int size = 8;
		double[] values = new double[size*size];
		ValueDisplay display = new ValueDisplay(8, values);
		display.setBorderWidth(1);
		display.setSize(100, 100);
		assertEquals("width", 100, display.getWidth());
		assertEquals("height", 100, display.getHeight());
		display.updateZoom();
		assertEquals("zoom", 12, display.getZoom());
		assertEquals("index of 0,0", -1, display.getIndexOf(0, 0));
		assertEquals("index of 1,1", 0, display.getIndexOf(1, 1));
		assertEquals("index of 12,12", 0, display.getIndexOf(12, 12));
		assertEquals("index of 13,13", 9, display.getIndexOf(13, 13));
		assertEquals("index of 96, 96", 63, display.getIndexOf(96, 96));
		assertEquals("index of 97, 97", -1, display.getIndexOf(97, 97));
	}
}
