/*
 *   This file is part of Skript.
 *
 *  Skript is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Skript is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Skript.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * Copyright 2011-2013 Peter Güttinger
 * 
 */

package ch.njol.skript.classes.data;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.painting.PaintingEvent;
import org.bukkit.event.painting.PaintingPlaceEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.ChunkEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldEvent;
import org.bukkit.inventory.ItemStack;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.SerializableGetter;
import ch.njol.skript.command.CommandEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.BlockStateBlock;
import ch.njol.skript.util.BlockUtils;
import ch.njol.skript.util.DelayedChangeBlock;

/**
 * @author Peter Güttinger
 */
@SuppressWarnings({"unchecked", "deprecation", "serial"})
public final class BukkitEventValues {
	
	public BukkitEventValues() {}
	
	static {
		
		// === WorldEvents ===
		EventValues.registerEventValue(WorldEvent.class, World.class, new SerializableGetter<World, WorldEvent>() {
			@Override
			public World get(final WorldEvent e) {
				return e.getWorld();
			}
		}, 0);
		// StructureGrowEvent - a WorldEvent
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, new SerializableGetter<Block, StructureGrowEvent>() {
			@Override
			public Block get(final StructureGrowEvent e) {
				return e.getLocation().getBlock();
			}
		}, 0);
		EventValues.registerEventValue(StructureGrowEvent.class, Block.class, new SerializableGetter<Block, StructureGrowEvent>() {
			@Override
			public Block get(final StructureGrowEvent e) {
				for (final BlockState bs : e.getBlocks()) {
					if (bs.getLocation().equals(e.getLocation()))
						return new BlockStateBlock(bs);
				}
				return e.getLocation().getBlock();
			}
		}, 1);
		// WeatherEvent - not a WorldEvent (wtf ô_Ô)
		EventValues.registerEventValue(WeatherEvent.class, World.class, new SerializableGetter<World, WeatherEvent>() {
			@Override
			public World get(final WeatherEvent e) {
				return e.getWorld();
			}
		}, 0);
		// ChunkEvents
		EventValues.registerEventValue(ChunkEvent.class, Chunk.class, new SerializableGetter<Chunk, ChunkEvent>() {
			@Override
			public Chunk get(final ChunkEvent e) {
				return e.getChunk();
			}
		}, 0);
		
