
import java.util.Comparator;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MatrixBrain {

    private static volatile MatrixBrain instance;
    private static Object mutex = new Object();
    private ConcurrentHashMap<Key, Future<Matrix>> cache;
    private ConcurrentLinkedQueue<MatrixInfo> queueInfo;
    private ExecutorService executorService;


    public MatrixBrain(){
        this.cache = new ConcurrentHashMap<>();
        this.queueInfo = new ConcurrentLinkedQueue<>();
        executorService = Executors.newCachedThreadPool();
    }

    public static MatrixBrain getInstance(){
        MatrixBrain result = instance;
        if(result == null){
            synchronized (mutex){
                result = instance;
                if(result == null){
                    instance = result = new MatrixBrain();
                }
            }
        }
        return instance;
    }

    public void addMatrixFutureToCacheSyncExtractor(Future<Matrix> matrixFuture){
        synchronized (mutex){
            try {
                String matrixName = matrixFuture.get().getName();
                String multipliedBy = matrixFuture.get().getMultipliedBy();
                Key key = new Key(matrixName, multipliedBy);
                cache.put(key, matrixFuture);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
//            Main.key.notifyAll();
        }
    }

    public void addMatrixFutureToCacheSync(Future<Matrix> matrixFuture){
        synchronized (Main.key){
            try {
                String matrixName = matrixFuture.get().getName();
                String multipliedBy = matrixFuture.get().getMultipliedBy();
                Key key = new Key(matrixName, multipliedBy);
                cache.put(key, matrixFuture);
                Main.key.notifyAll();
                Main.key.notifyAll();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }


    public String multiplyMatrices(String matrixAName, String matrixBName, String resultMatrixName){

        Matrix matrixA = getMatrixFromCacheByName(matrixAName);
        Matrix matrixB = getMatrixFromCacheByName(matrixBName);

        System.out.println("MatrixA je " + matrixA + ", MatrixB je: " + matrixB);

        String message = "";

        if(matrixA == null){
            message = "Ne postoji matrica " + matrixAName;
            return message;
        }

        if(matrixB == null){
            message = "Ne postoji matrica " + matrixBName;
            return message;
        }

        if(matrixA.getNumCols() != matrixB.getNumCols()){
//            System.out.println("Nemoguce je pomnoziti ove dve matrice, nekompatibilne su za mnozenje!");
            message = "Nemoguce je pomnoziti ove dve matrice, nekompatibilne su za mnozenje!";
            return message;
        }

        Matrix resultMatrix = new Matrix(resultMatrixName, matrixA.getNumRows(), matrixB.getNumCols());

        MatrixMultiplierTask multiplierTask = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix, 0, 0, 0, 0, matrixA.getNumRows(), matrixA.getNumCols(), matrixB.getNumCols());

        try {
            synchronized (TaskCoordinator.blockingQueue){
                TaskCoordinator.blockingQueue.put(multiplierTask);
                TaskCoordinator.blockingQueue.notifyAll();
                TaskCoordinator.blockingQueue.notifyAll();
                System.out.println("Ubacio sam u red task za mnozenje");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        message = "successful";
        return message;
    }

    public Future<Matrix> getMatrixFromCacheByNameOrMultipliedBy(String matrixA, String matrixB, String resultMatrix){

        String multipliedBy = matrixA + "," + matrixB;

        for(Key key : cache.keySet()){
            if(key.getMatrixName().equalsIgnoreCase(resultMatrix) || key.getMultipliedBy().equalsIgnoreCase(multipliedBy)){
                return cache.get(key);
            }
        }

        return null;
    }

    private Matrix getMatrixFromCacheByName(String matrixName){

        for(Key key : cache.keySet()){
            if(key.getMatrixName().equalsIgnoreCase(matrixName)){
                try {
                    return cache.get(key).get();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return null;
    }

    public void addMatrixInfoToQueueInfo(MatrixInfo matrixInfo){
        if(!isContainsInQueueInfo(matrixInfo)){
            queueInfo.add(matrixInfo);
        }
    }

    private boolean isContainsInQueueInfo(MatrixInfo matrixInfo){
        for(MatrixInfo q : queueInfo){
            if(q.getName().equalsIgnoreCase(matrixInfo.getName())){
                return true;
            }
        }
        return false;
    }

    public ConcurrentLinkedQueue<MatrixInfo> sortAscending(){
        return queueInfo.stream()
                .sorted()
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    public ConcurrentLinkedQueue<MatrixInfo> sortDescending(){
        return queueInfo.stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    public ConcurrentLinkedQueue<MatrixInfo> all(){
        return queueInfo;
    }

    public ConcurrentLinkedQueue<MatrixInfo> firstN(int n){
        if(n > queueInfo.size()){
            return queueInfo;
        }
        return queueInfo.stream()
                .limit(10).collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    public ConcurrentLinkedQueue<MatrixInfo> lastN(int n){
        if(n > queueInfo.size()){
            return queueInfo;
        }
        return queueInfo.stream()
                .skip(queueInfo.size() - n)
                .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
    }

    public Optional<MatrixInfo> getMatrixInfoByName(String name){
        return queueInfo.stream()
                .filter(matrixInfo -> matrixInfo.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public Optional<MatrixInfo> getMatrixInfoByPath(String file){
        return queueInfo.stream()
                .filter(matrixInfo -> matrixInfo.getFile().equalsIgnoreCase(file))
                .findFirst();
    }

    public void saveMatrixToFile(String matrixName, String dirPath, String fileName){
        Matrix matrix = getMatrixFromCacheByName(matrixName);

        if(matrix == null){
            System.out.println("Matrix with name " + matrixName + " not found.");
            return;
        }

        SaveMatrixTask saveMatrixTask = new SaveMatrixTask(matrix, dirPath, fileName);
        executorService.submit(saveMatrixTask);

        MatrixInfo matrixInfo = new MatrixInfo(matrix.getName(), matrix.getNumRows(), matrix.getNumCols(), dirPath);
        addMatrixInfoToQueueInfo(matrixInfo);


    }

    public String clearMatrix(String param){

        System.out.println("Usao sam u brisanje Matrice");

        String fileName = null;
        String matrixName = null;

        if(param.endsWith(".rix")){
            Optional<MatrixInfo> matrixInfo = getMatrixInfoByPath(param);
            if(matrixInfo.isPresent()){
                fileName = matrixInfo.get().getFile();
                matrixName = matrixInfo.get().getName();
            }
        }else{
            Optional<MatrixInfo> matrixInfo = getMatrixInfoByName(param);
            if(matrixInfo.isPresent()){
                fileName = matrixInfo.get().getFile();
                matrixName =matrixInfo.get().getName();
            }
        }


        String message = null;
        if(matrixName == null || fileName == null){
            message = "Ne postoji matrica pod tim imenom ili sa tim fajlom";
            return message;
        }

        for(Key key : cache.keySet()){
            if(key.getMatrixName().equalsIgnoreCase(matrixName)){
                cache.remove(key);
                break;
            }
        }

        String finalMatrixName = matrixName;
        queueInfo.removeIf(matrixInfo -> matrixInfo.getName().equalsIgnoreCase(finalMatrixName));

        SystemExplorer.removeFromMap(fileName);

        message = "successful";

        System.out.println("Uspesno sam obrisao matricu");

        return message;
    }

    public void stop(){
        if(executorService != null){
            executorService.shutdown();
            System.out.println("Izlazim iz Matrix Brain-a");
        }
    }

}
