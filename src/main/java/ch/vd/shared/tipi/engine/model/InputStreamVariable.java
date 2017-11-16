package ch.vd.shared.tipi.engine.model;

import ch.vd.shared.tipi.engine.utils.BlobFactory;
import ch.vd.shared.tipi.engine.utils.InputStreamHolder;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import java.io.InputStream;
import java.util.zip.DeflaterInputStream;
import java.util.zip.InflaterInputStream;

@Entity
@DiscriminatorValue("file")
public class InputStreamVariable extends BlobVariable<InputStreamHolder> {

    private static final long serialVersionUID = -9135399754284748931L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializableVariable.class);


    protected InputStreamVariable() {
    }

    public InputStreamVariable(String key, InputStreamHolder value, BlobFactory aBlobFactory) {
        super(key);
        setValue(value, aBlobFactory);
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
        DeflaterInputStream inputStream = null;
        try {
            inputStream = new DeflaterInputStream(is.getInputStream());
            setBlob(aBlobFactory.createBlob(inputStream));
        } catch (Exception e) {
            String msg = "Impossible de mettre à jour le blob";
            throw new RuntimeException(msg, e);
        } finally {
            IOUtils.closeQuietly(inputStream);
            IOUtils.closeQuietly(is.getInputStream());
        }
    }
}

