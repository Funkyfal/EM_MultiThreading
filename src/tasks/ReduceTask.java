package tasks;

import java.util.List;

public class ReduceTask {
    public final int id;
    public final List<String> intermediateFiles;

    public ReduceTask(int id, List<String> intermediateFiles) {
        this.id = id;
        this.intermediateFiles = intermediateFiles;
    }
}
