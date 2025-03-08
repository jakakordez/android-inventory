package org.partkeepr.inventory.api;

public interface IPayloadProvider<T> {
    T Provide() throws Exception;
}
