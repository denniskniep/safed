package de.denniskniep.safed.common.error;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RuntimeExceptionWithMetadata extends RuntimeException {

    private static final Logger LOG = LoggerFactory.getLogger(RuntimeExceptionWithMetadata.class);

    private final HashMap<String, List<String>> metadata;

    public RuntimeExceptionWithMetadata(List<LazyMetadata> metadataSuppliers) {
        super("");
        metadata = retrieveMetaData(metadataSuppliers);
    }

    public RuntimeExceptionWithMetadata(String message, List<LazyMetadata> metadataSuppliers) {
        super(message);
        metadata = retrieveMetaData(metadataSuppliers);
    }

    public RuntimeExceptionWithMetadata(String message, Throwable cause, List<LazyMetadata> metadataSuppliers) {
        super(message, cause);
        metadata = retrieveMetaData(metadataSuppliers);
    }

    public HashMap<String, List<String>> getMetadata() {
        return metadata;
    }

    private static HashMap<String, List<String>> retrieveMetaData(List<LazyMetadata> metadataSuppliers){
        HashMap<String, List<String>> metaData = new HashMap<>();
        for(LazyMetadata entry : metadataSuppliers){
            var key = entry.getKey();
            try{
                List<String> values = entry.getLazyValuesSupplier().get();
                metaData.put(key, values);
            }catch(Exception e){
                LOG.debug("Retrieve MetaData for key '{}' failed: {}", key, e.getMessage(), e);
            }
        }
        return metaData;
    }

    public static List<RuntimeExceptionWithMetadata> findOfTypeRecursive(Throwable t) {
        List<Throwable> flatList = ExceptionUtils.getThrowableList(t);
        List<RuntimeExceptionWithMetadata> exWithMetadata = new ArrayList<>();
        for(Throwable ex : flatList){
            if(ex instanceof RuntimeExceptionWithMetadata){
                exWithMetadata.add((RuntimeExceptionWithMetadata)ex);
            }
        }
        return exWithMetadata;
    }
}
