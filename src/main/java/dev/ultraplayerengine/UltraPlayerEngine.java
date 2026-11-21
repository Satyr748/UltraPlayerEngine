package dev.ultraplayerengine;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import java.util.Locale;

public final class UltraPlayerEngine implements ModInitializer {
    private static final TickMonitor MONITOR = new TickMonitor(120);

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> MONITOR.recordTick());

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                registerCommands(dispatcher));

        System.out.println("[UltraPlayerEngine] Server-side monitor initialized.");
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("upe")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("status").executes(context -> {
                    MinecraftServer server = context.getSource().getServer();
                    TickMonitor.Snapshot snapshot = MONITOR.snapshot();
                    Runtime runtime = Runtime.getRuntime();

                    long usedMb = (runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L;
                    long maxMb = runtime.maxMemory() / 1024L / 1024L;
                    int players = server.getPlayerList().getPlayers().size();

                    String message = String.format(Locale.ROOT,
                            "UltraPlayerEngine | Spieler: %d | Ø MSPT: %.2f | geschätzte TPS: %.2f | P95 MSPT: %.2f | RAM: %d/%d MB | Messungen: %d",
                            players,
                            snapshot.averageMspt(),
                            snapshot.estimatedTps(),
                            snapshot.p95Mspt(),
                            usedMb,
                            maxMb,
                            snapshot.samples());

                    context.getSource().sendSuccess(() -> Component.literal(message), false);
                    return 1;
                }))
                .then(Commands.literal("reset").executes(context -> {
                    MONITOR.reset();
                    context.getSource().sendSuccess(
                            () -> Component.literal("UltraPlayerEngine: Tick-Messwerte zurückgesetzt."),
                            false
                    );
                    return 1;
                }))
        );
    }
}
