import java.io.*;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;

public class SystemExplorer implements Runnable{

    private String directoryToSearch;
    private static Map<String, FileTime> fileLastModified;
    private final long pauseDurationMillis;

    private final Object mutex = new Object();

    private volatile boolean working;

    public SystemExplorer(String directoryToSearch, long pauseDurationMillis){
        this.directoryToSearch = directoryToSearch;
        this.pauseDurationMillis = pauseDurationMillis;
        fileLastModified = new HashMap<>();
        working = true;
    }

    @Override
    public void run() {
        try {
            while(working){
                exploreDirectory(directoryToSearch);
                Thread.sleep(pauseDurationMillis);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        TaskCoordinator.stop();
        System.out.println("Izlazim is SystemExporera");
    }

    private void exploreDirectory(String directory){
        File dir = new File(directory);
        if(!dir.isDirectory()){
            return;
        }

        File[] files = dir.listFiles();
        if(files != null){
            for(File file : files){
                if(file.isDirectory()){
                    exploreDirectory(file.getAbsolutePath());
                }else if(file.getName().endsWith(".rix")){
                    String filePath = file.getAbsolutePath();

                    //Treba optimizovati, da se ne pravi stalno objekat, nego samo po potrebi
                    MatrixBrain.getInstance().addMatrixInfoToQueueInfo(createMatrixIfo(file));

                    long lastModified = file.lastModified();
                    if(!fileLastModified.containsKey(filePath) || fileLastModified.get(filePath).toMillis() != lastModified){
                        synchronized (TaskCoordinator.blockingQueue){
                            try {
                                TaskCoordinator.blockingQueue.put(new MatrixExtractorTask(file, MatrixExtractor.getInstance().getLimit(), 0, (int)file.length()));
                                TaskCoordinator.blockingQueue.notifyAll();
                                fileLastModified.put(filePath, FileTime.fromMillis(lastModified));
                                System.out.println("nasao sam fajl + " + filePath);
//                                MatrixBrain.getInstance().addMatrixInfoToQueueInfo(createMatrixIfo(file));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    }
                }
            }
        }

    }

    private MatrixInfo createMatrixIfo(File file){
        MatrixInfo matrixInfo = null;
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader(file.getAbsolutePath()))){
            String line = bufferedReader.readLine();

            if(line != null){
                String[] parts = line.split(", ");

                String[] namePart = parts[0].split("=");
                String[] rowsPart = parts[1].split("=");
                String[] colsPart = parts[2].split("=");

                String name = namePart[1];
                String rows = rowsPart[1];
                String cols = colsPart[1];

                matrixInfo = new MatrixInfo(name, Integer.parseInt(rows), Integer.parseInt(cols), file.getName());
//                System.out.println(matrixInfo);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return matrixInfo;
    }

    public void stop(){
        working = false;
    }

    public static void removeFromMap(String path){
        if(fileLastModified.containsKey(path)){
            fileLastModified.remove(path);
        }
    }
}
