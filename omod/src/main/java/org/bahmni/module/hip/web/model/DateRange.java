package org.bahmni.module.hip.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@AllArgsConstructor
@Getter
public class DateRange {
    Date from;
    Date to;
}
