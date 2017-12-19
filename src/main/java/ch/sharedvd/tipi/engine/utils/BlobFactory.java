package ch.sharedvd.tipi.engine.utils;

import org.apache.commons.io.IOUtils;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.LobCreator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;

public class BlobFactory {

	private Session session;

	public BlobFactory(Session session) {
		this.session = session;
	}

	public Blob createBlob(byte[] aBytes) {
		return Hibernate.getLobCreator(session).createBlob(aBytes);
	}

	/**
	 * Permet de créer un Blob à partir d'un stream sans indiquer/connaître la taille. Fonctionne uniquement avec jdbc4. Une exception est
	 * lancée si jdbc4 n'est pas présent.
	 * 
	 * @param aInputStream
	 * @return
	 */
	public Blob createBlob(InputStream aInputStream) {

		final LobCreator lobCreator = Hibernate.getLobCreator(session);
//		// JDBC4 -> pas besoin de la taille
//		if (lobCreator instanceof ContextualLobCreator) {
//			// Passage de -1 comme taille: De toutes façons cette valeur n'est pas utilisée en jdbc4
//			return lobCreator.createBlob(aInputStream, -1);
//		}
//		else {
			// Fallback JDBC3
			// On récupère le stream pour connaitre la taille
			final ByteArrayOutputStream bos = new ByteArrayOutputStream();
			try {
				IOUtils.copy(aInputStream, bos);
				return lobCreator.createBlob(new ByteArrayInputStream(bos.toByteArray()), bos.size());
			}
			catch (IOException ioe) {
				throw new RuntimeException(ioe);
			}
//		}
	}

	public Blob createBlob(InputStream aInputStream, long aLength) {
		return Hibernate.getLobCreator(session).createBlob(aInputStream, aLength);
	}

}
