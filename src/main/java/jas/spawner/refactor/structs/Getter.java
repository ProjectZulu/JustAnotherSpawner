package jas.spawner.refactor.structs;

import akka.actor.dsl.Inbox.Get;

public interface Getter<T> {
	public T get();

	public static interface Setter<T> {
		public void set(T newObject);
	}

	public static class NonNullGetSet<T> implements Getter<T>, Setter<T> {
		private T object;

		public NonNullGetSet(T object) {
			if (object == null) {
				throw new IllegalArgumentException("Object cannot be null");
			}
			this.object = object;
		}

		@Override
		public T get() {
			return object;
		}

		@Override
		public void set(T newObject) {
			if (newObject != null) {
				this.object = newObject;
			}
		}
	}

	public static class Gettable<T> implements Getter<T> {
		private T object;

		public Gettable(T object) {
			if (object == null) {
				throw new IllegalArgumentException("Object cannot be null");
			}
			this.object = object;
		}

		@Override
		public T get() {
			return object;
		}
	}
}
