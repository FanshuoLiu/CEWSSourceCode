package com.encryptrdSoftware.hnust;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.SimplexOptimizer;

public class EquationSolver {
    public static void main(String[] args) {
        // 已知的x, x1, y, y1, axisangle
        double x = 1.0;
        double x1 = 1;
        double y = 1;
        double y1 = 1.0;
        double axisangle = Math.PI / 2;

        // 初始猜测值
        double[] initialGuess = {0, 1};

        // 定义方程
        MultivariateFunction equations = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double a = point[0];
                double r = point[1];
                double eq1 = x - x1 - r * Math.cos(a + axisangle);
                double eq2 = y - y1 - r * Math.sin(a + axisangle);
                return eq1 * eq1 + eq2 * eq2; // 最小化方程的平方和
            }
        };

        // 创建优化器
        SimplexOptimizer optimizer = new SimplexOptimizer(1e-10, 1e-10);
        NelderMeadSimplex simplex = new NelderMeadSimplex(2);

        // 进行优化
        PointValuePair result = optimizer.optimize(
                new MaxEval(1000),
                new ObjectiveFunction(equations),
                GoalType.MINIMIZE,
                new InitialGuess(initialGuess),
                simplex
        );

        double[] solution = result.getPoint();
        double a = solution[0];
        double r = solution[1];

        System.out.println("a = " + a);
        System.out.println("r = " + r);
    }
}