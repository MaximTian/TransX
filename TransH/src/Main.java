import TransH.TestRun;
import TransH.TrainRun;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Train or test? y/n");
        Scanner sc = new Scanner(System.in);
        boolean train_flag;
        train_flag = sc.next().equals("y");
        if (train_flag) {
            System.out.println("Begin train");
            TrainRun.train_run();
        } else {
            System.out.println("Begin test");
            TestRun.test_run();
        }
    }
}
