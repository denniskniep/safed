package de.denniskniep.safed.common.error;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class LazyMetadata {
    private final String key;
    private final Supplier<List<String>> lazyValues;

    private LazyMetadata(String key, Supplier<List<String>>  values) {
        this.key = key;
        this.lazyValues = values;
    }

    public static List<LazyMetadata> list(LazyMetadata...entries){
        return Arrays.stream(entries).toList();
    }

    public static List<LazyMetadata> list(List<LazyMetadata> existing, LazyMetadata...entries){
        List<LazyMetadata> copy = new ArrayList<>(existing);
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