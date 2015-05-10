package cz.kubaspatny.opendays.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cz.kubaspatny.opendays.TokenTest;

/**
 * Runs all unit tests in the project.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TokenTest.class})
public class UnitTestSuite {
}
