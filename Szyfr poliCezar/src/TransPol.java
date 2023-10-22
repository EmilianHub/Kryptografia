import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TransPol {

    private static final List<String> geometry = List.of("square", "rectangle");
    private static final List<String> encryptingMethod = List.of("spiral", "diagonal", "square");
    private static final Random random = new Random();
    private final String password;
    private char[] passwordAsCharArray;
    private Triplet<Pair<Integer, Integer>, String, Pair<Integer, Integer>> key;
    int repeat;
    int i;
    int p;
    int widthPoint;
    int heightPoint;

    TransPol(String password) {
        if (!password.isBlank()) {
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

    private Pair<Integer, Integer> getSize() {
        return key.getValue0();
    }

    private Pair<Integer, Integer> getStartPoint() {
        return key.getValue2();
    }

    public void generateKey() {
        int width;
        int height;
        int upperBound = Math.max(roundUp(password.length(), 2), 5);
        int lowerBound = upperBound - 1;
        String shape = geometry.get(random.nextInt(2));
        String method = encryptingMethod.get(random.nextInt(3));

        switch (shape) {
            case "square":
                int length = generateLength(lowerBound, upperBound);
                width = length;
                height = length;
                break;
            case "rectangle":
                width = generateLength(lowerBound, upperBound);
                height = generateLength(lowerBound, upperBound);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + shape);
        }

        Pair<Integer, Integer> size = new Pair<>(height, width);
        Pair<Integer, Integer> startPoint = findStartPoint(method, height, width);
        key = new Triplet<>(size, method, startPoint);
        String keyAsString = String.format("%s;%s;%s;%s;%s",height, width, method, startPoint.getValue0(), startPoint.getValue1());
        saveKeyToFile(keyAsString);
    }

    private Pair<Integer, Integer> findStartPoint(String method, int height, int width) {
        if ("spiral".equalsIgnoreCase(method)) {
            widthPoint = Math.floorDiv(width-1, 2);
            heightPoint = Math.floorDiv(height-1, 2);
        } else {
            widthPoint = width;
            heightPoint = 0;
        }
        return new Pair<>(heightPoint, widthPoint);
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
        passwordAsCharArray = password.toCharArray();
        Character[][] encrypt = new Character[0][0];

        switch (key.getValue1()) {
            case "spiral":
                encrypt = encryptAsSpiral();
                break;
            case "diagonal":
                encrypt = encryptAsDiagonal();
                break;
            case "square":
                encrypt = encryptAsSquare();
                break;
        }
        saveEncryptedText(encrypt);
    }

    private Character[][] encryptAsSpiral() {
        Pair<Integer, Integer> size = getSize();
        Character[][] shape = createShape(size);
        boolean encrypted = false;

        shape[heightPoint][widthPoint] = passwordAsCharArray[0];
        List<String> route = findRoute(size, getStartPoint());
        while (!encrypted) {
            for (String s : route) {
                encrypted = nextMove(shape, s, i++);
            }
        }
        return shape;
    }

    private Character[][] createShape(Pair<Integer, Integer> size) {
        return new Character[size.getValue0()][size.getValue1()];
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
                    widthPoint = Math.min(widthPoint + 1, shape[1].length -1);
                    shape[heightPoint][widthPoint] = passwordAsCharArray[p];
                }
                break;
            case "l":
                for (int l = 0; l < repeat; l++) {
                    if (isOutOfBound()) {
                        return true;
                    }
                    widthPoint = Math.max(widthPoint - 1, 0);
                    shape[heightPoint][widthPoint] = passwordAsCharArray[p];
                }
                break;
            case "d":
                for (int l = 0; l < repeat; l++) {
                    if (isOutOfBound()) {
                        return true;
                    }
                    heightPoint = Math.min(heightPoint + 1, shape.length -1);
                    shape[heightPoint][widthPoint] = passwordAsCharArray[p];
                }
                break;
            case "g":
                for (int l = 0; l < repeat; l++) {
                    if (isOutOfBound()) {
                        return true;
                    }
                    heightPoint = Math.max(heightPoint - 1, 0);
                    shape[heightPoint][widthPoint] = passwordAsCharArray[p];
                }
                break;
        }
        return false;
    }

    private boolean isOutOfBound() {
        p++;
        return p >= passwordAsCharArray.length;
    }

    private Character[][] encryptAsDiagonal() {
        Pair<Integer, Integer> size = getSize();
        Character[][] shape = createShape(size);

        toDiagonal(shape);
        return shape;
    }

    private Character[][] encryptAsSquare() {
        Pair<Integer, Integer> size = getSize();
        Character[][] shape = createShape(size);

        toSquare(shape);
        return shape;
    }

    private void toSquare(Character[][] shape) {
        for (int height = 0; height < shape.length; height ++) {
            for (int width = 0; width < shape[1].length; width ++) {
                shape[width][height] = passwordAsCharArray[p];
                if (isOutOfBound()) {
                    return;
                }
            }
        }
    }


    private void toDiagonal(Character[][] shape) {
        for (int width = shape[1].length -1; width >= 0; width --) {
            for (int height = 0; height <= i; height ++) {
                shape[height][width + height] = passwordAsCharArray[p];
                if (isOutOfBound()) {
                    return;
                }
            }
            i++;
        }
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

    private void saveEncryptedText(Character[][] encryptedText) {
        System.out.println(Arrays.deepToString(encryptedText));
        try {
            File file = new File("message");
            if (!file.exists()) {
                file.createNewFile();
            }
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            for (Character[] characters : encryptedText) {
                for (Character character : characters) {
                    char text = character == null ? '-' : character;
                    bufferedWriter.write(text);
                }
                bufferedWriter.write("$");
            }
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
