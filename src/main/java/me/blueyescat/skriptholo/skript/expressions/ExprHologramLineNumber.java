package me.blueyescat.skriptholo.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Hologram Line Number")
@Description("Returns the number of a hologram line. But please note that getting line number of a hologram line " +
		"will check every line of the hologram. You shouldn't make systems that relies on line numbers, but contents.")
@Examples("if line number of event-holo-line is 2:")
@Since("1.0.0")
public class ExprHologramLineNumber extends SimpleExpression<Number> {

	static {
		Skript.registerExpression(ExprHologramLineNumber.class, Number.class, ExpressionType.SIMPLE,
				"[the] line number[s] of [holo[gram] line[s]] %hologramlines%",
				"%hologramlines%'[s] line number[s]");
	}

	private Expression<HologramLine> lines;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		lines = (Expression<HologramLine>) exprs[0];
		return true;
	}

	@Override
	protected Number[] get(Event e) {
		List<Number> numbers = new ArrayList<>();
		for (HologramLine line : lines.getArray(e)) {
			Hologram holo = line.getParent().getParent();
			for (int l = 0; l < holo.size(); l++) {
				if (holo.getPage(0).getLine(l).equals(line))
					numbers.add(l + 1);
			}
		}
		return numbers.toArray(new Number[0]);
	}

	@Override
	public boolean isSingle() {
		return lines.isSingle();
	}

	@Override
	public Class<? extends Number> getReturnType() {
		return Number.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the line number of " + lines.toString(e, debug);
	}

}
