package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Multiclass Classification
 */
public class App {
    private static class Metrics {
        float crossEntropy;
        int[][] confusionMatrix = new int[5][5];
    }

    private static Metrics evaluateModel(String filePath) {
        try {
            FileReader filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            List<String[]> allData = csvReader.readAll();
            csvReader.close();

            Metrics metrics = new Metrics();
            int count = 0;

            for (String[] row : allData) {
                int yTrue = Integer.parseInt(row[0]);
                float[] yPredicted = new float[5];
                int predictedClass = 1;
                float maxProbability = -1.0f;

                for (int i = 0; i < 5; i++) {
                    yPredicted[i] = Float.parseFloat(row[i + 1]);
                    if (yPredicted[i] > maxProbability) {
                        maxProbability = yPredicted[i];
                        predictedClass = i + 1;
                    }
                }

                metrics.crossEntropy -= (float) Math.log(yPredicted[yTrue - 1]);
                metrics.confusionMatrix[predictedClass - 1][yTrue - 1]++;
                count++;
            }

            if (count > 0) {
                metrics.crossEntropy /= count;
            }

            return metrics;
        } catch (Exception e) {
            System.out.println("Error reading the CSV file: " + filePath);
            return null;
        }
    }

    public static void main(String[] args) {
        Metrics metrics = evaluateModel("model.csv");
        if (metrics == null) {
            return;
        }

        System.out.printf("CE =%.7f%n", metrics.crossEntropy);
        System.out.println("Confusion matrix");
        System.out.printf("%8s%8s%8s%8s%8s%8s%n", "", "y=1", "y=2", "y=3", "y=4", "y=5");

        for (int i = 0; i < 5; i++) {
            System.out.printf("%8s%8d%8d%8d%8d%8d%n",
                    "y^=" + (i + 1),
                    metrics.confusionMatrix[i][0],
                    metrics.confusionMatrix[i][1],
                    metrics.confusionMatrix[i][2],
                    metrics.confusionMatrix[i][3],
                    metrics.confusionMatrix[i][4]);
        }
    }
}
