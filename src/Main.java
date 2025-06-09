public class Main {
    public static final int MAX = 100;

    public static void main(String[] args) {
        EvenOddPrinter printer = new EvenOddPrinter();

        Thread evenThread = new Thread(() -> {
            for (int i = 0; i <= MAX; i += 2){
                printer.printEven(i);
            }
        }, "EvenThread");

        Thread oddThread = new Thread(() -> {
            for (int i = 1; i <= MAX; i += 2){
                printer.printOdd(i);
            }
        }, "OddThread");

        evenThread.start();
        oddThread.start();

        try {
            evenThread.join();
            oddThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    static class EvenOddPrinter {
        public boolean isEvenTurn = true;

        public synchronized void printEven(int number) {
            try {
                while (!isEvenTurn) {
                    wait();
                }
                System.out.println(number);
                isEvenTurn = false;
                notifyAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public synchronized void printOdd(int number) {
            try {
                while (isEvenTurn) {
                    wait();
                }
                System.out.println(number);
                isEvenTurn = true;
                notifyAll();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}