import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;

public class TaskCoordinator implements Runnable{

    public static BlockingQueue<Task> blockingQueue;
//    public ForkJoinPool multiplierPool;
//    public ForkJoinPool extractorPool;
    private static volatile boolean working;

    public TaskCoordinator(){
        blockingQueue = new LinkedBlockingQueue<>();
//        multiplierPool = new ForkJoinPool();
//        extractorPool = new ForkJoinPool();
        working = true;
    }


    @Override
    public void run() {
        while (working) {

//            if(working == false){
//                System.out.println("Usao sam da ubacim poison pill-ove");
//                MatrixExtractor.getInstance().executeTask(new MatrixMultiplierTask(true));
//                MatrixMultiplier.getInstance().executeTask(new MatrixExtractorTask(true));
//                continue;
//            }

            try {
                synchronized (blockingQueue) {
                    if (blockingQueue.isEmpty()) {
                        blockingQueue.wait();
                    }
                }

//                Task task = blockingQueue.take();
                Task task = blockingQueue.poll();

                if(task != null){
                    switch (task.getType()) {
                        case CREATE -> {
                            //
                            MatrixExtractor.getInstance().executeTask(task);
                        }
                        case MULTIPLY -> {
                            //
                            MatrixMultiplier.getInstance().executeTask(task);
                        }
                    }
                }



            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

//        System.out.println("Usao sam da ubacim poison pill-ove");
        Task matrixMultiplierTask = new MatrixMultiplierTask(true);
        Task matrixExtractorTask = new MatrixExtractorTask(true);
        MatrixExtractor.getInstance().executeTask(matrixExtractorTask);
        MatrixMultiplier.getInstance().executeTask(matrixMultiplierTask);
        System.out.println("Izlazim is TaskCoordinatora");
    }

    public static void stop(){
        working = false;
        synchronized (blockingQueue){
            blockingQueue.notifyAll();
//            blockingQueue.notifyAll();
        }
    }
}
