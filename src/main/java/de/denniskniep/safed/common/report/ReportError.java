package de.denniskniep.safed.common.report;

import de.denniskniep.safed.common.error.RuntimeExceptionWithMetadata;
import de.denniskniep.safed.common.utils.Serialization;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReportError {
    private final String message;
    private final HashMap<String, List<String>> metadata;

    public ReportError(String message) {
        this(message, new HashMap<>());
    }

    public ReportError(String message, HashMap<String, List<String>> metadata) {
        this.message = message;
        this.metadata = metadata;
    }

    public static ReportError from(Throwable e) {
        List<Throwable> exceptionsFlat = ExceptionUtils.getThrowableList(e);

        Throwable firstNonRuntimeExceptionWithMetadata = null;
        List<RuntimeExceptionWithMetadata> exWithMetadata = new ArrayList<>();
        for(Throwable ex : exceptionsFlat){
            if(ex instanceof RuntimeExceptionWithMetadata){
                exWithMetadata.add((RuntimeExceptionWithMetadata)ex);
            }else if(firstNonRuntimeExceptionWithMetadata  == null){
                firstNonRuntimeExceptionWithMetadata = ex;
            }
        }

        HashMap<String, List<String>> metadata = new HashMap<>();
        for(var ex : exWithMetadata) {
            metadata.putAll(ex.getMetadata());
        }
        var type = "";
        if(firstNonRuntimeExceptionWithMetadata != null){
            type = firstNonRuntimeExceptionWithMetadata.getClass().getSimpleName() + ": ";
        }

        var message = type + e.getMessage();
        return new ReportError(message, metadata);
    }

    public String getMessage() {
        return message;
    }

    public HashMap<String, List<String>> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        try {
            return Serialization.AsPrettyJson(this);
        }catch(Exception e) {
            return message;
        }
    }
}
