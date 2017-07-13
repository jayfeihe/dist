package me.test.util.unix.prococol.ssh2;

public interface UserInfo {
	String getPassphrase();

	String getPassword();

	boolean promptPassword(String message);

	boolean promptPassphrase(String message);

	boolean promptYesNo(String message);

	void showMessage(String message);
}
