package me.blueyescat.skriptholo.skript.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import org.skriptlang.skript.lang.comparator.Comparator;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import org.skriptlang.skript.lang.comparator.Comparators;
import ch.njol.util.Kleenean;
import ch.njol.util.coll.CollectionUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.enums.HologramLineType;
import me.blueyescat.skriptholo.skript.Types;
import me.blueyescat.skriptholo.util.Utils;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.comparator.Relation;

import java.util.ArrayList;
import java.util.List;

@Name("Hologram Lines")
@Description({"Returns lines of a hologram. Can be changed, removing a text or an item means it will search lines " +
		"that match exactly with the input and remove them.",
		"",
		"It is possible to create new lines using the `set` changer, for example if the hologram has 3 lines and " +
		"you set line 5, the line 4 will be blank."})
@Examples({"loop lines of the last created hologram:",
		"\tif loop-hologram line is an item line:",
		"\t\tdelete loop-hologram line",
		"",
		"set line 5 of create hologram to \"test\""})
@Since("1.0.0")
public class ExprHologramLines extends SimpleExpression<HologramLine> {

	static {
		Skript.registerExpression(ExprHologramLines.class, HologramLine.class, ExpressionType.PROPERTY,
				"[all] [the] lines of [holo[gram][s]] %holograms%",
				"%holograms%'[s] [all] lines",
				"[the] [holo[gram][s]] line %number% of [holo[gram][s]] %holograms%",
				"[the] %number%(st|nd|rd|th) line[s] of [holo[gram][s]] %holograms%",
				"[the] (first|1¦last) line[s] of [holo[gram][s]] %holograms%");
	}

	private Expression<Hologram> holograms;
	private Expression<Number> line;
	private Kleenean firstLine = Kleenean.UNKNOWN;
	private boolean allLines, isSingle;

