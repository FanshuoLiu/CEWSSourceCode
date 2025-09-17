package com.encryptrdSoftware.hnust.model;

import java.util.List;

public class MultiLine implements Shape{
    private List<Line> lines;

    public MultiLine(List<Line> lines) {
        this.lines = lines;
    }
    public List<Line> getLines() {
        return lines;
    }

    public int totalLength() {
        int totalLength = 0;
        for (Line lineString : lines) {
            totalLength += lineString.getLength();
        }
        return totalLength;
    }

    @Override
    public String toString() {
        return "MultiLineString{" +
                "lines=" + lines +
                '}';
    }
}
