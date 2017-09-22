package test.classes;

/**
 * Created by thomas on 23/11/2016.
 */
public class ExampleClass {
    public ExampleClass() {

    }

    public int abs(int x) {
        if (x > 0) {
            return x;
        }
        return -x;
    }

    public int abs(int x, int y) {
        if (x >  y) {
            return x;
        }
        return -x;
    }


}
