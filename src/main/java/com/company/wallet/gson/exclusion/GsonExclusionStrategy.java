package com.company.wallet.gson.exclusion;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

/**
 * Exclusion strategy for com.google.gson.GsonBuilder not to serialize
 * particular fields with fieldName and Class taken from fullyQualifierdName
 * @author Elena Medvedeva
 */
public class GsonExclusionStrategy implements ExclusionStrategy {

    private Class<?> c;
    private String fieldName;

    public GsonExclusionStrategy(String fullyQualifierdName) throws ClassNotFoundException {
        if(fullyQualifierdName != null) {
            this.c = Class.forName(fullyQualifierdName.substring(0, fullyQualifierdName.lastIndexOf(".")));
            this.fieldName = fullyQualifierdName.substring(fullyQualifierdName.lastIndexOf(".") + 1);
        }
    }

    @Override
    public boolean shouldSkipField(FieldAttributes fieldAttributes) {
        if( fieldAttributes != null) {
            return (fieldAttributes.getDeclaringClass() == c && fieldAttributes.getName().equals(fieldName));
        } else {
            return false;
        }
    }

    @Override
    public boolean shouldSkipClass(Class<?> aClass) {
        return false;
    }
}
