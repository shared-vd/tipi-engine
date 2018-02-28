package ch.sharedvd.tipi.engine.query;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.HashMap;
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

    public Query createQuery(EntityManager em) {
        final Query q = em.createQuery(hql.toString());
        for (Map.Entry<String, Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
        return q;
    }
}
