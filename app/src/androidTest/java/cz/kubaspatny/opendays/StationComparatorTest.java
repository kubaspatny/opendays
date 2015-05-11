package cz.kubaspatny.opendays;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;

import cz.kubaspatny.opendays.domainobject.StationDto;
import cz.kubaspatny.opendays.util.StationComparator;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class StationComparatorTest {

    @Test
    public void testCompare() throws Exception {

        StationComparator stationComparator = new StationComparator(1,2);

        StationDto s1 = new StationDto();
        s1.setSequencePosition(1);

        StationDto s2 = new StationDto();
        s2.setSequencePosition(2);

        Assert.assertEquals(0, stationComparator.compare(s1, s1));
        Assert.assertEquals(-1, stationComparator.compare(s1, s2));
        Assert.assertEquals(1, stationComparator.compare(s2, s1));
        Assert.assertEquals(0, stationComparator.compare(s2, s2));

    }

    @Test
    public void testSort() throws Exception {

        ArrayList<StationDto> stationDtos = new ArrayList<>();
        for(int i = 1; i <= 10; i++){
            StationDto s = new StationDto();
            s.setSequencePosition(i);
            stationDtos.add(s);
        }

        Collections.shuffle(stationDtos);
        Collections.sort(stationDtos, new StationComparator(1, 10));

        for(int i = 1; i <= 10; i++){
            StationDto s = stationDtos.get(i - 1);
            Assert.assertEquals(i, s.getSequencePosition());
        }

    }
}
