package de.denniskniep.safed.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Serialization {
    public static <T> T DeepCopy(T obj, Class<T> clazz){
        ObjectMapper objectMapper = new ObjectMapper();
        String objAsString = AsJsonString(objectMapper, obj);
        return FromJsonString(objectMapper, objAsString, clazz);
    }

    public static<T> String AsPrettyJson(T obj){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
        prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
        objectMapper.setDefaultPrettyPrinter(prettyPrinter);
        return Serialization.AsJsonString(objectMapper, obj);
    }

    public static<T> String AsJsonString(ObjectMapper objectMapper, T obj){
        String copyAsString;
        try {
            copyAsString = objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can not serialize", e);
        }
        return copyAsString;
    }

    public static<T> T FromJsonString(ObjectMapper objectMapper, String json, Class<T> clazz){
        T copy;
        try {
            copy = objectMapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Can not deserialize",e);
        }
        return copy;
    }
}
