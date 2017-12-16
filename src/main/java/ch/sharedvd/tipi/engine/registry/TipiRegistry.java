package ch.sharedvd.tipi.engine.registry;

import ch.vd.registre.tipi.action.TopProcess;
import ch.vd.registre.tipi.meta.TopProcessMetaModel;

import java.util.List;

public interface TipiRegistry {

	TopProcessMetaModel getTopProcess(Class<? extends TopProcess> clazz);

	TopProcessMetaModel getTopProcess(String name);

	List<TopProcessMetaModel> getAllTopProcesses();

	List<TopProcessMetaModel> getAllTopProcessesForUI();
}
