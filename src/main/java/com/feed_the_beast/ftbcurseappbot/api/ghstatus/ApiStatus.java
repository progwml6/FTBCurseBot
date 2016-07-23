package com.feed_the_beast.ftbcurseappbot.api.ghstatus;

import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.Objects;

@ToString
public class ApiStatus {
    @Getter
    private String status;
    @Getter
    private Date last_updated;

    @Override
    public boolean equals (Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        return Objects.equals(this.status, ((ApiStatus) obj).status);//we don't care about the dates when the messages haven't changed
    }

    @Override
    public int hashCode () {
        return Objects.hash(this.status);//should this include the date?
    }

}
