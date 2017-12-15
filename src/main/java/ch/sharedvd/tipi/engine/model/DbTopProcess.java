package ch.sharedvd.tipi.engine.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("process")
public class DbTopProcess extends DbSubProcess {

    private static final long serialVersionUID = 1L;

}
