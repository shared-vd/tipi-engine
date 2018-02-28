package ch.sharedvd.tipi.engine.query;

import ch.sharedvd.tipi.engine.utils.ResultListWithCount;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HqlQuery {

    private final StringBuilder hql = new StringBuilder();
    private final Map<String, Object> params = new HashMap<>();

    public HqlQuery() {
    }
    public HqlQuery(String hql) {
        append(hql);
    }

    public HqlQuery append(String hql) {
        this.hql.append(hql);
        return this;
    }

    public HqlQuery append(String hql, String paramName, Object paramValue) {
        this.hql.append(hql);
        this.params.put(paramName, paramValue);
        return this;
    }

    private Query createQuery(EntityManager em) {
        final Query q = em.createQuery(hql.toString());
        for (Map.Entry<String, Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
        return q;
    }

    private Query createCountQuery(EntityManager em) {
        final Query q = em.createQuery("select count(*) "+hql.toString());
        for (Map.Entry<String, Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
        return q;
    }

    public <T> ResultListWithCount<T> getResultListWithCount(EntityManager em, int maxHits) {
        final Query q1 = createCountQuery(em);
        final long count = (Long)q1.getSingleResult();
        final Query q2 = createQuery(em);
        if (maxHits > 0) {
            q2.setMaxResults(maxHits);
        }
        final List<T> resList = q2.getResultList();
        return new ResultListWithCount<T>(resList, count);
    }

    public <T> List<T> getResultList(EntityManager em) {
        Query q = createQuery(em);
        return q.getResultList();
    }

    public <T> T getSingleResult(EntityManager em) {
        Query q = createQuery(em);
        return (T)q.getSingleResult();
    }

}
