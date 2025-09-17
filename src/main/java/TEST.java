import org.gdal.gdal.gdal;
import org.gdal.gdal.Dataset;
import org.gdal.ogr.*;

import java.util.ArrayList;
import java.util.List;

public class TEST {
    static {
        // 初始化 GDAL  
        gdal.AllRegister();
        ogr.RegisterAll();
    }

    // 从 SHP 文件读取线数据  
    public static List<List<double[]>> readLineFromShp(String shpFilePath) {
        List<List<double[]>> lines = new ArrayList<>();

        // 打开 SHP 文件  
        DataSource dataset = ogr.Open(shpFilePath, 0);
        if (dataset == null) {
            System.err.println("无法打开文件: " + shpFilePath);
            return lines;
        }

        // 获取图层  
        Layer layer = dataset.GetLayer(0);
        if (layer == null) {
            System.err.println("无法获取图层");
            return lines;
        }

        // 遍历图层中的要素  
        for (int i = 0; i < layer.GetFeatureCount(); i++) {
            Feature feature = layer.GetFeature(i);
            Geometry geometry = feature.GetGeometryRef();

            // 只处理线几何类型  
            if (geometry.GetGeometryType() == ogr.wkbLineString || geometry.GetGeometryType() == ogr.wkbMultiLineString) {
                List<double[]> line = new ArrayList<>();

                // 提取坐标  
                for (int j = 0; j < geometry.GetPointCount(); j++) {
                    double[] point = new double[2];
                    geometry.GetPoint(j, point);
                    line.add(point);
                }

                lines.add(line);
            }
        }

        // 清理  
        dataset.delete();

        return lines;
    }

    // 计算 RMSE  
    public static double calculateRMSE(List<double[]> originalCoords, List<double[]> encryptedCoords) {
        if (originalCoords.size() != encryptedCoords.size()) {
            throw new IllegalArgumentException("两组坐标的点数量必须相同");
        }

        double sumSquaredErrors = 0.0;

        for (int i = 0; i < originalCoords.size(); i++) {
            double[] original = originalCoords.get(i);
            double[] encrypted = encryptedCoords.get(i);

            // 计算每个坐标的差异  
            double dx = original[0] - encrypted[0];
            double dy = original[1] - encrypted[1];

            // 计算平方误差  
            sumSquaredErrors += (dx * dx + dy * dy);
        }

        // 计算均值  
        double meanSquaredError = sumSquaredErrors / originalCoords.size();

        // 开平方获取 RMSE  
        return Math.sqrt(meanSquaredError);
    }

    public static void main(String[] args) {
        String shpFilePath = "path/to/your/shapefile.shp";  // 替换为你的 shapefile 路径  

        List<List<double[]>> originalLines = readLineFromShp(shpFilePath);

        // 假设加密后的坐标，这里你需要替换为实际数据  
        List<List<double[]>> encryptedLines = new ArrayList<>();

        // 这里用原始坐标加一些小的偏移值作为示例  
        for (List<double[]> line : originalLines) {
            List<double[]> encryptedLine = new ArrayList<>();
            for (double[] point : line) {
                // 简单假设增加一些随机偏移作为加密后的坐标  
                double[] encryptedPoint = new double[]{point[0] + 0.1, point[1] + 0.1}; // 示例偏移  
                encryptedLine.add(encryptedPoint);
            }
            encryptedLines.add(encryptedLine);
        }

        // 计算 RMSE  
        double totalRMSE = 0.0;
        int count = 0;

        for (int i = 0; i < originalLines.size(); i++) {
            List<double[]> originalLine = originalLines.get(i);
            List<double[]> encryptedLine = encryptedLines.get(i);

            totalRMSE += calculateRMSE(originalLine, encryptedLine);
            count++;
        }

        // 计算平均 RMSE  
        if (count > 0) {
            totalRMSE /= count;
        }

        System.out.println("平均 RMSE: " + totalRMSE);
    }
}