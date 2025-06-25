import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasks.MapTask;
import tasks.ReduceTask;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

public class Coordinator {
    private final BlockingQueue<MapTask> mapQueue;
    private final BlockingQueue<ReduceTask> reduceQueue;
    private final int numReduce;
    private final CountDownLatch mapDone;
    private final CountDownLatch reduceDone;

    private static final Logger logger = LoggerFactory.getLogger(Coordinator.class);

    public Coordinator(int numReduce, List<String> inputFiles) {
        this.mapQueue = new LinkedBlockingQueue<>();
        this.reduceQueue = new LinkedBlockingQueue<>();
        this.numReduce = numReduce;
        this.mapDone = new CountDownLatch(inputFiles.size());
        this.reduceDone = new CountDownLatch(numReduce);

        for (int i = 0; i < inputFiles.size(); i++) {
            mapQueue.add(new MapTask(i + 1, inputFiles.get(i)));
        }
    }

    public void mapTaskDone() {
        mapDone.countDown();
    }

    public MapTask getMapTask() {
        return mapQueue.poll();
    }

    public void reduceTaskDone() {
        reduceDone.countDown();
    }

    public ReduceTask getReduceTask() {
        return reduceQueue.poll();
    }

    public void awaitMapFinished() throws InterruptedException {
        mapDone.await();

        for (int r = 0; r < numReduce; r++) {
            List<String> parts = new ArrayList<>();
            try (DirectoryStream<Path> ds = Files
                    .newDirectoryStream(Paths.get("."), String.format("mr-*-" + r))) {
                for (Path p : ds) {
                    parts.add(p.toString());
                }
            } catch (IOException e) {
                logger.error("An error occurred in Map tasks awaiting method.", e);
                throw new RuntimeException("Failed to process Map results.", e);
            }

            reduceQueue.add(new ReduceTask(r + 1, parts));
        }
    }

    public void awaitReduceFinished() throws InterruptedException {
        reduceDone.await();
    }
}
