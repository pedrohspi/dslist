import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Sum {

    private static Semaphore mutex = new Semaphore(1);
    private static int totalSum = 0;
    private static Semaphore multiplex;
    private static HashMap<Integer, List<String>> sumMap = new HashMap<>();

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: java Sum filepath1 filepath2 filepathN");
            System.exit(1);
        }

        multiplex = new Semaphore(args.length / 2 > 0 ? args.length / 2 : 1);
        ArrayList<Thread> threads = new ArrayList<>(args.length);

        for (String path : args) {
            Thread thread = new Thread(new FileSum(path));
            threads.add(thread);
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (Integer sum : sumMap.keySet()) {
            List<String> files = sumMap.get(sum);
            if (files.size() > 1) {
                System.out.println(sum + " " + String.join(" ", files));
            }
        }

        System.out.println("Soma Total: " + totalSum);
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
                addToTotal(parcial, path);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                multiplex.release();
            }
        }

        public int sum() throws IOException {
            Path filePath = Paths.get(path);
            if (Files.isRegularFile(filePath)) {
                try (FileInputStream fis = new FileInputStream(filePath)) {
                    return sum(fis);
                }
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

        public static void addToTotal(int parcial, String path) {
            try {
                mutex.acquire();
                totalSum += parcial;
                sumMap.computeIfAbsent(parcial, k -> new ArrayList<>()).add(path);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                mutex.release();
            }
        }
    }
}




import sys
import threading

totalSoma = 0
mutex = threading.Semaphore(1)
soma_dic = {}

def do_sum(path):
    sum_local = 0
    with open(path, 'rb') as f:
        byte = f.read(1)
        while byte:
            sum_local += int.from_bytes(byte, byteorder='big', signed=False)
            byte = f.read(1)
        return sum_local

def addSum(sum_local):
    global totalSoma
    try:
        mutex.acquire()
        totalSoma += sum_local
    finally:
        mutex.release()    

def addDic(soma, path):
    global soma_dic
    try:
        mutex.acquire()
        if soma not in soma_dic:
            soma_dic[soma] = []
        soma_dic[soma].append(path)
    finally:
        mutex.release()


def run(path):
    multiplex.acquire()
    try:   
        soma_each = do_sum(path)
        addSum(soma_each)
        addDic(soma_each, path)
    finally:
        multiplex.release()

threads = [] 
if __name__ == "__main__":
    paths = sys.argv[1:]

    number_of_threads = len(paths)/2
    multiplex = threading.Semaphore(number_of_threads)

    for path in paths:
        thread = threading.Thread(target=run, args=(path,))
        threads.append(thread)
        thread.start()


    for thread in threads:
        thread.join()
    
    print(f"SOMA TOTAL {totalSoma}")

    for soma, files in soma_dic.items():
        if(len(files)>1):
            print(f"{soma} {''.join(files)}")




 public static void addSomaMap(int soma, String path){
            try{
                mutex.acquire();
                if(!somaMap.containsKey(soma)){
                    somaMap.put(soma, new ArrayList<>());
                }
                somaMap.get(soma).add(path);
            }catch(InterruptedException e ){

            }finally{
                mutex.release();
            }
        }
    }    
}
