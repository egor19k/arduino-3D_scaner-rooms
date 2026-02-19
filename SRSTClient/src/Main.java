import com.arduino.connection.serial.SerialConnection;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;
import java.io.File;

public class Main {
    public static SerialConnection sc;

    public static void init() {
        System.out.println("init COM serial connect");
        String initCOM = new Scanner(System.in).nextLine();
        sc = new SerialConnection(initCOM, 115200);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        init();
        File fileС = new File("data.csv");
        FileWriter file = new FileWriter("data.csv");
        Thread.sleep(2000);
        file.write("phi; theta; r\n");
        file.flush();
        if (!fileС.isFile()) fileС.createNewFile();
        System.out.println("System started!");
        sc.connect();

        while (true) {
            String scd = sc.receiveData(1000);
            if (scd != "") {
                System.out.println(scd);
                file.write(scd + "\n");
                file.flush();
            }
            if (!sc.isConnected()) {
                file.close();
                Err(0, 150);
            }
        }
    }

    public static void Err(int error, int err_code) throws InterruptedException, IOException {
        System.err.println("error: " + err_code);
        System.out.println("exit to System or try again");
        System.out.println("exit or SR");
        String hs = new Scanner(System.in).nextLine();
        if (Objects.equals(hs, "SR")) {
            main(null);
        } else {
            System.exit(error);
        }
    }
}