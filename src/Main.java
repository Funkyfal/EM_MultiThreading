import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        if (args.length < 3) {
            logger.error("You need to configure application: <numReduce> <numWorkers> <input files...>");
            System.exit(1);
        }

        int numReduce = Integer.parseInt(args[0]);
        int numWorkers = Integer.parseInt(args[1]);
        List<String> inputFiles = Arrays.asList(Arrays.copyOfRange(args, 2, args.length));

        Coordinator coordinator = new Coordinator(numReduce, inputFiles);
        ExecutorService executor = Executors.newFixedThreadPool(numWorkers);

        for (int i = 0; i < numWorkers; i++) {
            executor.submit(new Worker(coordinator, numReduce));
        }
        executor.shutdown();
        try {
            boolean terminated = executor.awaitTermination(60, TimeUnit.SECONDS);
            if (!terminated) {
                logger.warn("Executor didn't terminate within the timeout.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Thread was interrupted while waiting Executor termination.");
            throw new RuntimeException(e);
        }
        logger.info("Job has been done");
    }
}