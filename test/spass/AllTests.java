package spass;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
	TrafoTest.class,
	SpassTest.class,
	ValueDisplayTest.class
})

public class AllTests {

}
