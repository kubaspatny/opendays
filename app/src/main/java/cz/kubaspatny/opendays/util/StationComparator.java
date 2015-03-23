package cz.kubaspatny.opendays.util;

import java.util.Comparator;

import cz.kubaspatny.opendays.domainobject.StationDto;

/**
 * Created by Kuba on 23/3/2015.
 */
public class StationComparator implements Comparator<StationDto> {

    private int groupStartingPosition = 1;
    private int stationCount = 0;

    public StationComparator(int groupStartingPosition, int stationCount) {
        this.groupStartingPosition = groupStartingPosition;
        this.stationCount = stationCount;
    }

    @Override
    public int compare(StationDto st1, StationDto st2) {
        Integer st1_index = (st1.getSequencePosition() - groupStartingPosition + stationCount) % stationCount;
        Integer st2_index = (st2.getSequencePosition() - groupStartingPosition + stationCount) % stationCount;

        return st1_index.compareTo(st2_index);
    }

}
