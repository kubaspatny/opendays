package cz.kubaspatny.opendays.domainobject;

import java.util.List;

/**
 * Created by Kuba on 8/3/2015.
 */
public class StationDto extends BaseDto {

    private String name;
    private String location;
    private String information;
    private boolean closed;
    private int timeLimit;
    private int relocationTime;
    private int sequencePosition;

    private RouteDto route;
    private List<LocationUpdateDto> locationUpdates;

    public StationDto() {
    }

    public StationDto(Long id) {
        this.id = id;
    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setClosed(String status){
        setClosed(Boolean.parseBoolean(status));
    }

    public int getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    public int getRelocationTime() {
        return relocationTime;
    }

    public void setRelocationTime(int relocationTime) {
        this.relocationTime = relocationTime;
    }

    public int getSequencePosition() {
        return sequencePosition;
    }

    public void setSequencePosition(int sequencePosition) {
        this.sequencePosition = sequencePosition;
    }

    public RouteDto getRoute() {
        return route;
    }

    public void setRoute(RouteDto route) {
        this.route = route;
    }

    public List<LocationUpdateDto> getLocationUpdates() {
        return locationUpdates;
    }

    public void setLocationUpdates(List<LocationUpdateDto> locationUpdates) {
        this.locationUpdates = locationUpdates;
    }
}
