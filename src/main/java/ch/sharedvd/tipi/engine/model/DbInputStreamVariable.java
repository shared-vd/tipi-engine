package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.utils.BlobFactory;
import ch.sharedvd.tipi.engine.utils.InputStreamHolder;
import org.apache.commons.io.IOUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.InputStream;
import java.sql.Blob;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

@Entity
@DiscriminatorValue("file")
public class DbInputStreamVariable extends DbVariable<InputStreamHolder> {

    @Column(name = "BLOB_VALUE")
    private Blob blob;

    protected DbInputStreamVariable() {
    }

    public DbInputStreamVariable(String key, InputStreamHolder value, BlobFactory aBlobFactory) {
        super(key);
        setValue(value, aBlobFactory);
    }

    public Blob getBlob() {
        return blob;
    }

    public void setBlob(Blob b) {
        blob = b;
    }


    private transient InputStreamHolder deserialBlob = null;

    @Override
    @Transient
    public InputStreamHolder getValue() {
        if (deserialBlob == null) {
            if (getBlob() != null) {
                InflaterInputStream inputStream = null;
                InputStream binaryStream = null;
                try {
                    if (getBlob().length() > 0) {
                        binaryStream = getBlob().getBinaryStream();
                        inputStream = new InflaterInputStream(binaryStream);
                        deserialBlob = new InputStreamHolder(inputStream);
                        return deserialBlob;
                    }
                } catch (Exception e) {
                    IOUtils.closeQuietly(inputStream);
                    IOUtils.closeQuietly(binaryStream);
                    String msg = "Impossible de lire le blob";
                    throw new RuntimeException(msg, e);
                }
            }
            return null;
        } else {
            return deserialBlob;
        }
    }

    public void setValue(InputStreamHolder is, BlobFactory aBlobFactory) {
        deserialBlob = null;
        try (DeflaterInputStream inputStream = new DeflaterInputStream(is.getInputStream())) {
            setBlob(aBlobFactory.createBlob(inputStream));
        } catch (Exception e) {
            String msg = "Impossible de mettre à jour le blob";
            throw new RuntimeException(msg, e);
        }
    }
}

