package me.blueyescat.skriptholo.skript.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import me.blueyescat.skriptholo.util.Utils;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Delete Hologram")
@Description({"Deletes a hologram. The hologram type has the delete changer already, but this effect exists for variables. " +
		"For example `delete {_holo}` would delete the variable but you can use `delete hologram {_holo}` to avoid that.",
		"",
		"When you delete a hologram that is stored in a variable, the hologram object " +
		"will still exist in the variable but will not be usable. You should delete the variable too in this case."})
@Examples({"delete holo {_var}",
		"remove holograms {_holograms::*}"})
@Since("1.0.0")
public class EffDeleteHologram extends Effect {

	static {
		Skript.registerEffect(EffDeleteHologram.class,
				"(delete|remove|clear) holo[gram][s] %holograms%");
	}

	private Expression<Hologram> holograms;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		holograms = (Expression<Hologram>) exprs[0];
		return true;
	}

	@Override
	protected void execute(Event e) {
		Utils.deleteHologram(holograms.getArray(e));
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		return "delete " + holograms.toString(e, debug);
	}

}