		// === BlockEvents ===
		EventValues.registerEventValue(BlockEvent.class, Block.class, new SerializableGetter<Block, BlockEvent>() {
			@Override
			public Block get(final BlockEvent e) {
				return e.getBlock();
			}
		}, 0);
		EventValues.registerEventValue(BlockEvent.class, World.class, new SerializableGetter<World, BlockEvent>() {
			@Override
			public World get(final BlockEvent e) {
				return e.getBlock().getWorld();
			}
		}, 0);
		// TODO workaround of the event's location being at the entity in block events that have an entity event value
		EventValues.registerEventValue(BlockEvent.class, Location.class, new SerializableGetter<Location, BlockEvent>() {
			@Override
			public Location get(final BlockEvent e) {
				return BlockUtils.getLocation(e.getBlock());
			}
		}, 0);
		// BlockPlaceEvent
		EventValues.registerEventValue(BlockPlaceEvent.class, Player.class, new SerializableGetter<Player, BlockPlaceEvent>() {
			@Override
			public Player get(final BlockPlaceEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(BlockPlaceEvent.class, Block.class, new SerializableGetter<Block, BlockPlaceEvent>() {
			@Override
			public Block get(final BlockPlaceEvent e) {
				return new BlockStateBlock(e.getBlockReplacedState());
			}
		}, -1);
		// BlockFadeEvent
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new SerializableGetter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new SerializableGetter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return new DelayedChangeBlock(e.getBlock(), e.getNewState());
			}
		}, 0);
		EventValues.registerEventValue(BlockFadeEvent.class, Block.class, new SerializableGetter<Block, BlockFadeEvent>() {
			@Override
			public Block get(final BlockFadeEvent e) {
				return new BlockStateBlock(e.getNewState());
			}
		}, 1);
		// BlockFormEvent
		EventValues.registerEventValue(BlockFormEvent.class, Block.class, new SerializableGetter<Block, BlockFormEvent>() {
			@Override
			public Block get(final BlockFormEvent e) {
				if (e instanceof BlockSpreadEvent)
					return e.getBlock();
				return new BlockStateBlock(e.getNewState());
			}
		}, 0);
		EventValues.registerEventValue(BlockFormEvent.class, Block.class, new SerializableGetter<Block, BlockFormEvent>() {
			@Override
			public Block get(final BlockFormEvent e) {
				return e.getBlock();
			}
		}, -1);
		// BlockDamageEvent
		EventValues.registerEventValue(BlockDamageEvent.class, Player.class, new SerializableGetter<Player, BlockDamageEvent>() {
			@Override
			public Player get(final BlockDamageEvent e) {
				return e.getPlayer();
			}
		}, 0);
		// BlockBreakEvent
		EventValues.registerEventValue(BlockBreakEvent.class, Player.class, new SerializableGetter<Player, BlockBreakEvent>() {
			@Override
			public Player get(final BlockBreakEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new SerializableGetter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				return e.getBlock();
			}
		}, -1);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new SerializableGetter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				return new DelayedChangeBlock(e.getBlock());
			}
		}, 0);
		EventValues.registerEventValue(BlockBreakEvent.class, Block.class, new SerializableGetter<Block, BlockBreakEvent>() {
			@Override
			public Block get(final BlockBreakEvent e) {
				final BlockState s = e.getBlock().getState();
				s.setType(s.getType() == Material.ICE ? Material.STATIONARY_WATER : Material.AIR);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		// BlockIgniteEvent
		EventValues.registerEventValue(BlockIgniteEvent.class, Player.class, new SerializableGetter<Player, BlockIgniteEvent>() {
			@Override
			public Player get(final BlockIgniteEvent e) {
				return e.getPlayer();
			}
		}, 0);
		// BlockDispenseEvent
		EventValues.registerEventValue(BlockDispenseEvent.class, ItemStack.class, new SerializableGetter<ItemStack, BlockDispenseEvent>() {
			@Override
			public ItemStack get(final BlockDispenseEvent e) {
				return e.getItem();
			}
		}, 0);
		// SignChangeEvent
		EventValues.registerEventValue(SignChangeEvent.class, Player.class, new SerializableGetter<Player, SignChangeEvent>() {
			@Override
			public Player get(final SignChangeEvent e) {
				return e.getPlayer();
			}
		}, 0);
		
		// === EntityEvents ===
		EventValues.registerEventValue(EntityEvent.class, Entity.class, new SerializableGetter<Entity, EntityEvent>() {
			@Override
			public Entity get(final EntityEvent e) {
				return e.getEntity();
			}
		}, 0, "Use 'attacker' and/or 'victim' in damage events", EntityDamageEvent.class);
		EventValues.registerEventValue(EntityEvent.class, World.class, new SerializableGetter<World, EntityEvent>() {
			@Override
			public World get(final EntityEvent e) {
				return e.getEntity().getWorld();
			}
		}, 0);
		// EntityDamageEvent
		EventValues.registerEventValue(EntityDamageEvent.class, DamageCause.class, new SerializableGetter<DamageCause, EntityDamageEvent>() {
			@Override
			public DamageCause get(final EntityDamageEvent e) {
				return e.getCause();
			}
		}, 0);
		EventValues.registerEventValue(EntityDamageByEntityEvent.class, Projectile.class, new SerializableGetter<Projectile, EntityDamageByEntityEvent>() {
			@Override
			public Projectile get(final EntityDamageByEntityEvent e) {
				if (e.getDamager() instanceof Projectile)
					return (Projectile) e.getDamager();
				return null;
			}
		}, 0);
		// EntityDeathEvent
		EventValues.registerEventValue(EntityDeathEvent.class, Projectile.class, new SerializableGetter<Projectile, EntityDeathEvent>() {
			@Override
			public Projectile get(final EntityDeathEvent e) {
				if (e.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) e.getEntity().getLastDamageCause()).getDamager() instanceof Projectile)
					return (Projectile) ((EntityDamageByEntityEvent) e.getEntity().getLastDamageCause()).getDamager();
				return null;
			}
		}, 0);
		EventValues.registerEventValue(EntityDeathEvent.class, DamageCause.class, new SerializableGetter<DamageCause, EntityDeathEvent>() {
			@Override
			public DamageCause get(final EntityDeathEvent e) {
				return e.getEntity().getLastDamageCause().getCause();
			}
		}, 0);
		// ProjectileHitEvent
		EventValues.registerEventValue(ProjectileHitEvent.class, Entity.class, new SerializableGetter<Entity, ProjectileHitEvent>() {
			@Override
			public Entity get(final ProjectileHitEvent e) {
				assert false;
				return e.getEntity().getShooter();
			}
		}, 0, "Use 'projectile' and/or 'shooter' in projectile hit events", ProjectileHitEvent.class);
		EventValues.registerEventValue(ProjectileHitEvent.class, Projectile.class, new SerializableGetter<Projectile, ProjectileHitEvent>() {
			@Override
			public Projectile get(final ProjectileHitEvent e) {
				return e.getEntity();
			}
		}, 0);
		// ProjectileLaunchEvent
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Entity.class, new SerializableGetter<Entity, ProjectileLaunchEvent>() {
			@Override
			public Entity get(final ProjectileLaunchEvent e) {
				assert false;
				return e.getEntity().getShooter();
			}
		}, 0, "Use 'projectile' and/or 'shooter' in shoot events", ProjectileLaunchEvent.class);
		EventValues.registerEventValue(ProjectileLaunchEvent.class, Projectile.class, new SerializableGetter<Projectile, ProjectileLaunchEvent>() {
			@Override
			public Projectile get(final ProjectileLaunchEvent e) {
				return e.getEntity();
			}
		}, 0);
		// EntityTameEvent
		EventValues.registerEventValue(EntityTameEvent.class, Player.class, new SerializableGetter<Player, EntityTameEvent>() {
			@Override
			public Player get(final EntityTameEvent e) {
				return e.getOwner() instanceof Player ? (Player) e.getOwner() : null;
			}
		}, 0);
		
		// --- PlayerEvents ---
		EventValues.registerEventValue(PlayerEvent.class, Player.class, new SerializableGetter<Player, PlayerEvent>() {
			@Override
			public Player get(final PlayerEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(PlayerEvent.class, World.class, new SerializableGetter<World, PlayerEvent>() {
			@Override
			public World get(final PlayerEvent e) {
				return e.getPlayer().getWorld();
			}
		}, 0);
		// PlayerBedEnterEvent
		EventValues.registerEventValue(PlayerBedEnterEvent.class, Block.class, new SerializableGetter<Block, PlayerBedEnterEvent>() {
			@Override
			public Block get(final PlayerBedEnterEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBedLeaveEvent
		EventValues.registerEventValue(PlayerBedLeaveEvent.class, Block.class, new SerializableGetter<Block, PlayerBedLeaveEvent>() {
			@Override
			public Block get(final PlayerBedLeaveEvent e) {
				return e.getBed();
			}
		}, 0);
		// PlayerBucketEvents
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, new SerializableGetter<Block, PlayerBucketFillEvent>() {
			@Override
			public Block get(final PlayerBucketFillEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, 0);
		EventValues.registerEventValue(PlayerBucketFillEvent.class, Block.class, new SerializableGetter<Block, PlayerBucketFillEvent>() {
			@Override
			public Block get(final PlayerBucketFillEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setTypeId(0);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 1);
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new SerializableGetter<Block, PlayerBucketEmptyEvent>() {
			@Override
			public Block get(final PlayerBucketEmptyEvent e) {
				return e.getBlockClicked().getRelative(e.getBlockFace());
			}
		}, -1);
		EventValues.registerEventValue(PlayerBucketEmptyEvent.class, Block.class, new SerializableGetter<Block, PlayerBucketEmptyEvent>() {
			@Override
			public Block get(final PlayerBucketEmptyEvent e) {
				final BlockState s = e.getBlockClicked().getRelative(e.getBlockFace()).getState();
				s.setType(e.getBucket() == Material.WATER_BUCKET ? Material.STATIONARY_WATER : Material.STATIONARY_LAVA);
				s.setRawData((byte) 0);
				return new BlockStateBlock(s, true);
			}
		}, 0);
		// PlayerDropItemEvent
		EventValues.registerEventValue(PlayerDropItemEvent.class, Item.class, new SerializableGetter<Item, PlayerDropItemEvent>() {
			@Override
			public Item get(final PlayerDropItemEvent e) {
				return e.getItemDrop();
			}
		}, 0);
		EventValues.registerEventValue(PlayerDropItemEvent.class, ItemStack.class, new SerializableGetter<ItemStack, PlayerDropItemEvent>() {
			@Override
			public ItemStack get(final PlayerDropItemEvent e) {
				return e.getItemDrop().getItemStack();
			}
		}, 0);
		// PlayerInteractEntityEvent
		EventValues.registerEventValue(PlayerInteractEntityEvent.class, Entity.class, new SerializableGetter<Entity, PlayerInteractEntityEvent>() {
			@Override
			public Entity get(final PlayerInteractEntityEvent e) {
				return e.getRightClicked();
			}
		}, 0);
		// PlayerInteractEvent
		EventValues.registerEventValue(PlayerInteractEvent.class, Block.class, new SerializableGetter<Block, PlayerInteractEvent>() {
			@Override
			public Block get(final PlayerInteractEvent e) {
				return e.getClickedBlock();
			}
		}, 0);
		// PlayerShearEntityEvent
		EventValues.registerEventValue(PlayerShearEntityEvent.class, Entity.class, new SerializableGetter<Entity, PlayerShearEntityEvent>() {
			@Override
			public Entity get(final PlayerShearEntityEvent e) {
				return e.getEntity();
			}
		}, 0);
		
		// --- HangingEvents ---
		if (Skript.isRunningMinecraft(1, 4, 3)) {
			EventValues.registerEventValue(HangingEvent.class, Hanging.class, new SerializableGetter<Hanging, HangingEvent>() {
				@Override
				public Hanging get(final HangingEvent e) {
					return e.getEntity();
				}
			}, 0);
			EventValues.registerEventValue(HangingEvent.class, World.class, new SerializableGetter<World, HangingEvent>() {
				@Override
				public World get(final HangingEvent e) {
					return e.getEntity().getWorld();
				}
			}, 0);
			// HangingPlaceEvent
			EventValues.registerEventValue(HangingPlaceEvent.class, Player.class, new SerializableGetter<Player, HangingPlaceEvent>() {
				@Override
				public Player get(final HangingPlaceEvent e) {
					return e.getPlayer();
				}
			}, 0);
		} else {
			EventValues.registerEventValue(PaintingEvent.class, Painting.class, new SerializableGetter<Painting, PaintingEvent>() {
				@Override
				public Painting get(final PaintingEvent e) {
					return e.getPainting();
				}
			}, 0);
			EventValues.registerEventValue(PaintingEvent.class, World.class, new SerializableGetter<World, PaintingEvent>() {
				@Override
				public World get(final PaintingEvent e) {
					return e.getPainting().getWorld();
				}
			}, 0);
			// PaintingPlaceEvent
			EventValues.registerEventValue(PaintingPlaceEvent.class, Player.class, new SerializableGetter<Player, PaintingPlaceEvent>() {
				@Override
				public Player get(final PaintingPlaceEvent e) {
					return e.getPlayer();
				}
			}, 0);
		}
		
		// --- VehicleEvents ---
		EventValues.registerEventValue(VehicleEvent.class, Vehicle.class, new SerializableGetter<Vehicle, VehicleEvent>() {
			@Override
			public Vehicle get(final VehicleEvent e) {
				return e.getVehicle();
			}
		}, 0);
		EventValues.registerEventValue(VehicleEvent.class, World.class, new SerializableGetter<World, VehicleEvent>() {
			@Override
			public World get(final VehicleEvent e) {
				return e.getVehicle().getWorld();
			}
		}, 0);
		EventValues.registerEventValue(VehicleExitEvent.class, LivingEntity.class, new SerializableGetter<LivingEntity, VehicleExitEvent>() {
			@Override
			public LivingEntity get(final VehicleExitEvent e) {
				return e.getExited();
			}
		}, 0);
		EventValues.registerEventValue(VehicleEvent.class, Entity.class, new SerializableGetter<Entity, VehicleEvent>() {
			@Override
			public Entity get(final VehicleEvent e) {
				return e.getVehicle().getPassenger();
			}
		}, 0);
		
		// === CommandEvents ===
		// PlayerCommandPreprocessEvent is a PlayerEvent
		EventValues.registerEventValue(ServerCommandEvent.class, CommandSender.class, new SerializableGetter<CommandSender, ServerCommandEvent>() {
			@Override
			public CommandSender get(final ServerCommandEvent e) {
				return e.getSender();
			}
		}, 0);
		EventValues.registerEventValue(CommandEvent.class, CommandSender.class, new SerializableGetter<CommandSender, CommandEvent>() {
			@Override
			public CommandSender get(final CommandEvent e) {
				return e.getSender();
			}
		}, 0);
		EventValues.registerEventValue(CommandEvent.class, World.class, new SerializableGetter<World, CommandEvent>() {
			@Override
			public World get(final CommandEvent e) {
				return e.getSender() instanceof Player ? ((Player) e.getSender()).getWorld() : null;
			}
		}, 0);
		
		// === InventoryEvents ===
		EventValues.registerEventValue(InventoryClickEvent.class, Player.class, new SerializableGetter<Player, InventoryClickEvent>() {
			@Override
			public Player get(final InventoryClickEvent e) {
				return e.getWhoClicked() instanceof Player ? (Player) e.getWhoClicked() : null;
			}
		}, 0);
		EventValues.registerEventValue(InventoryClickEvent.class, World.class, new SerializableGetter<World, InventoryClickEvent>() {
			@Override
			public World get(final InventoryClickEvent e) {
				return e.getWhoClicked().getWorld();
			}
		}, 0);
		EventValues.registerEventValue(CraftItemEvent.class, ItemStack.class, new SerializableGetter<ItemStack, CraftItemEvent>() {
			@Override
			public ItemStack get(final CraftItemEvent e) {
				return e.getRecipe().getResult();
			}
		}, 0);
		
	}
	
}