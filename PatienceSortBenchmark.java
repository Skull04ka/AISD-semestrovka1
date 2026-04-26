import java.io.*;
import java.util.*;

public class PatienceSortBenchmark {

    static class Metrics {
        long iterations = 0;
    }

    public static void main(String[] args) {
        int start = 100;
        int step = 100;
        int maxSize = 10000;

        String dataFolder = "data";

        try {
            generateInputFiles(dataFolder, start, maxSize, step);

            try (PrintWriter csvWriter = new PrintWriter(new FileWriter("patience_sort_comparison.csv"))) {
                csvWriter.println("Structure;Type;Size;Iterations;Time_NS");

                System.out.println("Тестирование массивов: RANDOM");
                runArrayTestSequence(csvWriter, dataFolder, "Random", start, maxSize, step);

                System.out.println("\nТестирование массивов: SORTED");
                runArrayTestSequence(csvWriter, dataFolder, "Sorted", start, maxSize, step);

                System.out.println("\nТестирование коллекций: RANDOM");
                runCollectionTestSequence(csvWriter, dataFolder, "Random", start, maxSize, step);

                System.out.println("\nТестирование коллекций: SORTED");
                runCollectionTestSequence(csvWriter, dataFolder, "Sorted", start, maxSize, step);

                System.out.println("\nГотово! Результаты в файле 'patience_sort_comparison.csv'");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void generateInputFiles(String folderName, int start, int maxSize, int step) throws IOException {
        File folder = new File(folderName);

        if (!folder.exists()) {
            folder.mkdir();
        }

        Random random = new Random();

        for (int size = start; size <= maxSize; size += step) {
            writeArrayToFile(folderName + "/random_" + size + ".txt", generateRandomArray(size, random));
            writeArrayToFile(folderName + "/sorted_" + size + ".txt", generateSortedArray(size));
        }
    }

    private static int[] generateRandomArray(int size, Random random) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(100000);
        }

        return array;
    }

    private static int[] generateSortedArray(int size) {
        int[] array = new int[size];

        for (int i = 0; i < size; i++) {
            array[i] = i;
        }

        return array;
    }

    private static void writeArrayToFile(String fileName, int[] array) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            for (int value : array) {
                writer.print(value);
                writer.print(" ");
            }
        }
    }

    private static int[] readArrayFromFile(String fileName) throws IOException {
        List<Integer> numbers = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextInt()) {
                numbers.add(scanner.nextInt());
            }
        }

        int[] array = new int[numbers.size()];

        for (int i = 0; i < numbers.size(); i++) {
            array[i] = numbers.get(i);
        }

        return array;
    }

    private static List<Integer> readListFromFile(String fileName) throws IOException {
        List<Integer> numbers = new ArrayList<>();

        try (Scanner scanner = new Scanner(new File(fileName))) {
            while (scanner.hasNextInt()) {
                numbers.add(scanner.nextInt());
            }
        }

        return numbers;
    }

    private static void runArrayTestSequence(
            PrintWriter writer,
            String folderName,
            String type,
            int start,
            int end,
            int step
    ) throws IOException {

        for (int size = start; size <= end; size += step) {
            String fileName = folderName + "/" + type.toLowerCase() + "_" + size + ".txt";

            int[] array = readArrayFromFile(fileName);

            Metrics metrics = new Metrics();

            long startTime = System.nanoTime();
            patienceSort(array, metrics);
            long duration = System.nanoTime() - startTime;

            writer.println("Array;" + type + ";" + size + ";" + metrics.iterations + ";" + duration);

            if (size % 1000 == 0) {
                System.out.println(type + " Array - Размер " + size + " ок.");
            }
        }
    }

    private static void runCollectionTestSequence(
            PrintWriter writer,
            String folderName,
            String type,
            int start,
            int end,
            int step
    ) throws IOException {

        for (int size = start; size <= end; size += step) {
            String fileName = folderName + "/" + type.toLowerCase() + "_" + size + ".txt";

            List<Integer> list = readListFromFile(fileName);

            Metrics metrics = new Metrics();

            long startTime = System.nanoTime();
            patienceSort(list, metrics);
            long duration = System.nanoTime() - startTime;

            writer.println("Collection;" + type + ";" + size + ";" + metrics.iterations + ";" + duration);

            if (size % 1000 == 0) {
                System.out.println(type + " Collection - Размер " + size + " ок.");
            }
        }
    }

    public static int[] patienceSort(int[] array, Metrics metrics) {
        List<Deque<Integer>> piles = new ArrayList<>();
        List<Integer> tops = new ArrayList<>();

        for (int value : array) {
            metrics.iterations++;

            int position = findPilePosition(tops, value, metrics);

            if (position == piles.size()) {
                Deque<Integer> pile = new ArrayDeque<>();
                pile.push(value);

                piles.add(pile);
                tops.add(value);
            } else {
                piles.get(position).push(value);
                tops.set(position, value);
            }
        }

        PriorityQueue<int[]> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        for (int i = 0; i < piles.size(); i++) {
            metrics.iterations++;

            if (!piles.get(i).isEmpty()) {
                priorityQueue.add(new int[]{piles.get(i).pollFirst(), i});
            }
        }

        int[] result = new int[array.length];
        int index = 0;

        while (!priorityQueue.isEmpty()) {
            metrics.iterations++;

            int[] current = priorityQueue.poll();
            int value = current[0];
            int pileIndex = current[1];

            result[index++] = value;

            if (!piles.get(pileIndex).isEmpty()) {
                priorityQueue.add(new int[]{piles.get(pileIndex).pollFirst(), pileIndex});
            }
        }

        return result;
    }

    public static List<Integer> patienceSort(List<Integer> list, Metrics metrics) {
        List<Deque<Integer>> piles = new ArrayList<>();
        List<Integer> tops = new ArrayList<>();

        for (int value : list) {
            metrics.iterations++;

            int position = findPilePosition(tops, value, metrics);

            if (position == piles.size()) {
                Deque<Integer> pile = new ArrayDeque<>();
                pile.push(value);

                piles.add(pile);
                tops.add(value);
            } else {
                piles.get(position).push(value);
                tops.set(position, value);
            }
        }

        PriorityQueue<int[]> priorityQueue = new PriorityQueue<>(Comparator.comparingInt(a -> a[0]));

        for (int i = 0; i < piles.size(); i++) {
            metrics.iterations++;

            if (!piles.get(i).isEmpty()) {
                priorityQueue.add(new int[]{piles.get(i).pollFirst(), i});
            }
        }

        List<Integer> result = new ArrayList<>();

        while (!priorityQueue.isEmpty()) {
            metrics.iterations++;

            int[] current = priorityQueue.poll();
            int value = current[0];
            int pileIndex = current[1];

            result.add(value);

            if (!piles.get(pileIndex).isEmpty()) {
                priorityQueue.add(new int[]{piles.get(pileIndex).pollFirst(), pileIndex});
            }
        }

        return result;
    }

    private static int findPilePosition(List<Integer> tops, int value, Metrics metrics) {
        int left = 0;
        int right = tops.size();

        while (left < right) {
            metrics.iterations++;

            int middle = (left + right) / 2;

            if (tops.get(middle) >= value) {
                right = middle;
            } else {
                left = middle + 1;
            }
        }

        return left;
    }
}