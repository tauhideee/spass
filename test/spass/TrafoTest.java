package spass;

import static org.junit.Assert.*;

import org.junit.Test;

public class TrafoTest {
	
	@Test
	public void testGetSIParams(){
		Trafo trafo = new Trafo();
		int size = 32;
		double angle = 0.0;
		double phase = 0.0;
		double wvlen = 8.0;
		double[] values = Spass.createSIPattern(size, angle, phase, wvlen);
		trafo.transform(values, size, Trafo.Mode.FFT);
		
		assertEquals("trafo 4 re", 0, (int)trafo.getReal(4));
		assertEquals("trafo -4 re", 0, (int)trafo.getReal(size-4));
		assertEquals("trafo 4 im", -256, (int)trafo.getImag(4));
		assertEquals("trafo -4 im", 256, (int)trafo.getImag(size-4));
		
		SIParams params = trafo.getSIParams(4);
		assertEquals("SIP angle from trafo", angle, params.getAngle(), 0.001);
		assertEquals("SIP phase from trafo", phase, params.getPhase(), 0.001);
		assertEquals("SIP wvlen from trafo", wvlen, params.getWvlen(), 0.001);
	}
}
