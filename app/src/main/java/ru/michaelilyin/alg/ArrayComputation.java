package ru.michaelilyin.alg;

import java.util.Random;

/**
 * Created by micha on 24.04.2017.
 */

public class ArrayComputation {

    public double sumArrayJava(double[] array) {
        double result = 0.0;
        int size = array.length;
        for (int i = 0; i < size; i++) {
            if (i > 0 && i < size - 1) {
                result += array[i-1] * array[i] * array[i+1];
            } else {
                result += array[i];
            }
        }
        return result;
    }

    public native double sumArrayNative(double[] array);

    public double[] genArray(int size) {
        double[] res = new double[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            res[i] = random.nextDouble();
        }
        return res;
    }
}
