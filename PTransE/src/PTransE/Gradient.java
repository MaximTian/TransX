package PTransE;

import java.util.List;

import static PTransE.GlobalValue.*;
import static PTransE.GlobalValue.learning_rate;
import static PTransE.Utils.abs;

public class Gradient {

    static double calc_sum(int e1, int e2, int rel) {
        double sum = 0;
        for (int i = 0; i < vector_len; i++) {
            sum += abs(entity_vec[e2][i] - entity_vec[e1][i] - relation_vec[rel][i]);
        }
        return sum;
    }

    static double train_kb(int head_a, int tail_a, int relation_a, int head_b, int tail_b, int relation_b, double res) {
        double sum1 = calc_sum(head_a, tail_a, relation_a);
        double sum2 = calc_sum(head_b, tail_b, relation_b);
        if (sum1 + margin > sum2) {
            res += margin + sum1 - sum2;
            gradient(head_a, tail_a, relation_a, -1);
            gradient(head_b, tail_b, relation_b, 1);
        }
        return res;
    }

    private static void gradient(int head, int tail, int relation, int beta) {
        for (int i = 0; i < vector_len; i++) {
            double delta = entity_vec[tail][i] - entity_vec[head][i] - relation_vec[relation][i];
            double x = (delta > 0) ? 1 : -1;
            relation_vec[relation][i] -= x * learning_rate * beta;
            entity_vec[head][i] -= x * learning_rate * beta;
            entity_vec[tail][i] += x * learning_rate * beta;
        }
    }

    static double train_path(int relation, int neg_relation, List<Integer> path, double alpha, double loss) {
        double sum1 = calc_path(relation, path);
        double sum2 = calc_path(neg_relation, path);
        if (sum1 + margin_relation > sum2) {
            loss += alpha * (sum1 + margin_relation - sum2);
            gradient_path(relation, path, -1 * alpha);
            gradient_path(neg_relation, path, alpha);
        }
        return loss;
    }

    static private void gradient_path(int relation, List<Integer> path, double beta) {
        /**
         * 相关联的路径和关系之间的空间位置相近，反之疏远
         */
        for (int i = 0; i < vector_len; i++) {
            double x = relation_vec[relation][i];
            for (int path_id: path) {
                x -= relation_vec[path_id][i];
            }
            int flag = (x > 0) ? 1 : -1;
            relation_vec[relation][i] += beta * learning_rate * flag;
            for (int path_id : path) {
                relation_vec[path_id][i] -= beta * learning_rate * flag;
            }
        }
    }

    static private double calc_path(int relation, List<Integer> path) {
        double sum = 0;
        for (int i = 0; i < vector_len; i++) {
            double x = relation_vec[relation][i];
            for (int path_id : path) {
                x -= relation_vec[path_id][i];
            }
            sum += abs(x);
        }
        return sum;
    }

}
