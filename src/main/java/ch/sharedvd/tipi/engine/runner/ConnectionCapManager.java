package ch.sharedvd.tipi.engine.runner;

import ch.sharedvd.tipi.engine.command.MetaModelHelper;
import ch.sharedvd.tipi.engine.meta.ActivityMetaModel;
import ch.sharedvd.tipi.engine.utils.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConnectionCapManager {

    private ConnectionCap defaultCap;
    private final List<ConnectionCap> ordered = new ArrayList<ConnectionCap>();
    private final Map<String, ConnectionCap> caps = new HashMap<String, ConnectionCap>();
    private final Map<ConnectionCap, Integer> nbCurrentConcurrents = new HashMap<ConnectionCap, Integer>();
    private final Map<Long, ActivityMetaModel> currentActivities = new HashMap<Long, ActivityMetaModel>();

    public void register(ConnectionCap cap) {
        Assert.notNull(cap);
        Assert.notNull(cap.getName());
        Assert.notNull(cap.getDescription());
        Assert.isTrue(cap.getNbMaxConcurrent() > 0);

        caps.put(cap.getName(), cap);
        ordered.add(cap);
        if (cap.isDefault()) {
            Assert.isNull(defaultCap);
            defaultCap = cap;
        }
        nbCurrentConcurrents.put(cap, 0);
    }

    public int getNbMaxConcurrent(String aName) {
        return caps.get(aName).getNbMaxConcurrent();
    }

    public void setNbMaxConcurrent(String aName, int aNbMaxConcurrent) {
        caps.get(aName).setNbMaxConcurrent(aNbMaxConcurrent);
    }

    public int getNbCurrentConcurrent(String aName) {
        ConnectionCap cap = getCap(aName);
        Assert.notNull(cap);
        return nbCurrentConcurrents.get(cap);
    }

    /**
     * Vérifie s'il reste des connections pour cette activité
     *
     * @param aActivity
     * @return
     */
    public boolean hasConnections(final ActivityMetaModel aActivity) {
        boolean hasRoom = true;
        synchronized (nbCurrentConcurrents) {
            hasRoom = getFreeConnections(defaultCap) > 0;
            if (hasRoom) {
                for (String name : aActivity.getUsedConnections()) {
                    ConnectionCap ct = getCap(name);
                    hasRoom = getFreeConnections(ct) > 0;
                    if (!hasRoom) {
                        break;
                    }
                }
            }
        }
        return hasRoom;
    }

    /**
     * Retourne le nombre de connections disponibles
     *
     * @param aActivity
     * @return
     */
    public int getAvailableConnections(final ActivityMetaModel aActivity) {
        int availableConnections = 0;
        synchronized (nbCurrentConcurrents) {
            availableConnections = getFreeConnections(defaultCap);
            for (String name : aActivity.getUsedConnections()) {
                ConnectionCap ct = getCap(name);
                int freeConnection = getFreeConnections(ct);
                if (freeConnection < availableConnections) {
                    availableConnections = freeConnection;
                }
            }
        }
        return availableConnections;
    }


    public void add(String aActivityName, Long aActivityId) {
        Assert.notNull(aActivityName);
        Assert.notNull(aActivityId);
        ActivityMetaModel amm = MetaModelHelper.getMeta(aActivityName);
        Assert.notNull(amm);
        currentActivities.put(aActivityId, amm);
        synchronized (nbCurrentConcurrents) {
            incCurrent(defaultCap);
            for (String name : amm.getUsedConnections()) {
                ConnectionCap ct = getCap(name);
                incCurrent(ct);
            }
        }
    }

    public void remove(final Long aActivityId) {
        Assert.notNull(aActivityId);
        ActivityMetaModel amm = currentActivities.remove(aActivityId);
        if (null != amm) { // possiblement null pendant les tests...
            synchronized (nbCurrentConcurrents) {
                for (String name : amm.getUsedConnections()) {
                    ConnectionCap ct = getCap(name);
                    decCurrent(ct);
                }
                decCurrent(defaultCap);
            }
        }
    }

    public ConnectionCap getCap(String name) {
        final ConnectionCap cap = caps.get(name);
        Assert.notNull(cap, "Cap not found: " + name);
        return cap;
    }

    private int getFreeConnections(ConnectionCap cap) {
        Assert.notNull(cap);
        Assert.isTrue(nbCurrentConcurrents.get(cap).intValue() >= 0, "Cap: " + cap.getName());
        return cap.getNbMaxConcurrent() - nbCurrentConcurrents.get(cap).intValue();
    }

    private void incCurrent(ConnectionCap cap) {
        Assert.notNull(cap);
        nbCurrentConcurrents.put(cap, Integer.valueOf(nbCurrentConcurrents.get(cap).intValue() + 1));
        Assert.isTrue(nbCurrentConcurrents.get(cap).intValue() >= 0, "Cap: " + cap.getName());
    }

    private void decCurrent(ConnectionCap cap) {
        Assert.notNull(cap);
        nbCurrentConcurrents.put(cap, Integer.valueOf(nbCurrentConcurrents.get(cap).intValue() - 1));
        Assert.isTrue(nbCurrentConcurrents.get(cap).intValue() >= 0, "Cap: " + cap.getName());
    }

    public List<ConnectionCap> getCaps() {
        return ordered;
    }

}
