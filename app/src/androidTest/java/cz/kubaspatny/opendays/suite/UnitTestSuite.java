package cz.kubaspatny.opendays.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import cz.kubaspatny.opendays.ConnectivityTest;
import cz.kubaspatny.opendays.DateTimeSerializerTest;
import cz.kubaspatny.opendays.FetcherTest;
import cz.kubaspatny.opendays.GCMTest;
import cz.kubaspatny.opendays.StationComparatorTest;
import cz.kubaspatny.opendays.TokenTest;

/**
 * Runs all unit tests in the project.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({TokenTest.class,
        DateTimeSerializerTest.class,
        FetcherTest.class,
        ConnectivityTest.class,
        StationComparatorTest.class,
        GCMTest.class})
public class UnitTestSuite {
}
