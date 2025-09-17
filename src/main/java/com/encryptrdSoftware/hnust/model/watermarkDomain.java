package com.encryptrdSoftware.hnust.model;

import java.io.Serializable;

public class watermarkDomain extends Domain{
    private double decimalRadius;
    private double decimalAngle;

    public watermarkDomain(double decimalRadius, double decimalAngle) {
        this.decimalRadius = decimalRadius;
        this.decimalAngle = decimalAngle;
    }

    public double getDecimalRadius() {
        return decimalRadius;
    }

    public double getDecimalAngle() {
        return decimalAngle;
    }

    @Override
    public String toString() {
        return "{" +
                "decimalRadius=" + decimalRadius +
                ", decimalAngle=" + decimalAngle +
                '}';
    }
}
