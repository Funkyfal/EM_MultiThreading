import java.util.concurrent.Semaphore;

public class Main {
    public static final int MAX = 100;

    public static void main(String[] args) {
        Semaphore oddSemaphore = new Semaphore(1);
        Semaphore evenSemaphore = new Semaphore(0);

        Runnable oddThread = new OddThread(oddSemaphore, evenSemaphore);
        Runnable evenThread = new EvenThread(oddSemaphore, evenSemaphore);

        Thread realOddThread = new Thread(oddThread);
        Thread realEvenThread = new Thread(evenThread);

        realOddThread.start();
        realEvenThread.start();

        try {
            realOddThread.join();
            realEvenThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static class OddThread implements Runnable{
        private final Semaphore oddSemaphore;
        private final Semaphore evenSemaphore;

        OddThread(Semaphore oddSemaphore, Semaphore evenSemaphore) {
            this.oddSemaphore = oddSemaphore;
            this.evenSemaphore = evenSemaphore;
        }

        @Override
        public void run() {
            for (int i = 1; i <= MAX; i += 2) {
                try {
                    oddSemaphore.acquire();
                    System.out.println(i + " - printed by OddThread");
                    evenSemaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    static class EvenThread implements Runnable{
        private final Semaphore oddSemaphore;
        private final Semaphore evenSemaphore;

        EvenThread(Semaphore oddSemaphore, Semaphore evenSemaphore) {
            this.oddSemaphore = oddSemaphore;
            this.evenSemaphore = evenSemaphore;
        }

        @Override
        public void run() {
            for (int i = 2; i <= MAX; i += 2) {
                try {
                    evenSemaphore.acquire();
                    System.out.println(i + " - printed by EvenThread");
                    oddSemaphore.release();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}