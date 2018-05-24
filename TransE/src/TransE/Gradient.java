package TransE;

import static TransE.GlobalValue.*;
import static TransE.GlobalValue.L1_flag;
import static TransE.GlobalValue.learning_rate;
import static TransE.Utils.abs;
import static TransE.Utils.sqr;

public class Gradient {

    static double calc_sum(int e1, int e2, int rel) {
        double sum = 0;
        if (L1_flag) {
            for (int i = 0; i < vector_len; i++) {
                sum += abs(entity_vec[e2][i] - entity_vec[e1][i] - relation_vec[rel][i]);
            }
        } else {
            for (int i = 0; i < vector_len; i++) {
                sum += sqr(entity_vec[e2][i] - entity_vec[e1][i] - relation_vec[rel][i]);
            }
        }
        return sum;
    }

    static double train_kb(int head_a, int tail_a, int relation_a, int head_b, int tail_b, int relation_b, double res) {
        double sum1 = calc_sum(head_a, tail_a, relation_a);
        double sum2 = calc_sum(head_b, tail_b, relation_b);
        if (sum1 + margin > sum2) {
            res += margin + sum1 - sum2;
            gradient(head_a, tail_a, relation_a, head_b, tail_b, relation_b);
            return res;
        }
        return 0;
    }

    static void gradient(int head_a, int tail_a, int relation_a, int head_b, int tail_b, int relation_b) {
        for (int i = 0; i < vector_len; i++) {
            double delta1 = entity_vec[tail_a][i] - entity_vec[head_a][i] - relation_vec[relation_a][i];
            double delta2 = entity_vec[tail_b][i] - entity_vec[head_b][i] - relation_vec[relation_b][i];
            double x;
            if (L1_flag) {
                if (delta1 > 0) {
                    x = 1;
                } else {
                    x = -1;
                }
                relation_vec[relation_a][i] += x * learning_rate;
                entity_vec[head_a][i] += x * learning_rate;
                entity_vec[tail_a][i] -= x * learning_rate;

                if (delta2 > 0) {
                    x = 1;
                } else {
                    x = -1;
                }
                relation_vec[relation_b][i] -= x * learning_rate;
                entity_vec[head_b][i] -= x * learning_rate;
                entity_vec[tail_b][i] += x * learning_rate;
            } else {
                delta1 = abs(delta1);
                delta2 = abs(delta2);
                relation_vec[relation_a][i] += learning_rate * 2 * delta1;
                entity_vec[head_a][i] += learning_rate * 2 * delta1;
                entity_vec[tail_a][i] -= learning_rate * 2 * delta1;

                relation_vec[relation_b][i] -= learning_rate * 2 * delta2;
                entity_vec[head_b][i] -= learning_rate * 2 * delta2;
                entity_vec[tail_b][i] += learning_rate * 2 * delta2;
            }
        }
    }
}
