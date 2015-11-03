
public class Div2 {
    public static int diva(int a) {
        int i = 10/a;
        return i;
    }

    public static int div(int a) {
        diva(a+150);
        return 0;
    }    
    
    public static int test(boolean a) {
        int div = 0;
        if (a==true) {
            return 0;
        }
        else {
            throw new RuntimeException("hi");
        }
    }

    public static void main(String[] args) {
      int i =  diva(3);
    }
}
