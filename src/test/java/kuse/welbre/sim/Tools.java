package kuse.welbre.sim;

import kuse.welbre.sim.electrical.abstractt.Element;

import java.util.ArrayList;
import java.util.List;

public class Tools {
    private static double average(List<Double> doubles){
        double sum = 0;
        for (Double v : doubles)
            sum +=v;
        return sum / doubles.size();
    }

    private static double rms(List<Double> doubles){
        double sum = 0;
        for (Double v : doubles)
            sum += Math.pow(v,2);
        return Math.sqrt(sum);
    }

    private static double integral(List<Double> doubles, double dx){
        double sum = 0;
        for (int i = 0; i < doubles.size() - 1; i++)
            sum += (doubles.get(i) + doubles.get(i+1)) * dx / 2;//trapezoidal integral.
        return sum;
    }

    public static class ElementStatusOMeter {
        private final List<Double> voltage = new ArrayList<>();
        private final List<Double> current = new ArrayList<>();
        private final Element element;

        public ElementStatusOMeter(Element element) {
            this.element = element;
        }

        public void tick(double dt){
            voltage.add(element.getVoltageDifference());
            current.add(element.getCurrent());
        }

        public enum method {average,rms,integral}
        public double getVoltage(method m, Object meta){
            switch (m){
                case average -> {
                    return average(voltage);
                }
                case rms -> {
                    return rms(voltage);
                }
                case integral -> integral(voltage, (Double) meta);
            }
            throw new IllegalArgumentException("Method can't be null!");
        }
        public double getCurrent(method m, Object meta){
            switch (m){
                case average -> {
                    return average(current);
                }
                case rms -> {
                    return rms(current);
                }
                case integral -> integral(current, (Double) meta);
            }
            throw new IllegalArgumentException("Method can't be null!");
        }
    }
}
