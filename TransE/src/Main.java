import TransE.TestPrepare;
import TransE.TrainPrepare;

import java.io.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.println("Train or test? y/n");  //  训练还是测试
        Scanner sc = new Scanner(System.in);
        boolean train_flag;
        train_flag = sc.next().equals("y");
        if (train_flag) {
            System.out.println("Begin train");
            TrainPrepare.train_run();
        } else {
            System.out.println("Begin test");
            TestPrepare.test_run();
        }
    }

}