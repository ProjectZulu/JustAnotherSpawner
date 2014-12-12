package jas.refactor.entities;

import jas.common.JASLog;
import jas.common.math.SetAlgebra;
import jas.common.math.SetAlgebra.OPERATION;
import jas.refactor.entities.Groups.Group;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

public class LivingGroupBuilder {
	public final String groupID;
	public String configName;
	public transient Set<String> entityJASNames = new HashSet<String>();
	/* String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
	public List<String> contents;

	public LivingGroupBuilder() {
		this.groupID = "";
		this.configName = "";
		contents = new ArrayList<String>();
	}

	public LivingGroupBuilder(String groupID) {
		if (groupID == null || groupID.trim().equals("")) {
			throw new IllegalArgumentException("Group ID cannot be " + groupID == null ? "null" : "empty");
		}
		this.groupID = groupID;
		String[] parts = groupID.split("\\.");
		if (parts.length > 1) {
			this.configName = parts[0];
		} else {
			this.configName = "";
		}
		contents = new ArrayList<String>();
	}

	public LivingGroupBuilder(String groupID, ArrayList<String> contents) {
		if (groupID == null || groupID.trim().equals("")) {
			throw new IllegalArgumentException("Group ID cannot be " + groupID == null ? "null" : "empty");
		}
		this.groupID = groupID;
		String[] parts = groupID.split("\\.");
		if (parts.length > 1) {
			this.configName = parts[0];
		} else {
			this.configName = "";
		}
		this.contents = contents;
	}

	public LivingGroupBuilder(String groupID, String configName, ArrayList<String> contents) {
		this.groupID = groupID;
		this.configName = configName;
		this.contents = new ArrayList<String>(contents);
	}

	public LivingGroupBuilder(LivingGroup attribute) {
		this.groupID = attribute.groupID;
		this.configName = attribute.configName;
		this.contents = new ArrayList<String>(attribute.contents);
		this.entityJASNames = new HashSet<String>(attribute.entityJASNames);
	}

	/**
	 * Build the LivingGroup form the builder.
	 * 
	 * Requires Previous processed Attribtues and Entity Mappings
	 */
	public LivingGroup build(Mappings mappings, Groups attributes) {
		/* Evaluate contents and fill in jasNames */
		for (String contentComponent : contents) {
			OPERATION operation;
			if (contentComponent.startsWith("-")) {
				contentComponent = contentComponent.substring(1);
				operation = OPERATION.COMPLEMENT;
			} else if (contentComponent.startsWith("&")) {
				contentComponent = contentComponent.substring(1);
				operation = OPERATION.INTERSECT;
			} else {
				operation = OPERATION.UNION;
				if (contentComponent.startsWith("+")) {
					contentComponent = contentComponent.substring(1);
				}
			}

			if (contentComponent.startsWith("A|")) {
				Group groupToAdd = attributes.iDToAttribute().get(contentComponent.substring(2));
				if (groupToAdd != null) {
					SetAlgebra.operate(this.entityJASNames, groupToAdd.results(), operation);
					continue;
				}
			} else if (mappings.jASNametoEntityClass().containsKey(contentComponent)) {
				SetAlgebra.operate(this.entityJASNames, Sets.newHashSet(contentComponent), operation);
				continue;
			}
			JASLog.log().severe("Error processing %s content from %s. The component %s does not exist.", this.groupID,
					contents.toString(), contentComponent);
		}

		return new LivingGroup(this);
	}

	@Override
	public String toString() {
		return groupID.concat(" contains ").concat(
				entityJASNames.toString().concat(" from ").concat(contents.toString()));
	}

	public class LivingGroup implements Group {
		public final String groupID;
		public final String configName;
		public final transient ImmutableSet<String> entityJASNames;
		/* String Used to Build Group Content Names i.e. {desert,A|Forest,glacier} */
		public final ImmutableList<String> contents;

		@Override
		public String iD() {
			return groupID;
		}

		@Override
		public Set<String> results() {
			return entityJASNames;
		}

		@Override
		public List<String> contents() {
			return contents;
		}

		private LivingGroup(LivingGroupBuilder builder) {
			this.groupID = builder.groupID;
			this.configName = builder.configName;
			this.entityJASNames = ImmutableSet.<String> builder().addAll(builder.entityJASNames).build();
			this.contents = ImmutableList.<String> builder().addAll(builder.contents).build();
		}

		@Override
		public boolean equals(Object paramObject) {
			if (paramObject == null || !(paramObject instanceof LivingGroup)) {
				return false;
			}
			return ((LivingGroup) paramObject).groupID.equals(groupID);
		}

		@Override
		public int hashCode() {
			return groupID.hashCode();
		}

		@Override
		public String toString() {
			return groupID.concat(" contains ").concat(
					entityJASNames.toString().concat(" from ").concat(contents.toString()));
		}
	}
}
