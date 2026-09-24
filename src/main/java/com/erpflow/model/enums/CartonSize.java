package com.erpflow.model.enums;

public enum CartonSize {

    SMALL(20.0, 15.0, 10.0),
    MEDIUM(30.0, 20.0, 15.0),
    LARGE(40.0, 30.0, 25.0);

    private final double length;
    private final double width;
    private final double height;

    CartonSize(double length, double width, double height) {
        this.length = length;
        this.width = width;
        this.height = height;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public double getVolume() {
        return length * width * height;
    }
}