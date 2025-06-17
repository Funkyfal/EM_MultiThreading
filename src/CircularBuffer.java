import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CircularBuffer<T> {
    public final T[] buffer;
    public final int capacity;
    private int head = 0;
    private int tail = 0;
    private int current_count = 0;

    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    @SuppressWarnings("unchecked")
    public CircularBuffer(int capacity) {
        this.buffer = (T[]) new Object[capacity];
        this.capacity = capacity;
    }

    public void put(T element) throws InterruptedException {
        lock.lock();
        try {
            while (current_count == capacity) {
                notFull.await();
            }
            buffer[tail] = element;
            tail = (tail + 1) % capacity;
            current_count++;
            notEmpty.signal();
//            System.out.println("Producer added an element " + element + " to the buffer.");
        } finally {
            lock.unlock();
        }
    }

    public T get() throws InterruptedException {
        lock.lock();
        try {
            while (current_count == 0) {
                notEmpty.await();
            }

            T element = buffer[head];
            head = (head + 1) % capacity;
            current_count--;
            notFull.signal();
            return element;
        } finally {
            lock.unlock();
        }
    }
}
