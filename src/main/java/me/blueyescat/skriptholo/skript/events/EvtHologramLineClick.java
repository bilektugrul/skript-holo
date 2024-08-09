package me.blueyescat.skriptholo.skript.events;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptEvent;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.event.HologramClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class EvtHologramLineClick extends SkriptEvent {

	static {
		Skript.registerEvent("Hologram Line Click", EvtHologramLineClick.class, HologramClickEvent.class,
				"holo[gram] [line] click")
				.description("Called when a player clicks on a hologram line. " +
						"See the `Make Hologram Line Click-able` effect.")
				.examples("on hologram click:",
						"\tif event-hologram-line is \"test\":")
				.since("1.0.0");

		EventValues.registerEventValue(HologramClickEvent.class, Player.class, new Getter<Player, HologramClickEvent>() {
			@Override
			public Player get(HologramClickEvent e) {
				return e.getPlayer();
			}
		}, 0);
		EventValues.registerEventValue(HologramClickEvent.class, Hologram.class, new Getter<Hologram, HologramClickEvent>() {
			@Override
			public Hologram get(HologramClickEvent e) {
				return e.getHologram();
			}
		}, 0);
	}

	@Override
	public boolean init(Literal<?>[] args, int matchedPattern, SkriptParser.ParseResult parser) {
		return true;
	}

	@Override
	public boolean check(Event e) {
		return e instanceof HologramClickEvent;
	}

	@Override
	public String toString(Event e, boolean debug) {
		return "hologram line click";
	}

}
