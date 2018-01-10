package org.vt.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Object Byte Array Serializable
 * 
 * @author Zerg Law
 */
public final class ObjectByteArrayOutputStream extends ByteArrayOutputStream {

    private static int lastSize = 512;

    /**
     * ObjectByteArrayOutputStream
     * 
     * @param size
     *            int
     */
    public ObjectByteArrayOutputStream(int size) {
	super(size);
    }

    /**
     * getBytes
     * 
     * @return byte
     */
    public synchronized byte[] getBytes() {
	return this.buf;
    }

    /**
     * serialize
     * 
     * @param serializable
     *            Serializable
     * @param estimatedPayloadSize
     *            int
     * @return ObjectByteArrayOutputStream
     * @throws IOException
     *             IOException
     */
    public static ObjectByteArrayOutputStream serialize(
	    Serializable serializable, int estimatedPayloadSize)
	    throws IOException {
	ObjectByteArrayOutputStream outstr = new ObjectByteArrayOutputStream(
		estimatedPayloadSize);
	ObjectOutputStream objstr = new ObjectOutputStream(outstr);
	objstr.writeObject(serializable);
	objstr.close();
	return outstr;
    }

    /**
     * serialize
     * 
     * @param serializable
     *            Object
     * @return ObjectByteArrayOutputStream
     * @throws IOException
     *             IOException
     */
    public static ObjectByteArrayOutputStream serialize(Object serializable)
	    throws IOException {
	ObjectByteArrayOutputStream outstr = new ObjectByteArrayOutputStream(
		lastSize);
	ObjectOutputStream objstr = new ObjectOutputStream(outstr);
	objstr.writeObject(serializable);
	objstr.close();
	lastSize = outstr.getBytes().length;
	return outstr;
    }

    /**
     * getObject
     * 
     * @param arr
     *            Object
     * @return Object
     * @throws ClassNotFoundException
     *             Object
     * @throws IOException
     *             Object
     */
    public static Object getObject(byte[] arr) throws ClassNotFoundException,
	    IOException {
	return new ObjectInputStream(new ByteArrayInputStream(arr))
		.readObject();
    }
}