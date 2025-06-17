public class Main {
    public static void main(String[] args) {
        CircularBuffer<Integer> buffer = new CircularBuffer<>(10);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 20; i++) {
                    buffer.put(i);
                    System.out.println("Producer added a number "  + i + " to the buffer.");
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Producer");

        Thread consumer = new Thread(() -> {
            try {
                for(int i = 0; i < 20; i++) {
                    Integer element = buffer.get();
                    System.out.println("Consumer got an element " + element);
                    /*
                      Задержку сделал чуть больше для наглядности.
                      Также при запуске будет писаться сначала: "Consumer got an element 0" до того, как
                      "Producer added a number 0 to the buffer.". Судя по всему, это связано с тем, что
                      там висят локи в буффере и лок для get разблокируется быстрее, чем
                      producer thread выводит в консоль "Producer added a number 0 to the buffer."
                      В CircularBuffer я закомментил вывод в консоль, если его раскоментить, то будет ясно, что,
                      на самом деле, сначала producer кладет значение, а потом consumer забирает.
                      */
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Consumer");

        producer.start();
        consumer.start();
    }
}