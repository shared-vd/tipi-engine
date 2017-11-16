package ch.vd.shared.tipi.engine.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("process")
public class TopProcessModel extends SubProcessModel {

	private static final long serialVersionUID = 1L;

}
