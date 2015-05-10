package cz.kubaspatny.opendays.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all test in the project (JUnit and Instrumentation).
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({UnitTestSuite.class})
public class AndroidTestSuite {
}
