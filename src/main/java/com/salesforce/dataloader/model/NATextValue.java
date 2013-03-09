package com.salesforce.dataloader.model;

public class NATextValue {

    private static final NATextValue INSTANCE = new NATextValue();
    private static final String NA_VALUE = "#N/A";

    private NATextValue() {
    }

    public static NATextValue getInstance() {
        return INSTANCE;
    }

    public static boolean isNA(Object obj) {
        return INSTANCE.equals(obj);
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
