package me.test.util.unix.prococol.ssh2;

import java.util.Vector;

class UserAuthPublicKey extends UserAuth {
	UserInfo userinfo;

	UserAuthPublicKey(UserInfo userinfo) {
		this.userinfo = userinfo;
	}

	public boolean start(Session session) throws Exception {
		// super.start(session);

		// Vector identities=JSch.identities;
		Vector identities = session.jsch.identities;

		Packet packet = session.packet;
		Buffer buf = packet.buffer;

		String passphrase = null;
		final String username = session.username;

		byte[] _username = null;
		try {
			_username = username.getBytes("UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			_username = username.getBytes();
		}

		for (int i = 0; i < identities.size(); i++) {
			Identity identity = (Identity) (identities.elementAt(i));
			byte[] pubkeyblob = identity.getPublicKeyBlob();

			// System.out.println("UserAuthPublicKey: "+identity+"
			// "+pubkeyblob);

			if (pubkeyblob != null) {
				// send
				// byte SSH_MSG_USERAUTH_REQUEST(50)
				// string user name
				// string service name ("ssh-connection")
				// string "publickey"
				// boolen FALSE
				// string plaintext password (ISO-10646 UTF-8)
				packet.reset();
				buf.putByte((byte) Session.SSH_MSG_USERAUTH_REQUEST);
				buf.putString(_username);
				buf.putString("ssh-connection".getBytes());
				buf.putString("publickey".getBytes());
				buf.putByte((byte) 0);
				buf.putString(identity.getAlgName().getBytes());
				buf.putString(pubkeyblob);
				session.write(packet);

				loop1: while (true) {
					// receive
					// byte SSH_MSG_USERAUTH_PK_OK(52)
					// string service name
					buf = session.read(buf);
					// System.out.println("read: 60 ? "+ buf.buffer[5]);
					if (buf.buffer[5] == Session.SSH_MSG_USERAUTH_PK_OK) {
						break;
					} else if (buf.buffer[5] == Session.SSH_MSG_USERAUTH_FAILURE) {
						// System.out.println("USERAUTH publickey
						// "+session.getIdentity()+
						// " is not acceptable.");
						break;
					} else if (buf.buffer[5] == Session.SSH_MSG_USERAUTH_BANNER) {
						buf.getInt();
						buf.getByte();
						buf.getByte();
						byte[] _message = buf.getString();
						@SuppressWarnings("unused")
						byte[] lang = buf.getString();
						String message = null;
						try {
							message = new String(_message, "UTF-8");
						} catch (java.io.UnsupportedEncodingException e) {
							message = new String(_message);
						}
						if (userinfo != null) {
							userinfo.showMessage(message);
						}
						continue loop1;
					} else {
						// System.out.println("USERAUTH fail
						// ("+buf.buffer[5]+")");
						// throw new JSchException("USERAUTH fail
						// ("+buf.buffer[5]+")");
						break;
					}
				}
				if (buf.buffer[5] != Session.SSH_MSG_USERAUTH_PK_OK) {
					continue;
				}
			}

			// System.out.println("UserAuthPublicKey:
			// identity.isEncrypted()="+identity.isEncrypted());

			int count = 5;
			while (true) {
				if ((identity.isEncrypted() && passphrase == null)) {
					if (userinfo == null)
						throw new SshException("USERAUTH fail");
					if (identity.isEncrypted()
							&& !userinfo.promptPassphrase("Passphrase for "
									+ identity.getName())) {
						throw new SshAuthCancelException("publickey");
						// throw new JSchException("USERAUTH cancel");
						// break;
					}
					passphrase = userinfo.getPassphrase();
				}

				if (!identity.isEncrypted() || passphrase != null) {
					// System.out.println("UserAuthPublicKey: @1 "+passphrase);
					if (identity.setPassphrase(passphrase))
						break;
				}
				passphrase = null;
				count--;
				if (count == 0)
					break;
			}

			// System.out.println("UserAuthPublicKey:
			// identity.isEncrypted()="+identity.isEncrypted());

			if (identity.isEncrypted())
				continue;
			if (pubkeyblob == null)
				pubkeyblob = identity.getPublicKeyBlob();

			// System.out.println("UserAuthPublicKey: pubkeyblob="+pubkeyblob);

			if (pubkeyblob == null)
				continue;

			// send
			// byte SSH_MSG_USERAUTH_REQUEST(50)
			// string user name
			// string service name ("ssh-connection")
			// string "publickey"
			// boolen TRUE
			// string plaintext password (ISO-10646 UTF-8)
			packet.reset();
			buf.putByte((byte) Session.SSH_MSG_USERAUTH_REQUEST);
			buf.putString(_username);
			buf.putString("ssh-connection".getBytes());
			buf.putString("publickey".getBytes());
			buf.putByte((byte) 1);
			buf.putString(identity.getAlgName().getBytes());
			buf.putString(pubkeyblob);

			// byte[] tmp=new byte[buf.index-5];
			// System.arraycopy(buf.buffer, 5, tmp, 0, tmp.length);
			// buf.putString(signature);

			byte[] sid = session.getSessionId();
			int sidlen = sid.length;
			byte[] tmp = new byte[4 + sidlen + buf.index - 5];
			tmp[0] = (byte) (sidlen >>> 24);
			tmp[1] = (byte) (sidlen >>> 16);
			tmp[2] = (byte) (sidlen >>> 8);
			tmp[3] = (byte) (sidlen);
			System.arraycopy(sid, 0, tmp, 4, sidlen);
			System.arraycopy(buf.buffer, 5, tmp, 4 + sidlen, buf.index - 5);
			byte[] signature = identity.getSignature(tmp);
			if (signature == null) { // for example, too long key length.
				break;
			}
			buf.putString(signature);
			session.write(packet);

			loop2: while (true) {
				// receive
				// byte SSH_MSG_USERAUTH_SUCCESS(52)
				// string service name
				buf = session.read(buf);
				// System.out.println("read: 52 ? "+ buf.buffer[5]);
				if (buf.buffer[5] == Session.SSH_MSG_USERAUTH_SUCCESS) {
					return true;
				} else if (buf.buffer[5] == Session.SSH_MSG_USERAUTH_BANNER) {
					buf.getInt();
					buf.getByte();
					buf.getByte();
					byte[] _message = buf.getString();
					@SuppressWarnings("unused")
					byte[] lang = buf.getString();
					String message = null;
					try {
						message = new String(_message, "UTF-8");
					} catch (java.io.UnsupportedEncodingException e) {
						message = new String(_message);
					}
					if (userinfo != null) {
						userinfo.showMessage(message);
					}
					continue loop2;
				} else if (buf.buffer[5] == Session.SSH_MSG_USERAUTH_FAILURE) {
					buf.getInt();
					buf.getByte();
					buf.getByte();
					byte[] foo = buf.getString();
					int partial_success = buf.getByte();
					// System.out.println(new String(foo)+
					// " partial_success:"+(partial_success!=0));
					if (partial_success != 0) {
						throw new SshPartialAuthException(new String(foo));
					}
					break;
				}
				// System.out.println("USERAUTH fail ("+buf.buffer[5]+")");
				// throw new JSchException("USERAUTH fail ("+buf.buffer[5]+")");
				break;
			}
		}
		return false;
	}
}