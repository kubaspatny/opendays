package cz.kubaspatny.opendays.domainobject;

/**
 * Created by Kuba on 8/3/2015.
 */
public abstract class BaseDto {

    public Long id;
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

}
