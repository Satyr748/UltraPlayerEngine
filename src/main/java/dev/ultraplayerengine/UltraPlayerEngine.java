
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

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            MONITOR.recordTick();
        });

        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> {
                registerCommands(dispatcher);
            }
        );

        System.out.println(
            "[UltraPlayerEngine] Successfully initialized!"
        );
    }

    private static void registerCommands(
        CommandDispatcher<CommandSourceStack> dispatcher
    ) {

        dispatcher.register(
            Commands.literal("upe")
                .then(Commands.literal("status")
                    .executes(context -> {

                        MinecraftServer server =
                            context.getSource().getServer();

                        TickMonitor.Snapshot snapshot =
                            MONITOR.snapshot();

                        Runtime runtime = Runtime.getRuntime();

                        long usedMb =
                            (runtime.totalMemory() -
                            runtime.freeMemory()) / 1024 / 1024;

                        long maxMb =
                            runtime.maxMemory() / 1024 / 1024;

                        int players =
                            server.getPlayerList()
                            .getPlayers().size();

                        String message = String.format(
                            Locale.ROOT,
                            "UltraPlayerEngine | Players: %d | Average MSPT: %.2f | TPS: %.2f | P95 MSPT: %.2f | RAM: %d/%d MB",
                            players,
                            snapshot.averageMspt(),
                            snapshot.estimatedTps(),
                            snapshot.p95Mspt(),
                            usedMb,
                            maxMb
                        );

                        context.getSource().sendSuccess(
                            () -> Component.literal(message),
                            false
                        );

                        return 1;
                    })
                )
        );
    }
}
