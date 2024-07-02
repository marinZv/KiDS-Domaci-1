import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class MatrixMultiplier {


    private static volatile MatrixMultiplier instance;
    private static volatile Object mutex = new Object();
    private static ForkJoinPool pool;

    public MatrixMultiplier(){
        pool = new ForkJoinPool();
    }

    public static MatrixMultiplier getInstance(){
        MatrixMultiplier result = instance;
        if(result == null){
            synchronized (mutex){
                result = instance;
                if(result == null){
                    instance = result = new MatrixMultiplier();
                }
            }
        }
        return result;
    }

    public void executeTask(Task task){
        MatrixMultiplierTask multiplierTask = (MatrixMultiplierTask) task;
        if(multiplierTask.isPoison()){
            pool.shutdown();
//            synchronized (Main.key){
//                Main.key.notifyAll();
//            }
            System.out.println("Izlazim iz MatrixMultipliera");
            return;
        }

        Future<Matrix> matrixFuture = pool.submit(multiplierTask);
//        MatrixBrain.addMatrixFutureToCacheSync(matrixFuture);
        MatrixBrain.getInstance().addMatrixFutureToCacheSync(matrixFuture);
    }

}
