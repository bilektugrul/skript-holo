package me.blueyescat.skriptholo.skript;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Converter;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.util.coll.CollectionUtils;
import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import eu.decentsoftware.holograms.api.holograms.enums.HologramLineType;
import eu.decentsoftware.holograms.api.utils.items.HologramItem;
import me.blueyescat.skriptholo.util.Utils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.eclipse.jdt.annotation.Nullable;

public class Types {

	public static Changer<HologramLine> hologramLineChanger;

	static {
		// Hologram
		Classes.registerClass(new ClassInfo<>(Hologram.class, "hologram")
				.user("holo(gram)?s?")
				.name("Hologram")
				.description("A HolographicDisplays hologram. Can be deleted using the `delete/clear` changer or " +
						"can be reset using the `reset` changer. " +
						"When you delete a hologram that is stored in a variable, the hologram object will still " +
						"exist in the variable but will not be usable. You should delete the variable too in this case.")
				.since("1.0.0")
				.changer(new Changer<Hologram>() {
					@Override
					public Class<?>[] acceptChange(ChangeMode mode) {
						if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET)
							return CollectionUtils.array();
						return null;
					}

					@Override
					public void change(Hologram[] holograms, @Nullable Object[] delta, ChangeMode mode) {
						if (mode == ChangeMode.DELETE) {
							Utils.deleteHologram(holograms);
						} else {
							for (Hologram holo : holograms) {
								if (!holo.isDisabled())
									holo.removePage(0);
									holo.addPage();
							}
						}
					}
				})
				.parser(new Parser<Hologram>() {
					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(Hologram holo, int flags) {
						return "hologram";
					}

					@Override
					public String toVariableNameString(Hologram holo) {
						return "hologram";
					}

					@Override
					public String getVariableNamePattern() {
						return "\\S+";
					}
				}));

		Converters.registerConverter(Hologram.class, Location.class, (Converter<Hologram, Location>) Hologram::getLocation);

		// Hologram Line
		hologramLineChanger = new Changer<HologramLine>() {
			@Override
			public Class<?>[] acceptChange(ChangeMode mode) {
				if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET || mode == ChangeMode.SET)
					return CollectionUtils.array(String.class, ItemType.class);
				return null;

			}

			@Override
			public void change(HologramLine[] lines, @Nullable Object[] delta, ChangeMode mode) {
				if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
					for (HologramLine line : lines)
						line.delete();
				} else {
					for (HologramLine line : lines) {
						Object o = delta[0];
						if (o instanceof String) {
							if (line.getType() == HologramLineType.TEXT) {
								line.setText((String) o);
							// Find the line and make it TextLine
							} else {
								Hologram holo = line.getParent().getParent();
								int i = 0;
								for (HologramLine l : Utils.getHologramLines(holo)) {
									if (l.equals(line)) {
										line.delete();
										DHAPI.insertHologramLine(holo, i, (String) o);
									}
									i++;
								}
							}

						} else {
							if (line.getType() == HologramLineType.ICON) {
								line.setItem(HologramItem.fromItemStack(((ItemType) o).getItem().getRandom()));
							// Find the line and make it ItemLine
							} else {
								Hologram holo = line.getParent().getParent();
								int i = 0;
								for (HologramLine l : Utils.getHologramLines(holo)) {
									if (l.equals(line)) {
										line.delete();
										DHAPI.insertHologramLine(holo, i, ((ItemType) o).getItem().getRandom());
									}
									i++;
								}
							}
						}
					}
				}
			}
		};

		Classes.registerClass(new ClassInfo<>(HologramLine.class, "hologramline")
				.user("holo(gram)?( |-)?lines?")
				.name("Hologram Line")
				.description("A line of a HolographicDisplays hologram. Can be deleted using the 'delete/clear' changer.",
						"",
						"Has converters to `text`, `item type` and `number`. " +
						"Converters mean that you can use this type like the converted types. " +
						"For example you can do `give line 1 of hologram to player` and `if event-hologram-line is 5:`, but please note that getting line " +
						"number of a hologram line will check every line of the hologram. " +
						"You shouldn't make systems that relies on line numbers, but contents.")
				.since("1.0.0")
				.changer(hologramLineChanger)
				.parser(new Parser<HologramLine>() {
					@Override
					public boolean canParse(ParseContext context) {
						return false;
					}

					@Override
					public String toString(HologramLine line, int flags) {
						if (line.getType() == HologramLineType.ICON)
							return "hologram item line";
						return "hologram line";
					}

					@Override
					public String toVariableNameString(HologramLine line) {
						if (line.getType() == HologramLineType.ICON)
							return "hologram item line";
						return "hologram line";
					}

					@Override
					public String getVariableNamePattern() {
						return "\\S+";
					}
				}));

		Converters.registerConverter(HologramLine.class, String.class, (Converter<HologramLine, String>) line -> line.getType() == HologramLineType.TEXT ? line.getText() : null);
		Converters.registerConverter(HologramLine.class, ItemType.class, (Converter<HologramLine, ItemType>) line -> line.getType() == HologramLineType.ICON
				? new ItemType(line.getItem().getMaterial().getId()) : null);
		Converters.registerConverter(HologramLine.class, Number.class, (Converter<HologramLine, Number>) line -> line.getType() == HologramLineType.TEXT
				? Double.parseDouble(line.getText()) : null);

	}

}
