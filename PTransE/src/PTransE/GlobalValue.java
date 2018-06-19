package PTransE;

import java.util.Map;

public class GlobalValue {
    // some train parameters
    static int vector_len = 100; // the embedding vector dimension
    static double learning_rate = 0.02;
    static double margin = 0.1;
    static double margin_relation = 0.2;

    static int relation_num, entity_num;
    static Map<String, Integer> relation2id, entity2id;
    static Map<Integer, String> id2relation, id2entity;
    static Map<Integer, Map<Integer, Integer>> left_entity, right_entity;
    static Map<Integer, Double> left_num, right_num;

    static double[][] relation_vec;
    static double[][] entity_vec;

    static Map<Pair<String, Integer>, Double> path_confidence;
}
