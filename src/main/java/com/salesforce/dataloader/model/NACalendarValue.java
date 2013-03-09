package com.salesforce.dataloader.model;

import java.util.GregorianCalendar;

public class NACalendarValue extends GregorianCalendar {

    private static final NACalendarValue INSTANCE = new NACalendarValue();
    private static final String NA_VALUE = "#N/A";

    private NACalendarValue() {
    }

    public static NACalendarValue getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        return NA_VALUE.equals(obj.toString());
    }

    @Override
    public int hashCode() {
        return NA_VALUE.hashCode();
    }

    @Override
    public String toString() {
        return NA_VALUE;
    }
}
