package me.test.util.unix.prococol.ssh2.jce;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import me.test.util.unix.prococol.ssh2.Cipher;


public class AES128CBC implements Cipher {
	private static final int ivsize = 16;
	private static final int bsize = 16;
	private javax.crypto.Cipher cipher;

	public int getIVSize() {
		return ivsize;
	}

	public int getBlockSize() {
		return bsize;
	}

	public void init(int mode, byte[] key, byte[] iv) throws Exception {
		String pad = "NoPadding";
		byte[] tmp;
		if (iv.length > ivsize) {
			tmp = new byte[ivsize];
			System.arraycopy(iv, 0, tmp, 0, tmp.length);
			iv = tmp;
		}
		if (key.length > bsize) {
			tmp = new byte[bsize];
			System.arraycopy(key, 0, tmp, 0, tmp.length);
			key = tmp;
		}

		try {
			SecretKeySpec keyspec = new SecretKeySpec(key, "AES");
			cipher = javax.crypto.Cipher.getInstance("AES/CBC/" + pad);
			cipher.init(
					(mode == ENCRYPT_MODE ? javax.crypto.Cipher.ENCRYPT_MODE
							: javax.crypto.Cipher.DECRYPT_MODE), keyspec,
					new IvParameterSpec(iv));
		} catch (Exception e) {
			// System.out.println(e);
			cipher = null;
		}
	}

	public void update(byte[] foo, int s1, int len, byte[] bar, int s2)
			throws Exception {
		cipher.update(foo, s1, len, bar, s2);
	}
}
