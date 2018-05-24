package TransE;

import java.io.*;
import java.util.*;

import static TransE.Gradient.train_kb;
import static TransE.Utils.*;
import static TransE.GlobalValue.*;

public class Train {

    // region private members
    private double res; //loss function value
    private List<Integer> fb_h;
    private List<Integer> fb_l;
    private List<Integer> fb_r;
    private Map<Pair<Integer, Integer>, Set<Integer>> head_relation2tail; // to save the (h, r, t)
    // endregion

    Train() {
        fb_h = new ArrayList<>();
        fb_l = new ArrayList<>();
        fb_r = new ArrayList<>();
        head_relation2tail = new HashMap<>();
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

    private void bfgs(int nepoch, int nbatches) throws IOException {
        int batchsize = fb_h.size() / nbatches;
        System.out.printf("Batch size = %s\n", batchsize);
        for (int epoch = 0; epoch < nepoch; epoch++) {
            res = 0;  // means the total loss in each epoch
            for (int batch = 0; batch < nbatches; batch++) {
                for (int k = 0; k < batchsize; k++) {
                    int i = rand_max(fb_h.size());
                    int j = rand_max(entity_num);
                    int relation_id = fb_r.get(i);
                    double pr = 1000 * right_num.get(relation_id) / (right_num.get(relation_id) + left_num.get(relation_id));
                    if (method == 0) {
                        pr = 500;
                    }
                    if (rand() % 1000 < pr) {
                        Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), fb_r.get(i));
                        Set<Integer> values = head_relation2tail.get(key);  // 获取头实体和关系对应的尾实体集合
                        while (values.contains(j)) {
                            j = rand_max(entity_num);
                        }
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i), res);
                    } else {
                        Pair<Integer, Integer> key = new Pair<>(j, fb_r.get(i));
                        Set<Integer> values = head_relation2tail.get(key);
                        if (values != null) {
                            while (values.contains(fb_l.get(i))) {
                                j = rand_max(entity_num);
                                key = new Pair<>(j, fb_r.get(i));
                                values = head_relation2tail.get(key);
                                if (values == null) break;
                            }
                        }
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i), res);
                    }
                    norm(relation_vec[fb_r.get(i)], vector_len);
                    norm(entity_vec[fb_h.get(i)], vector_len);
                    norm(entity_vec[fb_l.get(i)], vector_len);
                    norm(entity_vec[j], vector_len);
                }
            }
            System.out.printf("epoch: %s %s\n", epoch, res);
        }
        Write_Vec2File("resource/result/relation2vec." + version, relation_vec, relation_num);
        Write_Vec2File("resource/result/entity2vec." + version, entity_vec, entity_num);
    }

    // region public members & methods

    public void add(int head, int relation, int tail) {
        fb_h.add(head);
        fb_r.add(relation);
        fb_l.add(tail);
        Pair<Integer, Integer> key = new Pair<>(head, relation);
        if (!head_relation2tail.containsKey(key)) {
            head_relation2tail.put(key, new HashSet<>());
        }
        Set<Integer> tail_set = head_relation2tail.get(key);
        tail_set.add(tail);
    }

    public void run(int nepoch, int nbatches) throws IOException {
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
