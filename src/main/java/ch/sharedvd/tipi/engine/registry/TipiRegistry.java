package ch.sharedvd.tipi.engine.registry;

import ch.sharedvd.tipi.engine.action.TopProcess;
import ch.sharedvd.tipi.engine.meta.TopProcessMetaModel;

import java.util.List;

public interface TipiRegistry {

    void register(final TopProcessMetaModel tp);

    TopProcessMetaModel getTopProcess(Class<? extends TopProcess> clazz);

    TopProcessMetaModel getTopProcess(String name);

    List<TopProcessMetaModel> getAllTopProcesses();

    List<TopProcessMetaModel> getAllTopProcessesForUI();
}
