package TransE;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static TransE.GlobalValue.*;

public class TrainRun {

    private static Train train;

    private static int Read_Data(String file_name, Map<String, Integer> data2id, Map<Integer, String> id2data) throws IOException {
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

    private static void GlobalValueInit() {
        relation2id = new HashMap<>();
        entity2id = new HashMap<>();
        id2relation = new HashMap<>();
        id2entity = new HashMap<>();
        left_entity = new HashMap<>();
        right_entity = new HashMap<>();
        left_num = new HashMap<>();
        right_num = new HashMap<>();
    }

    private static void prepare() throws IOException {
        GlobalValueInit();
        entity_num = Read_Data("resource/data/entity2id.txt", entity2id, id2entity);
        relation_num = Read_Data("resource/data/relation2id.txt", relation2id, id2relation);

        File f = new File("resource/data/train.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(f),"UTF-8"));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
            String[] split_data = line.split("\t");
            String head = split_data[0];
            String tail = split_data[1];
            String relation = split_data[2];
            if (!entity2id.containsKey(head)) {
                System.out.printf("miss entity: %s\n", head);
                continue;
            }
            if (!entity2id.containsKey(tail)) {
                System.out.printf("miss entity: %s\n", tail);
                continue;
            }
            if (!relation2id.containsKey(relation)) {
                relation2id.put(relation, relation_num);
                relation_num++;
            }

            if (!left_entity.containsKey(relation2id.get(relation))) {
                left_entity.put(relation2id.get(relation), new HashMap<>());
            }
            Map<Integer, Integer> value = left_entity.get(relation2id.get(relation));
            if (!value.containsKey(entity2id.get(head))) {
                value.put(entity2id.get(head), 0);
            }
            value.put(entity2id.get(head), value.get(entity2id.get(head)) + 1);

            if (!right_entity.containsKey(relation2id.get(relation))) {
                right_entity.put(relation2id.get(relation), new HashMap<>());
            }
            value = right_entity.get(relation2id.get(relation));
            if (!value.containsKey(entity2id.get(tail))) {
                value.put(entity2id.get(tail), 0);
            }
            value.put(entity2id.get(tail), value.get(entity2id.get(tail)) + 1);

            train.add(entity2id.get(head), relation2id.get(relation), entity2id.get(tail));
        }
        for (int i = 0; i < relation_num; i++) {
            int count = 0;
            double sum = 0;
            Map<Integer, Integer> value = left_entity.get(i);
            for (int id : value.keySet()) {
                count++;
                sum += value.get(id);
            }
            left_num.put(i, sum / count); // 每个关系中的每个头实体平均出现的次数

            count = 0;
            sum = 0;
            value = right_entity.get(i);
            for (int id : value.keySet()) {
                count++;
                sum += value.get(id);
            }
            right_num.put(i, sum / count);
        }

        System.out.printf("entity number = %s\n", entity_num);
        System.out.printf("relation number = %s\n", relation_num);
    }

    public static void train_run() throws IOException {
        int nepoch = 1200;
        int nbatches = 150;
        if (method == 0) {
            version = "unif";
        }
        System.out.printf("iteration times = %s\n", nepoch);
        System.out.printf("nbatches = %s\n", nbatches);
        train = new Train();
        prepare();
        train.run(nepoch, nbatches);
    }
}
