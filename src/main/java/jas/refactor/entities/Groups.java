package jas.refactor.entities;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Groups {
	public Map<String, ? extends Group> iDToAttribute();

	public static interface Group {
		public String iD();

		public Set<String> results();

		public List<String> contents();
	}
}
