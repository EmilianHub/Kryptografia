import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        System.out.print("Podaj tekst do zaszyforwania: ");
        Scanner input = new Scanner(System.in);
        String message = input.nextLine();

        TransPol transPol = new TransPol(message);
        String s = transPol.readEncryptedMessage();
        System.out.println(s);
    }
}