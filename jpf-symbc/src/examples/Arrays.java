public class Arrays {
    public static int counter(int[] arr) {
        int i = arr.length;
        int a = arr[2];
        return i;
    }

    public static int counter_bis(int i, int[] arr) {
        int j = arr.length;
        int a = arr[i];
        int b = 10/i;
        return j;
    }

    public static void main(String[] args) {
        int[] test = {1,2,3};
        int j = counter_bis(1, test);
    }
}
