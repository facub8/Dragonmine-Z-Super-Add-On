package com.dragonminez.common.stats;

import com.dragonminez.common.config.ConfigManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.HashMap;
import java.util.Map;

public class Skills {
    private final Map<String, Skill> skillMap = new HashMap<>();

    public Skills() {
        registerDefaultSkills();
    }

    private void registerDefaultSkills() {
        skillMap.put("superform", new Skill("superform", 50));
        skillMap.put("godform", new Skill("godform", 5));
        skillMap.put("legendaryforms", new Skill("legendaryforms", 50));
        skillMap.put("ultrainstinct", new Skill("ultrainstinct", 1));
    }

    public void updateTransformationMaxLevels(int superformMax, int godformMax, int legendaryformsMax, int androidformsMax) {
        Skill superform = skillMap.get("superform");
        if (superform != null) {
            int finalMax = Math.max(superform.getMaxLevel(), superformMax);
            Skill updated = new Skill("superform", superform.getLevel(), superform.isActive(), finalMax);
            skillMap.put("superform", updated);
        }

        Skill godform = skillMap.get("godform");
        if (godform != null) {
            int finalMax = Math.max(godform.getMaxLevel(), godformMax);
            Skill updated = new Skill("godform", godform.getLevel(), godform.isActive(), finalMax);
            skillMap.put("godform", updated);
        }

        Skill legendaryforms = skillMap.get("legendaryforms");
        if (legendaryforms != null) {
            int finalMax = Math.max(legendaryforms.getMaxLevel(), legendaryformsMax);
            Skill updated = new Skill("legendaryforms", legendaryforms.getLevel(), legendaryforms.isActive(), finalMax);
            skillMap.put("legendaryforms", updated);
        }

        Skill androidforms = skillMap.get("androidforms");
        if (androidforms != null) {
            int finalMax = Math.max(androidforms.getMaxLevel(), androidformsMax);
            Skill updated = new Skill("androidforms", androidforms.getLevel(), androidforms.isActive(), finalMax);
            skillMap.put("androidforms", updated);
        }
    }

    public Skill getSkill(String name) {
        return skillMap.get(name.toLowerCase());
    }

    public boolean hasSkill(String name) {
        return skillMap.containsKey(name.toLowerCase());
    }

    public int getSkillLevel(String name) {
        Skill skill = skillMap.get(name.toLowerCase());
        return skill != null ? skill.getLevel() : 0;
    }

	public int getMaxSkillLevel(String name) {
		Skill skill = skillMap.get(name.toLowerCase());
		return skill != null ? skill.getMaxLevel() : 0;
	}

    public void setSkillLevel(String name, int level) {
        String lowerName = name.toLowerCase();
        if (!skillMap.containsKey(lowerName)) {
            int costBasedMaxLevel = ConfigManager.getSkillsConfig().getSkillCosts(lowerName).getCosts().size();
            int finalMaxLevel;
            if (lowerName.equalsIgnoreCase("potentialunlock")) {
                finalMaxLevel = Math.min(costBasedMaxLevel, 30);
            } else {
                finalMaxLevel = Math.min(costBasedMaxLevel, 50);
            }
            skillMap.put(lowerName, new Skill(name, 0, false, finalMaxLevel));
        }
        skillMap.get(lowerName).setLevel(level);
    }

    public void removeSkill(String name) {
        String lowerName = name.toLowerCase();
        if (lowerName.equals("superform") || lowerName.equals("godform") || lowerName.equals("legendaryforms")) {
            Skill skill = skillMap.get(lowerName);
            if (skill != null) {
                skill.setLevel(0);
                skill.setActive(false);
            }
        } else {
            skillMap.remove(lowerName);
        }
    }

	public void removeAllSkills() {
		skillMap.clear();
		registerDefaultSkills();
	}

    public void addSkillLevel(String name, int amount) {
        Skill skill = skillMap.get(name.toLowerCase());
        if (skill != null) {
            skill.addLevel(amount);
        }
    }

    public boolean isSkillActive(String name) {
        Skill skill = skillMap.get(name.toLowerCase());
        return skill != null && skill.isActive();
    }

    public void setSkillActive(String name, boolean active) {
        Skill skill = skillMap.get(name.toLowerCase());
        if (skill != null) {
            skill.setActive(active);
        }
    }

    public void toggleSkillActive(String name) {
        Skill skill = skillMap.get(name.toLowerCase());
        if (skill != null) {
            skill.setActive(!skill.isActive());
        }
    }

    public Map<String, Skill> getAllSkills() {
        return new HashMap<>(skillMap);
    }

    public CompoundTag save() {
        CompoundTag nbt = new CompoundTag();
        ListTag skillsList = new ListTag();

        for (Skill skill : skillMap.values()) {
            skillsList.add(skill.save());
        }

        nbt.put("SkillsList", skillsList);
        return nbt;
    }

    public void load(CompoundTag nbt) {
        if (nbt.contains("SkillsList", Tag.TAG_LIST)) {
            ListTag skillsList = nbt.getList("SkillsList", Tag.TAG_COMPOUND);
            skillMap.clear();
            registerDefaultSkills();
            for (int i = 0; i < skillsList.size(); i++) {
                CompoundTag skillTag = skillsList.getCompound(i);
                Skill skill = Skill.load(skillTag);
                skillMap.put(skill.getName().toLowerCase(), skill);
            }
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(skillMap.size());
        for (Skill skill : skillMap.values()) {
            skill.toBytes(buf);
        }
    }

    public void fromBytes(FriendlyByteBuf buf) {
        int size = buf.readInt();
        skillMap.clear();
        for (int i = 0; i < size; i++) {
            Skill skill = Skill.fromBytes(buf);
            skillMap.put(skill.getName().toLowerCase(), skill);
        }
    }

    public void copyFrom(Skills other) {
        this.skillMap.clear();
        for (Map.Entry<String, Skill> entry : other.skillMap.entrySet()) {
            Skill newSkill = new Skill(
                entry.getValue().getName(),
                entry.getValue().getLevel(),
                entry.getValue().isActive(),
                entry.getValue().getMaxLevel()
            );
            this.skillMap.put(entry.getKey(), newSkill);
        }
    }
}
