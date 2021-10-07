package com.geekbrains.service;

import java.util.List;
import java.util.Optional;

/**
 * Created by Artem Kropotov on 24.05.2021
 */
public interface CrudService<T, ID> {
    Optional<T> save(T object);

    T remove(T object);

    T removeById(ID id);

    T findById(ID id);

    List<T> findAll();

}
