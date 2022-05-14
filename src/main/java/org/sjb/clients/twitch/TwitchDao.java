package org.sjb.clients.twitch;

import org.sjb.clients.twitch.models.TwitchEntity;
import org.sjb.core.persistence.AbstractDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Singleton
public class TwitchDao extends AbstractDao<TwitchEntity, UUID> {

    private final Logger log = LoggerFactory.getLogger(TwitchDao.class);

    @Inject
    private EntityManagerFactory emf;

    public Optional<TwitchEntity> findByLogin(String login) {
        TwitchEntity result = null;
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
             result = em.createQuery("SELECT t FROM TwitchEntity t WHERE t.login = :login", TwitchEntity.class)
                    .setParameter("login", login)
                    .setMaxResults(1)
                    .getSingleResult();
            em.getTransaction().commit();
        } catch (NoResultException ignored) {
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return Optional.ofNullable(result);
    }

    @Transactional
    @Override
    public TwitchEntity create(TwitchEntity entity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entity);
            entity = em.getReference(TwitchEntity.class, entity.getId());
            em.getTransaction().commit();
            log.debug("created: " + entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return entity;
    }

    @Override
    public TwitchEntity update(TwitchEntity entity) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            entity = em.merge(entity);
            em.getTransaction().commit();
            log.debug("updated: " + entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return entity;
    }

    @Override
    public List<TwitchEntity> list() {
        List<TwitchEntity> result;
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        result = em.createQuery("SELECT t from TwitchEntity t", TwitchEntity.class).getResultList();
        em.getTransaction().commit();
        em.close();
        return result;
    }

    @Override
    public Optional<TwitchEntity> get(UUID id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        TwitchEntity entity = em.find(TwitchEntity.class, id);
        em.getTransaction().commit();
        em.close();
        return Optional.ofNullable(entity);
    }

    @Override
    public void delete(UUID id) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        get(id).ifPresent(e ->{
                em.remove(e);
                log.debug("deleted: " + e);
        });
        em.getTransaction().commit();
        em.close();
    }
}
