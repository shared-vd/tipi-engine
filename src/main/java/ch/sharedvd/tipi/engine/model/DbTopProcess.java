package ch.sharedvd.tipi.engine.model;

import javax.persistence.*;
import java.util.List;

@Entity
@DiscriminatorValue("process")
@NamedQueries({
        @NamedQuery(name = "DbTopProcess.findProcessesByFqn", query = "from DbTopProcess p where p.fqn = (?1) and p.parent is null")
})
public class DbTopProcess extends DbSubProcess {

    // Utile seulement pour la requete:
    //    ActivityRepository.findTopProcessNamesByStateAndReqEnd
    @OneToMany(mappedBy = "process")
    private List<DbActivity> children;

}
