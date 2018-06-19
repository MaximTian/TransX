package PTransE;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static PTransE.GlobalValue.*;

public class TrainRun {

    private Train train;

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

    private void GlobalValueInit() {
        relation2id = new HashMap<>();
        entity2id = new HashMap<>();
        id2relation = new HashMap<>();
        id2entity = new HashMap<>();
        left_entity = new HashMap<>();
        right_entity = new HashMap<>();
        left_num = new HashMap<>();
        right_num = new HashMap<>();
        path_confidence = new HashMap<>();
    }

    private void prepare() throws IOException {
        GlobalValueInit();
        entity_num = Read_Data("resource/data/entity2id.txt", entity2id, id2entity);
        relation_num = Read_Data("resource/data/relation2id.txt", relation2id, id2relation);

        File f = new File("resource/path_data/train_prob.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] split_data = line.split(" ");
            int head_id = entity2id.get(split_data[0]);
            int tail_id = entity2id.get(split_data[1]);
            int relation_id = Integer.valueOf(split_data[2]);

            String[] path_info = reader.readLine().split(" ");
            List<Pair<List<Integer>, Double>> path2prob_list = new ArrayList<>();
            for (int i = 1; i < path_info.length;) {
                int path_length = Integer.valueOf(path_info[i]);
                List<Integer> relation_id_list = new ArrayList<>();
                for (int j = 1; j <= path_length; j++) {
                    relation_id_list.add(Integer.valueOf(path_info[i + j]));
                }
                double prob = Double.valueOf(path_info[i + path_length + 1]);
                Pair<List<Integer>, Double> path2prob = new Pair<>(relation_id_list, prob);
                path2prob_list.add(path2prob);

                i += path_length + 2;
            }
            train.add(head_id, relation_id, tail_id, path2prob_list);
        }

        f = new File("resource/path_data/confident.txt");
        reader = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        while ((line = reader.readLine()) != null) {
            String[] line_split = line.split(" ");
            StringBuilder path = new StringBuilder();
            for (int i = 1; i < line_split.length; i++) {
                if (path.length() > 0) path.append(" ");
                path.append(line_split[i]);
            }

            String[] path_info = reader.readLine().split(" ");
            for (int i = 1; i < path_info.length; i += 2) {
                int relation_id = Integer.valueOf(path_info[i]);
                double prob = Double.valueOf(path_info[i + 1]);

                Pair<String, Integer> path2relation = new Pair<>(path.toString(), relation_id);
                path_confidence.put(path2relation, prob);
            }
        }

        System.out.printf("entity number = %s\n", entity_num);
        System.out.printf("relation number = %s\n", relation_num);
    }

    public void train_run() throws IOException {
        int nepoch = 1200;
        int nbatches = 150;
        System.out.printf("iteration times = %s\n", nepoch);
        System.out.printf("nbatches = %s\n", nbatches);
        train = new Train();
        prepare();
        train.run(nepoch, nbatches);
    }
}
