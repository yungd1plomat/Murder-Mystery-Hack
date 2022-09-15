package net.d1plomat.murdermod;


import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.d1plomat.murdermod.models.MMConfig;
import net.d1plomat.murdermod.models.TextType;
import net.d1plomat.murdermod.utils.ConfigUtils;
import net.d1plomat.murdermod.utils.ItemUtils;
import net.d1plomat.murdermod.utils.RenderUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.item.Item;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MurderEsp {
    private KeyBinding mSolver;

    private KeyBinding nSolver;

    private boolean isEnabled;

    private ChatHud chat;

    private List<AbstractClientPlayerEntity> murders;

    private List<AbstractClientPlayerEntity> detectives;

    private boolean inGame;

    private boolean followEnabled;

    private ArrayList<Item> murderItems;

    private ArrayList<Item> detectiveItems;

    private boolean isMurder;

    private boolean isDetective;

    private MMConfig config;

    private HttpClient httpClient;

    private boolean inLobby;

    private Object locker;

    public void Initialize()
    {
        locker = new Object();
        httpClient = HttpClient.newHttpClient();
        config = ConfigUtils.LoadConfig();
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

        murders = new ArrayList<>();
        detectives = new ArrayList<>();
        isEnabled = false;
        inGame = false;
        followEnabled = false;
        inLobby = false;
        mSolver = new KeyBinding("Toggle MurderMysterySolver",  GLFW.GLFW_KEY_M, "Murder and mystery mod");
        nSolver = new KeyBinding("Toggle follower",  GLFW.GLFW_KEY_N, "Murder and mystery mod");
        ClientTickEvents.END_CLIENT_TICK.register(this::Tick);
        WorldRenderEvents.AFTER_ENTITIES.register(this::onWorldRender);
        ClientCommandRegistrationCallback.EVENT.register(this::configCommand);
        Notify("Config successfully loaded!");
    }

    private void SendConfig()
    {
        SendMessage("Current config:", TextType.Info);
        SendMessage("Chat Id: " + config.ChatId, TextType.Info);
        SendMessage("Token: " + config.Token, TextType.Info);
    }

    private void configCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess dedicated) {
        dispatcher.register(ClientCommandManager.literal("config")
                                                .then(
                                                        ClientCommandManager.argument("chatId", LongArgumentType.longArg())
                                                                .then(
                                                                        ClientCommandManager.argument("token", StringArgumentType.greedyString()).executes(context -> {
                                                                            long chatId = LongArgumentType.getLong(context, "chatId");
                                                                            String token = StringArgumentType.getString(context, "token");
                                                                            config.ChatId = chatId;
                                                                            config.Token = token;
                                                                            ConfigUtils.SaveConfig(config);
                                                                            SendConfig();
                                                                            return 1;
                                                                        })
                                                                )
                                                ).executes(context -> {
                                                    SendConfig();
                                                    return 1;
                                                })
        );
    }

    private void onWorldRender(WorldRenderContext context) {
        if (!isEnabled || !inGame)  {
            return;
        }
        try {
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            MatrixStack stack = context.matrixStack();
            Matrix4f matrix = stack.peek().getPositionMatrix();
            stack.push();
            RenderUtils.prep();
            if (!isMurder) {
                for (AbstractClientPlayerEntity murder : murders) {
                    if (!murder.isSpectator() && !murder.isInvisible()) {
                        RenderUtils.draw3dBox(buffer, matrix, murder.getBoundingBox(), 230, 0, 255, 50);
                    } else {
                        synchronized (locker) {
                            murders.remove(murder);
                            if (murders.stream().count() == 0) {
                                MinecraftClient.getInstance().player.sendChatMessage("#follow players", Text.empty());
                            }
                        }
                    }
                }
            }
            for (AbstractClientPlayerEntity detective : detectives) {
                if (!detective.isSpectator() && !detective.isInvisible()) {
                    RenderUtils.draw3dBox(buffer, matrix, detective.getBoundingBox(), 0, 145, 255, 50);
                }
            }
            tessellator.draw();
            RenderUtils.rev();
            stack.pop();
        } catch (Exception ex)
        {
            SendMessage(ex.getMessage(), TextType.Error);
        }
    }

    private void SendMessage(String text, TextType type)
    {
        String prefix = null;
        switch (type)
        {
            case Info -> prefix = "\u00a7c[\u00A79MMHack\u00a7c]\u00a7r ";
            case Warning -> prefix = "\u00a7c[\u00a76\u00a7lWARNING\u00a7c]\u00a7r ";
            case Error -> prefix = "\u00a7c[\u00a74\u00a7lERROR\u00a7c]\u00a7r ";
        }
        chat.addMessage(Text.literal(prefix + text));
    }

    private void InGameChanged() {
        isDetective = false;
        isMurder = false;
        String text = inGame ? "Found new game session.." : "Game ended..";
        SendMessage(text, TextType.Info);
        if (!inGame && followEnabled && isEnabled) {
            if (murders.stream().count() > 0) {
                synchronized (locker) {
                    murders.clear();
                }
            }
            if (detectives.stream().count() > 0) {
                synchronized (locker) {
                    detectives.clear();
                }
            }
            StartNewGame();
            return;
        }
        if (followEnabled && inGame && isEnabled) {
            followPlayers();
            CheckForRole();
        }
    }

    private void CheckForRole()
    {
        new Thread(() -> {
            waitTime(16500);
            SendMessage("Checking current role..", TextType.Info);
            detectiveItems.forEach(item -> {
                if (MinecraftClient.getInstance().player.getInventory().contains(item.getDefaultStack())) {
                    synchronized (locker) {
                        isDetective = true;
                    }
                    MinecraftClient.getInstance().player.sendChatMessage("#stop", Text.empty());
                    Notify("You're detective!!");
                }
            });
            murderItems.forEach(item -> {
                if (MinecraftClient.getInstance().player.getInventory().contains(item.getDefaultStack())) {
                    synchronized (locker) {
                        isMurder = true;
                    }
                    MinecraftClient.getInstance().player.sendChatMessage("#stop", Text.empty());
                    murders.clear();
                    Notify("You're murder!!");
                }
            });
        }).start();
    }

    private void SetInGame(boolean bool)
    {
        if (bool != inGame) {
            synchronized (locker) {
                inGame = !inGame;
            }
            InGameChanged();
        }
    }

    private void waitTime(int MS) {
        try
        {
            TimeUnit.MILLISECONDS.sleep(MS);
        }
        catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }

    private void StartNewGame()
    {
        synchronized (locker) {
            inLobby = true;
        }
        new Thread(() -> {
            do {
                SendMessage("Going to lobby..", TextType.Info);
                MinecraftClient.getInstance().player.sendChatMessage("#stop", Text.empty());
                MinecraftClient.getInstance().player.sendChatMessage("/play murder_double_up", Text.empty());
                waitTime(6000);
            } while(MinecraftClient.getInstance().world.getPlayers().stream().count() < 8);
            synchronized (locker) {
                inLobby = false;
            }
        }).start();
    }

    private void Notify(String text) {
        try {
            URL url = new URL("https://api.telegram.org/bot" +
                                    config.Token +
                                    "/sendMessage?chat_id=" +
                                    config.ChatId +
                                    "&text=" +
                                    text);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);
            MurderMod.LOGGER.info(String.valueOf(con.getResponseCode()));

        } catch (Exception ex) {
            MurderMod.LOGGER.error(ex.getMessage());
        }
    }

    private boolean CheckConfigLoaded()
    {
        return config.ChatId != 0 && config.Token != "";
    }

    private void followMurders()
    {
        new Thread(() -> {
            MinecraftClient.getInstance().player.sendChatMessage("#stop", Text.empty());
            waitTime(1500);
            MinecraftClient.getInstance().player.sendChatMessage("#follow player " + String.join(" ", murders.stream().map(x -> x.getGameProfile().getName()).toList()), Text.empty());
        }).start();

    }

    private void followPlayers()
    {
        MinecraftClient.getInstance().player.sendChatMessage("#follow players", Text.empty());
    }

    private void Tick(MinecraftClient client) {
        if (chat == null) {
            chat = MinecraftClient.getInstance().inGameHud.getChatHud();
        }
        if (client == null || client.world == null) {
            SetInGame(false);
            return;
        }
        try {
            if (mSolver.wasPressed()) {
                if (!CheckConfigLoaded()) {
                    SendMessage("Set chat id and token with /config command", TextType.Error);
                    return;
                }
                isEnabled = !isEnabled;
                String msg = isEnabled ? "Enabled" : "Disabled";
                SendMessage(msg, TextType.Info);
            }
            if (nSolver.wasPressed()) {
                if (!CheckConfigLoaded()) {
                    SendMessage("Set chat id and token with /config command", TextType.Error);
                    return;
                }
                followEnabled = !followEnabled;
                String msg = followEnabled ? "Enabled follower" : "Disabled follower";
                SendMessage(msg, TextType.Info);
                if (followEnabled) {
                    StartNewGame();
                } else {
                    MinecraftClient.getInstance().player.sendChatMessage("#stop", Text.empty());
                }
            }
            if (client.player.isInvisible() && followEnabled && !inLobby) {
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
                if (client.player != player) {
                    Item item = player.getInventory().getMainHandStack().getItem();
                    if (murderItems.contains(item) && !murders.contains(player) && !isMurder) {
                        SendMessage("Found new murder " + player.getGameProfile().getName(), TextType.Info);
                        synchronized (locker) {
                            murders.add(player);
                        }
                        if (followEnabled && !isDetective) {
                            followMurders();
                        }
                    }
                    if (detectiveItems.contains(item) && !detectives.contains(player)) {
                        SendMessage("Found player with bow " + player.getGameProfile().getName(), TextType.Info);
                        synchronized (locker) {
                            detectives.add(player);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            SendMessage(ex.getMessage(), TextType.Error);
        }
    }
}
