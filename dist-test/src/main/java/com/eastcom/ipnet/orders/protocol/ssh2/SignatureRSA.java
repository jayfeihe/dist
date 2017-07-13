package com.eastcom.ipnet.orders.protocol.ssh2;

public interface SignatureRSA {
	void init() throws Exception;

	void setPubKey(byte[] e, byte[] n) throws Exception;

	void setPrvKey(byte[] d, byte[] n) throws Exception;

	void update(byte[] H) throws Exception;

	boolean verify(byte[] sig) throws Exception;

	byte[] sign() throws Exception;
}
