package com.ontariotechu.sofe3980U;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Binary Regression
 */
public class App {
    private static final double THRESHOLD = 0.5;
    private static final double EPSILON = 0.000001;

    private static class Record {
        int yTrue;
        double yPredicted;

        Record(int yTrue, double yPredicted) {
            this.yTrue = yTrue;
            this.yPredicted = yPredicted;
        }
    }

    private static class Metrics {
        double bce;
        int tp;
        int fp;
        int tn;
        int fn;
        double accuracy;
        double precision;
        double recall;
        double f1Score;
        double aucRoc;
    }

    private static List<Record> readModel(String filePath) {
        try {
            FileReader filereader = new FileReader(filePath);
            CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build();
            List<String[]> allData = csvReader.readAll();
            csvReader.close();

            List<Record> records = new ArrayList<Record>();
            for (String[] row : allData) {
                records.add(new Record(Integer.parseInt(row[0]), Double.parseDouble(row[1])));
            }
            return records;
        } catch (Exception e) {
            System.out.println("Error reading the CSV file: " + filePath);
            return null;
        }
    }

    private static Metrics evaluateModel(String filePath) {
        List<Record> records = readModel(filePath);
        if (records == null || records.isEmpty()) {
            return null;
        }

        Metrics metrics = new Metrics();
        int total = records.size();
        int positiveCount = 0;
        int negativeCount = 0;

        for (Record record : records) {
            double prediction = Math.min(Math.max(record.yPredicted, EPSILON), 1.0 - EPSILON);

            if (record.yTrue == 1) {
                metrics.bce -= Math.log(prediction);
                positiveCount++;
            } else {
                metrics.bce -= Math.log(1.0 - prediction);
                negativeCount++;
            }

            int yBinary = record.yPredicted >= THRESHOLD ? 1 : 0;
            if (yBinary == 1 && record.yTrue == 1) {
                metrics.tp++;
            } else if (yBinary == 1 && record.yTrue == 0) {
                metrics.fp++;
            } else if (yBinary == 0 && record.yTrue == 0) {
                metrics.tn++;
            } else {
                metrics.fn++;
            }
        }

        metrics.bce /= total;
        metrics.accuracy = (double) (metrics.tp + metrics.tn) / total;
        metrics.precision = metrics.tp == 0 ? 0.0 : (double) metrics.tp / (metrics.tp + metrics.fp);
        metrics.recall = metrics.tp == 0 ? 0.0 : (double) metrics.tp / (metrics.tp + metrics.fn);
        if (metrics.precision + metrics.recall == 0.0) {
            metrics.f1Score = 0.0;
        } else {
            metrics.f1Score = 2.0 * metrics.precision * metrics.recall / (metrics.precision + metrics.recall);
        }

        double[] x = new double[101];
        double[] y = new double[101];
        for (int i = 0; i <= 100; i++) {
            double threshold = i / 100.0;
            int tp = 0;
            int fp = 0;

            for (Record record : records) {
                if (record.yPredicted >= threshold) {
                    if (record.yTrue == 1) {
                        tp++;
                    } else {
                        fp++;
                    }
                }
            }

            y[i] = positiveCount == 0 ? 0.0 : (double) tp / positiveCount;
            x[i] = negativeCount == 0 ? 0.0 : (double) fp / negativeCount;
        }

        for (int i = 1; i <= 100; i++) {
            metrics.aucRoc += (y[i - 1] + y[i]) * Math.abs(x[i - 1] - x[i]) / 2.0;
        }

        return metrics;
    }

    public static void main(String[] args) {
        String[] filePaths = { "model_1.csv", "model_2.csv", "model_3.csv" };

        String bestBceModel = "";
        String bestAccuracyModel = "";
        String bestPrecisionModel = "";
        String bestRecallModel = "";
        String bestF1Model = "";
        String bestAucRocModel = "";

        double bestBce = Double.MAX_VALUE;
        double bestAccuracy = -1.0;
        double bestPrecision = -1.0;
        double bestRecall = -1.0;
        double bestF1 = -1.0;
        double bestAucRoc = -1.0;

        for (String filePath : filePaths) {
            Metrics metrics = evaluateModel(filePath);
            if (metrics == null) {
                continue;
            }

            System.out.println("for " + filePath);
            System.out.printf("\tBCE =%.7f%n", metrics.bce);
            System.out.println("\tConfusion matrix");
            System.out.println("\t\t\ty=1\t\ty=0");
            System.out.println("\t\ty^=1\t" + metrics.tp + "\t" + metrics.fp);
            System.out.println("\t\ty^=0\t" + metrics.fn + "\t" + metrics.tn);
            System.out.printf("\tAccuracy =%.4f%n", metrics.accuracy);
            System.out.printf("\tPrecision =%.7f%n", metrics.precision);
            System.out.printf("\tRecall =%.8f%n", metrics.recall);
            System.out.printf("\tf1 score =%.8f%n", metrics.f1Score);
            System.out.printf("\tauc roc =%.8f%n", metrics.aucRoc);

            if (metrics.bce < bestBce) {
                bestBce = metrics.bce;
                bestBceModel = filePath;
            }
            if (metrics.accuracy > bestAccuracy) {
                bestAccuracy = metrics.accuracy;
                bestAccuracyModel = filePath;
            }
            if (metrics.precision > bestPrecision) {
                bestPrecision = metrics.precision;
                bestPrecisionModel = filePath;
            }
            if (metrics.recall > bestRecall) {
                bestRecall = metrics.recall;
                bestRecallModel = filePath;
            }
            if (metrics.f1Score > bestF1) {
                bestF1 = metrics.f1Score;
                bestF1Model = filePath;
            }
            if (metrics.aucRoc > bestAucRoc) {
                bestAucRoc = metrics.aucRoc;
                bestAucRocModel = filePath;
            }
        }

        System.out.println("According to BCE, The best model is " + bestBceModel);
        System.out.println("According to Accuracy, The best model is " + bestAccuracyModel);
        System.out.println("According to Precision, The best model is " + bestPrecisionModel);
        System.out.println("According to Recall, The best model is " + bestRecallModel);
        System.out.println("According to F1 score, The best model is " + bestF1Model);
        System.out.println("According to AUC ROC, The best model is " + bestAucRocModel);
    }
}
