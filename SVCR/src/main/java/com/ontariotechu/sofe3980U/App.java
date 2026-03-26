package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Continuous Regression
 */
public class App {
    private static final float EPSILON = 0.001f;

    private static class Metrics {
        float mse;
        float mae;
        float mare;
    }

    private static Metrics evaluateModel(String filePath) {
        FileReader filereader;
        List<String[]> allData;

        try {
            filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            allData = csvReader.readAll();
            csvReader.close();
        } catch (Exception e) {
            System.out.println("Error reading the CSV file: " + filePath);
            return null;
        }

        Metrics metrics = new Metrics();
        int count = 0;

        for (String[] row : allData) {
            float yTrue = Float.parseFloat(row[0]);
            float yPredicted = Float.parseFloat(row[1]);
            float error = yTrue - yPredicted;

            metrics.mse += error * error;
            metrics.mae += Math.abs(error);
            metrics.mare += Math.abs(error) / (Math.abs(yTrue) + EPSILON);
            count++;
        }

        if (count > 0) {
            metrics.mse /= count;
            metrics.mae /= count;
            metrics.mare /= count;
        }

        return metrics;
    }

    public static void main(String[] args) {
        String[] filePaths = { "model_1.csv", "model_2.csv", "model_3.csv" };
        String bestMseModel = "";
        String bestMaeModel = "";
        String bestMareModel = "";
        float bestMse = Float.MAX_VALUE;
        float bestMae = Float.MAX_VALUE;
        float bestMare = Float.MAX_VALUE;

        for (String filePath : filePaths) {
            Metrics metrics = evaluateModel(filePath);
            if (metrics == null) {
                continue;
            }

            System.out.println("for " + filePath);
            System.out.println("\tMSE =" + metrics.mse);
            System.out.println("\tMAE =" + metrics.mae);
            System.out.println("\tMARE =" + metrics.mare);

            if (metrics.mse < bestMse) {
                bestMse = metrics.mse;
                bestMseModel = filePath;
            }
            if (metrics.mae < bestMae) {
                bestMae = metrics.mae;
                bestMaeModel = filePath;
            }
            if (metrics.mare < bestMare) {
                bestMare = metrics.mare;
                bestMareModel = filePath;
            }
        }

        System.out.println("According to MSE, The best model is " + bestMseModel);
        System.out.println("According to MAE, The best model is " + bestMaeModel);
        System.out.println("According to MARE, The best model is " + bestMareModel);
    }
}
