package cz.kubaspatny.opendays.domainobject;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;

import org.joda.time.DateTime;

/**
 * Created by Kuba on 8/3/2015.
 */
public class LocationUpdateDto extends BaseDto {

    public enum LocationUpdateType {
        CHECKIN, CHECKOUT, SKIP, EMPTY;

        public static LocationUpdateType parseLocationUpdateType(String type){
            if(type == null || TextUtils.isEmpty(type)) return null;

            if(type.toUpperCase().equals(CHECKIN.name())){
                return CHECKIN;
            } else if(type.toUpperCase().equals(CHECKOUT.name())){
                return CHECKOUT;
            } else if(type.toUpperCase().equals(SKIP.name())){
                return SKIP;
            }

            return null;
        }

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

    public void setType(String type){

        if("CHECKIN".equals(type)){
            setType(LocationUpdateType.CHECKIN);
        } else if("CHECKOUT".equals(type)){
            setType(LocationUpdateType.CHECKOUT);
        } else if("SKIP".equals(type)){
            setType(LocationUpdateType.SKIP);
        }

    }

    /**
     * Acts a NULL OBJECT pattern.
     */
    public boolean isEmpty(){
        return type == LocationUpdateType.EMPTY;
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
