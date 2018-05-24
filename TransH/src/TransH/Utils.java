package TransH;

import java.util.Random;

import static TransH.GlobalValue.*;

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
        for (int i = 0; i < vector_dimension; i++) {
            res += sqr(a[i]);
        }
        return sqrt(res);
    }

    static void norm(double[] a) {
        // limit the element a under 1
        double x = vec_len(a);
        if (x > 1) {
            for (int i = 0; i < vector_dimension; i++) {
                a[i] /= x;
            }
        }
    }

    static void norm2one(double[] a) {
        // limit the element equal 1
        double x = vec_len(a);
        for (int i = 0; i < vector_dimension; i++) {
            a[i] /= x;
        }
    }

    static void norm(double[] a, double[] Wr) {
        double sum = 0;
        while (true) {
            for (int i = 0; i < vector_dimension; i++) {
                sum += sqr(Wr[i]);
            }
            sum = sqrt(sum);
            for (int i = 0; i < vector_dimension; i++) {
                Wr[i] /= sum;
            }

            double x = 0;
            for (int i = 0; i < vector_dimension; i++) {
                x += Wr[i] * a[i];
            }
            if (x > 0.1) {
                for (int i = 0; i < vector_dimension; i++) {
                    double tmp = a[i];
                    a[i] -= learning_rate * Wr[i];
                    Wr[i] -= learning_rate * tmp;
                }
            } else {
                break;
            }
        }
        norm2one(Wr);
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
