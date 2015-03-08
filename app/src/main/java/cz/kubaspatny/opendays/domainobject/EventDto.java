package cz.kubaspatny.opendays.domainobject;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by Kuba on 8/3/2015.
 */
public class EventDto extends BaseDto {

    private String name;
    private DateTime date;
    private String information;
    private UserDto organizer;
    private List<RouteDto> routes;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getDate() {
        return date;
    }

    public void setDate(DateTime date) {
        this.date = date;
    }

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
    }

    public UserDto getOrganizer() {
        return organizer;
    }

    public void setOrganizer(UserDto organizer) {
        this.organizer = organizer;
    }

    public List<RouteDto> getRoutes() {
        return routes;
    }

    public void setRoutes(List<RouteDto> routes) {
        this.routes = routes;
    }
}