	@SuppressWarnings("unchecked")
	@Override
	public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
		// All lines
		if (matchedPattern <= 1) {
			holograms = (Expression<Hologram>) exprs[0];
			allLines = true;
		// Line x
		} else if (matchedPattern <= 3) {
			line = (Expression<Number>) exprs[0];
			holograms = (Expression<Hologram>) exprs[1];
		// First/Last Line
		} else {
			firstLine = Kleenean.get(parseResult.mark == 0);
			holograms = (Expression<Hologram>) exprs[0];
		}
		isSingle = !allLines && holograms.isSingle();
		return true;
	}

	@Override
	protected HologramLine[] get(Event e) {
		List<HologramLine> lines = new ArrayList<>();
		for (Hologram holo : holograms.getArray(e)) {
			if (this.line == null) {
				// All lines
				if (allLines) {
					lines.addAll(Utils.getHologramLines(holo));
				// First/Last Line
				} else {
					int line = firstLine.isTrue() ? 0 : holo.getPage(0).getLines().size() - 1;
					lines.add(DHAPI.getHologramLine(DHAPI.getHologramPage(holo, 0), line));
				}
			// Line x
			} else {
				Number line = this.line.getSingle(e);
				if (line == null) {
					continue;
				}

				int li = line.intValue();
				/*if (!(li >= 0 && li < holo.getPage(0).getLines().size())) {
					// 0dan büyük veya 0 ise ve de hologramın satır sayısından küçükse
					System.out.println("condition cant pass");
					continue;
				}*/
				lines.add(DHAPI.getHologramLine(DHAPI.getHologramPage(holo, 0), li - 1));
			}
		}
		return lines.toArray(new HologramLine[0]);
	}

	@Override
	public Class<?>[] acceptChange(ChangeMode mode) {
		// All lines
		if (allLines) {
			switch (mode) {
				case ADD:
				case REMOVE:
				case REMOVE_ALL:
				case SET:
				case DELETE:
				case RESET:
					return CollectionUtils.array(String[].class, ItemType[].class);
			}
		} else if (mode == ChangeMode.SET || mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
			return CollectionUtils.array(String.class, ItemType.class);
		}
		return null;
	}

	@Override
	public void change(Event e, @Nullable Object[] delta, ChangeMode mode) {
		// All Lines
		if (allLines) {
			switch (mode) {
				case ADD:
					for (Hologram holo : holograms.getArray(e)) {
						if (holo.isDisabled())
							continue;
						for (Object o : delta) {
							if (o instanceof String) {
								DHAPI.addHologramLine(holo, (String) o);
							} else {
								for (ItemStack item : ((ItemType) o).getItem().getAll())
									DHAPI.addHologramLine(holo, item);
							}
						}
					}
					break;
				case REMOVE:
				case REMOVE_ALL:
					HologramLine removedLine;
					for (Hologram holo : holograms.getArray(e)) {
						if (holo.isDisabled()) {
							continue;
						}
						for (int line = 0; line < holo.getPage(0).getLines().size(); line++) {
							removedLine = holo.getPage(0).getLine(line);
							for (Object o : delta) {
								if (o instanceof String) {
									if (removedLine.getType() == HologramLineType.TEXT) {
										if (Comparators.compare(removedLine.getText(), o).isImpliedBy(Relation.EQUAL))
											removedLine.delete();
									}
								} else {
									if (removedLine.getType() == HologramLineType.ICON) {
										for (ItemStack item : ((ItemType) o).getItem().getAll()) {
											if (Comparators.compare(removedLine.getItem().getMaterial(), item.getType()).isImpliedBy(Relation.EQUAL))
												removedLine.delete();
										}
									}
								}
							}
						}
					}
					break;
				case SET:
					for (Hologram holo : holograms.getArray(e)) {
						if (holo.isDisabled()) {
							continue;
						}

						DHAPI.removeHologramPage(holo, 0);
						DHAPI.addHologramPage(holo);
						for (Object o : delta) {
							if (o instanceof String) {
								DHAPI.addHologramLine(holo, (String) o);
							} else {
								for (ItemStack item : ((ItemType) o).getItem().getAll()) {
									DHAPI.addHologramLine(holo, item);
								}
							}
						}
					}
					break;
				case DELETE:
				case RESET:
					for (Hologram holo : holograms.getArray(e)) {
						if (!holo.isDisabled()) {
							DHAPI.removeHologramPage(holo, 0);
							DHAPI.addHologramPage(holo);
						}
					}
			}
		// Single lines will use changers of the HologramLine type
		} else {
			// But make it possible to create new lines using 'line x' with SET
			if (mode == ChangeMode.SET && line != null) {
				Number line = this.line.getSingle(e);
				if (line != null) {
					int li = line.intValue();
					if (li <= 0) {
						return;
					}

					for (Hologram holo : holograms.getArray(e)) {
						if (holo.isDisabled()) {
							continue;
						}

						int size = holo.getPage(0).getLines().size();
						if (li > size) {
							for (int i = 0; i < li - size; i++) {
								DHAPI.addHologramLine(holo, "");
							}
							Object o = delta[0];
							if (o instanceof String) {
								DHAPI.setHologramLine(holo, li - 1, (String) o);
							} else {
								DHAPI.setHologramLine(holo, li - 1, ((ItemType) o).getItem().getRandom());
							}
						} else {
							Types.hologramLineChanger.change(CollectionUtils.array(holo.getPage(0).getLine(li - 1)), delta, ChangeMode.SET);
						}
					}
				}
			} else {
				Types.hologramLineChanger.change(get(e), delta, mode);
			}
		}
	}

	@Override
	public boolean isSingle() {
		return isSingle;
	}

	@Override
	public Class<? extends HologramLine> getReturnType() {
		return HologramLine.class;
	}

	@Override
	public String toString(@Nullable Event e, boolean debug) {
		if (allLines) {
			return "the lines of " + holograms.toString(e, debug);
		} else {
			if (line == null)
				return "the " + (firstLine.isTrue() ? "first" : "last") + " line of " + holograms.toString(e, debug);
			return "the line " + line.toString(e, debug) + " of " + holograms.toString(e, debug);
		}
	}

}
