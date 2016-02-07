package jas.spawner.refactor;

import jas.spawner.modern.spawner.CountInfo;
import jas.spawner.refactor.mvel.MVELExpression;
import net.minecraft.world.World;

import com.google.common.base.Optional;

public class LivingTypeSpawnTriggerBuilder {
	private String triggerID;
	private String spawnerID;
	private int triggerPriority;
	private String isTriggeredExpression;
	private String performSpawnCycleExpression;
	private String isFinishedSpawningExpression;

	/*
	 * 
	 * LivingType "MONSTER": "Trigger": { TriggerType: Passive SpawnMode: World, Chunk, Player MultipleTriggerBehaviour:
	 * REPLACE, TOGGLE, or IGNORE CanStartExpression: "ticks % 1 == 0" or "randomInt(100) < 10" CanEndExpression: "True"
	 * ot "Player.someStat (only for Player type)" }
	 */

	public LivingTypeSpawnTriggerBuilder(String triggerID) {
		this.triggerID = triggerID;
	}

	public static class LivingTypeSpawnTrigger {
		public final TRIGGER trigger;
		public final SPAWNER spawner; // World, Chunk, Player
		public final int triggerPriority; // HIgher priorty, replaces existing spawn efect
		public final MVELExpression<Boolean> isTriggered;
		public final MVELExpression<Boolean> performSpawnCycle;
		public final MVELExpression<Boolean> isFinishedSpawning;

		public LivingTypeSpawnTrigger(LivingTypeSpawnTriggerBuilder builder) {
			Optional<TRIGGER> trigger = TRIGGER.getFrom(builder.triggerID);
			if (!trigger.isPresent()) {
				throw new IllegalArgumentException("Invalid TriggerType " + builder.triggerID + " does not exist.");
			}
			this.trigger = trigger.get();

			Optional<SPAWNER> spawner = SPAWNER.getFrom(builder.triggerID);
			if (!spawner.isPresent()) {
				throw new IllegalArgumentException("Invalid SpawnMode " + builder.spawnerID + " does not exist.");
			}
			this.spawner = spawner.get();

			this.triggerPriority = builder.triggerPriority;
			this.isTriggered = new MVELExpression<Boolean>(builder.isTriggeredExpression);
			this.performSpawnCycle = new MVELExpression<Boolean>(builder.performSpawnCycleExpression);
			this.isFinishedSpawning = new MVELExpression<Boolean>(builder.isFinishedSpawningExpression);
		}
	}

	public interface SpawnProcess {
		public String livingType();

		public SPAWNER spawner();

		public int priority();

		// This is NOT needed, by the time ActiveSpawn is created, it is already triggered
		public boolean isTriggered(World world);

		public boolean performSpawnCycle(World world);

		public boolean isFinished(World world);

		// Performance optimization for repeated similar events
		// i.e. 10 Chunk spawns happen, get combined into a single chunkSpawn to share common entity counting costs
		// @result boolean True if Added, False if it was possible (i.e. Unique Conditions)
		// PREMATURE OPTIMIZATION, implement only if possible ends up being a problem
		// public boolean addTo(ActiveSpawn activeSpawnToBeAdded);

		public void incremenetDuration();
	}

	public static class ActiveTrigger {
		private int duration;
		private String livingTypeID;

		public ActiveTrigger(String livingTypeID) {
			duration = 0;
			this.livingTypeID = livingTypeID;
		}
	}

	@Deprecated
	public static enum CONFLICT_BEHAVIOUR {
		// Removes the existing trigger and starts a new instance
		REPLACE,
		// Disables the active instance
		TOGGLE,
		// New triggers are ignored until existing is finished
		IGNORE;

		public static boolean isValidKey(String behaviourID) {
			for (CONFLICT_BEHAVIOUR behaviour : CONFLICT_BEHAVIOUR.values()) {
				if (behaviour.toString().equalsIgnoreCase(behaviourID)) {
					return true;
				}
			}
			return false;
		}

		public static Optional<CONFLICT_BEHAVIOUR> getFrom(String behaviourID) {
			for (CONFLICT_BEHAVIOUR behaviour : CONFLICT_BEHAVIOUR.values()) {
				if (behaviour.toString().equalsIgnoreCase(behaviourID)) {
					return Optional.of(behaviour);
				}
			}
			return Optional.<CONFLICT_BEHAVIOUR> absent();
		}
	}

	public enum TRIGGER {
		PASSIVE, CHUNK, PLAYERS;

		public static Optional<TRIGGER> getFrom(String triggerID) {
			for (TRIGGER trigger : TRIGGER.values()) {
				if (trigger.toString().equalsIgnoreCase(triggerID)) {
					return Optional.of(trigger);
				}
			}
			return Optional.<TRIGGER> absent();
		}
	}

	public enum SPAWNER {
		WORLD, CHUNK, PLAYER;

		public static Optional<SPAWNER> getFrom(String spawnerID) {
			for (SPAWNER spawner : SPAWNER.values()) {
				if (spawner.toString().equalsIgnoreCase(spawnerID)) {
					return Optional.of(spawner);
				}
			}
			return Optional.<SPAWNER> absent();
		}
	}
}
