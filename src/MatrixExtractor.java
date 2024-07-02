import java.io.File;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class MatrixExtractor {

    private static volatile MatrixExtractor instance;
    private static Object mutex = new Object();

    private static ForkJoinPool pool;
    private int limit;

//    public MatrixExtractor(int configLimit){
//        pool = new ForkJoinPool();
//        limit = configLimit;
//    }

    public MatrixExtractor(){
        pool = new ForkJoinPool();
    }


    public static MatrixExtractor getInstance(){
        MatrixExtractor result = instance;
        if(result == null){
            synchronized (mutex){
                result = instance;
                if(result == null){
                    instance = result = new MatrixExtractor();
                }
            }
        }
        return instance;
    }

    public void executeTask(Task task){
        MatrixExtractorTask extractorTask = (MatrixExtractorTask) task;
        if(extractorTask.isPoison()){
            pool.shutdown();
//            synchronized (Main.key){
//                Main.key.notifyAll();
//            }
            System.out.println("Izlazim iz Extractora");
            return;
        }
        Future<Matrix> matrixFuture = pool.submit(extractorTask);
//        MatrixBrain.addMatrixFutureToCacheSync(matrixFuture);
        MatrixBrain.getInstance().addMatrixFutureToCacheSyncExtractor(matrixFuture);
    }


    public void setLimit(int configLimit){
        limit = configLimit;
    }
    public int getLimit(){return this.limit;}

}
