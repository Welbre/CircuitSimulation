package kuse.welbre.sim;

import kuse.welbre.tools.Tools;
import sun.misc.Unsafe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Benchmark {
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    public @interface benchmark{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface preBenchmark{}
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.METHOD, ElementType.TYPE})
    @SuppressWarnings("unused")
    public @interface Order {
        int value();
    }

    private final Class<?> mainClass;
    private final Stack<String> classPath = new Stack<>();
    private final Queue<String[]> results = new ArrayDeque<>();

    private static final int DEFAULT_TEST_AMOUNT = 50;

    public Benchmark(Class<?> mainClass) {
        this.mainClass = mainClass;
    }

    public void benchmark(){
        benchmark(DEFAULT_TEST_AMOUNT);
    }

    public void benchmark(int sample){
        prepare_test(mainClass, sample);

        System.out.println("#".repeat(50) + "\n".repeat(3));
        int name_Aliment = 0;
        int warmup_Aliment = 0;
        int time_Aliment = 0;
        for (String[] result : results) {
            if (result[0].length() > name_Aliment)
                name_Aliment = result[0].length();
            if (result[1].length() > warmup_Aliment)
                warmup_Aliment = result[1].length();
            if (result[2].length() > time_Aliment)
                time_Aliment = result[2].length();
        }

        String leftAlignFormat = "| %-" + name_Aliment+ "s | %-" + warmup_Aliment + "s | %-" + time_Aliment + "s |%n";
        String lines = "+" + "-".repeat(name_Aliment + 2) + "+" + "-".repeat(warmup_Aliment + 2) + "+" + "-".repeat(time_Aliment + 2) + "+\n";

        System.out.format(lines);
        System.out.format(head("Method name", "WarmUp", "Test", new int[]{name_Aliment,warmup_Aliment,time_Aliment}));
        System.out.format(lines);

        for (String[] result : results)
            System.out.format(leftAlignFormat, result[0], result[1],result[2]);

        System.out.format(lines);
    }

    private String head(String a, String b, String c, int[] aliment){
        int[] fa = new int[]{
                (int) Math.floor(aliment[0]/2.0 - Math.floor(a.length()/2.0 + 0.5) + 0.5),
                (int) Math.floor(aliment[1]/2.0 - Math.floor(b.length()/2.0 + 0.5) + 0.5),
                (int) Math.floor(aliment[1]/2.0 - Math.floor(c.length()/2.0 + 0.5)+ 0.5)
        };
        return "|" + " ".repeat(fa[0]+1) + a + " ".repeat(fa[0]+1) + " |"
                + " ".repeat(fa[1]+1) + b + " ".repeat(fa[1]+1) + "|"
                + " ".repeat(fa[2]+1) + c + " ".repeat(fa[2]+1) + "|"
                + "\n";
    }

    private void prepare_test(Class<?> aclass, int sample){
        Unsafe unsafe;
        List<Method> methods = new ArrayList<>();
        List<Method> preMethods = new ArrayList<>();
        Map<Method, List<Long>> result = new HashMap<>();
        Map<Method, List<Long>> warmup_result = new HashMap<>();
        System.out.println("Fetching methods");
        try {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe) f.get(null);

            for (Method method : aclass.getDeclaredMethods()) {
                if (method.isAnnotationPresent(benchmark.class)) {
                    methods.add(method);
                    result.put(method, new ArrayList<>());
                    warmup_result.put(method, new ArrayList<>());
                }
                else if (method.isAnnotationPresent(preBenchmark.class)) {
                    preMethods.add(method);
                    result.put(method, new ArrayList<>());
                    warmup_result.put(method, new ArrayList<>());
                }
            }

            //sort
            {
                methods = methods.stream().sorted((method, t1) -> {
                    int v1 = 0, v2 = 0;
                    if (method.isAnnotationPresent(Order.class))
                        v1 = method.getAnnotation(Order.class).value();
                    if (t1.isAnnotationPresent(Order.class))
                        v2 = t1.getAnnotation(Order.class).value();
                    return v1 - v2;
                }).toList();
                preMethods = preMethods.stream().sorted((method, t1) -> {
                    int v1 = 0, v2 = 0;
                    if (method.isAnnotationPresent(Order.class))
                        v1 = method.getAnnotation(Order.class).value();
                    if (t1.isAnnotationPresent(Order.class))
                        v2 = t1.getAnnotation(Order.class).value();
                    return v1 - v2;
                }).toList();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Methods finished");

        classPath.push(aclass.getSimpleName());

        System.out.println("Starting warmup!");

        for (int i = 0; i < sample; i++) {//warm up
            try {
                Object instance = unsafe.allocateInstance(aclass);
                Map<Method, Long> test_result = run_test(instance, preMethods, methods);
                for (Map.Entry<Method, Long> entry : test_result.entrySet())//add a result in results
                    warmup_result.get(entry.getKey()).add(entry.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.printf("\rWarmup [%d/%d]!", i+1, sample);
        }

        System.out.println("\nFinished warmup!");
        System.out.println("Start test!");

        for (int i = 0; i < sample; i++) {
            try {
                Object instance = unsafe.allocateInstance(aclass);
                Map<Method, Long> test_result = run_test(instance, preMethods, methods);
                for (Map.Entry<Method, Long> entry : test_result.entrySet())//add a result in results
                    result.get(entry.getKey()).add(entry.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            System.out.printf("\rTesting [%d/%d]!", i+1, sample);
        }

        System.out.println("\nTest finished!");
        System.out.println("Processing results!");

        for (Method method : preMethods) {
            List<Long> times = result.get(method);
            List<Long> warmup_times = warmup_result.get(method);
            long sum = 0, warm_sum = 0;
            for (Long time : times)
                sum += time;
            for (Long time : warmup_times)
                warm_sum += time;

            results.add(new String[]{
                    "!pre->"+getPath()+"#"+method.getName()+"()",
                    Tools.proprietyToSi(((double) sum /times.size())*1e-9,"s",5),
                    Tools.proprietyToSi(((double) warm_sum /warmup_times.size())*1e-9,"s",5)
            });
        }

        for (Method method : methods) {
            List<Long> times = result.get(method);
            List<Long> warmup_times = warmup_result.get(method);
            long sum = 0, warm_sum = 0;
            for (Long time : times)
                sum += time;
            for (Long time : warmup_times)
                warm_sum += time;

            results.add(new String[]{
                    getPath()+"#"+method.getName()+"()",
                    Tools.proprietyToSi(((double) sum /times.size())*1e-9,"s",5),
                    Tools.proprietyToSi(((double) warm_sum /warmup_times.size())*1e-9,"s",5)
            });
        }

        for (Class<?> bclass : aclass.getDeclaredClasses())
            if (bclass.isAnnotationPresent(benchmark.class))
                prepare_test(bclass, sample);

        classPath.pop();
    }

    private Map<Method,Long> run_test(Object instance, List<Method> preMethods, List<Method> methods){
        Map<Method,Long> result = new HashMap<>(preMethods.size() + methods.size());

        for (Method method : preMethods) {//pre test
            long t0,t1;
            try {
                t0 = System.nanoTime();
                method.invoke(instance);
                t1 = System.nanoTime();
                result.put(method, (t1-t0));
            } catch (Exception e) {
                throw new RuntimeException(e);//abort
            }
        }

        for (Method method : methods) {//test
            long t0, t_final;
            try {
                t0 = System.nanoTime();
                method.invoke(instance);
                t_final = System.nanoTime();

                result.put(method, t_final - t0);
            } catch (Exception e) {
                result.put(method, null);
            }
        }
        return result;
    }

    private String getPath(){
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < classPath.size(); i++) {
            builder.append(classPath.get(i));
            if (i < classPath.size() - 1)
                builder.append("->");
        }
        return builder.toString();
    }

    public static void main(String[] args) {
        new Benchmark(CircuitTest.class).benchmark();
    }
}
