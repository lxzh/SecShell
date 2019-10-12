package com.lxzh123.libsag.sag;

/**
 * <T extends SomeClass>
 */
public class TypeVariableItem {
    /**
     * T
     */
    public String tName;

    /**
     * simple name of parent class
     */
    public String parentName;

    @Override
    public String toString() {
        return parentName.equals("Object") ? tName : tName + " extends " + parentName;
    }
}
