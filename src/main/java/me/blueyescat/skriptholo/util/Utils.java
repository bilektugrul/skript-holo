package me.blueyescat.skriptholo.util;

import ch.njol.skript.util.Direction;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import eu.decentsoftware.holograms.api.holograms.HologramLine;
import me.blueyescat.skriptholo.SkriptHolo;
import me.blueyescat.skriptholo.skript.effects.EffCreateHologram;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class Utils {

	public static boolean hasPlugin(String name) {
		return Bukkit.getServer().getPluginManager().isPluginEnabled(name);
	}

	public static Plugin getPlugin(String name) {
		return Bukkit.getServer().getPluginManager().getPlugin(name);
	}

	public static List<HologramLine> getHologramLines(Hologram holo) {
		List<HologramLine> lines = new ArrayList<>();
		for (int l = 0; l < holo.getPage(0).getLines().size(); l++)
			lines.add(holo.getPage(0).getLine(l));
		return lines;
	}

	@SuppressWarnings("unchecked")
	public static void deleteHologram(Integer entityID, Hologram... holograms) {
		for (Hologram holo : holograms) {
			if (!holo.isDisabled())
				holo.delete();
			if (holo.equals(EffCreateHologram.lastCreated))
				EffCreateHologram.lastCreated = null;
			if (isFollowingHologram(holo)) {
				Iterator it;
				it = SkriptHolo.followingHologramsEntities.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry entry = (Map.Entry) it.next();
					List<Hologram> holoList = (List<Hologram>) entry.getValue();
					holoList.removeIf(holo2 -> holo2.equals(holo));
					if (holoList.isEmpty())
						it.remove();
				}
				if (entityID == null) {
					it = SkriptHolo.followingHolograms.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry entry = (Map.Entry) it.next();
						Map<Hologram, Direction[]> holoMap = (Map<Hologram, Direction[]>) entry.getValue();
						for (Object o2 : holoMap.entrySet()) {
							Map.Entry entry2 = (Map.Entry) o2;
							if (entry2.getKey().equals(holo)) {
								it.remove();
							}
						}
					}
				} else {
					SkriptHolo.followingHolograms.remove(entityID);
				}
			}
			SkriptHolo.followingHologramsList.remove(holo);
		}
	}

	public static void deleteHologram(Hologram... holograms) {
		deleteHologram(null, holograms);
	}

	public static void deleteFollowingHolograms(int entityID) {
		Map<Hologram, Direction[]> holoMap = SkriptHolo.followingHolograms.get(entityID);
		if (holoMap == null || holoMap.isEmpty())
			return;
		for (Object o : holoMap.entrySet()) {
			Map.Entry entry = (Map.Entry) o;
			Hologram holo = (Hologram) entry.getKey();
			Utils.deleteHologram(entityID, holo);
		}
	}

	@SuppressWarnings("unchecked")
	public static void cleanFollowingHolograms() {
		for (Object o : SkriptHolo.followingHologramsEntities.entrySet()) {
			Map.Entry entry = (Map.Entry) o;
			Entity entity = (Entity) entry.getKey();
			if (!entity.isValid()) {
				for (Hologram holo : (List<Hologram>) entry.getValue()) {
					if (!holo.isDisabled())
						holo.delete();
					if (holo.equals(EffCreateHologram.lastCreated))
						EffCreateHologram.lastCreated = null;
					SkriptHolo.followingHologramsList.remove(holo);
				}
				SkriptHolo.followingHologramsEntities.remove(entity);
				SkriptHolo.followingHolograms.remove(entity.getEntityId());
			}
		}
	}

	public static Location offsetLocation(Location loc, Direction... directions) {
		for (Direction d : directions)
			loc = d.getRelative(loc);
		return loc;
	}

	public static void makeHologramStartFollowing(Hologram holo, Entity entity, Direction[] offset) {
		SkriptHolo.followingHologramsList.add(holo);

		Map<Hologram, Direction[]> holoMap;
		int entityID = entity.getEntityId();
		holoMap = SkriptHolo.followingHolograms.get(entityID);
		if (holoMap == null)
			holoMap = new HashMap<>();
		holoMap.put(holo, offset);
		SkriptHolo.followingHolograms.put(entityID, holoMap);

		List<Hologram> holoList;
		holoList = SkriptHolo.followingHologramsEntities.get(entity);
		if (holoList == null)
			holoList = new ArrayList<>();
		holoList.add(holo);
		SkriptHolo.followingHologramsEntities.put(entity, holoList);

		Location location = entity.getLocation().clone();
		if (holo.getLocation().getWorld() == location.getWorld())
			holo.setLocation(offset != null ? offsetLocation(location, offset) : location);
	}

	@SuppressWarnings("unchecked")
	public static void makeHologramStopFollowing(Hologram holo) {
		Iterator it;
		it = SkriptHolo.followingHolograms.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			Map<Hologram, Direction[]> holoMap = (Map<Hologram, Direction[]>) entry.getValue();
			Iterator it2 = holoMap.entrySet().iterator();
			while (it2.hasNext()) {
				Hologram holo2 = (Hologram) ((Map.Entry) it2.next()).getKey();
				if (holo2.equals(holo))
					it2.remove();
			}
			it.remove();
		}

		it = SkriptHolo.followingHologramsEntities.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			List<Hologram> holoList = (List<Hologram>) entry.getValue();
			holoList.removeIf(holo2 -> holo2.equals(holo));
			if (holoList.isEmpty())
				it.remove();
		}
		SkriptHolo.followingHologramsList.remove(holo);
	}

	public static boolean isFollowingHologram(Hologram holo) {
		return SkriptHolo.followingHologramsList.contains(holo);
	}

}
