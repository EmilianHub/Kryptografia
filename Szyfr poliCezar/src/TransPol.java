import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;

public class TransPol {

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
    int decodedLetters;
    int foundLetters;

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

    private int getSiftKey(Pair<Integer, Integer> key) {
        return roundUp(key.getValue0() + key.getValue1(), 2);
    }

    public void generateKey() {
        int upperBound = Math.max(roundUp(password.length(), 4), 5);
        int lowerBound = upperBound - 1;
        String method = encryptingMethod.get(random.nextInt(3));

        int length = generateLength(lowerBound, upperBound);
        ///Miało być jeszcze losowanie figury (kwadrat/prostokąt) ale zabrakło czasu
        Pair<Integer, Integer> size = new Pair<>(length, length);
        Pair<Integer, Integer> startPoint = findStartPoint(method, length, length);
        key = new Triplet<>(size, method, startPoint);
        String keyAsString = String.format("%s;%s;%s;%s;%s", length, length, method, startPoint.getValue0(), startPoint.getValue1());
        saveKeyToFile(keyAsString);
    }

    private Pair<Integer, Integer> findStartPoint(String method, int height, int width) {
        if ("spiral".equalsIgnoreCase(method)) {
            widthPoint = Math.floorDiv(width - 1, 2);
            heightPoint = Math.floorDiv(height - 1, 2);
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
            if (!file.exists()) file.createNewFile();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(key);
            bufferedWriter.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String shiftEncryption(String message, int key) {
        StringBuilder encryptedMessage = new StringBuilder();

        for (char character : message.toCharArray()) {
            if (Character.isLetter(character)) {
                char base = Character.isUpperCase(character) ? 'A' : 'a';
                character = (char) (((character - base + key) % 26) + base);
            }

            encryptedMessage.append(character);
        }

        return encryptedMessage.toString();
    }

    public String shiftDecryption(String text, Pair<Integer, Integer> readKey) {
        int key = (26 - getSiftKey(readKey)) % 26;
        return shiftEncryption(text, key);
    }

    public void encrypt() {
        passwordAsCharArray = shiftEncryption(password, getSiftKey(key.getValue2())).toCharArray();
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
        if (i != 0 && Math.floorMod(i, 2) == 0) {
            repeat++;
        }
        switch (route) {
            case "r":
                for (int l = 0; l < repeat; l++) {
                    if (isOutOfBound()) {
                        return true;
                    }
                    widthPoint = Math.min(widthPoint + 1, shape[1].length - 1);
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
                    heightPoint = Math.min(heightPoint + 1, shape.length - 1);
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
        for (int width = 0; width < shape[1].length; width++) {
            for (int height = 0; height < shape.length; height++) {
                shape[height][width] = passwordAsCharArray[p];
                if (isOutOfBound()) {
                    return;
                }
            }
        }
    }


    private void toDiagonal(Character[][] shape) {
        for (int width = shape[1].length - 1; width >= 0; width--) {
            for (int height = 0; height <= i; height++) {
                shape[height][width + height] = passwordAsCharArray[p];
                if (isOutOfBound()) {
                    return;
                }
            }
            i++;
        }
        if (p != passwordAsCharArray.length) {
            for (int height = 1; height < shape.length -1; height++) {
                for (int width = 0; width < i-1; width++) {
                    shape[height + width][width] = passwordAsCharArray[p];
                    if (isOutOfBound()) {
                        return;
                    }
                }
                i--;
            }
        }
    }

    private List<String> findRoute(Pair<Integer, Integer> size, Pair<Integer, Integer> startPoint) {
        List<String> order = new ArrayList<>();
        if (size.getValue0() - startPoint.getValue0() >= startPoint.getValue0()) {
            order.add("r");
            order.add("l");
        } else {
            order.add("l");
            order.add("r");
        }
        if (size.getValue1() - startPoint.getValue1() >= startPoint.getValue1()) {
            order.add(1, "d");
            order.add("g");
        } else {
            order.add(1, "g");
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

    private Triplet<Pair<Integer, Integer>, String, Pair<Integer, Integer>> readKey() {
        try {
            File file = new File("key");
            if (file.exists()) {
                Scanner myReader = new Scanner(file);
                if (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    String[] split = data.split(";");
                    return createReadKey(split);
                }
                myReader.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Couldn't read key");
        }
        return null;
    }

    private Triplet<Pair<Integer, Integer>, String, Pair<Integer, Integer>> createReadKey(String[] split) {
        Pair<Integer, Integer> size = new Pair<>(Integer.valueOf(split[0]), Integer.valueOf(split[1]));
        String method = split[2];
        Pair<Integer, Integer> startPoints = new Pair<>(Integer.valueOf(split[3]), Integer.valueOf(split[4]));
        return new Triplet<>(size, method, startPoints);
    }

    public String decrypt() {
        try {
            File file = new File("message");
            if (file.exists()) {
                Scanner myReader = new Scanner(file);
                if (myReader.hasNextLine()) {
                    String data = myReader.nextLine();
                    return decodeMessageBasedOnKey(data);
                }
                myReader.close();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    private String decodeMessageBasedOnKey(String data) {
        Triplet<Pair<Integer, Integer>, String, Pair<Integer, Integer>> readKey = readKey();
        String[] strings = data.split("\\$");
        i = 0;
        String decryptedMessage = "";
        switch (readKey.getValue1()) {
            case "spiral":
                decryptedMessage = decodeSpiral(readKey, strings);
                break;
            case "diagonal":
                decryptedMessage = decodeDiagonal(readKey, strings);
                break;
            case "square":
                decryptedMessage = decodeSquare(readKey, strings);
                break;
        }
        return shiftDecryption(decryptedMessage, readKey.getValue2());
    }

    private String decodeSquare(Triplet<Pair<Integer, Integer>, String, Pair<Integer, Integer>> readKey, String[] strings) {
        Pair<Integer, Integer> size = readKey.getValue0();
        StringBuilder decodedTextBuilder = new StringBuilder();

        for (int width = 0; width < size.getValue1(); width++) {
            for (int height = 0; height < size.getValue0(); height++) {
                char[] charArray = strings[height].toCharArray();
                char c = charArray[width];
                isNotStopCharacter(c, decodedTextBuilder);
            }
        }
        return decodedTextBuilder.toString();
    }

    private String decodeDiagonal(Triplet<Pair<Integer, Integer>, String, Pair<Integer, Integer>> readKey, String[] strings) {
        Pair<Integer, Integer> size = readKey.getValue0();
        StringBuilder decodedTextBuilder = new StringBuilder();
        countLetters(strings);

        for (int width = size.getValue1() - 1; width >= 0; width--) {
            for (int height = 0; height <= i; height++) {
                char[] charArray = strings[height].toCharArray();
                char c = charArray[width + height];
                isNotStopCharacter(c, decodedTextBuilder);
            }
            i++;
        }
        if (decodedLetters != foundLetters) {
            for (int height = 1; height < size.getValue0() -1; height++) {
                for (int width = 0; width < i-1; width++) {
                    char[] charArray = strings[width + height].toCharArray();
                    char c = charArray[width];
                    isNotStopCharacter(c, decodedTextBuilder);
                }
                i--;
            }
        }
        return decodedTextBuilder.toString();
    }

    private String decodeSpiral(Triplet<Pair<Integer, Integer>, String, Pair<Integer, Integer>> readKey, String[] strings) {
        Pair<Integer, Integer> size = readKey.getValue0();
        Pair<Integer, Integer> startPoints = readKey.getValue2();
        widthPoint = startPoints.getValue1();
        heightPoint = startPoints.getValue0();
        p = 0;
        repeat = 1;
        decodedLetters = 1;
        boolean encrypted = false;
        List<String> route = findRoute(size, startPoints);
        countLetters(strings);
        StringBuilder decodedTextBuilder = new StringBuilder();

        char[] charArray = strings[heightPoint].toCharArray();
        char c = charArray[widthPoint];
        decodedTextBuilder.append(c);

        while (!encrypted) {
            for (String s : route) {
                encrypted = nextMove(strings, s, i++, decodedTextBuilder);
                if (encrypted) break;
            }
        }
        return decodedTextBuilder.toString();
    }

    private void countLetters(String[] strings) {
        int count = 0;
        for (String string : strings) {
            for (char letter : string.toCharArray()) {
                if (letter != '-') count++;
            }
        }
        foundLetters = count;
    }

    private boolean nextMove(String[] strings, String route, Integer i, StringBuilder decodedTextBuilder) {
        if (i != 0 && Math.floorMod(i, 2) == 0) {
            repeat++;
        }
        switch (route) {
            case "r":
                for (int l = 0; l < repeat; l++) {
                    char[] row = strings[heightPoint].toCharArray();
                    widthPoint = Math.min(widthPoint + 1, row.length - 1);
                    char character = row[widthPoint];
                    isNotStopCharacter(character, decodedTextBuilder);
                    if (decodedLetters == foundLetters) {
                        return true;
                    }
                    p++;
                }
                break;
            case "l":
                for (int l = 0; l < repeat; l++) {
                    char[] row = strings[heightPoint].toCharArray();
                    widthPoint = Math.max(widthPoint - 1, 0);
                    char character = row[widthPoint];
                    isNotStopCharacter(character, decodedTextBuilder);
                    if (decodedLetters == foundLetters) {
                        return true;
                    }
                    p++;
                }
                break;
            case "d":
                for (int l = 0; l < repeat; l++) {
                    heightPoint = Math.min(heightPoint + 1, strings.length - 1);
                    char[] row = strings[heightPoint].toCharArray();
                    char character = row[widthPoint];
                    isNotStopCharacter(character, decodedTextBuilder);
                    if (decodedLetters == foundLetters) {
                        return true;
                    }
                    p++;
                }
                break;
            case "g":
                for (int l = 0; l < repeat; l++) {
                    heightPoint = Math.max(heightPoint - 1, 0);
                    char[] row = strings[heightPoint].toCharArray();
                    char character = row[widthPoint];
                    isNotStopCharacter(character, decodedTextBuilder);
                    if (decodedLetters == foundLetters) {
                        return true;
                    }
                    p++;
                }
                break;
        }
        return false;
    }

    private void isNotStopCharacter(char character, StringBuilder decodedTextBuilder) {
        if ('-' != character) {
            decodedTextBuilder.append(character);
            decodedLetters++;
        }
    }
}
