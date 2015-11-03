public class Arrays {
    public static int counter(int[] arr) {
        int i = arr.length;
        int a = arr[2];
        return i;
    }

    public static int counter_bis(int[] arr, int i) {
        int j = arr.length;
        int a = arr[i];
        return j;
    }

    public static void main(String[] args) {
        int[] test = {1,2,3};
        int i = counter(test);
        int j = counter_bis(test, 3);
    }
}
