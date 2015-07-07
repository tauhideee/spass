package spass;

import static org.junit.Assert.*;

import org.jtransforms.fft.DoubleFFT_2D;
import org.junit.Test;

import spass.Spass;

public class SpassTest {
	
	@Test
	public void testFindMaxInFirstHalf(){
		int size=4;
		double[] values = new double[size*size];
		
		// A) easy test w/o mask
		
		for(int i = 0; i < size*size; i++) values[i] = i;
		int iTest = size - 2;
		values[iTest] = 10000.0;
		int iMaxA = Spass.findMaxInFirstHalf(values, null);
		assertEquals("findMax", iTest, iMaxA);
		
		// B) easy test with mask
		
		boolean [] mask = new boolean[size*size];
		for(int i = 0; i < size*size; i++) mask[i] = true;
		mask[iTest] = false;
		int iMaxB = Spass.findMaxInFirstHalf(values, mask);
		assertEquals("findMax masked", values.length/2-1, iMaxB);
	}
	
	@Test
	public void testCreateMask(){
		int size = 256;
		double r = 5.0;
		boolean[] mask = Spass.createMask(size, r);
		
		// test edge points
		
		assertEquals("mask (0, 5)", false, mask[5]);
		assertEquals("mask (0, 6)", true, mask[6]);
		assertEquals("mask (5, 0)", false, mask[size*5]);
		assertEquals("mask (6, 0)", true, mask[size*6]);
		
		// test boundary between radius 5 and 6
		
		int col = (int) (5.0 / Math.sqrt(2.0));
		int row = col;
		int i5_45 = row*size+col;
		assertEquals("mask ("+col+", "+row+")", false, mask[i5_45]);
		col = (int) (6.0 / Math.sqrt(2.0));
		row = col;
		int i6_45 = row*size+col;
		assertEquals("mask ("+col+", "+row+")", true, mask[i6_45]);
	}
	
	@Test
	public void testCalcSIPFromTrafo(){
		int size = 32;
		double angle = 0.0;
		double phase = 0.0;
		double wvlen = 8.0;
		double[] values = Spass.createSIPattern(size, angle, phase, wvlen);
		DoubleFFT_2D transformerFFT = new DoubleFFT_2D(size, size);
		double[] complex = new double[size*size*2];
		for(int i=0; i<values.length; i++){
			complex[i*2] = values[i];
			complex[i*2+1] = 0.0;
		}
		transformerFFT.complexForward(complex);
		assertEquals("trafo 4 re", 0, (int)complex[4*2]);
		assertEquals("trafo 4 re", 0, (int)complex[size*2-4*2]);
		assertEquals("trafo 4 im", -256, (int)complex[4*2+1]);
		assertEquals("trafo 4 im", 256, (int)complex[size*2-4*2+1]);
		
		SIParams params = Spass.calcSIPFromTrafo(complex, size, 4);
		assertEquals("SIP angle from trafo", angle, params.getAngle(), 0.001);
		assertEquals("SIP phase from trafo", phase, params.getPhase(), 0.001);
		assertEquals("SIP wvlen from trafo", wvlen, params.getWvlen(), 0.001);
	}

}
