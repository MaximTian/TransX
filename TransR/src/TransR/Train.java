package TransR;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;

import static TransR.GlobalValue.*;
import static TransR.Gradient.train_kb;
import static TransR.Utils.*;

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

    private void Write_Vec(String file_name, double[][] vec, int number) throws IOException {
        File f = new File(file_name);
        if (!f.exists()) {	// if file does not exist, then create it
            File dir = new File(f.getParent());
            dir.mkdirs();
            f.createNewFile();
        }
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < relation_dimension; j++) {
                String str = String.format("%.6f\t", vec[i][j]);
                writer.write(str);
            }
            writer.write("\n");
            writer.flush();
        }
    }

    private void Write_Wr(String file_name, double[][][] vec, int number) throws IOException {
        File f = new File(file_name);
        if (!f.exists()) {	// if file does not exist, then create it
            File dir = new File(f.getParent());
            dir.mkdirs();
            f.createNewFile();
        }
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
        for (int i = 0; i < number; i++) {
            for (int j = 0; j < entity_dimension; j++) {
                for (int k = 0; k < relation_dimension; k++) {
                    String str = String.format("%.6f\t", vec[i][j][k]);
                    writer.write(str);
                }
                writer.write("\n");
                writer.flush();
            }
        }
    }

    private void bfgs() throws IOException {
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
                    if (rand() % 1000 < pr) {  // 替换头实体
                        Pair<Integer, Integer> key = new Pair<>(fb_h.get(i), fb_r.get(i));
                        Set<Integer> values = head_relation2tail.get(key);  // 获取头实体和关系对应的尾实体集合
                        while (values.contains(j)) {
                            j = rand_max(entity_num);
                        }
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));
                    } else {  // 替换尾实体
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
                        res += train_kb(fb_h.get(i), fb_l.get(i), fb_r.get(i), j, fb_l.get(i), fb_r.get(i));
                    }
                    norm(relation_copy[fb_r.get(i)]);
                    norm(entity_copy[fb_h.get(i)]);
                    norm(entity_copy[fb_l.get(i)]);
                    norm(entity_copy[j]);

                    norm(entity_copy[fb_h.get(i)], Wr_vec[fb_r.get(i)]);
                    norm(entity_copy[fb_l.get(i)], Wr_vec[fb_r.get(i)]);
                    norm(entity_copy[j], Wr_vec[fb_r.get(i)]);
                }
                relation_vec = relation_copy;
                entity_vec = entity_copy;
                Wr_vec = Wr_copy;
            }
            System.out.printf("epoch: %s %s\n", epoch, res);
        }
        Write_Vec("resource/result/relation2vec." + version, relation_vec, relation_num);
        Write_Vec("resource/result/entity2vec." + version, entity_vec, entity_num);
        Write_Wr("resource/result/Wr_vec." + version, Wr_vec, relation_num);
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

    public void run() throws IOException {
        Wr_vec = new double[relation_num][entity_dimension][relation_dimension];
        Wr_copy = new double[relation_num][entity_dimension][relation_dimension];
        for (int i = 0; i < relation_num; i++) {
            for (int j = 0; j < entity_dimension; j++) {
                for (int k = 0; k < relation_dimension; k++) {
                    Wr_vec[i][j][k] = (j == k) ? 1 : 0;
                    Wr_copy[i][j][k] = (j == k) ? 1 : 0;
                }
            }
        }

        relation_vec = new double[relation_num][relation_dimension];
        relation_copy = new double[relation_num][relation_dimension];
        for (int i = 0; i < relation_num; i++) {
            for (int j = 0; j < relation_dimension; j++) {
                relation_vec[i][j] = uniform(-1, 1);
                relation_copy[i][j] = relation_vec[i][j];
            }
        }

        entity_vec = new double[entity_num][relation_dimension];
        entity_copy = new double[entity_num][relation_dimension];
        for (int i = 0; i < entity_num; i++) {
            for (int j = 0; j < relation_dimension; j++) {
                entity_vec[i][j] = uniform(-1, 1);
                entity_copy[i][j] = entity_vec[i][j];
            }
        }
        bfgs();
    }
    // endregion
}
