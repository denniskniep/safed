package de.denniskniep.safed.oidc.auth;

import io.jsonwebtoken.impl.DefaultJwtBuilder;
import io.jsonwebtoken.security.SecureDigestAlgorithm;
import io.jsonwebtoken.security.SecureRequest;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.security.Key;
import io.jsonwebtoken.impl.lang.Function;

public class CustomJwtBuilder extends DefaultJwtBuilder  {

    Function<SecureRequest<InputStream, Key>, byte[]> NO_OP = request -> new byte[0];

    public CustomJwtBuilder doNotSign() {
        setByReflection("signFunction", NO_OP);
        setByReflection("key", null);
        return this;
    }

    private void setByReflection(String fieldName, Object value){
        try {
            Field field = this.getClass().getSuperclass().getDeclaredField(fieldName);

            // Make the private field accessible
            field.setAccessible(true);

            // Set the field value
            field.set(this, value);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Can not set " + fieldName, e);
        }
    }

    public CustomJwtBuilder signWith(SecureDigestAlgorithm<Key, Key> alg) {
        setByReflection("sigAlg", alg);
        return this;
    }
}
