package cc.spea.sbac;

public enum NestedCraftingSourcePriority {
	STORAGE_FIRST,
	NESTED_FIRST,
	PLAYER_FIRST,
	MEMORIZED_FIRST;

	private static final NestedCraftingSourcePriority[] VALUES = values();

	public static NestedCraftingSourcePriority byId(int id) {
		return VALUES[Math.floorMod(id, VALUES.length)];
	}

	public int getId() {
		return ordinal();
	}

	public NestedCraftingSourcePriority next() {
		return byId(getId() + 1);
	}
}
