package cz.kubaspatny.opendays.domainobject;

import org.joda.time.DateTime;

/**
 * Created by Kuba on 8/3/2015.
 */
public class GroupSizeDto extends BaseDto {

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

}
