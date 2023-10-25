public class Main {
    public static void main(String[] args) {
        TransPol transPol = new TransPol("Makumba");
        String s = transPol.readEncryptedMessage();
        System.out.println(s);
    }
}