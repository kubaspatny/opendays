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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseDto baseDto = (BaseDto) o;

        if (id != null ? !id.equals(baseDto.id) : baseDto.id != null) return false;

        return true;
    }

}
