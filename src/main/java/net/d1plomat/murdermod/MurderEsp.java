package net.d1plomat.murdermod;


import net.d1plomat.murdermod.utils.ItemUtils;
import net.d1plomat.murdermod.utils.RenderUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageDecoratorEvent;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.impl.event.lifecycle.ClientLifecycleEventsImpl;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.linux.XClientMessageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

public class MurderEsp {
    private KeyBinding solver;

    private boolean isEnabled;

    private ChatHud chat;

    private List<AbstractClientPlayerEntity> murders;

    private List<AbstractClientPlayerEntity> detectives;

    private boolean inGame;

    private ArrayList<Item> murderItems;
    
    private ArrayList<Item> detectiveItems;
    
    public void Initialize()
    {
        detectiveItems = new ArrayList<Item>();
        detectiveItems.add(ItemUtils.getItemFromNameOrID("minecraft:bow"));
        murderItems = new ArrayList<Item>();
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:iron_sword"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:stone_sword"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:iron_shovel"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:stick"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:wooden_axe"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:wooden_sword"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:dead_bush"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:stone_shovel"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:blaze_rod"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:diamond_shovel"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:quartz"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:pumpkin_pie"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:golden_pickaxe"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:apple"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:name_tag"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:sponge"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:carrot_on_a_stick"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:bone"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:carrot"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:golden_carrot"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:cookie"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:diamond_axe"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:rose_bush"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:prismarine_shard"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:cooked_beef"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:nether_brick"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:cooked_chicken"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:music_disk_blocks"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:golden_sword"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:diamond_sword"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:diamond_hoe"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:shears"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:salmon"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:red_dye"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:oak_boat"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:glistering_melon_slice"));
        murderItems.add(ItemUtils.getItemFromNameOrID("minecraft:book"));

        murders = new ArrayList<AbstractClientPlayerEntity>();
        detectives = new ArrayList<>();
        isEnabled = false;
        inGame = false;
        solver = new KeyBinding("Toggle MurderMysterySolver",  GLFW.GLFW_KEY_M, "Murder and mystery mod");
        ClientTickEvents.END_CLIENT_TICK.register(this::Tick);
        ServerMessageEvents.GAME_MESSAGE.register(this::GameMessage);
        WorldRenderEvents.AFTER_ENTITIES.register(this::onWorldRender);
    }

    private void GameMessage(MinecraftServer minecraftServer, Text text, boolean b) {
        SendMessage(text.toString(), TextType.Warning);
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled || !inGame)  {
            return;
        }
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        MatrixStack stack = context.matrixStack();
        Matrix4f matrix = stack.peek().getPositionMatrix();
        stack.push();
        RenderUtil.prep();
        for (AbstractClientPlayerEntity murder : murders) {
            if (murder.isAlive() &&
                !murder.isSpectator())
            {
                RenderUtil.draw3dBox(buffer, matrix, murder.getBoundingBox(), 230, 0, 255, 50);
            }
        }
        for (AbstractClientPlayerEntity detective : detectives) {
            if (detective.isAlive() &&
                !detective.isSpectator())
            {
                RenderUtil.draw3dBox(buffer, matrix, detective.getBoundingBox(), 0, 145, 255, 50);
            }
        }
        tessellator.draw();
        RenderUtil.rev();
        stack.pop();
    }

    private void SendMessage(String text, TextType type)
    {
        String prefix = null;
        switch (type)
        {
            case Info -> prefix = "\u00a7c[\u00A79MMHack\u00a7c]\u00a7r ";
            case Warning -> prefix = "\u00a7c[\u00a76\u00a7lWARNING\u00a7c]\u00a7r ";
            case  Error -> prefix = "\u00a7c[\u00a74\u00a7lERROR\u00a7c]\u00a7r";
        }
        chat.addMessage(Text.literal(prefix + text));
    }

    private void InGameChanged()
    {
        if (!inGame && murders.stream().count() > 0) murders.clear();
        if (!inGame && detectives.stream().count() > 0) detectives.clear();
        String text = inGame ? "Found new game session.." : "Game ended..";
        SendMessage(text, TextType.Info);
    }

    private void SetInGame(boolean bool)
    {
        if (bool != inGame) {
            inGame = !inGame;
            InGameChanged();
        }
    }

    private void Tick(MinecraftClient client) {
        if (chat == null) {
            chat = MinecraftClient.getInstance().inGameHud.getChatHud();
        }
        if (solver.wasPressed()) {
            isEnabled = !isEnabled;
            String msg = isEnabled ? "Enabled" : "Disabled";
            SendMessage(msg, TextType.Info);
        }
        if (client == null ||
                client.world == null) {
            SetInGame(false);
            return;
        }
        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) {
            SetInGame(false);
            return;
        }
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
        if (objective == null) {
            SetInGame(false);
            return;
        }
        String name = objective.getName();
        if (!name.equals("MurderMystery")) {
            SetInGame(false);
            return;
        }
        SetInGame(true);
        if (!isEnabled) return;
        var players = client.world.getPlayers();
        for (AbstractClientPlayerEntity player : players) {
            if (client.player != player) { //
                Item item = player.getInventory().getMainHandStack().getItem();
                if (murderItems.contains(item) && !murders.contains(player))
                {
                    SendMessage("Found new murder " + player.getGameProfile().getName(), TextType.Info);
                    murders.add(player);
                }
                if (detectiveItems.contains(item) && !detectives.contains(player))
                {
                    SendMessage("Found player with bow " + player.getGameProfile().getName(), TextType.Info);
                    detectives.add(player);
                }
            }
        }
    }
}
