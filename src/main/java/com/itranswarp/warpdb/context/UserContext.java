package com.itranswarp.warpdb.context;

import com.itranswarp.warpdb.entity.BaseEntity;

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
public class UserContext<T extends BaseEntity> implements AutoCloseable {

	static final ThreadLocal<BaseEntity> current = new ThreadLocal<BaseEntity>();

	public UserContext(T user) {
		current.set(user);
	}

	/**
	 * Return current user id, or throw exception if no user signed in.
	 * 
	 * @return User object.
	 */
	public static String getRequiredCurrentUserId() {
		BaseEntity user = current.get();
		if (user == null) {
			throw new MissingContextException();
		}
		return user.id;
	}

	/**
	 * Return current user, or throw exception if no user signed in.
	 * 
	 * @return User object.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends BaseEntity> T getRequiredCurrentUser() {
		BaseEntity user = current.get();
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
	public static <T extends BaseEntity> T getCurrentUser() {
		return (T) current.get();
	}

	@Override
	public void close() {
		current.remove();
	}

}
