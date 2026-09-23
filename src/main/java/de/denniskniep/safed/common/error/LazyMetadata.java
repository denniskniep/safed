package de.denniskniep.safed.common.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LazyMetadata {

    public static final String TITLE = "title";
    public static final String CURRENT_URL = "currentUrl";
    public static final String TRAFFIC_LOG = "trafficLog";
    public static final String BROWSER_LOGS = "browserLogs";
    public static final String VISIBLE_TEXT = "visibleText";
    public static final String SCREENSHOT = "screenshot";
    public static final String CAPTURED_REQUEST_URL = "capturedRequestUrl";

    private final String key;
    private final Supplier<List<String>> lazyValues;

    private LazyMetadata(String key, Supplier<List<String>>  values) {
        this.key = key;
        this.lazyValues = values;
    }

    public static ArrayList<LazyMetadata> list(LazyMetadata...entries){
        return Arrays.stream(entries).collect(Collectors.toCollection(ArrayList::new));
    }

    public static ArrayList<LazyMetadata> list(List<LazyMetadata> existing, LazyMetadata...entries){
        ArrayList<LazyMetadata> copy = new ArrayList<>(existing);
        copy.addAll(List.of(entries));
        return copy;
    }

    public static LazyMetadata ofOne(String key, Supplier<String> values){
        return new LazyMetadata(key, () -> List.of(values.get()));
    }

    public static LazyMetadata ofList(String key, Supplier<List<String>> values){
        return new LazyMetadata(key, values);
    }

    public String getKey() {
        return key;
    }

    public Supplier<List<String>> getLazyValuesSupplier() {
        return lazyValues;
    }
}