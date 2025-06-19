import java.util.Arrays;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CircularBuffer<T> {
    public final T[] buffer;
    public final int capacity;
    private int head = 0;
    private int tail = 0;
    private int currentCount = 0;

    private final Lock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();
    private final Condition notEmpty = lock.newCondition();

    private boolean closed = false;

    @SuppressWarnings("unchecked")
    public CircularBuffer(int capacity) {
        this.buffer = (T[]) new Object[capacity];
        this.capacity = capacity;
    }

    public void put(T element) throws InterruptedException {
        lock.lock();
        try {
            if (closed) {
                throw new IllegalStateException("Buffer is closed.");
            }

            while (currentCount == capacity) {
                notFull.await();
                if (closed) {
                    throw new IllegalStateException("Buffer is closed.");
                }
            }
            buffer[tail] = element;
            tail = (tail + 1) % capacity;
            currentCount++;
            notEmpty.signal();
//            System.out.println("Producer added an element " + element + " to the buffer.");
        } finally {
            lock.unlock();
        }
    }

    public T get() throws InterruptedException {
        lock.lock();
        try {
            if (closed) {
                throw new IllegalStateException("Buffer is closed.");
            }
            while (currentCount == 0) {
                notEmpty.await();
                if (closed) {
                    throw new IllegalStateException("Buffer is closed.");
                }
            }

            T element = buffer[head];
            head = (head + 1) % capacity;
            currentCount--;
            notFull.signal();
            return element;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return currentCount == 0;
        } finally {
            lock.unlock();
        }
    }

    public boolean isFull() {
        lock.lock();
        try {
            return currentCount == capacity;
        } finally {
            lock.unlock();
        }
    }

    public int size() {
        lock.lock();
        try {
            return currentCount;
        } finally {
            lock.unlock();
        }
    }

    public void close() {
        lock.lock();
        try {
            if (closed) return;
            closed = true;
            notFull.signalAll();
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void clear() {
        lock.lock();
        try {
            currentCount = head = tail = 0;
            Arrays.fill(buffer, null);
        } finally {
            lock.unlock();
        }
    }
}
