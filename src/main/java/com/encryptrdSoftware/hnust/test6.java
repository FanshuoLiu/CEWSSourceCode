package com.encryptrdSoftware.hnust;

import com.encryptrdSoftware.hnust.model.Domain;
import com.encryptrdSoftware.hnust.model.Point;
import com.encryptrdSoftware.hnust.util.SecretUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class test6 {
    public static double axisAngle;
    public static Point polarPoint;
    public static Point polarAxisPoint;
    public static void main(String[] args) {
      List<Double> radius = new ArrayList<>();
        radius.add(0.060029803879192344);
        radius.add(0.25498353410024804);
        radius.add(0.024583671111727674);
        radius.add(1.1411368532868649);
        radius.add(1.8500595086361582);
        radius.add(0.8752908575308799);
        radius.add(1.4247059154265822);
        radius.add(1.1765829860543295);
        radius.add(0.29042966686771277);

        List<Double> angle = new ArrayList<>();
        angle.add(56.5467900399156);
        angle.add(301.5467900399156);
        angle.add(128.546790039915606);
        angle.add(206.5467900399156);
        angle.add(69.5467900399156);
        angle.add(318.5467900399156);
        angle.add(52.5467900399156);
        angle.add(171.5467900399156);
        angle.add(73.5467900399156);

        for (int i = 0; i < radius.size(); i++){
            System.out.println(i+1);
            System.out.println((radius.get(i)*Math.cos(Math.toRadians(68.19713373055026+angle.get(i))))+120.2534612);
            System.out.println((radius.get(i)*Math.sin(Math.toRadians(68.19713373055026+angle.get(i))))+29.0868624);
            System.out.println();
        }
    }

}
