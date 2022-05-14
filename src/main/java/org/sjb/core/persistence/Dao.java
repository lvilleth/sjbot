package org.sjb.core.persistence;

import java.util.List;
import java.util.Optional;

public interface Dao<T,D> {

    T create(T entity);

    T update(T entity);

    void delete(D id);

    Optional<T> get(D id);

    List<T> list();



}
