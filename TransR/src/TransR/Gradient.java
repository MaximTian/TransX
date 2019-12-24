package TransR;

import static TransR.GlobalValue.*;
import static TransR.Utils.*;

public class Gradient {

    static double calc_sum(int head, int tail, int relation) {
        double[] e1_vec = new double[relation_dimension];
        double[] e2_vec = new double[relation_dimension];
        for (int i = 0; i < entity_dimension; i++) {
            e1_vec[i] = 0;
            e2_vec[i] = 0;
            for (int j = 0; j < relation_dimension; j++) {
                e1_vec[i] += Wr_vec[relation][i][j] * entity_vec[head][i];
                e2_vec[i] += Wr_vec[relation][i][j] * entity_vec[tail][i];
            }
        }
        double sum = 0, tmp;
        for (int i = 0; i < entity_dimension; i++) {
            tmp = e2_vec[i] - e1_vec[i] - relation_vec[relation][i];
            sum += abs(tmp);
        }
        return sum;
    }

    static double train_kb(int head_a, int tail_a, int relation_a, int head_b, int tail_b, int relation_b) {
        double sum1 = calc_sum(head_a, tail_a, relation_a);
        double sum2 = calc_sum(head_b, tail_b, relation_b);
        double res = 0;
        if (sum1 + margin > sum2) {
            res = margin + sum1 - sum2;
            gradient(head_a, tail_a, relation_a, -1);
            gradient(head_b, tail_b, relation_b, 1);
        }
        return res;
    }

    private static void gradient(int head, int tail, int relation, double beta) {
        for (int i = 0; i < entity_dimension; i++) {
            double Wrh = 0;
            double Wrt = 0;
            for (int j = 0; j < relation_dimension; j++) {
                Wrh += Wr_vec[relation][i][j] * entity_vec[head][j];
                Wrt += Wr_vec[relation][i][j] * entity_vec[tail][j];
            }
            double x = 2 * (Wrt - Wrh - relation_vec[relation][i]);
            for (int j = 0; j < relation_dimension; j++) {
                Wr_copy[relation][i][j] -= beta * learning_rate * (entity_vec[head][j] - entity_vec[tail][j]);
                entity_copy[head][j] -= beta * learning_rate * Wr_vec[relation][i][j];
                entity_copy[tail][j] += beta * learning_rate * Wr_vec[relation][i][j];
            }
            relation_copy[relation][i] -= beta * learning_rate * x;
        }
    }
}
