package cz.kubaspatny.opendays.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cz.kubaspatny.opendays.DateTimeSerializerTest;
import cz.kubaspatny.opendays.FetcherTest;
import cz.kubaspatny.opendays.TokenTest;

/**
 * Runs all unit tests in the project.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TokenTest.class, DateTimeSerializerTest.class, FetcherTest.class})
public class UnitTestSuite {
}
