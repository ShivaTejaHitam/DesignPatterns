import java.util.Scanner;
public class Settings {
    public static void main(String[] args){
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the preferred furniture style:");
        System.out.println("Modern");
        System.out.println("Antique");
        String factoryName = scanner.nextLine();
        FurnitureFactory factory;
        if(factoryName.equals("Modern")){
            factory = new ModernFactory();
        } else {
            factory = new AntiqueFactory();
        }

        Ikea ikea = new Ikea(factory);
        ikea.display();
    }
}