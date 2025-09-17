import java.util.ArrayList;
import java.util.List;

public class testcal {

    // 计算多项式的值
    public static double evaluatePolynomial(double[] coefficients, double x) {
        double result = 0;
        for (int i = 0; i < coefficients.length; i++) {
            result += coefficients[i] * Math.pow(x, i);
        }
        return result;
    }

    // 拉格朗日插值法
    public static double lagrangeInterpolation(double[] xValues, double[] yValues, double x) {
        double result = 0;
        int n = xValues.length;
        for (int i = 0; i < n; i++) {
            double li = 1;
            for (int j = 0; j < n; j++) {
                if (i != j) {
                    li *= (x - xValues[j]) / (xValues[i] - xValues[j]);
                }
            }
            result += yValues[i] * li;
        }
        return result;
    }

    public static void main(String[] args) {
        // 多项式系数，例如 3x^2 + 2x + 1
        double[] coefficients = {1, 2, 3};

        // 选择一些 x 值
        double[] xValues = {1, 2, 3, 4, 5};
        int n = xValues.length;
        double[] yValues = new double[n];

        // 根据多项式生成 y 值
        for (int i = 0; i < n; i++) {
            yValues[i] = evaluatePolynomial(coefficients, xValues[i]);
        }

        // 要恢复的 x 值
        double xToInterpolate = 2.5;

        // 使用拉格朗日插值法恢复值
        double interpolatedValue = lagrangeInterpolation(xValues, yValues, xToInterpolate);

        System.out.println("多项式在 x = " + xToInterpolate + " 的精确值: " + evaluatePolynomial(coefficients, xToInterpolate));
        System.out.println("拉格朗日插值法恢复的值: " + interpolatedValue);
    }
}