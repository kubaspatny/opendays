package cz.kubaspatny.opendays.domainobject;

import org.joda.time.DateTime;

/**
 * Created by Kuba on 8/3/2015.
 */
public class GroupSizeDto extends BaseDto {

    private Long groupId;
    private DateTime timestamp;
    private int size;

    public DateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

}
