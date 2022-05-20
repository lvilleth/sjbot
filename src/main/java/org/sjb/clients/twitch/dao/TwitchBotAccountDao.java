package org.sjb.clients.twitch.dao;

import org.hibernate.Session;
import org.sjb.clients.twitch.models.TwitchBotAccountEntity;
import org.sjb.core.persistence.AbstractDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Singleton
public class TwitchBotAccountDao extends AbstractDao<TwitchBotAccountEntity, UUID> {

    private final Logger log = LoggerFactory.getLogger(TwitchBotAccountDao.class);

    @Inject
    private EntityManagerFactory emf;

    public Optional<Instant> oldestUpdate(){
        Instant result = null;
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            result = em.createQuery(
                    "SELECT t.updated from TwitchBotAccountEntity t where " +
                            "t.updated = ( SELECT min(t2.updated) from TwitchBotAccountEntity t2)"
                    , Instant.class)
                    .setMaxResults(1)
                    .getSingleResult();
            em.getTransaction().commit();
        } catch (NoResultException ignored){
        }finally {
            em.close();
        }
        return Optional.ofNullable(result);
    }

    public void save(List<TwitchBotAccountEntity> list) {
        EntityManager em = emf.createEntityManager();
        int batchSize = 500;
        try {
            Session s = em.unwrap(Session.class);
            s.setJdbcBatchSize(batchSize);
            s.beginTransaction();
            for (int i = 0; i < list.size(); i++) {
                if(i > 0 && i % batchSize == 0){
                    s.flush();
                    s.clear();
                }
                s.persist(list.get(i));
            }
            s.flush();
            s.clear();
            s.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    public List<TwitchBotAccountEntity> listByUsername(Set<String> usernames) {
        List<TwitchBotAccountEntity> result = null;
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            result = em.createQuery("SELECT t from TwitchBotAccountEntity t where t.username in (:usernames)"
                    , TwitchBotAccountEntity.class)
            .setParameter("usernames", usernames)
            .getResultList();
            em.getTransaction().commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            em.close();
        }
        return Optional.ofNullable(result).orElse(List.of());
    }

}
