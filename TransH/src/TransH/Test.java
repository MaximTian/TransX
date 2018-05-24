package TransH;

import java.io.*;
import java.util.*;

import static TransH.GlobalValue.*;
import static TransH.Gradient.calc_sum;

public class Test {
    // region private members
    private List<Integer> fb_h;
    private List<Integer> fb_l;
    private List<Integer> fb_r;
    private Map<Pair<Integer, Integer>, Set<Integer>> head_relation2tail; // to save the (h, r, t)
    // endregion

    Test() {
        fb_h = new ArrayList<>();
        fb_l = new ArrayList<>();
        fb_r = new ArrayList<>();
        head_relation2tail = new HashMap<>();
    }

    public void add(int head, int relation, int tail, boolean flag) {
        /**
         * head_relation2tail用于存放 正确的三元组
         * flag=true 表示该三元组关系正确
         */
        if (flag) {
            Pair<Integer, Integer> key = new Pair<>(head, relation);
            if (!head_relation2tail.containsKey(key)) {
                head_relation2tail.put(key, new HashSet<>());
            }
            Set<Integer> tail_set = head_relation2tail.get(key);
            tail_set.add(tail);
        } else {
            fb_h.add(head);
            fb_r.add(relation);
            fb_l.add(tail);
        }
    }

    private void Read_Vec_File(String file_name, double[][] vec) throws IOException {
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        String line;
        for (int i = 0; (line = reader.readLine()) != null; i++) {
            String[] line_split = line.split("\t");
            for (int j = 0; j < vector_dimension; j++) {
                vec[i][j] = Double.valueOf(line_split[j]);
            }
        }
    }

    private void relation_add(Map<Integer, Integer> relation_num, int relation) {
        if (!relation_num.containsKey(relation)) {
            relation_num.put(relation, 0);
        }
        int count = relation_num.get(relation);
        relation_num.put(relation, count + 1);
    }

    private void map_add_value(Map<Integer, Integer> tmp_map, int id, int value) {
        if (!tmp_map.containsKey(id)) {
            tmp_map.put(id, 0);
        }
        int tmp_value = tmp_map.get(id);
        tmp_map.put(id, tmp_value + value);
    }

    private boolean hrt_isvalid(int head, int relation, int tail) {
        /**
         * 如果实体之间已经存在正确关系，则不需要计算距离
         * 如果头实体与尾实体一致，也排除该关系的距离计算
         */
        if (head == tail) {
            return true;
        }
        Pair<Integer, Integer> key = new Pair<>(head, relation);
        Set<Integer> values = head_relation2tail.get(key);
        if (values == null || !values.contains(tail)) {
            return false;
        } else {
            return true;
        }
    }

    public void run() throws IOException {
        relation_vec = new double[relation_num][vector_dimension];
        entity_vec = new double[entity_num][vector_dimension];
        Wr_vec = new double[relation_num][vector_dimension];

        Read_Vec_File("resource/result/relation2vec.bern", relation_vec);
        Read_Vec_File("resource/result/entity2vec.bern", entity_vec);
        Read_Vec_File("resource/result/Wr_vec.bern", Wr_vec);

        int lsum = 0, rsum = 0;
        int lp_n = 0, rp_n = 0;
        Map<Integer, Integer> lsum_r = new HashMap<>();
        Map<Integer, Integer> rsum_r = new HashMap<>();
        Map<Integer, Integer> lp_n_r = new HashMap<>();
        Map<Integer, Integer> rp_n_r = new HashMap<>();
        Map<Integer, Integer> rel_num = new HashMap<>();

        File out_file = new File("resource/result/output_detail.txt");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(out_file), "UTF-8");

        System.out.printf("Total iterations = %s\n", fb_l.size());
        for (int id = 0; id < fb_l.size(); id++) {
            System.out.println(id);
            int head = fb_h.get(id);
            int tail = fb_l.get(id);
            int relation = fb_r.get(id);
            relation_add(rel_num, relation);
            List<Pair<Integer, Double>> head_dist = new ArrayList<>();
            for (int i = 0; i < entity_num; i++) {
                if (hrt_isvalid(i, relation, tail)) {
                    continue;
                }
                double sum = calc_sum(i, tail, relation);
                head_dist.add(new Pair<>(i, sum));
            }
            Collections.sort(head_dist, (o1, o2) -> Double.compare(o1.b, o2.b));
            for (int i = 0; i < head_dist.size(); i++) {
                int cur_head = head_dist.get(i).a;
                if (cur_head == head) {
                    lsum += i; // 统计小于<h, l, r>距离的数量
                    map_add_value(lsum_r, relation, i);
                    if (i <= 10) {
                        lp_n++;
                        map_add_value(lp_n_r, relation, 1);
                    }
                    String str = String.format("%s  %s  %s, dist=%f, %d\n\n", id2entity.get(head), id2relation.get(relation),
                            id2entity.get(tail), calc_sum(head, tail, relation), i);
                    writer.write(str);
                    writer.flush();
                    break;
                } else {
                    String temp_str = String.format("%s  %s  %s, dist=%f, %d\n", id2entity.get(cur_head), id2relation.get(relation),
                            id2entity.get(tail), calc_sum(cur_head, tail, relation), i);
                    writer.write(temp_str);
                    writer.flush();
                }
            }

            List<Pair<Integer, Double>> tail_dist = new ArrayList<>();
            for (int i = 0; i < entity_num; i++) {
                if (hrt_isvalid(head, relation, i)) {
                    continue;
                }
                double sum = calc_sum(head, i, relation);
                tail_dist.add(new Pair<>(i, sum));
            }
            Collections.sort(tail_dist, (o1, o2) -> Double.compare(o1.b, o2.b));
            for (int i = 0; i < tail_dist.size(); i++) {
                int cur_tail = tail_dist.get(i).a;
                if (cur_tail == tail) {
                    rsum += i;
                    map_add_value(rsum_r, relation, i);
                    if (i <= 10) {
                        rp_n++;
                        map_add_value(rp_n_r, relation, 1);
                    }
                    break;
                }
            }
        }
        System.out.printf("lsum = %s, tail number = %s\n", lsum, fb_l.size());
        System.out.printf("left: %s\t%s\n", (lsum * 1.0) / fb_l.size(), (lp_n * 1.0) / fb_l.size());
        System.out.printf("right: %s\t%s\n", (rsum * 1.0) / fb_h.size(), (rp_n * 1.0) / fb_h.size());
    }

}
