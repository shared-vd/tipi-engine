package ch.sharedvd.tipi.engine.query;

import ch.sharedvd.tipi.engine.utils.ResultListWithCount;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HqlQuery {

    private final StringBuilder select = new StringBuilder();
    private final StringBuilder from = new StringBuilder();
    private final StringBuilder where = new StringBuilder();
    private final Map<String, Object> params = new HashMap<>();
    private final StringBuilder order = new StringBuilder();

    public HqlQuery() {
    }
    public HqlQuery(String select, String from ) {
        this.select.append(select);
        this.from.append(from);
    }

    public HqlQuery select(String hql) {
        this.select.append(" ").append(hql);
        return this;
    }

    public HqlQuery from(String hql) {
        this.from.append(" ").append(hql);
        return this;
    }

    public HqlQuery where(String hql) {
        this.where.append(" ").append(hql);
        return this;
    }
    public HqlQuery where(String hql, String paramName, Object paramValue) {
        this.where.append(" ").append(hql);
        this.params.put(paramName, paramValue);
        return this;
    }

    public HqlQuery order(String hql) {
        this.order.append(" ").append(hql);
        return this;
    }

    private Query createQuery(EntityManager em) {
        final Query q = em.createQuery(concat(false));
        for (Map.Entry<String, Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
        return q;
    }

    private Query createCountQuery(EntityManager em) {
        final Query q = em.createQuery(concat(true));
        for (Map.Entry<String, Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
        return q;
    }

    public <T> ResultListWithCount<T> getResultListWithCount(EntityManager em, int maxHits) {
        final Query q1 = createCountQuery(em);
        final long count = (Long) q1.getSingleResult();
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
        return (T) q.getSingleResult();
    }

    private String concat(boolean isForCount) {
        final StringBuilder str = new StringBuilder();

        if (isForCount) {
            str.append("select count(*) ");
        }
        else if (select.length() > 0) {
            str.append("select ").append(select).append(" ");
        }

        if (from.length() > 0) {
            str.append("from ").append(from).append(" ");
        }

        if (where.length() > 0) {
            str.append("where ").append(where).append(" ");
        }

        if (!isForCount && order.length() > 0) {
            str.append("order by ").append(order);
        }
        return str.toString();
    }
}
