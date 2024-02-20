package org.partkeepr.inventory;

@FunctionalInterface
public interface Consumer<T> {
    void accept(T var1);
}
