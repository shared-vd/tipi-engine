package ch.sharedvd.tipi.engine.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@DiscriminatorValue("subproc")
public class DbSubProcess extends DbActivity {

    @Column(name = "IS_EXECUTED")
    private boolean executed;

    @Column(name = "DATE_START_TERMINATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateStartTerminate; // la date de début du terminate()

    @Column(name = "DATE_END_TERMINATE")
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateEndTerminate; // la date de fin du terminate()

    public boolean isExecuted() {
        return executed;
    }
    public void setExecuted(boolean aExecuted) {
        executed = aExecuted;
    }

    /**
     * La date quand l'activité commence la méthode terminate()
     *
     * @return
     */
    public Date getDateStartTerminate() {
        return dateStartTerminate;
    }
    public void setDateStartTerminate(Date aDateStartTerminate) {
        this.dateStartTerminate = aDateStartTerminate;
    }

    /**
     * La date quand l'activité termine la méthode terminate()
     *
     * @return
     */
    public Date getDateEndTerminate() {
        return dateEndTerminate;
    }
    public void setDateEndTerminate(Date aDateEndTerminate) {
        this.dateEndTerminate = aDateEndTerminate;
    }

}
