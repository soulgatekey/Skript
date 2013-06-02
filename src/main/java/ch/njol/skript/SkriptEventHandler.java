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

package ch.njol.skript;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Result;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.EventExecutor;

import ch.njol.skript.ScriptLoader.ScriptInfo;
import ch.njol.skript.command.Commands;
import ch.njol.skript.lang.SelfRegisteringSkriptEvent;
import ch.njol.skript.lang.Trigger;

/**
 * @author Peter Güttinger
 */
public abstract class SkriptEventHandler {
	private SkriptEventHandler() {}
	
	private final static Map<Class<? extends Event>, List<Trigger>> triggers = new HashMap<Class<? extends Event>, List<Trigger>>();
	
	private final static List<Trigger> selfRegisteredTriggers = new ArrayList<Trigger>();
	
	private final static Iterator<Trigger> getTriggers(final Class<? extends Event> event) {
		return new Iterator<Trigger>() {
			private Class<?> e = event;
			private Iterator<Trigger> current = null;
			
			@Override
			public boolean hasNext() {
				while (current == null || !current.hasNext()) {
					if (e == null || !Event.class.isAssignableFrom(e))
						return false;
					final List<Trigger> l = triggers.get(e);
					current = l == null ? null : l.iterator();
					e = e.getSuperclass();
				}
				return true;
			}
			
			@Override
			public Trigger next() {
				if (!hasNext())
					throw new NoSuchElementException();
				return current.next();
			}
			
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	private static Event last = null;
	
	final static EventExecutor ee = new EventExecutor() {
		@Override
		public void execute(final Listener l, final Event e) {
			if (last == e) // an event is received multiple times if multiple superclasses of it are registered
				return;
			last = e;
			check(e);
		}
	};
	
	static void check(final Event e) {
		Iterator<Trigger> ts = getTriggers(e.getClass());
		if (!ts.hasNext())
			return;
		
		if (Skript.logVeryHigh()) {
			boolean hasTrigger = false;
			while (ts.hasNext()) {
				if (ts.next().getEvent().check(e)) {
					hasTrigger = true;
					break;
				}
			}
			if (!hasTrigger)
				return;
			ts = getTriggers(e.getClass());
			
			logEventStart(e);
		}
		
		if (e instanceof Cancellable && ((Cancellable) e).isCancelled() &&
				!(e instanceof PlayerInteractEvent && (((PlayerInteractEvent) e).getAction() == Action.LEFT_CLICK_AIR || ((PlayerInteractEvent) e).getAction() == Action.RIGHT_CLICK_AIR) && ((PlayerInteractEvent) e).useItemInHand() != Result.DENY)
				|| e instanceof ServerCommandEvent && (((ServerCommandEvent) e).getCommand() == null || ((ServerCommandEvent) e).getCommand().isEmpty())) {
			if (Skript.logVeryHigh())
				Skript.info(" -x- was cancelled");
			return;
		}
		
		while (ts.hasNext()) {
			final Trigger t = ts.next();
			if (!t.getEvent().check(e))
				continue;
			logTriggerStart(t);
			t.execute(e);
			logTriggerEnd(t);
		}
		
		logEventEnd();
	}
	
	private static long startEvent;
	
	public static void logEventStart(final Event e) {
		if (!Skript.logVeryHigh())
			return;
		startEvent = System.nanoTime();
		Skript.info("");
		Skript.info("== " + e.getClass().getName() + " ==");
	}
	
	public static void logEventEnd() {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("== took " + 1. * (System.nanoTime() - startEvent) / 1000000. + " milliseconds ==");
	}
	
	static long startTrigger;
	
	public static void logTriggerStart(final Trigger t) {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + t.getName());
		startTrigger = System.nanoTime();
	}
	
	public static void logTriggerEnd(final Trigger t) {
		if (!Skript.logVeryHigh())
			return;
		Skript.info("# " + t.getName() + " took " + 1. * (System.nanoTime() - startTrigger) / 1000000. + " milliseconds");
	}
	
	static void addTrigger(final Class<? extends Event>[] events, final Trigger trigger) {
		for (final Class<? extends Event> e : events) {
			List<Trigger> ts = triggers.get(e);
			if (ts == null)
				triggers.put(e, ts = new ArrayList<Trigger>());
			ts.add(trigger);
		}
	}
	
	/**
	 * Stores a self registered trigger to allow for it to be unloaded later on.
	 * 
	 * @param t Trigger that has already been registered to its event
	 */
	static void addSelfRegisteringTrigger(final Trigger t) {
		assert t.getEvent() instanceof SelfRegisteringSkriptEvent;
		selfRegisteredTriggers.add(t);
	}
	
	static ScriptInfo removeTriggers(final File script) {
		final ScriptInfo info = new ScriptInfo();
		final Iterator<List<Trigger>> triggersIter = SkriptEventHandler.triggers.values().iterator();
		while (triggersIter.hasNext()) {
			final List<Trigger> ts = triggersIter.next();
			for (int i = 0; i < ts.size(); i++) {
				if (ts.get(i).getScript().equals(script)) {
					info.triggers++;
					ts.remove(i);
					i--;
					if (ts.isEmpty())
						triggersIter.remove();
				}
			}
		}
		for (int i = 0; i < selfRegisteredTriggers.size(); i++) {
			final Trigger t = selfRegisteredTriggers.get(i);
			if (t.getScript().equals(script)) {
				info.triggers++;
				((SelfRegisteringSkriptEvent) t.getEvent()).unregister(t);
				selfRegisteredTriggers.remove(i);
				i--;
			}
		}
		info.commands = Commands.unregisterCommands(script);
		
		return info;
	}
	
	static void removeAllTriggers() {
		triggers.clear();
		for (final Trigger t : selfRegisteredTriggers) {
			((SelfRegisteringSkriptEvent) t.getEvent()).unregisterAll();
		}
		selfRegisteredTriggers.clear();
	}
	
	/**
	 * As it's difficult to unregister events with Bukkit this set is used to prevent that any event will ever be registered more than once when reloading.
	 * <p>
	 * Subclasses of these events will not be registered, but superclasses can, resulting in a few superflouous registrations. //TODO improve?
	 */
	private final static Set<Class<? extends Event>> registeredEvents = new HashSet<Class<? extends Event>>();
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	final static void registerBukkitEvents() {
		final Listener l = new Listener() {};
		for (final Class<? extends Event> e : triggers.keySet()) {
			if (!containsSuperclass((Set) registeredEvents, e)) { // I just love Java's generics
				Bukkit.getPluginManager().registerEvent(e, l, SkriptConfig.defaultEventPriority.value(), ee, Skript.getInstance());
				registeredEvents.add(e);
			}
		}
	}
	
	public final static boolean containsSuperclass(final Set<Class<?>> classes, final Class<?> c) {
		for (final Class<?> cl : classes) {
			if (cl.isAssignableFrom(c))
				return true;
		}
		return false;
	}
	
}