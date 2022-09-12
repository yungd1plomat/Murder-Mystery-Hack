package net.d1plomat.murdermod;


import net.d1plomat.murdermod.utils.ItemUtils;
import net.d1plomat.murdermod.utils.RenderUtil;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class MurderEsp {
    private KeyBinding solver;

    private boolean isEnabled;

    private ChatHud chat;

    private List<AbstractClientPlayerEntity> murders;

    private boolean inGame;

    private ArrayList<Item> items;

    public void Initialize()
    {
        items = new ArrayList<Item>();
        items.add(ItemUtils.getItemFromNameOrID("minecraft:iron_sword"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:stone_sword"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:iron_shovel"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:stick"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:wooden_axe"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:wooden_sword"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:dead_bush"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:stone_shovel"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:blaze_rod"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:diamond_shovel"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:quartz"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:pumpkin_pie"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:golden_pickaxe"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:apple"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:name_tag"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:sponge"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:carrot_on_a_stick"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:bone"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:carrot"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:golden_carrot"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:cookie"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:diamond_axe"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:rose_bush"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:prismarine_shard"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:cooked_beef"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:nether_brick"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:cooked_chicken"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:music_disk_blocks"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:golden_sword"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:diamond_sword"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:diamond_hoe"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:shears"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:salmon"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:red_dye"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:oak_boat"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:glistering_melon_slice"));
        items.add(ItemUtils.getItemFromNameOrID("minecraft:book"));

        murders = new ArrayList<AbstractClientPlayerEntity>();
        isEnabled = false;
        inGame = false;
        solver = new KeyBinding("Toggle MurderMysterySolver",  GLFW.GLFW_KEY_M, "Murder and mystery mod");
        ClientTickEvents.END_CLIENT_TICK.register(this::Tick);
        //ServerMessageEvents.CHAT_MESSAGE.register(this::ReceiveMessage);
        WorldRenderEvents.AFTER_ENTITIES.register(this::onWorldRender);
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled || !inGame)  {
            if (!inGame && murders.stream().count() > 0) murders.clear();
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
            if (!murder.isDead() && !murder.isSpectator()) {
                RenderUtil.draw3dBox(buffer, matrix, murder.getBoundingBox(), 230, 0, 255, 50);
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
            inGame = false;
            return;
        }
        Scoreboard scoreboard = client.world.getScoreboard();
        if (scoreboard == null) {
            inGame = false;
            return;
        }
        ScoreboardObjective objective = scoreboard.getObjectiveForSlot(1);
        if (objective == null) {
            inGame = false;
            return;
        }
        String name = objective.getName();
        if (!name.equals("MurderMystery")) {
            inGame = false;
            return;
        }
        if (!inGame)
        {
            SendMessage("Found new game session", TextType.Info);
            inGame = true;
        }
        if (!isEnabled) return;
        var players = client.world.getPlayers();
        for (AbstractClientPlayerEntity player : players) {
            if (client.player != player) {
                Item item = player.getInventory().getMainHandStack().getItem();
                if (items.contains(item) && !murders.contains(player))
                {
                    SendMessage("Found new murder " + player.getGameProfile().getName(), TextType.Info);
                    murders.add(player);
                }
            }
        }
    }

    private void ReceiveMessage(SignedMessage signedMessage, ServerPlayerEntity serverPlayerEntity, MessageType.Parameters parameters) {

    }
}
