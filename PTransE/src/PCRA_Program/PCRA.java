package PCRA_Program;

import java.io.*;
import java.util.*;

import static PCRA_Program.Utils.*;

public class PCRA {
    private Map<String, Integer> relation2id;
    private Map<Integer, String> id2relation;
    private int relation_num;
    private Map<String, Map<Integer, Set<String>>> head_relation2tail;  // (头实体，关系) -> (尾实体)
    private Map<String, HashSet<Integer>> head_tail2relation;  // (头实体，尾实体) -> (关系)

    private Map<String, Map<String, Double>> head_tail2path;   // (头实体，尾实体) -> (关系路径)

    private Map<String, Integer> paths;  // 记录每条路径，以及该路径出现的次数
    private Map<String, Integer> path2relation;  // 记录每个路径推理的边("path->rel")，已经对应出现的次数

    private Set<String> path_valid;  // 存储符合条件的路径

    private void init() {
        relation2id = new HashMap<>();
        id2relation = new HashMap<>();
        head_relation2tail = new HashMap<>();
        head_tail2relation = new HashMap<>();

        head_tail2path = new HashMap<>();
        paths = new HashMap<>();
        path2relation = new HashMap<>();

        path_valid = new HashSet<>();
    }

    private int Read_Data(String file_name, Map<String, Integer> data2id, Map<Integer, String> id2data) throws IOException {
        int count = 0;
        File f = new File(file_name);
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            data2id.put(split_data[0], Integer.valueOf(split_data[1]));
            id2data.put(Integer.valueOf(split_data[1]), split_data[0]);
            count++;
        }
        return count;
    }

    private void prepare() throws IOException {
        relation_num = Read_Data("resource/data/relation2id.txt", relation2id, id2relation);

        File f = new File("resource/data/train.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            int relation_id = relation2id.get(split_data[2]);

            map_add_relation(head_tail2relation, head, relation_id, tail);
            map_add_tail(head_relation2tail, head, relation_id, tail);
        }
    }

    public void run() throws IOException {
        init();
        prepare();
        ArrayDeque<String> cur_entity_list = new ArrayDeque<>();
        for (String head: head_relation2tail.keySet()) {
            cur_entity_list.addFirst(head);
            Map<Integer, Set<String>> visit_relation_tail = head_relation2tail.get(head);
            ArrayDeque<Integer> cur_relation_list = new ArrayDeque<>();

            dfs(cur_entity_list, visit_relation_tail, cur_relation_list, 0, 1, 1.0);
            dfs(cur_entity_list, visit_relation_tail, cur_relation_list, 0, 2, 1.0);
            cur_entity_list.removeFirst();
        }
        Write_Path();
        Write_Confident();
        calculate_prob("train");
        calculate_prob("test");
    }

    private void dfs(ArrayDeque<String> entity_list, Map<Integer, Set<String>> relation_tail,
                            ArrayDeque<Integer> relation_list, int depth, int max_depth, double prob) {
        /**
         * entity_list: record those visited entities, to prevent visiting again
         * relation_tail: record the relation_set needed to visit, which is from the last_entity in the entity_list
         * realtion_list: record those visited relations
         */
        if (relation_tail == null && depth < max_depth) {
            return;
        }
        if (depth == max_depth) {
            String head = entity_list.getFirst();
            String tail = entity_list.getLast();
            StringBuilder path = new StringBuilder();
            for (int relation_id: relation_list) {
                if (path.length() > 0) {
                    path.append(" ");
                }
                path.append(String.valueOf(relation_id));
            }
            map_add_path(paths, path.toString());

            if (head_tail2relation.containsKey(head + " " + tail)) {
                Set<Integer> relation_set = head_tail2relation.get(head + " " + tail);
                add_Path2Relation(path2relation, path.toString(), relation_set);
            }

            map_add_RelationPath(head_tail2path, head, tail, path.toString(), prob);
            return;
        }

        for (int relation_id: relation_tail.keySet()) {
            Set<String> tail_set = relation_tail.get(relation_id);
            relation_list.addLast(relation_id);
            double cur_prob = prob * (1.0 / tail_set.size());
            for (String tail: tail_set) {
                if (!entity_list.contains(tail)) {
                    entity_list.addLast(tail);
                    Map<Integer, Set<String>> visit_relation_tail = head_relation2tail.get(tail);
                    dfs(entity_list, visit_relation_tail, relation_list, depth + 1, max_depth, cur_prob);
                    entity_list.removeLast();
                }
            }
            relation_list.removeLast();
        }
    }

    private void Write_Path() throws IOException {
        File f = new File("resource/path_data/path.txt");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");

        for (String head: head_relation2tail.keySet()) {
            for (String tail: head_relation2tail.keySet()) {
                if (head.equals(tail)) {
                    continue;
                }
                String head_tail = head + " " + tail;
                if (head_tail2path.containsKey(head_tail)) {
                    Map<String, Double> path_prob_valid = generate_valid_path(head_tail2path, head_tail);
                    writer.write(head_tail + "\n");
                    writer.write(String.valueOf(path_prob_valid.size()));
                    for (String path: path_prob_valid.keySet()) {
                        path_valid.add(path);
                        String[] split_path = path.split(" ");
                        writer.write(" " + String.valueOf(split_path.length) + " " + path
                                + " " + String.valueOf(path_prob_valid.get(path)));
                    }
                    writer.write("\n");
                    writer.flush();
                }
            }
        }
    }

    private void Write_Confident() throws IOException {
        /**
         * path_length, path
         * relation counts, (relation, prob) ...
          */

        File f = new File("resource/path_data/confident.txt");
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");

        for (String path: path_valid) {
            List<String> out_list = new ArrayList<>();
            for (int i = 0; i < relation_num; i++) {
                String tmp_path2relation = String.format("%s->%d", path, i);
                if (path2relation.containsKey(tmp_path2relation)) {
                    double prob = 1.0 / paths.get(path);
                    String str = String.format(" %d %f", i, path2relation.get(tmp_path2relation) * prob);
                    out_list.add(str);
                }
            }
            if (!out_list.isEmpty()) {
                writer.write(String.format("%d %s\n", path.split(" ").length, path));
                writer.write(String.valueOf(out_list.size()));
                for (String out: out_list) {
                    writer.write(out);
                }
                writer.write("\n");
                writer.flush();
            }
        }
    }

    private void calculate_prob(String file_name) throws IOException {
        File f = new File(String.format("resource/data/%s.txt", file_name));
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));

        f = new File(String.format("resource/path_data/%s_prob.txt", file_name));
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");

        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            int relation_id = relation2id.get(split_data[2]);

            String head_tail = head + " " + tail;
            Map<String, Double> path_prob_valid = new HashMap<>();  // 记录符合概率阈值的路径
            if (head_tail2path.containsKey(head_tail)) {
                path_prob_valid = generate_valid_path(head_tail2path, head_tail);
            }

            writer.write(String.format("%s %s %d\n", head, tail, relation_id));
            writer.write(String.valueOf(path_prob_valid.size()));
            for (String path: path_prob_valid.keySet()) {
                String[] split_path = path.split(" ");
                writer.write(String.format(" %d %s %f", split_path.length, path, path_prob_valid.get(path)));
            }
            writer.write("\n");
            /**
             * to do reverse
             */
            writer.flush();
        }
    }

}
