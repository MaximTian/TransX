import PCRA_Program.PCRA;
import PTransE.TestRun;
import PTransE.TrainRun;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    private static void PCRA_run() throws IOException {
        File f = new File("resource/path_data/confident.txt");
        if (!f.exists()) {
            PCRA pcra = new PCRA();
            pcra.run();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Train or test? y/n");
        Scanner sc = new Scanner(System.in);
        boolean train_flag;
        train_flag = sc.next().equals("y");
        if (train_flag) {
            PCRA_run();
            TrainRun trainRun = new TrainRun();
            trainRun.train_run();
        } else {
            TestRun testRun = new TestRun();
            testRun.test_run();
        }
    }
}
