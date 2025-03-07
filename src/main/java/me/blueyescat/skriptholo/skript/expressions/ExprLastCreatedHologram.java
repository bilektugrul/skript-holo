package me.blueyescat.skriptholo.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.blueyescat.skriptholo.skript.effects.EffCreateHologram;
import me.blueyescat.skriptholo.util.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Last Created Hologram")
@Description({"Holds the hologram that was created most recently with the `Create Hologram` effect.",
		"Can be deleted using the `delete/clear` changer which means the hologram will be " +
		"removed from the world and this expression will be empty."})
@Examples("set {_holo} to the created hologram")
@Since("1.0.0")
public class ExprLastCreatedHologram extends SimpleExpression<Hologram> {

	static {
		Skript.registerExpression(ExprLastCreatedHologram.class, Hologram.class, ExpressionType.SIMPLE,
				"[the] [last[ly]] [(created|spawned)] holo[gram]");
	}

	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		return true;
	}

	@Override
	protected Hologram[] get(Event e) {
		return CollectionUtils.array(EffCreateHologram.lastCreated);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
			return CollectionUtils.array();
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		Utils.deleteHologram(EffCreateHologram.lastCreated);
	}

	@Override
	public boolean isSingle() {
		return true;
	}

	@Override
	public Class<? extends Hologram> getReturnType() {
		return Hologram.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the last created hologram";
	}

}
