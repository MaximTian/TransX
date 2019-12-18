package TransE;
import java.io.*;
import java.util.*;

import static TransE.GlobalValue.*;
import static TransE.Gradient.calc_sum;

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
            TransE.Pair<Integer, Integer> key = new Pair<>(head, relation);
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

    private void Read_Vec_File(String file_name, double[][] vec) throws IOException {  //  读取向量文件
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        String line;
        for (int i = 0; (line = reader.readLine()) != null; i++) {
            String[] line_split = line.split("\t");
            for (int j = 0; j < vector_len; j++) {
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
        relation_vec = new double[relation_num][vector_len];
        entity_vec = new double[entity_num][vector_len];

        Read_Vec_File("resource/result/relation2vec.bern", relation_vec);
        Read_Vec_File("resource/result/entity2vec.bern", entity_vec);

        int head_meanRank_raw = 0, tail_meanRank_raw = 0, head_meanRank_filter = 0, tail_meanRank_filter = 0;  // 在正确三元组之前的匹配距离之和
        int head_hits10 = 0, tail_hits10 = 0, head_hits10_filter = 0, tail_hits10_filter = 0;  // 在正确三元组之前的匹配个数之和

        // ------------------------ evaluation link predict ----------------------------------------
        System.out.printf("Total test triple = %s\n", fb_l.size());
        System.out.printf("The evaluation of link predict\n");
        for (int id = 0; id < fb_l.size(); id++) {
            int head = fb_h.get(id);
            int tail = fb_l.get(id);
            int relation = fb_r.get(id);
            List<Pair<Integer, Double>> head_dist = new ArrayList<>();
            for (int i = 0; i < entity_num; i++) {
                double sum = calc_sum(i, tail, relation);
                head_dist.add(new Pair<>(i, sum));
            }
            Collections.sort(head_dist, (o1, o2) -> Double.compare(o1.b, o2.b));
            int filter = 0;  // 统计匹配过程已有的正确三元组个数
            for (int i = 0; i < head_dist.size(); i++) {
                int cur_head = head_dist.get(i).a;
                if (hrt_isvalid(cur_head, relation, tail)) {  // 如果当前三元组是正确三元组，则记录到filter中
                    filter += 1;
                }
                if (cur_head == head) {
                    head_meanRank_raw += i; // 统计小于<h, l, r>距离的数量
                    head_meanRank_filter += i - filter;
                    if (i <= 10) {
                        head_hits10++;
                    }
                    if (i - filter <= 10) {
                        head_hits10_filter++;
                    }
                    break;
                }
            }

            filter = 0;
            List<Pair<Integer, Double>> tail_dist = new ArrayList<>();
            for (int i = 0; i < entity_num; i++) {
                double sum = calc_sum(head, i, relation);
                tail_dist.add(new Pair<>(i, sum));
            }
            Collections.sort(tail_dist, (o1, o2) -> Double.compare(o1.b, o2.b));
            for (int i = 0; i < tail_dist.size(); i++) {
                int cur_tail = tail_dist.get(i).a;
                if (hrt_isvalid(head, relation, cur_tail)) {
                    filter++;
                }
                if (cur_tail == tail) {
                    tail_meanRank_raw += i;
                    tail_meanRank_filter += i - filter;
                    if (i <= 10) {
                        tail_hits10++;
                    }
                    if (i - filter <= 10) {
                        tail_hits10_filter++;
                    }
                    break;
                }
            }
        }
        System.out.printf("-----head prediction------\n");
        System.out.printf("Raw MeanRank: %.3f,  Filter MeanRank: %.3f\n",
                (head_meanRank_raw * 1.0) / fb_l.size(), (head_meanRank_filter * 1.0) / fb_l.size());
        System.out.printf("Raw Hits@10: %.3f,  Filter Hits@10: %.3f\n",
                (head_hits10 * 1.0) / fb_l.size(), (head_hits10_filter * 1.0) / fb_l.size());

        System.out.printf("-----tail prediction------\n");
        System.out.printf("Raw MeanRank: %.3f,  Filter MeanRank: %.3f\n",
                (tail_meanRank_raw * 1.0) / fb_l.size(), (tail_meanRank_filter * 1.0) / fb_l.size());
        System.out.printf("Raw Hits@10: %.3f,  Filter Hits@10: %.3f\n",
                (tail_hits10 * 1.0) / fb_l.size(), (tail_hits10_filter * 1.0) / fb_l.size());
    }

}
