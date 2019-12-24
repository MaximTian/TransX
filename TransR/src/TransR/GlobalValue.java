package TransR;

import java.util.Map;

public class GlobalValue {
    // some train parameters
    static int relation_dimension = 100;
    static int entity_dimension = 100;

    static double learning_rate = 0.02;
    static double margin = 0.2;
    static int method = 1;  // method = 1 means bern version, else unif version

    static String version = "bern";
    static int relation_num, entity_num;
    static Map<String, Integer> relation2id, entity2id;
    static Map<Integer, String> id2relation, id2entity;
    static Map<Integer, Map<Integer, Integer>> left_entity, right_entity;
    static Map<Integer, Double> left_num, right_num;

    static double[][] relation_vec, relation_copy;
    static double[][] entity_vec, entity_copy;
    static double[][][] Wr_vec, Wr_copy;

    static int nepoch = 1200;  // the SGD iteration times
    static int nbatches = 150;  // the number of triples needed to train in each iteration
}
