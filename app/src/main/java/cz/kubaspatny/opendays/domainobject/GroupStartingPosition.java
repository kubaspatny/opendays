package cz.kubaspatny.opendays.domainobject;

/**
 * Created by Kuba on 28/3/2015.
 */
public class GroupStartingPosition {

    public Long groupId;
    public Integer startingPosition;

    public GroupStartingPosition() {
    }

    public GroupStartingPosition(Long groupId, Integer startingPosition) {
        this.groupId = groupId;
        this.startingPosition = startingPosition;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public Integer getStartingPosition() {
        return startingPosition;
    }

    public void setStartingPosition(Integer startingPosition) {
        this.startingPosition = startingPosition;
    }

}
