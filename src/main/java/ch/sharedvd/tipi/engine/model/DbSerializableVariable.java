package ch.sharedvd.tipi.engine.model;

import ch.sharedvd.tipi.engine.utils.BlobFactory;
import org.apache.commons.io.IOUtils;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.*;
import java.sql.Blob;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

@Entity
@DiscriminatorValue("serializable")
public class DbSerializableVariable extends DbVariable<Serializable> {

    @Column(name = "BLOB_VALUE")
    private Blob blob;

    protected DbSerializableVariable() {
    }

    public DbSerializableVariable(String key, Serializable value, BlobFactory aBlobFactory) {
        super(key);
        setValue(value, aBlobFactory);
    }

    public Blob getBlob() {
        return blob;
    }

    public void setBlob(Blob b) {
        blob = b;
    }

    private transient Serializable deserialBlob = null;

    @Override
    @Transient
    public Serializable getValue() {
        if (deserialBlob == null) {
            if (getBlob() != null) {
                InputStream bis = null;
                InflaterInputStream iis = null;
                ObjectInputStream ois = null;
                try {
                    if (getBlob().length() > 0) {
                        bis = getBlob().getBinaryStream();
                        iis = new InflaterInputStream(bis);
                        ois = new ObjectInputStream(iis);
                        Serializable serial = (Serializable) ois.readObject();
                        deserialBlob = serial;
                        return deserialBlob;
                    }
                } catch (Exception e) {
                    IOUtils.closeQuietly(bis);
                    IOUtils.closeQuietly(iis);
                    IOUtils.closeQuietly(ois);
                    String msg = "Impossible de lire le blob";
                    throw new RuntimeException(msg, e);
                }
            }
            return null;
        } else {
            return deserialBlob;
        }
    }

    public void setValue(Serializable serial, BlobFactory aBlobFactory) {
        deserialBlob = null;
        ByteArrayOutputStream bos = null;
        DeflaterOutputStream dos = null;
        ObjectOutputStream oos = null;
        try {
            bos = new ByteArrayOutputStream();
            dos = new DeflaterOutputStream(bos);
            oos = new ObjectOutputStream(dos);
            oos.writeObject(serial);
            oos.close();
            setBlob(aBlobFactory.createBlob(bos.toByteArray()));
        } catch (IOException e) {
            String msg = "Impossible de mettre à jour le blob";
            throw new RuntimeException(msg, e);
        } finally {
            IOUtils.closeQuietly(bos);
            IOUtils.closeQuietly(dos);
            IOUtils.closeQuietly(oos);
        }
    }
}
