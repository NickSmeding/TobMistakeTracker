package com.tobmistaketracker.detector;

import com.tobmistaketracker.TobBossNames;
import com.tobmistaketracker.TobMistakeEvent.MistakeEvent;
import com.tobmistaketracker.TobMistakeEvent.TobMistake;
import com.tobmistaketracker.TobRaider;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.events.*;
import net.runelite.api.gameval.AnimationID;
import net.runelite.api.gameval.NpcID;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Singleton;
import java.util.*;

@Slf4j
@Singleton
public class VerzikP1MistakeDetector extends BaseTobMistakeDetector {

    private static final Set<Integer> VERZIK_P1_IDS = Set.of(
            NpcID.VERZIK_PHASE1,
            NpcID.VERZIK_PHASE1_STORY,
            NpcID.VERZIK_PHASE1_HARD
    );

    // Common spec animation IDs for other weapons (examples)
    private static final HashMap<Integer, String> NON_DAWNBRINGER_SPEC_MESSAGES = new HashMap<Integer, String>() {
        {
            put(AnimationID.PUNCTURE, "swee swee"); // Dragon dagger
            put(AnimationID.SLAYER_GRANITE_MAUL_SPECIAL_ATTACK, "Granite maul"); // Granite maul
            put(AnimationID.HUMAN_DRAGON_CLAWS_SPEC, "Dragon claws"); // Dragon claws
            put(AnimationID.HUMAN_WEAPON_BURNING_CLAWS_02_SPEC, "Burning claws"); // Burning claws
            put(AnimationID.SARADOMIN_SWORD_SPECIAL_PLAYER, "Saradomin godsword"); // Saradomin godsword
            put(AnimationID.DRAGON_WARHAMMER_SA_PLAYER, "Dragon Warhammer"); // Dragon Warhammer
            put(AnimationID.HUMAN_ELDER_MAUL_SPEC, "Elder maul"); // Elder maul
            put(AnimationID.HUMAN_EYE_OF_AYAK_SPECIAL, "Eye of Ayak"); // Eye of Ayak
            put(AnimationID.DTTD_DAGGER_SP_ATTACK, "Bone dagger"); // Bone dagger?
            put(AnimationID.DRAGON_HALBERD_SPECIAL_ATTACK, "Dragon halberd or crystal halber"); // Dragon halberd and crystal halberd
            put(AnimationID.TOXIC_BLOWPIPE_SPECIAL_UPDATED, "Blowpipe"); // Blowpipe
            put(AnimationID.ABYSSAL_BLUDGEON_SPECIAL_ATTACK, "Bludgeon"); // Bludgeon
            put(AnimationID.ABYSSAL_DAGGER_SPECIAL, "Abby dagger"); // Abby dagger
            put(AnimationID.BGS_SPECIAL_PLAYER, "Bandos godsword"); // Bandos godsword
            put(AnimationID.NIGHTMARE_STAFF_SPECIAL, "I'm A Magician"); // Nightmare staff
            put(AnimationID.WEAPON_SWORD_OSMUMTEN03_SPECIAL, "Osmunten Fang"); // Osmunten Fang
            put(AnimationID.HUMAN_SPECIAL02_VOIDWAKER, "Feel my thunder"); // VoidWaker
            put(AnimationID.DARK_SPEC_PLAYER, "Dark bow"); // Dark bow
            put(AnimationID.HUMAN_HALBERD_VIRULENCE_01, "How was I even posioned?"); // Noxious halberd?
            put(AnimationID.VMQ4_ARKAN_BLADE_SPECIAL, "Oui Oui, minen baguette"); // ARKAN BLADE
            put(AnimationID.SHOVE, "BLUB"); // Dragon spear, Zamorakian spear and hasta
            // TODO: support for ZCB and ACB, its harder to add because the projectile has an animation not the player
        }};

    private final HashMap<String, Integer> playersWhoSpeccedWrong = new HashMap<>();

    @Override
    public void shutdown() {
        super.shutdown();
        playersWhoSpeccedWrong.clear();
    }

    @Override
    protected void computeDetectingMistakes() {
        if (!detectingMistakes && isAlreadySpawned()) {
            detectingMistakes = true;
        }
    }

    @Override
    public List<MistakeEvent> detectMistakes(TobRaider raider) {
        List<MistakeEvent> mistakes = new ArrayList<>();

        if (raider.isDead()) {
            return mistakes;
        }

        if (playersWhoSpeccedWrong.containsKey(raider.getName())) {
            Integer specID = playersWhoSpeccedWrong.get(raider.getPlayer().getName());
            mistakes.add(new MistakeEvent(TobMistake.MAIDEN_BLOOD_SPAWN, NON_DAWNBRINGER_SPEC_MESSAGES.get(specID)));
        }

        return mistakes;
    }

    @Override
    public void afterDetect() {
        playersWhoSpeccedWrong.clear();
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        if (!(event.getActor() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getActor();
        int animationId = player.getAnimation();

        //There is currently no way to detect spec without looking at animations
        //This will not cover every spec weapon, but it will have a list with weapons that is used a lot at tob
        //And ofc some Easter egg items
        if (NON_DAWNBRINGER_SPEC_MESSAGES.containsKey(animationId)) {
            playersWhoSpeccedWrong.put(player.getName(), animationId);
        }
    }


    //@Subscribe
    //public void onNpcSpawned(NpcSpawned event) {
    //    if (!detectingMistakes && isVerzikP1(event.getNpc())) {
    //        detectingMistakes = true;
    //    }
    //}

    //testing purpose so we can test at maiden :)
    @Subscribe
    public void onNpcSpawned(NpcSpawned event) {
        if (!detectingMistakes && TobBossNames.MAIDEN.equals(event.getActor().getName())) {
            detectingMistakes = true;
        }
    }

    @Subscribe
    public void onNpcChanged(NpcChanged event) {
        if (!detectingMistakes && isVerzikP1(event.getNpc())) {
            detectingMistakes = true;
        }
    }

    @Subscribe
    public void onActorDeath(ActorDeath event) {
        if (event.getActor() instanceof NPC && isVerzikP1((NPC) event.getActor())) {
            shutdown();
        }
    }

    private boolean isAlreadySpawned() {
        return client.getNpcs().stream().anyMatch(VerzikP1MistakeDetector::isVerzikP1);
    }

    private static boolean isVerzikP1(NPC npc) {
        return TobBossNames.VERZIK.equals(npc.getName()) && VERZIK_P1_IDS.contains(npc.getId());
    }
}
