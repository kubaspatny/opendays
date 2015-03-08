package cz.kubaspatny.opendays.domainobject;

import com.google.gson.annotations.Expose;

import org.joda.time.DateTime;

/**
 * Created by Kuba on 8/3/2015.
 */
public class LocationUpdateDto extends BaseDto {

    public enum LocationUpdateType {
        CHECKIN, CHECKOUT, SKIP;
    }

    private DateTime timestamp;
    private LocationUpdateType type;
    private StationDto station;
    private GroupDto group;

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocationUpdateType getType() {
        return type;
    }

    public void setType(LocationUpdateType type) {
        this.type = type;
    }

    public StationDto getStation() {
        return station;
    }

    public void setStation(StationDto station) {
        this.station = station;
    }

    public GroupDto getGroup() {
        return group;
    }

    public void setGroup(GroupDto group) {
        this.group = group;
    }
}
