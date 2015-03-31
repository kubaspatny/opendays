package cz.kubaspatny.opendays.domainobject;

import android.content.ContentValues;

import java.util.List;

/**
 * Created by Kuba on 8/3/2015.
 */
public class GroupDto extends BaseDto {

    private RouteDto route;
    private UserDto guide;
    private Integer startingPosition;
    private Integer lastStation;
    private GroupSizeDto latestGroupSize;
    private LocationUpdateDto latestLocationUpdate;
    private List<GroupSizeDto> groupSizes;
    private List<LocationUpdateDto> locationUpdates;
    private boolean active;
    private boolean currentUser = false;

    public GroupDto() {
    }

    public GroupDto(Long id) {
        this.id = id;
    }

    public RouteDto getRoute() {
        return route;
    }

    public void setRoute(RouteDto route) {
        this.route = route;
    }

    public UserDto getGuide() {
        return guide;
    }

    public void setGuide(UserDto guide) {
        this.guide = guide;
    }

    public Integer getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(Integer startingPosition) {
        this.startingPosition = startingPosition;
    }

    public GroupSizeDto getLatestGroupSize() {
        return latestGroupSize;
    }

    public void setLatestGroupSize(GroupSizeDto latestGroupSize) {
        this.latestGroupSize = latestGroupSize;
    }

    public LocationUpdateDto getLatestLocationUpdate() {
        return latestLocationUpdate;
    }

    public void setLatestLocationUpdate(LocationUpdateDto latestLocationUpdate) {
        this.latestLocationUpdate = latestLocationUpdate;
    }

    public List<GroupSizeDto> getGroupSizes() {
        return groupSizes;
    }

    public void setGroupSizes(List<GroupSizeDto> groupSizes) {
        this.groupSizes = groupSizes;
    }

    public List<LocationUpdateDto> getLocationUpdates() {
        return locationUpdates;
    }

    public void setLocationUpdates(List<LocationUpdateDto> locationUpdates) {
        this.locationUpdates = locationUpdates;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setActive(String status){
        setActive(Boolean.parseBoolean(status));
    }

    public boolean isCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(boolean currentUser) {
        this.currentUser = currentUser;
    }

    public void computeLastStation(int numberStations){
        int n = startingPosition - 1;
        if(n <= 0) n = n + numberStations;
        setLastStation(n);
    }

    public Integer getLastStation() {
        return lastStation;
    }

    public void setLastStation(Integer lastStation) {
        this.lastStation = lastStation;
    }

    public boolean isAfterLast(int currentStation, LocationUpdateDto.LocationUpdateType type){
        return getLastStation().equals(currentStation) && (type == LocationUpdateDto.LocationUpdateType.SKIP || type == LocationUpdateDto.LocationUpdateType.CHECKOUT);
    }
}
