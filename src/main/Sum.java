import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Sum {

    private static Semaphore mutex = new Semaphore(1);
    private static int totalSum = 0;
    private static Semaphore multiplex;

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        multiplex =  new Semaphore(args.length / 2);
        ArrayList<Thread> threads = new ArrayList<>(args.length);

        for (String path : args) {
                Thread thread = new Thread(new FileSum(path), path);
                threads.add(thread);
                thread.start();
        }

        for (Thread thread: threads) {
            thread.join();
        }

        System.out.print(totalSum);
    }

    public static class FileSum implements Runnable {

        private final String path;
        
        public FileSum(String path) {
            this.path = path;
        }

        public void run() {
            try {
                multiplex.acquire();
                int parcial = sum();
                mutex.acquire();
                totalSum += parcial;
                mutex.release();
            } catch (IOException | InterruptedException e ) {

            } finally {
                multiplex.release();
            }
        }
        public int sum() throws IOException {
            Path filePath = Paths.get(path);
            if (Files.isRegularFile(filePath)) {
                FileInputStream fis = new FileInputStream(filePath.toString());
                return sum(fis);
            } else {
                throw new RuntimeException("Non-regular file: " + path);
            }
        }

        public int sum(FileInputStream fis) throws IOException {
            int byteRead;
            int sum = 0;
            
            while ((byteRead = fis.read()) != -1) {
                sum += byteRead;
            }
            return sum;
        }
    }
}
