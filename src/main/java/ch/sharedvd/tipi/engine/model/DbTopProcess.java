package ch.sharedvd.tipi.engine.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue("process")
@NamedQueries({
        @NamedQuery(name = "DbTopProcess.findProcessesByFqn", query = "from DbTopProcess p where p.fqn = (?1) and p.parent is null")
})
public class DbTopProcess extends DbSubProcess {

}
