package com.github.tatercertified.equicap;

import com.github.tatercertified.equicap.interfaces.MobCapAccess;
import com.github.tatercertified.equicap.interfaces.MobCapTracker;
import com.github.tatercertified.equicap.interfaces.SpawnedFrom;
import com.github.tatercertified.equicap.interfaces.VisualDebug;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MobCapCommand {
    public static void registerCommand() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated, environment) ->
                dispatcher.register(Commands.literal("equicap")
                        .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                        .then(Commands.literal("set")
                                .then(Commands.argument("group", StringArgumentType.string())
                                        .suggests(SPAWN_GROUP_SUGGESTIONS)
                                        .then(Commands.argument("size", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    MobCategory group = getSpawnGroup(context);
                                                    int size = IntegerArgumentType.getInteger(context, "size");

                                                    ((MobCapAccess)(Object)group).setMobCapSize(size);
                                                    Config.getInstance().spawnGroupCapacityOverrides.put(group, size);
                                                    Config.getInstance().saveConfig();
                                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("Set " + group.getName() + " per-player mob cap to " + size), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("get")
                                .then(Commands.argument("group", StringArgumentType.string())
                                        .suggests(SPAWN_GROUP_SUGGESTIONS)
                                        .executes(context -> {
                                            MobCategory group = getSpawnGroup(context);
                                            context.getSource().sendSuccess(() -> Component.nullToEmpty(group.getName() + " per-player mob cap total is " + group.getMaxInstancesPerChunk()), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("merge")
                                .executes(context -> {
                                    context.getSource().sendSuccess(() -> Component.nullToEmpty("The current merge mode is " + Config.getInstance().mergeMode.getName()), false);
                                    return 1;
                                })
                                .then(Commands.argument("mode", StringArgumentType.string())
                                        .suggests(MERGE_MODE_SUGGESTIONS)
                                        .executes(context -> {
                                            MobCapMerge merge = getMergeMode(context);
                                            Config.getInstance().mergeMode = merge;
                                            Config.getInstance().saveConfig();
                                            context.getSource().sendSuccess(() -> Component.nullToEmpty("Set merge mode to " + merge.name()), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("debug")
                                .then(Commands.literal("player")
                                        .executes(context -> {
                                            MutableComponent text = getPrintOut(null, context);
                                            if (text != null) {
                                                context.getSource().sendSuccess(() -> text, false);
                                                return 1;
                                            } else {
                                                return 0;
                                            }
                                        })
                                        .then(Commands.argument("target", EntityArgument.player())
                                                .executes(context -> {
                                                    ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                                    MutableComponent text = getPrintOut(((MobCapTracker)target).getPlayerMobCapData(), context);
                                                    if (text != null) {
                                                        context.getSource().sendSuccess(() -> text, false);
                                                        return 1;
                                                    } else {
                                                        return 0;
                                                    }
                                                })
                                        )
                                )
                                .then(Commands.literal("check")
                                        .then(Commands.argument("target", EntityArgument.entity())
                                                .executes(context -> {
                                                    Entity entity = EntityArgument.getEntity(context, "target");
                                                    if (entity instanceof Mob mob) {
                                                        MutableComponent text = Component.literal("Debug info for " + entity.getName().getString() + ":\n");
                                                        text.append("UUID: " + entity.getStringUUID() + "\n");
                                                        text.append("Group: " + mob.getType().getCategory().getName() + "\n");
                                                        text.append("Persistent: " + mob.isPersistenceRequired() + "\n");
                                                        text.append("Cannot Despawn: " + mob.requiresCustomPersistence() + "\n");
                                                        text.append("Should Track: " + ((SpawnedFrom)mob).shouldBeInCap() + "\n");
                                                        ServerPlayer owner = ((SpawnedFrom)mob).getSpawnedFrom();
                                                        text.append("Spawned From: " + (owner != null ? owner.getName().getString() : "null") + "\n");
                                                        
                                                        PlayerTeam team = context.getSource().getServer().getScoreboard().getPlayersTeam(entity.getScoreboardName());
                                                        text.append("Team: " + (team != null ? team.getName() : "null") + "\n");
                                                        
                                                        text.append("Watched by you: " + ((VisualDebug)mob).isDebugMarkerToggled(context.getSource().getPlayer()));
                                                        
                                                        context.getSource().sendSuccess(() -> text, false);
                                                    } else {
                                                        context.getSource().sendFailure(Component.nullToEmpty("Target is not a mob"));
                                                    }
                                                    return 1;
                                                })
                                        )
                                )
                                .then(Commands.literal("dimension")
                                        .executes(context -> {
                                            MutableComponent text = getPrintOut(MobCapTracker.getDimensionMobCount(context.getSource().getLevel()), context);
                                            if (text != null) {
                                                context.getSource().sendSuccess(() -> text, false);
                                                return 1;
                                            } else {
                                                return 0;
                                            }
                                        })
                                        .then(Commands.argument("dimension", DimensionArgument.dimension())
                                                .executes(context -> {
                                                    ServerLevel world = DimensionArgument.getDimension(context, "dimension");
                                                    MutableComponent text = getPrintOut(MobCapTracker.getDimensionMobCount(world), context);
                                                    if (text != null) {
                                                        context.getSource().sendSuccess(() -> text, false);
                                                        return 1;
                                                    } else {
                                                        return 0;
                                                    }
                                                })
                                        )
                                )
                                .then(Commands.literal("global")
                                        .executes(context -> {
                                            MutableComponent text = getPrintOut(MobCapTracker.getTotalMobCount(context.getSource().getServer()), context);
                                            if (text != null) {
                                                context.getSource().sendSuccess(() -> text, false);
                                                return 1;
                                            } else {
                                                return 0;
                                            }
                                        })
                                )
                                .then(Commands.literal("visual")
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayer();
                                            if (((VisualDebug)player).isDebugMarkerToggled(null)) {
                                                VisualDebug.removeWatcher(player);
                                                context.getSource().sendSuccess(() -> Component.nullToEmpty("Disabled visual debug"), false);
                                            } else {
                                                ((VisualDebug)player).toggleDebugMarker(player, null);
                                                PacketUtils.addNewEntitiesToDebugRenderer(player, player);
                                                MutableComponent msg = Component.literal("Enabled visual debug for self. Colors: ");
                                                msg.append(Component.literal("Monster").withStyle(ChatFormatting.RED)).append(", ");
                                                msg.append(Component.literal("Creature").withStyle(ChatFormatting.GREEN)).append(", ");
                                                msg.append(Component.literal("Ambient").withStyle(ChatFormatting.GRAY)).append(", ");
                                                msg.append(Component.literal("Axolotls").withStyle(ChatFormatting.LIGHT_PURPLE)).append(", ");
                                                msg.append(Component.literal("U.Water").withStyle(ChatFormatting.BLUE)).append(", ");
                                                msg.append(Component.literal("Water").withStyle(ChatFormatting.AQUA)).append(", ");
                                                msg.append(Component.literal("Misc").withStyle(ChatFormatting.YELLOW));
                                                msg.append(Component.literal("\n(Darker colors indicate >75% cap usage)"));
                                                context.getSource().sendSuccess(() -> msg, false);
                                            }
                                            return 1;
                                        })
                                        .then(Commands.argument("player", EntityArgument.entities())
                                                .executes(context -> {
                                                    VisualDebug.removeWatcher(context.getSource().getPlayer());
                                                    ServerPlayer input = EntityArgument.getPlayer(context, "player");
                                                    ((VisualDebug)context.getSource().getPlayer()).toggleDebugMarker(input, null);
                                                    PacketUtils.addNewEntitiesToDebugRenderer(context.getSource().getPlayer(), input);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("help")
                                .executes(context -> {
                                    context.getSource().sendSuccess(() -> Component.nullToEmpty(
                                            """
                                                    Welcome to EquiCap, a per-player mob cap solution
                                                    Command Usage:
                                                    - debug:
                                                        - player: Prints out the number of mobs in the player's mob cap
                                                        - dimension: Prints out the number of mobs in dimension
                                                        - global: Prints out the number of mobs in the server
                                                        - visual: Toggles highlighting mobs that belong to the player
                                                    - get: Gets the max player mob cap size
                                                    - merge: Sets the mob cap merging mode:
                                                        None: No merging when players are close
                                                        Combine: Divide the player's mob cap size by the number of near players
                                                        Vanillalike: Decrease the player's mob cap size based on the proximity of the nearest player
                                                    - set: Sets the max player mob cap size
                                                    """
                                    ), false);
                                    return 1;
                                })
                        )
                )
        );
    }

    private static final SuggestionProvider<CommandSourceStack> SPAWN_GROUP_SUGGESTIONS = (context, builder) -> {
        for (MobCategory group : MobCategory.values()) {
            builder.suggest(group.getName());
        }
        return builder.buildFuture();
    };

    private static final SuggestionProvider<CommandSourceStack> MERGE_MODE_SUGGESTIONS = (context, builder) -> {
        for (MobCapMerge merge : MobCapMerge.values()) {
            builder.suggest(merge.getName());
        }
        return builder.buildFuture();
    };

    private static MobCategory getSpawnGroup(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String input = StringArgumentType.getString(context, "group");
        return Arrays.stream(MobCategory.values())
                .filter(g -> g.getName().equals(input))
                .findFirst()
                .orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create("Invalid group: " + input));
    }

    private static MobCapMerge getMergeMode(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String input = StringArgumentType.getString(context, "mode");
        return Arrays.stream(MobCapMerge.values())
                .filter(g -> g.getName().equals(input))
                .findFirst()
                .orElseThrow(() -> CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect().create("Invalid merge mode: " + input));
    }

    @Nullable
    private static MutableComponent getPrintOut(@Nullable EnumMap<MobCategory, int[]> data, CommandContext<CommandSourceStack> context) {
        if (context.getSource().isPlayer()) {
            ServerPlayer player = context.getSource().getPlayer();
            MutableComponent text = Component.empty();
            boolean first = true;
            if (data == null) {
                data = ((MobCapTracker)player).getPlayerMobCapData();
            }

            if (data.isEmpty()) {
                text.append(Component.literal("There are no mobs in this scope").withStyle(ChatFormatting.RED));
                return text;
            }

            for (Map.Entry<MobCategory, int[]> entry : data.entrySet()) {
                MobCategory group = entry.getKey();

                if (group == MobCategory.MISC) {
                    continue;
                }

                int current = entry.getValue()[0];
                int total = entry.getValue()[1];
                ChatFormatting color;
                float percent = (float) current / total;

                if (percent >= 1.0) {
                    color = ChatFormatting.RED;
                } else if (percent > 0.75) {
                    color = ChatFormatting.YELLOW;
                } else {
                    color = ChatFormatting.GREEN;
                }

                if (first) {
                    first = false;
                } else {
                    text.append("\n");
                }

                text.append(Component.literal(group.getSerializedName() + ": ")).append(Component.literal(current + "/" + total).withStyle(color));
            }
            return text;
        } else {
            context.getSource().sendFailure(Component.nullToEmpty("This command must be executed by a player"));
            return null;
        }
    }
}
