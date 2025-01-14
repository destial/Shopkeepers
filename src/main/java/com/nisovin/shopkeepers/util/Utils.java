package com.nisovin.shopkeepers.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class Utils {

	private Utils() {
	}

	/**
	 * Replaces the first occurrence of the given element inside the given list.
	 * 
	 * @param <E>
	 *            the element type
	 * @param list
	 *            the list, not <code>null</code>
	 * @param element
	 *            the element to replace
	 * @param replacement
	 *            the replacement
	 * @return <code>true</code> if the element has been found inside the list
	 */
	public static <E> boolean replace(List<E> list, E element, E replacement) {
		if (list instanceof RandomAccess) { // Also checks for null
			int index = list.indexOf(element);
			if (index != -1) {
				list.set(index, replacement);
				return true;
			}
			return false;
		} else {
			Validate.notNull(list, "list is null");
			ListIterator<E> iterator = list.listIterator();
			while (iterator.hasNext()) {
				E next = iterator.next();
				if (Objects.equals(element, next)) {
					iterator.set(replacement);
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Creates a fixed-sized list backed by the given array.
	 * <p>
	 * Unlike {@link Arrays#asList(Object...)} this returns an {@link Collections#emptyList() empty list} if the given
	 * array is <code>null</code>.
	 * 
	 * @param <E>
	 *            the element type
	 * @param array
	 *            the array
	 * @return the list backed by the given array, or an empty list if the given array is <code>null</code>
	 */
	@SafeVarargs
	public static <E> List<E> asList(E... array) {
		return (array == null) ? Collections.emptyList() : Arrays.asList(array);
	}

	/**
	 * Sorts the given list using the given {@link Comparator} and then returns the list.
	 * 
	 * @param <E>
	 *            the element type
	 * @param <L>
	 *            the type of the list
	 * @param list
	 *            the list to sort
	 * @param comparator
	 *            the comparator
	 * @return the given list sorted
	 * @see List#sort(Comparator)
	 */
	public static <E, L extends List<? extends E>> L sort(L list, Comparator<? super E> comparator) {
		assert list != null && comparator != null;
		list.sort(comparator);
		return list;
	}

	/**
	 * Adds the given elements to the given collection and then returns the collection.
	 * 
	 * @param <E>
	 *            the element type
	 * @param <C>
	 *            the type of the collection
	 * @param collection
	 *            the collection
	 * @param toAdd
	 *            the elements to add
	 * @return the given collection with the elements added
	 * @see Collection#addAll(Collection)
	 */
	public static <E, C extends Collection<? super E>> C addAll(C collection, Collection<? extends E> toAdd) {
		assert collection != null && toAdd != null;
		collection.addAll(toAdd);
		return collection;
	}

	/**
	 * Searches through the given {@link Iterable} for an element that is accepted by the given {@link Predicate}.
	 * 
	 * @param <E>
	 *            the element type
	 * @param iterable
	 *            the elements to search through
	 * @param predicate
	 *            the Predicate, not <code>null</code>
	 * @return the first found element accepted by the Predicate, or <code>null</code> if either no such element was
	 *         found, or if the Predicate accepted a <code>null</code> element
	 */
	public static <E> E findFirst(Iterable<E> iterable, Predicate<? super E> predicate) {
		Validate.notNull(predicate, "predicate is null");
		for (E element : iterable) {
			if (predicate.test(element)) {
				return element;
			}
		}
		return null;
	}

	/**
	 * Checks if the given {@link Iterable} contains an element that is accepted by the given {@link Predicate}.
	 * 
	 * @param <E>
	 *            the element type
	 * @param iterable
	 *            the elements to search through
	 * @param predicate
	 *            the Predicate, not <code>null</code>
	 * @return <code>true</code> if an element is found that is accepted by the given Predicate
	 */
	public static <E> boolean contains(Iterable<E> iterable, Predicate<? super E> predicate) {
		Validate.notNull(predicate, "predicate is null");
		for (E element : iterable) {
			if (predicate.test(element)) {
				return true;
			}
		}
		return false;
	}

	// Note: Doesn't work for primitive arrays.
	@SafeVarargs
	public static <T> T[] concat(T[] array1, T... array2) {
		if (array1 == null) return array2;
		if (array2 == null) return array1;

		int length1 = array1.length;
		int length2 = array2.length;
		T[] result = Arrays.copyOf(array1, length1 + length2);
		System.arraycopy(array2, 0, result, length1, length2);
		return result;
	}

	public static <T> Stream<T> stream(Iterable<T> iterable) {
		if (iterable instanceof Collection) {
			return ((Collection<T>) iterable).stream();
		} else {
			return StreamSupport.stream(iterable.spliterator(), false);
		}
	}

	// Note: The returned Iterable can only be iterated once!
	public static <T> Iterable<T> toIterable(Stream<T> stream) {
		return stream::iterator;
	}

	public static void printRegisteredListeners(Event event) {
		HandlerList handlerList = event.getHandlers();
		Log.info("Registered listeners for event " + event.getEventName() + ":");
		for (RegisteredListener rl : handlerList.getRegisteredListeners()) {
			Log.info(" - " + rl.getPlugin().getName() + " (" + rl.getListener().getClass().getName() + ")"
					+ ", priority: " + rl.getPriority() + ", ignoring cancelled: " + rl.isIgnoringCancelled());
		}
	}

	/**
	 * Checks if the player can interact with the given block.
	 * <p>
	 * This works by clearing the player's items in main and off hand, calling a dummy PlayerInteractEvent for plugins
	 * to react to and then restoring the player's items in main and off hand.
	 * <p>
	 * Since this involves calling a dummy PlayerInteractEvent, plugins reacting to the event might cause all kinds of
	 * side effects. Therefore, this should only be used in very specific situations, such as for specific blocks.
	 * 
	 * @param player
	 *            the player
	 * @param block
	 *            the block to check interaction with
	 * @return <code>true</code> if no plugin denied block interaction
	 */
	public static boolean checkBlockInteract(Player player, Block block) {
		// Simulating right click on the block to check if access is denied:
		// Making sure that block access is really denied, and that the event is not cancelled because of denying
		// usage with the items in hands:
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInMainHand = playerInventory.getItemInMainHand();
		ItemStack itemInOffHand = playerInventory.getItemInOffHand();
		playerInventory.setItemInMainHand(null);
		playerInventory.setItemInOffHand(null);

		TestPlayerInteractEvent dummyInteractEvent = new TestPlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, null, block, BlockFace.UP);
		Bukkit.getPluginManager().callEvent(dummyInteractEvent);
		boolean canAccessBlock = (dummyInteractEvent.useInteractedBlock() != Result.DENY);

		// Resetting items in main and off hand:
		playerInventory.setItemInMainHand(itemInMainHand);
		playerInventory.setItemInOffHand(itemInOffHand);
		return canAccessBlock;
	}

	/**
	 * Checks if the player can interact with the given entity.
	 * <p>
	 * This works by clearing the player's items in main and off hand, calling a dummy PlayerInteractEntityEvent for
	 * plugins to react to and then restoring the player's items in main and off hand.
	 * <p>
	 * Since this involves calling a dummy PlayerInteractEntityEvent, plugins reacting to the event might cause all
	 * kinds of side effects. Therefore, this should only be used in very specific situations, such as for specific
	 * entities, and its usage should be optional (i.e. guarded by a config setting).
	 * 
	 * @param player
	 *            the player
	 * @param entity
	 *            the entity to check interaction with
	 * @return <code>true</code> if no plugin denied interaction
	 */
	public static boolean checkEntityInteract(Player player, Entity entity) {
		// Simulating right click on the entity to check if access is denied:
		// Making sure that entity access is really denied, and that the event is not cancelled because of denying usage
		// with the items in hands:
		PlayerInventory playerInventory = player.getInventory();
		ItemStack itemInMainHand = playerInventory.getItemInMainHand();
		ItemStack itemInOffHand = playerInventory.getItemInOffHand();
		playerInventory.setItemInMainHand(null);
		playerInventory.setItemInOffHand(null);

		TestPlayerInteractEntityEvent dummyInteractEvent = new TestPlayerInteractEntityEvent(player, entity);
		Bukkit.getPluginManager().callEvent(dummyInteractEvent);
		boolean canAccessEntity = !dummyInteractEvent.isCancelled();

		// Resetting items in main and off hand:
		playerInventory.setItemInMainHand(itemInMainHand);
		playerInventory.setItemInOffHand(itemInOffHand);
		return canAccessEntity;
	}

	public static String getServerCBVersion() {
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		String cbVersion = packageName.substring(packageName.lastIndexOf('.') + 1);
		return cbVersion;
	}

	private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS;
	static {
		Map<Class<?>, Class<?>> primitiveWrappers = new HashMap<>();
		primitiveWrappers.put(boolean.class, Boolean.class);
		primitiveWrappers.put(byte.class, Byte.class);
		primitiveWrappers.put(char.class, Character.class);
		primitiveWrappers.put(double.class, Double.class);
		primitiveWrappers.put(float.class, Float.class);
		primitiveWrappers.put(int.class, Integer.class);
		primitiveWrappers.put(long.class, Long.class);
		primitiveWrappers.put(short.class, Short.class);
		PRIMITIVE_WRAPPERS = Collections.unmodifiableMap(primitiveWrappers);
	}

	public static boolean isPrimitiveWrapperOf(Class<?> targetClass, Class<?> primitive) {
		Validate.isTrue(primitive.isPrimitive(), "Second argument has to be a primitive!");
		return (PRIMITIVE_WRAPPERS.get(primitive) == targetClass);
	}

	public static boolean isAssignableFrom(Class<?> to, Class<?> from) {
		if (to.isAssignableFrom(from)) {
			return true;
		}
		if (to.isPrimitive()) {
			return isPrimitiveWrapperOf(from, to);
		}
		if (from.isPrimitive()) {
			return isPrimitiveWrapperOf(to, from);
		}
		return false;
	}

	/**
	 * Checks if the given locations represent the same world and coordinates (ignores pitch and yaw).
	 * 
	 * @param location1
	 *            location 1
	 * @param location2
	 *            location 2
	 * @return <code>true</code> if the locations correspond to the same position
	 */
	public static boolean isEqualPosition(Location location1, Location location2) {
		if (location1 == location2) return true; // Also handles both being null
		if (location1 == null || location2 == null) return false;
		if (!Objects.equals(location1.getWorld(), location2.getWorld())) {
			return false;
		}
		if (Double.doubleToLongBits(location1.getX()) != Double.doubleToLongBits(location2.getX())) {
			return false;
		}
		if (Double.doubleToLongBits(location1.getY()) != Double.doubleToLongBits(location2.getY())) {
			return false;
		}
		if (Double.doubleToLongBits(location1.getZ()) != Double.doubleToLongBits(location2.getZ())) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the squared distance between the given location.
	 * <p>
	 * Both locations are required to have a valid (non-<code>null</code>) world. If the locations are located in
	 * different worlds, this returns {@link Double#MAX_VALUE}.
	 * 
	 * @param location1
	 *            the first location, not <code>null</code>
	 * @param location2
	 *            the second location, not <code>null</code>
	 * @return the squared distance
	 */
	public static double getDistanceSquared(Location location1, Location location2) {
		Validate.notNull(location1, "First location is null!");
		Validate.notNull(location2, "Second location is null!");
		World world1 = location1.getWorld();
		World world2 = location2.getWorld();
		Validate.notNull(world1, "World of first location is null!");
		Validate.notNull(world2, "World of second location is null!");
		if (world1 != world2) return Double.MAX_VALUE; // Different worlds
		// Note: Not using Location#distanceSquared to avoid redundant precondition checks.
		double dx = location1.getX() - location2.getX();
		double dy = location1.getY() - location2.getY();
		double dz = location1.getZ() - location2.getZ();
		return dx * dx + dy * dy + dz * dz;
	}

	/**
	 * Gets the block's center location.
	 * 
	 * @param block
	 *            the block
	 * @return the block's center location
	 */
	public static Location getBlockCenterLocation(Block block) {
		Validate.notNull(block, "Block is null!");
		return block.getLocation().add(0.5D, 0.5D, 0.5D);
	}

	// Temporary objects getting re-used during ray tracing:
	private static final Location TEMP_START_LOCATION = new Location(null, 0, 0, 0);
	private static final Vector TEMP_START_POSITION = new Vector();
	private static final Vector DOWN_DIRECTION = new Vector(0.0D, -1.0D, 0.0D);
	private static final double RAY_TRACE_OFFSET = 0.01D;

	/**
	 * Get the distance to the nearest block collision in the range of the given <code>maxDistance</code>.
	 * <p>
	 * This performs a ray trace through the blocks' collision boxes, ignoring passable blocks and optionally ignoring
	 * specific types of fluids.
	 * <p>
	 * The ray tracing gets slightly offset (by <code>0.01</code>) in order to make sure that we don't miss any block
	 * directly at the start location. If this results in a hit above the start location, we ignore it and return
	 * <code>0.0</code>.
	 * 
	 * @param startLocation
	 *            the start location, has to use a valid world, does not get modified
	 * @param maxDistance
	 *            the max distance to check for block collisions, has to be positive
	 * @param collidableFluids
	 *            the types of fluids to collide with
	 * @return the distance to the ground, or <code>maxDistance</code> if there are no block collisions within the
	 *         specified range
	 */
	public static double getCollisionDistanceToGround(Location startLocation, double maxDistance, Set<Material> collidableFluids) {
		assert collidableFluids != null;
		World world = startLocation.getWorld();
		assert world != null;
		// Setup re-used offset start location:
		TEMP_START_LOCATION.setWorld(world);
		TEMP_START_LOCATION.setX(startLocation.getX());
		TEMP_START_LOCATION.setY(startLocation.getY() + RAY_TRACE_OFFSET);
		TEMP_START_LOCATION.setZ(startLocation.getZ());

		TEMP_START_POSITION.setX(TEMP_START_LOCATION.getX());
		TEMP_START_POSITION.setY(TEMP_START_LOCATION.getY());
		TEMP_START_POSITION.setZ(TEMP_START_LOCATION.getZ());

		double offsetMaxDistance = maxDistance + RAY_TRACE_OFFSET;

		RayTraceResult rayTraceResult = null;
		if (collidableFluids.isEmpty()) {
			// Considers block collision boxes, ignoring passable blocks and fluids (null if there is not hit):
			rayTraceResult = world.rayTraceBlocks(TEMP_START_LOCATION, DOWN_DIRECTION, offsetMaxDistance, FluidCollisionMode.NEVER, true);
		} else {
			// Take the given types of fluids into account, but still ignore other types of passable blocks:
			int offsetMaxDistanceBlocks = NumberConversions.ceil(offsetMaxDistance);
			BlockIterator blockIterator = new BlockIterator(world, TEMP_START_POSITION, DOWN_DIRECTION, 0.0D, offsetMaxDistanceBlocks);
			while (blockIterator.hasNext()) {
				Block block = blockIterator.next();
				if (!block.isPassable() || collidableFluids.contains(block.getType())) {
					rayTraceResult = block.rayTrace(TEMP_START_LOCATION, DOWN_DIRECTION, offsetMaxDistance, FluidCollisionMode.ALWAYS);
					if (rayTraceResult != null) {
						break;
					} // Else: The raytrace did not collide with the block (eg. open trap doors, etc.)
				} // Else: Continue.
			}
			// rayTraceResult can remain null if there are no block collisions in range.
		}
		TEMP_START_LOCATION.setWorld(null); // Cleanup temporarily used start location

		double distanceToGround;
		if (rayTraceResult == null) {
			// No collision with the range:
			distanceToGround = maxDistance;
		} else {
			distanceToGround = TEMP_START_POSITION.distance(rayTraceResult.getHitPosition()) - RAY_TRACE_OFFSET;
			// Might be negative if the hit is between the start location and the offset start location.
			// We ignore it then.
			if (distanceToGround < 0.0D) distanceToGround = 0.0D;
		}
		return distanceToGround;
	}
}
