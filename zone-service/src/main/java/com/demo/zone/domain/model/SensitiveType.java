package com.demo.zone.domain.model;

public enum SensitiveType {
    HOSPITAL(10),
    SCHOOL(8),
    PARK(4),
    NONE(1);

    private final int basePriority;

    SensitiveType(int basePriority) {
        this.basePriority = basePriority;
    }

    public int getBasePriority() { return basePriority; }
}
