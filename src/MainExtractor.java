import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class MainExtractor {

    public static void main(String[] args) {

        ForkJoinPool forkJoinPool1 = new ForkJoinPool();
        File file = new File("matrix.txt");
        Future<Matrix> matrixFuture = forkJoinPool1.submit(new MatrixExtractorTask(file, 1024,0, (int)file.length()));

        try {
            System.out.println(matrixFuture.get());
//            matrixFuture.get();
//            matrixFuture.get().printNonZeroElementsCount();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

    }

}
