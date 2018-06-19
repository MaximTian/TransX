package PTransE;

import java.io.*;
import java.util.*;

import static PTransE.Gradient.train_kb;
import static PTransE.Gradient.train_path;
import static PTransE.Utils.*;
import static PTransE.GlobalValue.*;

class Train {
    private List<Integer> fb_h;
    private List<Integer> fb_l;
    private List<Integer> fb_r;
    private List<List<Pair<List<Integer>, Double>>> fb_path2prob;
    private Map<Pair<Integer, Integer>, Set<Integer>> head_relation2tail; // to save the (h, r, t)

    private double loss;

    Train() {
        fb_h = new ArrayList<>();
        fb_l = new ArrayList<>();
        fb_r = new ArrayList<>();
        head_relation2tail = new HashMap<>();
        fb_path2prob = new ArrayList<>();
    }

    private void Write_Vec2File(String file_name, double[][] vec, int number) throws IOException {
        File f = new File(file_name);
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < vector_len; j++) {
                String str = String.format("%.6f\t", vec[i][j]);
                writer.write(str);
            }
            writer.write("\n");
            writer.flush();
        }
    }

    private int random_tail(int pos, int neg_pos) {
        // 随机替换尾实体
        Pair<Integer, Integer> key = new Pair<>(fb_h.get(pos), fb_r.get(pos));
        Set<Integer> values = head_relation2tail.get(key);  // 获取头实体和关系对应的尾实体集合
        while (values.contains(neg_pos)) {
            neg_pos = rand_max(entity_num);
        }
        return neg_pos;
    }

    private int random_head(int pos, int neg_pos) {
        // 随机替换头实体
        Pair<Integer, Integer> key = new Pair<>(neg_pos, fb_r.get(pos));
        Set<Integer> values = head_relation2tail.get(key);
        if (values != null) {
            while (values.contains(fb_l.get(pos))) {
                neg_pos = rand_max(entity_num);
                key = new Pair<>(neg_pos, fb_r.get(pos));
                values = head_relation2tail.get(key);
                if (values == null) break;
            }
        }
        return neg_pos;
    }

    private int random_relation(int pos, int neg_pos) {
        // 随机替换关系
        Pair<Integer, Integer> key = new Pair<>(fb_h.get(pos), neg_pos);
        Set<Integer> values = head_relation2tail.get(key);
        if (values != null) {
            while (values.contains(fb_l.get(pos))) {
                neg_pos = rand_max(relation_num);
                key = new Pair<>(fb_h.get(pos), neg_pos);
                values = head_relation2tail.get(key);
                if (values == null) break;
            }
        }
        return neg_pos;
    }

    private void bfgs(int nepoch, int nbatches) throws IOException {
        int batchsize = fb_h.size() / nbatches;
        System.out.printf("Batch size = %s\n", batchsize);
        for (int epoch = 0; epoch < nepoch; epoch++) {
            // region private members
            //loss function value
            loss = 0;
            for (int batch = 0; batch < nbatches; batch++) {
                for (int k = 0; k < batchsize; k++) {
                    int pos = rand_max(fb_h.size());  // 随机选取一行三元组, 行号
                    int tmp_rand = rand() % 100;
                    if (tmp_rand < 25) {
                        int tail_neg = rand_max(entity_num);
                        tail_neg = random_tail(pos, tail_neg);
                        loss = train_kb(fb_h.get(pos), fb_l.get(pos), fb_r.get(pos), fb_h.get(pos), tail_neg, fb_r.get(pos), loss);

                        norm(entity_vec[tail_neg], vector_len);
                    } else if (tmp_rand < 50) {
                        int head_neg = rand_max(entity_num);
                        head_neg = random_head(pos, head_neg);
                        loss = train_kb(fb_h.get(pos), fb_l.get(pos), fb_r.get(pos), head_neg, fb_l.get(pos), fb_r.get(pos), loss);

                        norm(entity_vec[head_neg], vector_len);
                    } else {
                        int relation_neg = rand_max(relation_num);
                        relation_neg = random_relation(pos, relation_neg); // 若某一对实体之间存在所有的关系，则陷入死循环
                        loss = train_kb(fb_h.get(pos), fb_l.get(pos), fb_r.get(pos), fb_h.get(pos), fb_l.get(pos), relation_neg, loss);

                        norm(relation_vec[relation_neg], vector_len);
                    }
                    update_relation(pos);
                    norm(relation_vec[fb_r.get(pos)], vector_len);
                    norm(entity_vec[fb_h.get(pos)], vector_len);
                    norm(entity_vec[fb_l.get(pos)], vector_len);
                }
            }
            System.out.printf("epoch: %s %s\n", epoch, loss);
        }
        Write_Vec2File("resource/result/relation2vec.txt", relation_vec, relation_num);
        Write_Vec2File("resource/result/entity2vec.txt", entity_vec, entity_num);
    }

    private void update_relation(int pos) {
        int relation_neg = rand_max(relation_num);
        relation_neg = random_relation(pos, relation_neg);

        List<Pair<List<Integer>, Double>> path2prob_list = fb_path2prob.get(pos);
        for (Pair<List<Integer>, Double> path2prob: path2prob_list) {
            List<Integer> path = path2prob.a;
            double prob = path2prob.b;

            StringBuilder str = new StringBuilder();
            for (int path_id: path) {
                if (str.length() > 0) str.append(" ");
                str.append(path_id);
            }

            Pair<String, Integer> tmp_path2rel = new Pair<>(str.toString(), fb_r.get(pos));
            double tmp_confidence = 0;
            if (path_confidence.containsKey(tmp_path2rel)) {
                tmp_confidence = path_confidence.get(tmp_path2rel);
            }
            tmp_confidence = (0.99 * tmp_confidence + 0.01) * prob;
            train_path(fb_r.get(pos), relation_neg, path, tmp_confidence, loss);
        }
    }

    // region public members & methods

    void add(int head, int relation, int tail, List<Pair<List<Integer>, Double>> path2prob_list) {
        fb_h.add(head);
        fb_r.add(relation);
        fb_l.add(tail);
        fb_path2prob.add(path2prob_list);

        Pair<Integer, Integer> key = new Pair<>(head, relation);
        if (!head_relation2tail.containsKey(key)) {
            head_relation2tail.put(key, new HashSet<>());
        }
        Set<Integer> tail_set = head_relation2tail.get(key);
        tail_set.add(tail);
    }

    void run(int nepoch, int nbatches) throws IOException {
        relation_vec = new double[relation_num][vector_len];
        entity_vec = new double[entity_num][vector_len];
        for (int i = 0; i < relation_num; i++) {
            for (int j = 0; j < vector_len; j++) {
                relation_vec[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
            }
        }
        for (int i = 0; i < entity_num; i++) {
            for (int j = 0; j < vector_len; j++) {
                entity_vec[i][j] = uniform(-6 / sqrt(vector_len), 6 / sqrt(vector_len));
            }
            norm(entity_vec[i], vector_len);
        }
        bfgs(nepoch, nbatches);
    }
    // endregion
}
