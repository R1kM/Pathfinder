public class Arrays {
    public static int counter(int i, int[] arr) {
        arr[1] = i;
        arr[2] = i;
        int a = arr[1];
        int b = 1/a;
        return a;
    }

    public static int counter_bis(int i, int[] arr) {
        int a = arr[i];
        int b = arr[i];
        int c = 1/a;
        return a;
    }
    
    public static int check_length(int i, int[] arr) {
        int j = arr.length;
        int a = arr[1];
        int b = 10/(i-j);
        return b;
    }

    public static void obj_array(int i, ObjTest[] arr) {
        int a = 1/(arr[i].y);
    }

    public static void main(String[] args) {
        int[] test = {1,2,3};
        ObjTest[] objTest = {new ObjTest(1,2), new ObjTest(1,0)};
        obj_array(0, objTest);
        int j = counter_bis(1, test);
        int k = counter(1, test);
        int b = check_length(2, test);
    }


    public static class ObjTest {
        int x;
        int y;

        public ObjTest(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    
}
