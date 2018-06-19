package PCRA_Program;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Utils {
    static void map_add_HeadTail(Set<String> map, String head, String tail) {
        String key = head + " " + tail;
        map.add(key);
    }

    static void map_add_path(Map<String, Integer> map, String path) {
        if (!map.containsKey(path)) {
            map.put(path, 0);
        }
        map.put(path, map.get(path) + 1);
    }

    static void map_add_tail(Map<String, Map<Integer, Set<String>>> map, String head, int relation_id, String tail) {
        if (!map.containsKey(head)) {
            map.put(head, new HashMap<>());
        }
        Map<Integer, Set<String>> relation2tail = map.get(head);
        if (!relation2tail.containsKey(relation_id)) {
            relation2tail.put(relation_id, new HashSet<>());
        }
        Set<String> tail_set = relation2tail.get(relation_id);
        tail_set.add(tail);
    }

    static void map_add_relation(Map<String, HashSet<Integer>> map, String head, int relation_id, String tail) {
        String key = head + " " + tail;
        if (!map.containsKey(key)) {
            map.put(key, new HashSet<>());
        }
        Set<Integer> relation_set = map.get(key);
        relation_set.add(relation_id);
    }

    static void map_add_RelationPath(Map<String, Map<String, Double>> map, String head, String tail,
                                     String relation_path, double prob) {
        String head_tail = head + " " + tail;
        if (!map.containsKey(head_tail)) {
            map.put(head_tail, new HashMap<>());
        }
        Map<String, Double> path_set = map.get(head_tail);
        if (!path_set.containsKey(relation_path)) {
            path_set.put(relation_path, 0.0);
        }
        path_set.put(relation_path, path_set.get(relation_path) + prob);
    }

    static void add_Path2Relation(Map<String, Integer> path2relation_set, String path, Set<Integer> relation_set) {
        for (int relation: relation_set) {
            String path_relation = path + "->" + relation;
            map_add_path(path2relation_set, path_relation);
        }
    }

    static Map<String, Double> generate_valid_path(Map<String, Map<String, Double>> head_tail2path, String head_tail) {
        Map<String, Double> path_prob = new HashMap<>();  // 记录所有路径以及相应的路径概率
        Map<String, Double> path_prob_valid = new HashMap<>();  // 记录符合概率阈值的路径

        double sum = 0.0;  // 用于归一化
        Map<String, Double> path_set = head_tail2path.get(head_tail);
        for (String path: path_set.keySet()) {
            double prob = path_set.get(path);
            path_prob.put(path, prob);
            sum += prob;
        }
        for (String path: path_prob.keySet()) {
            double prob = path_prob.get(path) / sum;
            path_prob.put(path, prob);
            if (prob > 0.01) { // 筛选条件
                path_prob_valid.put(path, prob);
            }
        }
        return path_prob_valid;
    }
}
