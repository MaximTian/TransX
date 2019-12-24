package TransR;

import java.util.Random;

import static TransR.GlobalValue.*;

public class Utils {
    static Random random = new Random();
    static double PI = Math.PI;

    static double sqrt(double x) {
        return Math.sqrt(x);
    }

    static double sqr(double x) {
        return x * x;
    }

    static double abs(double x) {
        return Math.abs(x);
    }

    static double exp(double x) {
        return Math.exp(x);
    }

    static double normal(double x) {
        // Standard Gaussian distribution
        return exp(-0.5 * sqr(x)) / sqrt(2 * PI);
    }

    static int rand() {
        return random.nextInt(32767);
    }

    static double uniform(double min, double max) {
        // generate a float number which is in [min, max), refer to the Python uniform
        return min + (max - min) * Math.random();
    }

    static double vec_len(double[] a) {
        // calculate the length of the vector
        double res = 0;
        for (int i = 0; i < relation_dimension; i++) {
            res += sqr(a[i]);
        }
        return sqrt(res);
    }

    static void norm(double[] a) {
        // limit the element a under 1
        double x = vec_len(a);
        if (x > 1) {
            for (int i = 0; i < relation_dimension; i++) {
                a[i] /= x;
            }
        }
    }

    static void norm(double[] a, double[][] Wr) {
        while (true) {
            double sum = 0;
            for (int i = 0; i < entity_dimension; i++) {
                double temp = 0;
                for (int j = 0; j < relation_dimension; j++) {
                    temp += Wr[i][j] * a[j];
                }
                sum += sqr(temp);
            }
            if (sum > 1) {
                for (int i = 0; i < entity_dimension; i++) {
                    double temp = 0;
                    for (int j = 0; j < relation_dimension; j++) {
                        temp += Wr[i][j] * a[j];
                    }
                    temp *= 2;
                    for (int j = 0; j < relation_dimension; j++) {
//                        double copy = Wr[j][i];
                        Wr[i][j] -= learning_rate * temp * a[j];
                        a[j] -= learning_rate * temp * Wr[i][j];
                    }
                }
            } else {
                break;
            }
        }
    }

    static int rand_max(int x) {
        // get a random number between (0, x)
        int res = (rand() * rand()) % x;
        while (res < 0) {
            res += x;
        }
        return res;
    }
}
