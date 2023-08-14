import java.util.*;

public class Main {
    public static List<Thread> threadList;
    public static int maxKey = 0;
    public static int maxValue = 0;
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) throws InterruptedException {
        threadList = new ArrayList<>();

        Runnable maxrequency = () -> {
            int maxKeyNow = 0;
            int maxValueNow = 0;

            while (!Thread.interrupted()) {
                synchronized (sizeToFreq) {
                    try {
                        sizeToFreq.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
                    Integer key = entry.getKey();
                    Integer value = entry.getValue();
                    if (value > maxValueNow) {
                        maxKeyNow = key;
                        maxValueNow = value;
                    }
                }
                System.out.println("Самое частое количество повторений " + maxKeyNow + " (встретилось " +
                        maxValueNow + " раз)");
            }

        };

        Thread maxThread = new Thread(maxrequency);

        Runnable logic = () -> {
            String text = generateRoute("RLRFR", 100);
            String[] strings = text.split("");
            int count = 0;
            for (String letter : strings) {
                if (letter.equals("R")) {
                    count += 1;
                }
            }
            synchronized (sizeToFreq) {
                if (sizeToFreq.containsKey(count)) {
                    sizeToFreq.put(count, sizeToFreq.get(count) + 1);
                } else {
                    sizeToFreq.put(count, 1);
                }
            }

            System.out.println("колличество букв R - " + count);

            synchronized (sizeToFreq) {
                sizeToFreq.notify();
            }
        };

        maxThread.start();

        for (int i = 0; i < 1000; i++) {
            Thread thread = new Thread(logic);
            thread.start();
            Thread.sleep(30);
            threadList.add(thread);
        }


        for (Thread thread : threadList) {
            thread.join();
        }

        synchronized (sizeToFreq) {
            sizeToFreq.notifyAll();
        }
        Thread.sleep(500);

        maxThread.interrupt();

        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            if (value > maxValue) {
                maxKey = key;
                maxValue = value;
            }
        }

        System.out.println("Самое частое количество повторений " + maxKey + " (встретилось " + maxValue + " раз)");
        System.out.println("Другие размеры: ");
        for (Map.Entry<Integer, Integer> entry : sizeToFreq.entrySet()) {
            Integer key = entry.getKey();
            Integer value = entry.getValue();
            if (key != maxKey) {
                System.out.println("- " + key + " (" + value + " раз)");
            }
        }
    }

    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }
}
