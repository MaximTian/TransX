package TransH;

import static TransH.GlobalValue.*;
import static TransH.Utils.*;

public class Gradient {

    static double calc_sum(int head, int tail, int relation) {
        double Wrh = 0;
        double Wrt = 0;
        for (int i = 0; i < vector_dimension; i++) {
            Wrh += Wr_vec[relation][i] * entity_vec[head][i];
            Wrt += Wr_vec[relation][i] * entity_vec[tail][i];
        }

        double sum = 0, tmp;
        for (int i = 0; i < vector_dimension; i++) {
            tmp = (entity_vec[tail][i] - Wrt * Wr_vec[relation][i])
                    - relation_vec[relation][i]
                    - (entity_vec[head][i] - Wrh * Wr_vec[relation][i]);
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
        double Wrh = 0;
        double Wrt = 0;
        for (int i = 0; i < vector_dimension; i++) {
            Wrh += Wr_vec[relation][i] * entity_vec[head][i];
            Wrt += Wr_vec[relation][i] * entity_vec[tail][i];
        }

        double sum = 0;
        for (int i = 0; i < vector_dimension; i++) {
            double delta = (entity_vec[tail][i] - Wrt * Wr_vec[relation][i])
                    - relation_vec[relation][i]
                    - (entity_vec[head][i] - Wrh * Wr_vec[relation][i]);
            double x = (delta > 0) ? 1 : -1;
            sum += x * Wr_vec[relation][i];
            relation_vec[relation][i] -= beta * learning_rate * x;
            entity_vec[head][i] -= beta * learning_rate * x;
            entity_vec[tail][i] += beta * learning_rate * x;
            Wr_vec[relation][i] += beta * x * learning_rate  * (Wrh - Wrt);
        }
        for (int i = 0; i < vector_dimension; i++) {
            Wr_vec[relation][i] += beta * learning_rate * sum * (entity_vec[head][i] - entity_vec[tail][i]);
        }
        norm(relation_vec[relation]);
        norm(entity_vec[head]);
        norm(entity_vec[tail]);

        norm2one(Wr_vec[relation]);
        norm(relation_vec[relation], Wr_vec[relation]);
    }
}
