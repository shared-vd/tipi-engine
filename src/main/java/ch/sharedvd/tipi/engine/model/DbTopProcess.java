package ch.sharedvd.tipi.engine.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue("process")
@NamedQueries({
        @NamedQuery(name = "DbTopProcess.findProcessesByName", query = "from DbTopProcess p where p.name = (?1) and p.parent is null")
})
public class DbTopProcess extends DbSubProcess {
}
