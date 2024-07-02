import java.io.*;
import java.nio.file.FileSystems;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.regex.Matcher;

public class Main {

    private static final String CONFIG_FILE_PATH = "src\\app.properties";

    private static volatile boolean working = true;

    public static final Object key = new Object();

    public static void main(String[] args) {



        Map<String, String> config = loadConfig();

        long sysExplorerSleepTime = Long.parseLong(config.get("sys_explorer_sleep_time"));
        int maxFileChunkSize = Integer.parseInt(config.get("maximum_file_chunk_size"));
        int maxRowsSize = Integer.parseInt(config.get("maximum_rows_size"));
        String startDirPath = config.get("start_dir");
        String systemStartDirPath = startDirPath.replaceAll("/", Matcher.quoteReplacement(System.getProperty("file.separator")));

        String workingDirectory = System.getProperty("user.dir");

        String dirForSystemExplorer = workingDirectory + "\\src" + systemStartDirPath;



        MatrixBrain.getInstance();
        MatrixMultiplier.getInstance();
        MatrixExtractor.getInstance();





        TaskCoordinator taskCoordinator = new TaskCoordinator();
        Thread taskCoordinatorThread = new Thread(taskCoordinator);

        taskCoordinatorThread.start();

        MatrixExtractor.getInstance().setLimit(maxFileChunkSize);

        SystemExplorer systemExplorer = null;

        Scanner scanner = new Scanner(System.in);
        String path = null;
        while(working){
            System.out.println("Enter command:");

            String input = scanner.nextLine().trim();

            String[] tokens = input.split("\\s+");
            String command = tokens[0];

            switch (command){
                case "dir":{
                    if(tokens.length != 2){
                        System.out.println("Invalid command. Usage: dir dir_name");
                        break;
                    }
                    if(!tokens[1].contains(System.getProperty("file.separator"))){
                        System.out.println("Invalid path.");
                        break;
                    }

                    path = tokens[1];

                    systemExplorer = new SystemExplorer(path,sysExplorerSleepTime);
                    Thread systemExplorerThread = new Thread(systemExplorer);
                    systemExplorerThread.start();

                    break;
                }
                case "info":{
                    if(tokens[1].equalsIgnoreCase("-all")){

                        printInfoAll(MatrixBrain.getInstance().all());

                    }else if(tokens[1].equalsIgnoreCase("-asc")){

                        printInfoAll(MatrixBrain.getInstance().sortAscending());

                    }else if(tokens[1].equalsIgnoreCase("-desc")){

                        printInfoAll(MatrixBrain.getInstance().sortDescending());

                    }else if(tokens[1].equalsIgnoreCase("-s")){
                        if(tokens.length < 3){
                            System.out.println("Invalid command. Usage: info -s N");
                            break;
                        }
                        int n = Integer.parseInt(tokens[2]);

//                        System.out.println(MatrixBrain.getInstance().firstN(n));

                        printInfoAll(MatrixBrain.getInstance().firstN(n));
                    }else if(tokens[1].equalsIgnoreCase("-e")){
                        if(tokens.length < 3){
                            System.out.println("Invalid command. Usage: info -e N");
                            break;
                        }
                        int n = Integer.parseInt(tokens[2]);
//                        System.out.println(MatrixBrain.getInstance().lastN(n));
                        printInfoAll(MatrixBrain.getInstance().lastN(n));

                    }else{
                        String name = tokens[1];
                        Optional<MatrixInfo> matrixInfo = MatrixBrain.getInstance().getMatrixInfoByName(name);
                        if(matrixInfo.isPresent()){
                            System.out.println(matrixInfo.get());
                        }else{
                            System.out.println("Matrix with required name doesn't exists.");
                        }
                    }
                    break;
                }
                case "multiply":{
                    if(tokens.length < 2){
                        System.out.println("Invalid command. Usage: multiply mat1,mat2 [-async] [-name matrix_name]");
                        break;
                    }
                    if(tokens.length == 4 && tokens[3].equalsIgnoreCase("-async")){
                        //TODO: asihnrono mnozenje
                        multiplyMatrices(true, tokens[1], tokens[2], tokens[1]+tokens[2]);

                    }else if(tokens.length == 4){
                        //TODO: mnozenje matrice uz prosledivanje imena
                        //Trebalo bi da se mnozi u ovom slucaju blokirajuce
                        multiplyMatrices(false, tokens[1], tokens[2], tokens[3]);
                    }else{
                        //TODO matrice bez prosledjivanje imena
                        //Mnozenje je blokirajuce
                        multiplyMatrices(false, tokens[1], tokens[2], tokens[1]+tokens[2]);

                    }
                    break;
                }
                case "save":{
                    if(tokens.length != 5){
                        System.out.println("Invalide command. Usage: save -name mat_name -file file_name");
                    }
                    //Cuvanje matrice u fajl. Neblokirajuce.
                    String matrixName = tokens[2];
                    String fileName = tokens[4];
                    if(path == null){
                        System.out.println("You must first add directory to search!");
                    }
                    MatrixBrain.getInstance().saveMatrixToFile(matrixName, path, fileName);
                    break;
                }
                case "clear":{
                    if(tokens.length != 2){
                        System.out.println("Invalid command. Usage: clear mat_name / clear file_name");
                        //TODO brisanje matrice
                    }
                    String param = tokens[1].trim();
                    String message = MatrixBrain.getInstance().clearMatrix(param);
                    System.out.println(message);
                    break;
                }
                case "stop": {
                    if(tokens.length != 1){
                        System.out.println("Invalid command. Usage: stop");
                    }

                    if(systemExplorer != null){
                        systemExplorer.stop();
                    }

                    MatrixBrain.getInstance().stop();
                    working = false;
                    System.out.println("Izlazim iz Main-a");
                    break;
                }
            }

        }

    }

