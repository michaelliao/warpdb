package com.itranswarp.warpdb.context;

/**
 * UserContext holds current user in current thread.
 * 
 * Using try (resource) { ... } is a MUST:
 * 
 * <code>
 * // start context A:
 * try (UserContext ctx = new UserContext(new User("User-123"))) {
 * 	   User u = UserContext.getCurrentUser(); // User-123
 * }
 * UserContext.getCurrentUser(); // null
 * </code>
 * 
 * @author michael
 */
public class UserContext<T> implements AutoCloseable {

	static final ThreadLocal<Object> current = new ThreadLocal<Object>();

	public UserContext(T user) {
		current.set(user);
	}

	/**
	 * Return current user, or throw exception if no user signed in.
	 * 
	 * @return User object.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getRequiredCurrentUser() {
		Object user = current.get();
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
	public static <T> T getCurrentUser() {
		return (T) current.get();
	}

	@Override
	public void close() {
		current.remove();
	}

}
