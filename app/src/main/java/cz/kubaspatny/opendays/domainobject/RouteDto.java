package cz.kubaspatny.opendays.domainobject;

import org.joda.time.DateTime;
import java.util.List;

/**
 * Created by Kuba on 8/3/2015.
 */
public class RouteDto extends BaseDto {

    private String name;
    private String hexColor;
    private String information;
    private DateTime date;
    private EventDto event;
    private List<StationDto> stations;
    private List<UserDto> stationManagers;
    private List<GroupDto> groups;

    public RouteDto() {
    }

    public RouteDto(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public EventDto getEvent() {
        return event;
    }

    public void setEvent(EventDto event) {
        this.event = event;
    }

    public List<StationDto> getStations() {
        return stations;
    }

    public void setStations(List<StationDto> stations) {
        this.stations = stations;
    }

    public List<UserDto> getStationManagers() {
        return stationManagers;
    }

    public void setStationManagers(List<UserDto> stationManagers) {
        this.stationManagers = stationManagers;
    }

    public List<GroupDto> getGroups() {
        return groups;
    }

    public void setGroups(List<GroupDto> groups) {
        this.groups = groups;
    }
}
