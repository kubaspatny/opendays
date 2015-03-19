package cz.kubaspatny.opendays.domainobject;

import java.util.List;

/**
 * Created by Kuba on 15/3/2015.
 */
public class StationWrapper {

    public StationDto station;
    public List<GroupDto> groups;

    public StationWrapper() {
    }

    public StationWrapper(StationDto station, List<GroupDto> groups) {
        this.station = station;
        this.groups = groups;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StationWrapper that = (StationWrapper) o;

        if (station != null ? !station.equals(that.station) : that.station != null) return false;

        return true;
    }

}
