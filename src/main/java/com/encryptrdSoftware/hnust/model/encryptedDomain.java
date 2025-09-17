package com.encryptrdSoftware.hnust.model;

import java.io.Serializable;

public class encryptedDomain extends Domain implements Comparable{
    private int integerRadius;
    private int integerAngle;

    public encryptedDomain(int integerRadius, int integerAngle) {
        this.integerRadius = integerRadius;
        this.integerAngle = integerAngle;
    }

    public int getIntegerRadius() {
        return integerRadius;
    }

    public int getIntegerAngle() {
        return integerAngle;
    }

    @Override
    public String toString() {
        return "{" +
                "integerRadius=" + integerRadius +
                ", integerAngle=" + integerAngle +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        encryptedDomain e = (encryptedDomain) o;
        if (this.integerRadius < e.integerRadius) {
            return -1;
        } else if (this.integerRadius > e.integerRadius) {
            return 1;
        } else {
            // 如果num1相等，则比较num2
            return Integer.compare(this.integerAngle, e.integerAngle);
        }
    }
}
