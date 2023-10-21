import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class PoliCezar {

    private static final List<String> geometry = List.of("square", "rectangle");
    private static final List<String> encryptingMethod = List.of("spiral", "diagonal");
    private static final Random random = new Random();
    private final String password;
    private char[] passwordAsCharArray;
    private Triplet<Pair<Integer, Integer>, String, Pair<Integer, Integer>> key;
    int repeat;
    int i;
    int p;
    int widthPoint;
    int heightPoint;

    PoliCezar(String password) {
        if (password.length() != 0) {
            this.password = password;
            this.repeat = 1;
            this.i = 0;
            this.p = 0;
            generateKey();
            encrypt();
        } else {
            throw new RuntimeException("Password has not been initialized");
        }
    }

    public void generateKey() {
        int width;
        int height;
        int passwordLength = Math.floorDiv(password.length(), 2);
        String shape = geometry.get(random.nextInt(2));
        String method = encryptingMethod.get(random.nextInt(2));

        switch (shape) {
            case "square":
                passwordLength = passwordLength > 4 ? passwordLength : passwordLength + 4;
                int length = generateLength(4, passwordLength);
                width = length;
                height = length;
                break;
            case "rectangle":
                passwordLength = passwordLength > 4 ? passwordLength : passwordLength + 4;
                width = generateLength(4, passwordLength);
                height = generateLength(4, passwordLength);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + shape);
        }

        Pair<Integer, Integer> size = new Pair<>(width, height);
        Pair<Integer, Integer> startPoint = findStartPoint(method, width, height);
        key = new Triplet<>(size, method, startPoint);
        String keyAsString = String.format("%s;%s;%s;%s;%s", width, height, method, startPoint.getValue0(), startPoint.getValue1());
        saveKeyToFile(keyAsString);
    }

    private Pair<Integer, Integer> findStartPoint(String method, int width, int height) {
        if ("spiral".equalsIgnoreCase(method)) {
            return new Pair<>(roundUp(width, 2), roundUp(height, 2));
        }
        return new Pair<>(width, height);
    }

    private int roundUp(int num, int divisor) {
        return (num + divisor - 1) / divisor;
    }

    private int generateLength(int min, int max) {
        return random.ints(min, max + 1).findFirst().getAsInt();
    }

    private void saveKeyToFile(String key) {
        try {
            File file = new File("key");
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(key);
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void encrypt() {
        switch (key.getValue1()) {
            case "spiral":
                encryptAsSpiral();
            case "diagonal":
        }
    }

    private void encryptAsSpiral() {
        passwordAsCharArray = password.toCharArray();
        Pair<Integer, Integer> size = key.getValue0();
        Pair<Integer, Integer> startPoint = key.getValue2();
        Character[][] shape = new Character[size.getValue0()][size.getValue1()];
        widthPoint = startPoint.getValue0();
        heightPoint = startPoint.getValue1();
        boolean encrypted = false;

        shape[widthPoint][heightPoint] = passwordAsCharArray[0];
        List<String> route = findRoute(size, startPoint);
        while (!encrypted) {
            for (String s : route) {
                encrypted = nextMove(shape, s, i++);
            }
        }
        System.out.println(Arrays.deepToString(shape));
    }

    private boolean nextMove(Character[][] shape, String route, Integer i) {
        if (i != 0 && Math.floorMod(i, 2)==0) {
            repeat++;
        }
        switch (route) {
            case "r":
                for (int l = 0; l < repeat; l++) {
                    if (isOutOfBound()) {
                        return true;
                    }
                    widthPoint = Math.min(widthPoint + 1, shape.length -1);
                    shape[widthPoint][heightPoint] = passwordAsCharArray[p];
                }
                break;
            case "l":
                for (int l = 0; l < repeat; l++) {
                    if (isOutOfBound()) {
                        return true;
                    }
                    widthPoint = Math.max(widthPoint - 1, 0);
                    shape[widthPoint][heightPoint] = passwordAsCharArray[p];
                }
                break;
            case "d":
                for (int l = 0; l < repeat; l++) {
                    if (isOutOfBound()) {
                        return true;
                    }
                    heightPoint = Math.min(heightPoint + 1, shape[1].length -1);
                    shape[widthPoint][heightPoint] = passwordAsCharArray[p];
                }
                break;
            case "g":
                for (int l = 0; l < repeat; l++) {
                    if (isOutOfBound()) {
                        return true;
                    }
                    heightPoint = Math.max(heightPoint - 1, 0);
                    shape[widthPoint][heightPoint] = passwordAsCharArray[p];
                }
                break;
        }
        return false;
    }

    private boolean isOutOfBound() {
        p++;
        return p >= passwordAsCharArray.length;
    }

    private List<String> findRoute(Pair<Integer, Integer> size, Pair<Integer, Integer> startPoint) {
        List<String> order = new ArrayList<>();
        if (size.getValue0() - startPoint.getValue0() >= startPoint.getValue0()) {
            order.add("r");
        } else {
            order.add("l");
        }
        if (size.getValue1() - startPoint.getValue1() >= startPoint.getValue1()) {
            order.add("d");
        } else {
            order.add("g");
        }

        if (order.get(0).equalsIgnoreCase("r")) {
            order.add("l");
        } else {
            order.add("r");
        }
        if (order.get(1).equalsIgnoreCase("d")) {
            order.add("g");
        } else {
            order.add("d");
        }
        return order;
    }
}
