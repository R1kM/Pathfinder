public class Arrays {
    public static int counter(int i, int[] arr) {
        arr[1] = i;
        int a = arr[1];
        int b = 1/a;
        return a;
    }

    public static int counter_bis(int i, int[] arr) {
        int j = arr.length;
        int a = arr[i];
        int b = arr[i];
        int c = 1/a;
        return j;
    }

    public static void main(String[] args) {
        int[] test = {1,2,3};
        int j = counter_bis(1, test);
        int k = counter(1, test);
    }
}
