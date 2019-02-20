package credibilitygraphs.model;

import java.util.LinkedList;

public class Performance {

    private static final int SIZE = 10;
    private static final int REPETITIONS = 1000000;
    private static final int MEASUREMENTS = 1000;

    public static void main(String[] args) {
        // addPojos();
        addInts();

        // opInts();
        // opPojos();
    }

    private static void opPojos() {
        long totalArray = 0;

        final QueueArray array = new QueueArray();
        for (int j = 0; j < SIZE + 2; j++) {
            array.increase(new Pojo(j));
        }

        for (int i = 0; i < REPETITIONS; i++) {
            final long start = System.nanoTime();
            array.opObject();
            final long stop = System.nanoTime();
            totalArray += stop - start;
        }

        System.out.printf("AR: %d%n", totalArray);

        long totalLinkedList = 0;

        final QueueLinkedList list = new QueueLinkedList();
        for (int j = 0; j < SIZE + 2; j++) {
            list.increase(new Pojo(j));
        }

        for (int i = 0; i < REPETITIONS; i++) {
            final long start = System.nanoTime();
            list.opObject();
            final long stop = System.nanoTime();
            totalLinkedList += stop - start;
        }

        System.out.printf("LL: %d%n", totalLinkedList);
    }

    private static void opInts() {
        long totalArray = 0;

        final QueueArray array = new QueueArray();
        for (int j = 0; j < SIZE + 2; j++) {
            array.increase(j);
        }

        for (int i = 0; i < REPETITIONS; i++) {
            final long start = System.nanoTime();
            array.opSimple();
            final long stop = System.nanoTime();
            totalArray += stop - start;
        }

        System.out.printf("AR: %d%n", totalArray);

        long totalLinkedList = 0;

        final QueueLinkedList list = new QueueLinkedList();
        for (int j = 0; j < SIZE + 2; j++) {
            list.increase(j);
        }

        for (int i = 0; i < REPETITIONS; i++) {
            final long start = System.nanoTime();
            list.opSimple();
            final long stop = System.nanoTime();
            totalLinkedList += stop - start;
        }

        System.out.printf("LL: %d%n", totalLinkedList);
    }

    public static void addPojos() {
        long totalArray = 0;

        for (int i = 0; i < MEASUREMENTS; i++) {
            final QueueArray array = new QueueArray();

            final long start = System.currentTimeMillis();
            for (int j = 0; j < REPETITIONS; j++) {
                array.increase(new Pojo(j));
            }
            final long stop = System.currentTimeMillis();
            totalArray += stop - start;
        }

        System.out.printf("AR: %d%n", totalArray);

        long totalLinkedList = 0;

        for (int i = 0; i < MEASUREMENTS; i++) {
            final QueueLinkedList list = new QueueLinkedList();

            // insert many times
            final long start = System.currentTimeMillis();
            for (int j = 0; j < REPETITIONS; j++) {
                list.increase(new Pojo(j));
            }
            final long stop = System.currentTimeMillis();
            totalLinkedList += stop - start;
        }

        System.out.printf("LL: %d%n", totalLinkedList);
    }

    public static void addInts() {
        long totalArray = 0;

        for (int i = 0; i < MEASUREMENTS; i++) {
            final QueueArray array = new QueueArray();

            final long start = System.currentTimeMillis();
            for (int j = 0; j < REPETITIONS; j++) {
                array.increase(j);
            }
            final long stop = System.currentTimeMillis();
            totalArray += stop - start;
        }

        System.out.printf("Array increase int: %d%n", totalArray);

        long totalLinkedList = 0;

        for (int i = 0; i < MEASUREMENTS; i++) {
            final QueueLinkedList array = new QueueLinkedList();

            // insert many times
            final long start = System.currentTimeMillis();
            for (int j = 0; j < REPETITIONS; j++) {
                array.increase(j);
            }
            final long stop = System.currentTimeMillis();
            totalLinkedList += stop - start;
        }

        System.out.printf("LinkedList increase int: %d%n", totalLinkedList);
    }

    static class Pojo {
        final double value;

        Pojo(double value) {
            this.value = value;
        }
    }

    static class QueueArray {
        final Pojo[] objects = new Pojo[SIZE];
        final int[] ints = new int[SIZE];

        void increase(Pojo value) {
            System.arraycopy(objects, 0, objects, 1, objects.length - 1);
            objects[0] = value;
        }

        void increase(int value) {
            System.arraycopy(ints, 0, ints, 1, ints.length - 1);
            ints[0] = value;
        }

        double opObject() {
            double sum = 0, weights = 0;
            for (Pojo e : objects) {
                if (e == null) {
                    break;
                } else {
                    sum += e.value;
                }
            }

            return sum;
        }

        int opSimple() {
            int sum = 0;

            if (ints.length == SIZE) {
                for (int i = 0; i < ints.length; i++) {
                    sum += ints[i];
                }
            } else {
                for (int i = 0; i < ints.length; i++) {
                    if (ints[i] > 0) {
                        sum += ints[i];
                    } else {
                        break;
                    }
                }
            }

            return sum;
        }
    }

    static class QueueLinkedList {
        final LinkedList<Pojo> objects = new LinkedList<>();
        final LinkedList<Integer> ints = new LinkedList<>();

        void increase(Pojo value) {
            objects.addFirst(value);

            if (objects.size() > SIZE) {
                objects.removeLast();
            }
        }

        void increase(int value) {
            ints.addFirst(value);

            if (ints.size() > SIZE) {
                ints.removeLast();
            }
        }

        double opObject() {
            double sum = 0, weights = 0;
            for (Pojo e : objects) {
                sum += e.value;
            }

            return sum;
        }

        int opSimple() {
            int sum = 0;
            for (int time : ints) {
                sum += time;
            }

            return sum;
        }
    }
}