    private static Map<String, String> loadConfig(){
        Map<String, String> configMap = new HashMap<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(CONFIG_FILE_PATH))) {

            String line;
            while((line = reader.readLine()) != null){
                line = line.trim();
                if(!line.isEmpty() && !line.startsWith("#")){
                    String[] parts = line.split("=");
                    if(parts.length == 2){
                        String key = parts[0].trim();
                        String value = parts[1].trim();
                        configMap.put(key,value);
                    }

                }
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return configMap;
    }


    private static void multiplyMatrices(boolean async, String matrixA, String matrixB, String resultMatrix){

        Future<Matrix> matrixFuture = MatrixBrain.getInstance().getMatrixFromCacheByNameOrMultipliedBy(matrixA, matrixB, resultMatrix);

        if(matrixFuture == null){

            String message = MatrixBrain.getInstance().multiplyMatrices(matrixA, matrixB, resultMatrix);

            if(!message.equalsIgnoreCase("successful")){
                System.out.println(message);
                return;
            }

            System.out.println("Calculating... " + matrixA + " x " + matrixB);

            if(!async){
//                synchronized (key) {
//                    try {
////                        System.out.println("Ulazim u wait Mainu");
//                        key.wait();
////                        System.out.println("Izlazim iz wait-a u Mainu");
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
                matrixFuture = MatrixBrain.getInstance().getMatrixFromCacheByNameOrMultipliedBy(matrixA, matrixB, resultMatrix);
                if(matrixFuture != null){
                    try {
                        matrixFuture.get();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                    System.out.println("[" + resultMatrix + "] " + "Calculating " + matrixA + " x " + matrixB + " is completed.");
                }
            }
        }else{
            if(!async){
                try {
                    matrixFuture.get();
                    System.out.println("[" + resultMatrix + "] " + "Calculating " + matrixA + " x " + matrixB + " is completed.");
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }


    }


    public static void printInfoAll(ConcurrentLinkedQueue<MatrixInfo> queueInfo){
        for (MatrixInfo q : queueInfo){
            System.out.println(q);
        }
    }

}
