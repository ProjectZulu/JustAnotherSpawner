package jas.modern.spawner;

import jas.modern.spawner.creature.handler.parsing.ParsingHelper;
import jas.modern.spawner.creature.handler.parsing.keys.Key;
import jas.modern.spawner.creature.handler.parsing.keys.KeyParser.KeyType;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettings.Operand;
import jas.modern.spawner.creature.handler.parsing.settings.OptionalSettingsBase;

import com.google.common.base.Optional;

public class TagConverter {
	public String parentTag;
	public String expression;
	public Operand operand;
	public Optional<Integer> despawnAge = Optional.absent();
	public Optional<Integer> entityCap = Optional.absent();;
	public Optional<Integer> maxSpawnRange = Optional.absent();;
	public Optional<Integer> minDespawnRage = Optional.absent();;
	public Optional<Integer> despawnRate = Optional.absent();;

	public TagConverter(String parseableString) {
		if (parseableString.equals("")) {
			expression = "";
			parentTag = "";
			operand = Operand.OR;
			return;
		}
		if (parseableString.endsWith("}")) {
			parseableString = parseableString.substring(0, parseableString.length() - 1);
		}
		if (parseableString.startsWith("{")) {
			parseableString = parseableString.substring(1);
		}
		boolean isEnabled = false;
		boolean isInverted = false;
		operand = Operand.OR;
		StringBuilder expressionBuilder = new StringBuilder(parseableString.length());
		String[] masterParts = parseableString.split(":");
		for (int i = 0; i < masterParts.length; i++) {
			if (i == 0) {
				for (Key key : Key.values()) {
					if (key.keyParser == null || key.keyParser.getKeyType() != KeyType.PARENT) {
						continue;
					}
					if (key.keyParser.isMatch(masterParts[i])) {
						isEnabled = true;
						if (key.keyParser.isInvertable() && key.keyParser.isInverted(masterParts[i])) {
							isInverted = true;
						} else {
							isInverted = false;
						}
						operand = key.keyParser.parseOperand(masterParts[i]);
						parentTag = masterParts[i];
					}
				}
			} else {
				String[] childParts = masterParts[i].split(",", 2);
				boolean foundMatch = false;
				for (int j = 0; j < Key.values().length; j++) {
					Key key = Key.values()[j];
					if (key.keyParser == null) {
						continue;
					}
					if (key.keyParser.isMatch(childParts[0])) {
						foundMatch = true;
						if (key.keyParser.getKeyType() == KeyType.CHAINABLE) {
							boolean isTagInverted = key.keyParser.isInverted(masterParts[i]);
							Operand tagOoperand = key.keyParser.parseOperand(masterParts[i]);
							String tagExpre = key.keyParser.toExpression(masterParts[i]);
							if (tagExpre.isEmpty()) {
								continue;
							}
							if (isTagInverted) {
								tagExpre = "!".concat(tagExpre);
							}
							if (i != 1) {
								if (tagOoperand == Operand.OR) {
									tagExpre = "||".concat(tagExpre);
								} else if (tagOoperand == Operand.AND) {
									tagExpre = "&&".concat(tagExpre);
								}
							}
							expressionBuilder.append(tagExpre);
						} else if (key.keyParser.getKeyType() == KeyType.VALUE) {
							String[] values = masterParts[i].split(",");
							if (key == Key.blockRange) {
								// Do Nothing with BlockRange. Too much work to import it.
							} else if (key == Key.despawnAge) {
								despawnAge = Optional.of(ParsingHelper.parseFilteredInteger(values[1], 32,
										Key.spawnRange.key));
							} else if (key == Key.entityCap) {
								entityCap = Optional.of(ParsingHelper.parseFilteredInteger(values[1], 0,
										Key.entityCap.key));
							} else if (key == Key.maxSpawnRange) {
								maxSpawnRange = Optional.of(ParsingHelper.parseFilteredInteger(values[1], 128,
										Key.maxSpawnRange.key));
							} else if (key == Key.spawnRange) {
								minDespawnRage = Optional.of(ParsingHelper.parseFilteredInteger(values[1], 32,
										Key.spawnRange.key));
							} else if (key == Key.spawnRate) {
								despawnRate = Optional.of(ParsingHelper.parseFilteredInteger(values[1],
										OptionalSettingsBase.defaultSpawnRate, Key.spawnRate.key));
							}
						}
						break;
					}
				}
			}
		}
		if (isInverted) {
			expressionBuilder.insert(0, "!(").append(")");
		}
		expression = isEnabled ? expressionBuilder.toString() : "";
	}
}
