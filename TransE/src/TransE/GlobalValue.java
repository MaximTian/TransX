package TransE;

import java.util.Map;

public class GlobalValue {
    // some train parameters
    static boolean L1_flag = true; // distance is l1 or l2
    static int vector_len = 100; // the embedding vector dimension
    static double learning_rate = 0.02;
    static double margin = 0.1;
    static int method = 1;  // method = 1 means bern version, else unif version

    static String version = "bern";
    static int relation_num, entity_num;
    static Map<String, Integer> relation2id, entity2id;
    static Map<Integer, String> id2relation, id2entity;
    static Map<Integer, Map<Integer, Integer>> left_entity, right_entity;
    static Map<Integer, Double> left_num, right_num;

    static double[][] relation_vec;
    static double[][] entity_vec;
}
