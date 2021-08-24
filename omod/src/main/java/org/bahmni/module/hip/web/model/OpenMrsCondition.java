package org.bahmni.module.hip.web.model;

import lombok.Getter;

import java.util.Date;

@Getter
public class OpenMrsCondition {
    private final String name;
    private final String uuid;
    private final Date recordedDate;

    public OpenMrsCondition(String uuid, String name, Date recordedDate) {
        this.uuid = uuid;
        this.name = name;
        this.recordedDate = recordedDate;
    }

    @Override
    public String toString() {
        return "OpenMrsCondition{" +
                "name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", recordedDate=" + recordedDate +
                '}';
    }
}
