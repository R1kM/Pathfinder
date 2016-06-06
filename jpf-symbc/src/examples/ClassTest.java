public class ClassTest {
    int x;
    int y;
    
    public ClassTest () {
        x = 1;
        y = 0;
    }

    public static int test (ClassTest lol) {
        int div = 0;
        if (lol.x == 1) {
            return 0;
        }
        else if (lol.x == 2) {
            return 1;
        } else {
            return 1/div;
        }
    }

    public static void main(String[] args) {
        ClassTest a = new ClassTest();
        int i = test(a);
        System.out.println(i);
    }
}
