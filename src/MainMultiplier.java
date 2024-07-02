import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class MainMultiplier {
    public static void main(String[] args) {

        //Kreiranje matrica za testiranje

//        Matrix matrixA = new Matrix("MatrixA", 3, 4);
//        Matrix matrixB = new Matrix("MatrixB", 4, 3);
//        Matrix resultMatrix = new Matrix("ResultMatrix", 4 , 4);
//
//        matrixA.setElement(0, 0, 1);
//        matrixA.setElement(0, 1, 2);
//        matrixA.setElement(0, 2, 3);
//        matrixA.setElement(0, 3, 4);
//        matrixA.setElement(1, 0, 1);
//        matrixA.setElement(1, 1, 2);
//        matrixA.setElement(1, 2, 3);
//        matrixA.setElement(1, 3, 4);
//        matrixA.setElement(2, 0, 1);
//        matrixA.setElement(2, 1, 2);
//        matrixA.setElement(2, 2, 3);
//        matrixA.setElement(2, 3, 4);
//
//        matrixB.setElement(0, 0, 1);
//        matrixB.setElement(0, 1, 2);
//        matrixB.setElement(0, 2, 3);
//        matrixB.setElement(1, 0, 1);
//        matrixB.setElement(1, 1, 2);
//        matrixB.setElement(1, 2, 3);
//        matrixB.setElement(2, 0, 1);
//        matrixB.setElement(2, 1, 2);
//        matrixB.setElement(2,2, 3);
//        matrixB.setElement(3, 0, 1);
//        matrixB.setElement(3, 1, 2);
//        matrixB.setElement(3, 2, 3);

//        Matrix matrixA = new Matrix("MatrixA", 3, 3);
//        Matrix matrixB = new Matrix("MatrixB", 3, 3);
//        Matrix resultMatrix = new Matrix("ResultMatrix", 3, 3);
//
//        matrixA.setElement(0, 0, 1);
//        matrixA.setElement(0, 1, 2);
//        matrixA.setElement(0, 2, 3);
//        matrixA.setElement(1, 0, 4);
//        matrixA.setElement(1, 1, 5);
//        matrixA.setElement(1, 2, 6);
//        matrixA.setElement(2,0, 7);
//        matrixA.setElement(2,1, 8);
//        matrixA.setElement(2, 2, 9);
//
//        matrixB.setElement(0, 0, 10);
//        matrixB.setElement(0, 1, 11);
//        matrixB.setElement(0, 2, 12);
//        matrixB.setElement(1, 0, 13);
//        matrixB.setElement(1, 1, 14);
//        matrixB.setElement(1, 2, 15);
//        matrixB.setElement(2,0, 16);
//        matrixB.setElement(2,1, 17);
//        matrixB.setElement(2, 2, 18);

        Matrix matrixA = new Matrix("MatrixA", 2, 3);
        Matrix matrixB = new Matrix("MatrixB", 3, 3);
        Matrix resultMatrix = new Matrix("ResultMatrix", 2, 3);

        matrixA.setElement(0, 0, 1);
        matrixA.setElement(0, 1, 2);
        matrixA.setElement(0, 2, 3);
        matrixA.setElement(1, 0, 1);
        matrixA.setElement(1, 1, 2);
        matrixA.setElement(1, 2, 3);

        matrixB.setElement(0, 0, 1);
        matrixB.setElement(0, 1, 2);
        matrixB.setElement(0, 2, 3);
        matrixB.setElement(1, 0, 1);
        matrixB.setElement(1, 1, 2);
        matrixB.setElement(1, 2, 3);
        matrixB.setElement(2, 0, 1);
        matrixB.setElement(2, 1, 2);
        matrixB.setElement(2, 2, 3);


        ForkJoinPool forkJoinPool = new ForkJoinPool();

//        MatrixMultiplierTask task = new MatrixMultiplierTask(matrixB, matrixA, resultMatrix,0, 0, 0, 0,4, 3, 4);
        MatrixMultiplierTask task = new MatrixMultiplierTask(matrixA, matrixB, resultMatrix,0, 0, 0, 0,2, 3, 3);


        Future<Matrix> futureResult = forkJoinPool.submit(task);

        try {
            resultMatrix = futureResult.get();
            System.out.println(resultMatrix);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

//        System.out.println("Result Matrix");
//        for (int i = 0; i < resultMatrix.getNumRows(); i++) {
//            for (int j = 0; j < resultMatrix.getNumCols(); j++) {
//                System.out.println(resultMatrix.getElement(i, j) + " ");
//            }
//            System.out.println();
//        }


    }

}