package com.feed_the_beast.ftbcurseappbot.api.ghstatus;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Date;
import java.util.Objects;

@ToString
public class StatusMessage {
    @Getter
    private String status;
    @Getter
    private String body;
    @Getter
    private Date created_on;

    @Override
    public boolean equals (Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        return Objects.equals(this.status, ((StatusMessage) obj).status) && Objects.equals(this.body, ((StatusMessage) obj).body);//we don't care about the dates when the messages haven't changed
    }

    @Override
    public int hashCode () {
        return Objects.hash(this.status, this.body);//should this include the date?
    }
}
