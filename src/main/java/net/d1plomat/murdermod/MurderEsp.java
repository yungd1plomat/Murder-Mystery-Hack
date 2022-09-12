package net.d1plomat.murdermod;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents.ChatMessage;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.time.format.TextStyle;
import java.util.ArrayList;

public class MurderEsp {
    private KeyBinding solver;

    private Boolean isEnabled;

    private ChatHud chat;

    private ArrayList<AbstractClientPlayerEntity> espPlayers = new ArrayList();
    public void Initialize()
    {
        isEnabled = false;
        solver = new KeyBinding("Toggle MurderMysterySolver",  GLFW.GLFW_KEY_M, "Murder and mystery mod");
        ClientTickEvents.END_CLIENT_TICK.register(this::Tick);
        ServerMessageEvents.CHAT_MESSAGE.register(this::ReceiveMessage);
        WorldRenderEvents.LAST.register(this::onWorldRender);
    }

    private void onWorldRender(WorldRenderContext worldRenderContext) {
        worldRenderContext.world().getPlayers();
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
        if (isEnabled) {
            if (client != null &&
                    client.world != null) {
                var players = client.world.getPlayers();
                for (AbstractClientPlayerEntity player : players) {
                    if (client.player != player) {
                        GL11.glPushMatrix();
                        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glDisable(GL11.GL_LIGHTING);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        GL11.glEnable(GL11.GL_LINE_SMOOTH);
                        GL11.glEnable(GL11.GL_BLEND);
                        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    }
                }
            }
        }
    }

    private void ReceiveMessage(SignedMessage signedMessage, ServerPlayerEntity serverPlayerEntity, MessageType.Parameters parameters) {

    }


}
