package com.itranswarp.warpdb.context;

/**
 * UserContext holds current user in current thread.
 * 
 * Using try (resource) { ... } is a MUST:
 * 
 * <pre>
 * // start context A:
 * try (UserContext ctx = new UserContext(new User("User-123"))) {
 * 	User u = UserContext.getCurrentUser(); // User-123
 * }
 * UserContext.getCurrentUser(); // null
 * </pre>
 * 
 * @author michael
 */
public class UserContext<T extends DbUser> implements AutoCloseable {

	static final ThreadLocal<DbUser> current = new ThreadLocal<DbUser>();

	public UserContext(T user) {
		current.set(user);
	}

	/**
	 * Return current user id, or throw exception if no user signed in.
	 * 
	 * @return User object.
	 */
	public static String getRequiredCurrentUserId() {
		DbUser user = current.get();
		if (user == null) {
			throw new MissingContextException();
		}
		return user.getId();
	}

	/**
	 * Return current user, or throw exception if no user signed in.
	 * 
	 * @return User object.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DbUser> T getRequiredCurrentUser() {
		DbUser user = current.get();
		if (user == null) {
			throw new MissingContextException();
		}
		return (T) user;
	}

	/**
	 * Return current user, or null if no user signed in.
	 * 
	 * @return User object or null.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DbUser> T getCurrentUser() {
		return (T) current.get();
	}

	@Override
	public void close() {
		current.remove();
	}

}
