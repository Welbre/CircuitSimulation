package kuse.welbre.sim;

import kuse.welbre.sim.electrical.*;
import kuse.welbre.sim.electrical.abstractt.Element;
import kuse.welbre.sim.electrical.elements.ACVoltageSource;
import kuse.welbre.sim.electrical.elements.Diode;
import kuse.welbre.sim.electrical.elements.Resistor;
import kuse.welbre.sim.electrical.elements.VoltageSource;
import kuse.welbre.sim.electrical.exemples.Circuits;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chart {

    public static void main(String[] args) throws Exception {
        Circuit c = new Circuit();
        ACVoltageSource v = new ACVoltageSource(10,4);
        Resistor r = new Resistor(1);
        Diode d = new Diode();

        v.connect(r.getPinA(), null);
        d.connect(r.getPinB(), null);

        c.addElement(v,r,d);

        c.setTickRate(0.005);
        String csv = createCsvFromCircuit(c, 2, new PlotConfigs(c)
                .see(0, true, true, false, "v")
                .see(1, true, true, false, "r")
                .see(2, true, true, false, "d")
        );
        c.exportToSpiceNetlist(System.out);

        File file = new File("./ggggkkkkkk.csv");

        FileWriter writer = new FileWriter(file);
        writer.write(csv);
        writer.close();

        Process process = Runtime.getRuntime().exec(new String[]{"py", "C:/Users/welbre/Desktop/mcm/ElectricalSim/Charts/Main.py"});
        process.waitFor();
    }

    public static class ElementData {
        public final double voltage;
        public final double current;
        public final double power;
        public ElementData(Element e) {
            voltage = e.getVoltageDifference();
            current = e.getCurrent();
            power = e.getPower();
        }
    }
    public static class CircuitData {
        private final Map<Element, List<ElementData>> dataMap = new HashMap<>();
        private final PlotConfigs configs;
        private final Circuit circuit;
        private double minY;
        private double maxY;

        public CircuitData(PlotConfigs configs, Circuit circuit) {
            this.configs = configs;
            this.circuit = circuit;
        }

        private void compare(double v) {
            if (v > maxY)
                maxY = v;
            if (v < minY)
                minY = v;
        }

        public void generateAndAddData(Circuit circuit) {
            Element[] elements = circuit.getElements();
            for (int i = 0; i < elements.length; i++) {
                Element element = elements[i];
                dataMap.putIfAbsent(element, new ArrayList<>());
                ElementData elementData = new ElementData(element);
                dataMap.get(element).add(elementData);

                PlotConfigs.ElementConfig config = configs.compiled[i];
                if (config == null) continue;
                double[] dataArray = new double[]{elementData.voltage, elementData.current, elementData.power};

                for (int j = 0; j < dataArray.length; j++)
                    if (config.compiled[j])
                        compare(dataArray[j]);
            }
        }

        public static final class Line {
            private String name;
            private final List<Double> points = new ArrayList<>();

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public void add(double v) {
                points.add(v);
            }
        }
        public Line[] convertToChart(int index){
            List<ElementData> data = dataMap.get(circuit.getElements()[index]);
            PlotConfigs.ElementConfig conf = configs.compiled[index];
            if (data == null || conf == null) return new Line[0];

            Line voltage = null;
            Line current = null;
            Line power = null;

            if (conf.compiled[0]) {
                voltage = new Line();
                if (conf.name.isEmpty())
                    voltage.setName("Voltage");
                else
                    voltage.setName(conf.name + "_Voltage");
            }
            if (conf.compiled[1]) {
                current = new Line();
                if (conf.name.isEmpty())
                    current.setName("Current");
                else
                    current.setName(conf.name + "_Current");
            }
            if (conf.compiled[2]) {
                power = new Line();
                if (conf.name.isEmpty())
                    power.setName("Power");
                else
                    power.setName(conf.name + "_Power");
            }

            for (ElementData eData : data) {
                if (voltage != null) voltage.add(eData.voltage);
                if (current != null) current.add(eData.current);
                if (power != null) power.add(eData.power);
            }

            return new Line[]{voltage, current, power};
        }
        public Line[] convertToChartUsingXAxes(Line xAxes){
            List<Line> list = new ArrayList<>();
            list.add(xAxes);

            for (int i = 0; i < circuit.getElements().length; i++) {
                Line[] chart = convertToChart(i);
                for (Line series : chart)
                    if (series != null)
                        list.add(series);
            }
            return list.toArray(new Line[0]);
        }

        public Line[] convertToChart(){
            Line time = new Line();
            time.setName("Time");
            for (int i = 0; i < dataMap.get(circuit.getElements()[0]).size(); i++)
                time.add(i*this.circuit.getTickRate());

            return convertToChartUsingXAxes(time);
        }
    }
    public static class PlotConfigs {
        public static class ElementConfig {
            protected final boolean[] compiled = new boolean[3];
            protected String name;

            public ElementConfig see(boolean v, boolean i, boolean w, String name) {
                compiled[0] = v;compiled[1] = i;compiled[2] = w;
                this.name = name;
                return this;
            }
            public ElementConfig see(boolean v, boolean i, boolean w) {
                compiled[0] = v;compiled[1] = i;compiled[2] = w;
                this.name = "";
                return this;
            }
            public ElementConfig seeAll() {
                return see(true, true, true, "");
            }
            public ElementConfig seeAll(String name) {
                return see(true, true, true, name);
            }
        }

        public final ElementConfig[] compiled;
        public PlotConfigs(Circuit c){
            compiled = new ElementConfig[c.getElements().length];
        }

        public PlotConfigs seeAll(){
            for (int i = 0; i < compiled.length; i++) {
                compiled[i] = new ElementConfig().seeAll();
            }
            return this;
        }

        public PlotConfigs see(int index, boolean v, boolean i, boolean w) {
            compiled[index] = new ElementConfig().see(v,i,w);
            return this;
        }

        public PlotConfigs see(int index, boolean v, boolean i, boolean w, String name) {
            compiled[index] = new ElementConfig().see(v,i,w,name);
            return this;
        }
    }

    public static String createCsvFromCircuit(Circuit circuit, double time, PlotConfigs configs){
        CircuitData data = new CircuitData(configs, circuit);
        circuit.preCompile();

        data.generateAndAddData(circuit);

        double i = 0;
        while (i < time) {
            circuit.tick(Circuit.DEFAULT_TIME_STEP);
            data.generateAndAddData(circuit);
            i += circuit.getTickRate();
        }

        StringBuilder builder = new StringBuilder();

        CircuitData.Line[] lines = data.convertToChart();
        for (int j = 0; j < lines.length; j++) {
            CircuitData.Line line = lines[j];
            builder.append(line.name);

            if (j < lines.length - 1)
                builder.append(",");
            else
                builder.append(",\n");
        }

        for (int k = 0; k < lines[0].points.size(); k++) {
            for (int j = 0; j < lines.length; j++) {
                CircuitData.Line line = lines[j];

                builder.append(line.points.get(k));

                if (j < lines.length - 1)
                    builder.append(",");
                else
                    builder.append(",\n");
            }
        }

        return builder.toString();
    }
}
