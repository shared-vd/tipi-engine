package ch.sharedvd.tipi.engine.registry;

import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

import java.util.*;

public class TipiRegistryImpl implements TipiRegistry {

    private Map<String, TopProcessMetaModel> topProcess = new HashMap<String, TopProcessMetaModel>();

    private Listener listener;

    public void register(final TopProcessMetaModel tp) {
        Assert.notNull(tp);
        Assert.notNull(tp.getSimpleName());

        final TopProcessMetaModel existing = topProcess.get(tp.getSimpleName());
        if (existing == null) {
            topProcess.put(tp.getFQN(), tp);
            if (listener != null) {
                listener.registered(tp);
            }
        } else {
            if (tp != existing) {
                Assert.isSame(tp, existing); // Meme pointeur si meme nom
            }
        }

    }

    @Override
    public TopProcessMetaModel getTopProcess(Class<? extends TopProcess> clazz) {
        return getTopProcess(clazz.getSimpleName());
    }

    @Override
    public TopProcessMetaModel getTopProcess(String name) {
        TopProcessMetaModel tpmm = topProcess.get(name);
        if (tpmm == null) {
            // Chercher avec le SimpleName
            for (String key : topProcess.keySet()) {
                if (key.endsWith(name)) {
                    return topProcess.get(key);
                }
            }
        }
        return tpmm;
    }

    @Override
    public List<TopProcessMetaModel> getAllTopProcesses() {
        return new ArrayList<TopProcessMetaModel>(topProcess.values());
    }

    @Override
    public List<TopProcessMetaModel> getAllTopProcessesForUI() {
        List<TopProcessMetaModel> availables = new ArrayList<TopProcessMetaModel>();
        for (TopProcessMetaModel meta : topProcess.values()) {
            if (meta.isShownInUI()) {
                availables.add(meta);
            }
        }
        Collections.sort(availables, new Comparator<TopProcessMetaModel>() {
            @Override
            public int compare(TopProcessMetaModel o1, TopProcessMetaModel o2) {
                if (o1 != null && o2 != null) {
                    return o1.getSimpleName().compareTo(o2.getSimpleName());
                }
                return 0;
            }
        });
        return availables;
    }

    public void setListener(Listener l) {
        listener = l;
    }

    public interface Listener {
        void registered(final TopProcessMetaModel tp);
    }

}
