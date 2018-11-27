package test.classes;

/**
 * Created by thomas on 23/11/2016.
 */
public class SubExampleClass extends ExampleClass {
    int x = 0;
    public SubExampleClass(Integer x) {
        this.x = abs(x);
    }

    public int abs(int x) {
        return super.abs(x);
    }

}
