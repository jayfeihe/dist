package com.eastcom.ipnet.orders.protocol.ssh2.jce;

import java.security.MessageDigest;

import com.eastcom.ipnet.orders.protocol.ssh2.HASH;

public class SHA1 implements HASH {
	MessageDigest md;

	public int getBlockSize() {
		return 20;
	}

	public void init() throws Exception {
		try {
			md = MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void update(byte[] foo, int start, int len) throws Exception {
		md.update(foo, start, len);
	}

	public byte[] digest() throws Exception {
		return md.digest();
	}
}
