package me.blueyescat.skriptholo.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.*;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.blueyescat.skriptholo.SkriptHolo;
import me.blueyescat.skriptholo.util.Utils;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Following Holograms of Entity")
@Description("Returns the following holograms of an entity. " +
		"Use the `Following Hologram` effect to make an exiting hologram start/stop following an entity.")
@Examples({"delete the following holograms of targeted entity",
		"set {_holograms::*} to all holograms that follows the player"})
@Since("1.0.0")
@RequiredPlugins("ProtocolLib")
public class ExprEntityFollowingHolograms extends SimpleExpression<Hologram> {

	static {
		Skript.registerExpression(ExprEntityFollowingHolograms.class, Hologram.class, ExpressionType.SIMPLE,
				"[all] [the] following holo[gram]s of %entities%",
				"[all] [the] holo[gram]s (following|that follows) %entities%");
	}

	private Expression<Entity> entities;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		if (!Utils.hasPlugin("ProtocolLib")) {
			Skript.error("The following hologram feature requires ProtocolLib");
			return false;
		}
		entities = (Expression<Entity>) exprs[0];
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Hologram[] get(Event e) {
		List<Hologram> holograms = new ArrayList<>();
		for (Entity entity : entities.getArray(e)) {
			List<Hologram> holoList = SkriptHolo.followingHologramsEntities.get(entity);
			if (holoList != null)
				holograms.addAll(holoList);
		}
		return holograms.toArray(new Hologram[0]);
	}

	@Override
	public boolean isSingle() {
		return false;
	}

	@Override
	public Class<? extends Hologram> getReturnType() {
		return Hologram.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "the holograms following " + entities.toString(e, debug);
	}

}
