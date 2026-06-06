package com.hrm.backend.config.prototype;

/**
 * Interface chung định nghĩa hành vi sao chép đối tượng độc lập vùng nhớ (Deep Copy).
 *
 * @param <T> Kiểu của đối tượng được nhân bản.
 */
public interface Prototype<T> {
    T clonePrototype();
}
