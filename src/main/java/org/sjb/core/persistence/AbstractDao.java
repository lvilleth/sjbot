package org.sjb.core.persistence;

import java.util.List;
import java.util.Optional;

public abstract class AbstractDao<T,D> implements Dao<T,D> {

    @Override
    public T create(T entity) {
        return null;
    }

    @Override
    public T update(T entity) {
        return null;
    }

    @Override
    public void delete(D id) {
    }

    @Override
    public Optional<T> get(D id) {
        return Optional.empty();
    }

    @Override
    public List<T> list() {
        return List.of();
    }

}
