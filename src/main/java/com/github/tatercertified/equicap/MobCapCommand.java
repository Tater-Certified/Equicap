package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.MobCapAccess;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;

public class MobCapCommand {
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) ->
                dispatcher.register(CommandManager.literal("equicap")
                        .executes(context -> {
                            if (context.getSource().isExecutedByPlayer()) {
                                ServerPlayerEntity player = context.getSource().getPlayer();
                                MutableText text = Text.empty();
                                boolean first = true;
                                for (SpawnGroup group : SpawnGroup.values()) {
                                    if (group == SpawnGroup.MISC) {
                                        continue;
                                    }
                                    int total = ((MobCapTracker)player).adjustedMobCapMaxSize(group);
                                    int current = ((MobCapTracker)player).getPlayerMobCap(group);
                                    Formatting color;
                                    float percent = (float) current / total;
                                    if (percent >= 1.0) {
                                        color = Formatting.RED;
                                    } else if (percent > 0.75) {
                                        color = Formatting.YELLOW;
                                    } else {
                                        color = Formatting.GREEN;
                                    }
                                    if (!first) {
                                        text.append("\n");
                                    } else {
                                        first = false;
                                    }
                                    text.append(Text.literal(group.asString() + ": ")).append(Text.literal(current + "/" + total).formatted(color));
                                }
                                context.getSource().sendFeedback(() -> text, false);
                                return 1;
                            } else {
                                context.getSource().sendError(Text.of("This command must be executed by a player"));
                                return 0;
                            }
                        })
                        .then(CommandManager.literal("set")
                                .then(CommandManager.argument("group", StringArgumentType.string())
                                        .suggests(SPAWN_GROUP_SUGGESTIONS)
                                        .then(CommandManager.argument("size", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    SpawnGroup group = getSpawnGroup(context, "group");
                                                    int size = IntegerArgumentType.getInteger(context, "size");

                                                    ((MobCapAccess)(Object)group).setMobCapSize(size);
                                                    Config.getInstance().spawnGroupCapacityOverrides.put(group, size);
                                                    Config.getInstance().saveConfig();
                                                    context.getSource().sendFeedback(() -> Text.of("Set " + group.getName() + " per-player mob cap to " + size), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(CommandManager.literal("get")
                                .then(CommandManager.argument("group", StringArgumentType.string())
                                        .suggests(SPAWN_GROUP_SUGGESTIONS)
                                        .executes(context -> {
                                            SpawnGroup group = getSpawnGroup(context, "group");
                                            context.getSource().sendFeedback(() -> Text.of(group.getName() + " per-player mob cap total is " + group.getCapacity()), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("merge")
                                .executes(context -> {
                                    context.getSource().sendFeedback(() -> Text.of("The current merge mode is " + Config.getInstance().mergeMode.getName()), false);
                                    return 1;
                                })
                                .then(CommandManager.argument("mode", StringArgumentType.string())
                                        .suggests(MERGE_MODE_SUGGESTIONS)
                                        .executes(context -> {
                                            MobCapMerge merge = getMergeMode(context, "mode");
                                            Config.getInstance().mergeMode = merge;
                                            Config.getInstance().saveConfig();
                                            context.getSource().sendFeedback(() -> Text.of("Set merge mode to " + merge.name()), true);
                                            return 1;
                                        })
                                )
                        )
                )
        );
    }

    private static final SuggestionProvider<ServerCommandSource> SPAWN_GROUP_SUGGESTIONS = (context, builder) -> {
        for (SpawnGroup group : SpawnGroup.values()) {
            builder.suggest(group.getName());
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<ServerCommandSource> MERGE_MODE_SUGGESTIONS = (context, builder) -> {
        for (MobCapMerge merge : MobCapMerge.values()) {
            builder.suggest(merge.getName());
        }
        return builder.buildFuture();
    };

    private static SpawnGroup getSpawnGroup(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        String input = StringArgumentType.getString(context, name);
        return Arrays.stream(SpawnGroup.values())
                .filter(g -> g.getName().equals(input))
                .findFirst()
                .orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create("Invalid group: " + input));
    }

    private static MobCapMerge getMergeMode(CommandContext<ServerCommandSource> context, String name) throws CommandSyntaxException {
        String input = StringArgumentType.getString(context, name);
        return Arrays.stream(MobCapMerge.values())
                .filter(g -> g.getName().equals(input))
                .findFirst()
                .orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create("Invalid merge mode: " + input));
    }
}
