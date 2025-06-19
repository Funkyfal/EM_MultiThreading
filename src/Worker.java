import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tasks.MapTask;
import tasks.ReduceTask;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Worker implements Runnable {

    private final Coordinator coordinator;
    private final int numReduce;

    public static final Logger logger = LoggerFactory.getLogger(Worker.class);

    public Worker(Coordinator coordinator, int numReduce) {
        this.coordinator = coordinator;
        this.numReduce = numReduce;
    }

    @Override
    public void run() {
        try {
            MapTask mapTask;
            while ((mapTask = coordinator.getMapTask()) != null) {
                String content = new String(Files.readAllBytes(Paths.get(mapTask.fileName)));
                List<KeyValue> keyValues = map(content);

                Map<Integer, List<KeyValue>> buckets = new HashMap<>();
                for (KeyValue keyValue : keyValues) {
                    int i = Math.floorMod(keyValue.key.hashCode(), numReduce);
                    buckets.computeIfAbsent(i, k -> new ArrayList<>()).add(keyValue);
                }

                for (int i = 0; i < numReduce; i++) {
                    String fileName = String.format("mr-%d-%d", mapTask.id, i);
                    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName))) {
                        List<KeyValue> list = buckets.getOrDefault(i, Collections.emptyList());
                        for (KeyValue keyValue : list) {
                            writer.write(keyValue.key + "\t" + keyValue.value + "\n");
                        }
                    }
                }
                coordinator.mapTaskDone();
            }

            coordinator.awaitMapFinished();

            ReduceTask reduceTask;
            while ((reduceTask = coordinator.getReduceTask()) != null) {
                List<KeyValue> all = new ArrayList<>();
                for (String file : reduceTask.intermediateFiles) {
                    List<String> lines = Files.readAllLines(Paths.get(file));
                    for (String line : lines) {
                        String[] parts = line.split("\t");
                        all.add(new KeyValue(parts[0], parts[1]));
                    }
                }

                all.sort(Comparator.comparing(keyValue -> keyValue.key));

                Map<String, List<String>> groups = new LinkedHashMap<>();
                for (KeyValue keyValue : all) {
                    groups.computeIfAbsent(keyValue.key, k -> new ArrayList<>()).add(keyValue.value);
                }
                String outFile = String.format("mr-%d.txt", reduceTask.id);
                try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outFile))) {
                    for (Map.Entry<String, List<String>> e : groups.entrySet()) {
                        String result = reduce(e.getValue());
                        writer.write(e.getKey() + " " + result + "\n");
                    }
                }
                coordinator.reduceTaskDone();
            }
            coordinator.awaitReduceFinished();

        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred in Worker.run() method");
            e.printStackTrace();
        }
    }

    public List<KeyValue> map(String content) {
        List<KeyValue> list = new ArrayList<>();
        for (String word : content.split("\\W+")) {
            if (!word.isEmpty()) {
                list.add(new KeyValue(word.toLowerCase(), "1"));
            }
        }

        return list;
    }

    public String reduce(List<String> values) {
        int sum = 0;
        for (String value : values) {
            sum += Integer.parseInt(value);
        }

        return String.valueOf(sum);
    }
}
