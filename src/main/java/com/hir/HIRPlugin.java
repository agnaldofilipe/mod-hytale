package com.hir;

import com.hir.command.HIRCommand;
import com.hir.config.HIRConfig;
import com.hir.blocks.HIRBlockRegistry;
import com.hir.items.HIRItemRegistry;
import com.hir.machines.HIRMachineRegistry;
import com.hir.energy.HIREnergySystem;
import com.hir.recipes.HIRRecipeRegistry;
import com.hypixel.hytale.server.core.command.system.CommandRegistry;
import com.hypixel.hytale.server.core.event.events.player.AddPlayerToWorldEvent;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.World;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * HIR - Hytale Industrial Revolution
 * Plugin principal para Hytale.
 */
public class HIRPlugin extends JavaPlugin {

    private static HIRPlugin instance;
    private final HIRConfig config;
    private final HIRBlockRegistry blockRegistry;
    private final HIRItemRegistry itemRegistry;
    private final HIRMachineRegistry machineRegistry;
    private final HIREnergySystem energySystem;
    private final HIRRecipeRegistry recipeRegistry;
    private final Set<String> activeTickerWorlds = ConcurrentHashMap.newKeySet();

    public HIRPlugin(JavaPluginInit init) {
        super(init);
        instance = this;
        this.config = new HIRConfig();
        this.blockRegistry = new HIRBlockRegistry();
        this.itemRegistry = new HIRItemRegistry();
        this.machineRegistry = new HIRMachineRegistry();
        this.energySystem = new HIREnergySystem();
        this.recipeRegistry = new HIRRecipeRegistry();
    }

    public static HIRPlugin getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        getLogger().at(Level.INFO).log("=================================================");
        getLogger().at(Level.INFO).log("[HIR] Inicializando Hytale Industrial Revolution");
        getLogger().at(Level.INFO).log("[HIR] Versao: 0.0.1-TEST (Era 1: Roda d'Agua + Eixo Mecanico)");
        getLogger().at(Level.INFO).log("=================================================");

        // Registro de comandos
        CommandRegistry commandRegistry = getCommandRegistry();
        if (commandRegistry != null) {
            commandRegistry.registerCommand(new HIRCommand());
            getLogger().at(Level.INFO).log("[HIR] Comandos /hir e /hirtest registrados com sucesso.");
        }

        // Registro de evento para ativar monitoramento de maquinas no mundo
        if (getEventRegistry() != null) {
            getEventRegistry().registerGlobal(AddPlayerToWorldEvent.class, this::onPlayerAddToWorld);
        }

        // Inicializacao dos subsistemas modulares
        blockRegistry.init();
        itemRegistry.init();
        machineRegistry.init();
        energySystem.init();
        recipeRegistry.init();

        getLogger().at(Level.INFO).log("[HIR] Setup concluido com sucesso.");
    }

    private void onPlayerAddToWorld(AddPlayerToWorldEvent event) {
        World world = event.getWorld();
        if (world != null && !activeTickerWorlds.contains(world.getName())) {
            activeTickerWorlds.add(world.getName());
            getLogger().at(Level.INFO).log("[HIR] Ativando monitoramento de energia e maquinas para o mundo: %s", world.getName());
            scheduleEnergyTick(world);
        }
    }

    private void scheduleEnergyTick(World world) {
        if (world == null || !world.isAlive()) {
            activeTickerWorlds.remove(world != null ? world.getName() : "");
            return;
        }

        world.scheduleAfter(() -> {
            try {
                if (world.isAlive()) {
                    // Atualiza toda a rede mecanica conectada no mundo
                    energySystem.tickWorld(world);
                    scheduleEnergyTick(world);
                }
            } catch (Exception e) {
                getLogger().at(Level.WARNING).log("[HIR] Erro no tick do sistema de energia: %s", e.getMessage());
            }
        }, 2, TimeUnit.SECONDS);
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("[HIR] Hytale Industrial Revolution ativo e operante!");
    }

    @Override
    protected void shutdown() {
        getLogger().at(Level.INFO).log("[HIR] Desativando Hytale Industrial Revolution...");
        energySystem.shutdown();
        activeTickerWorlds.clear();
        getLogger().at(Level.INFO).log("[HIR] Finalizacao concluida.");
    }

    public HIRConfig getConfig() {
        return config;
    }

    public HIRMachineRegistry getMachineRegistry() {
        return machineRegistry;
    }

    public HIRBlockRegistry getBlockRegistry() {
        return blockRegistry;
    }

    public HIRItemRegistry getItemRegistry() {
        return itemRegistry;
    }

    public HIREnergySystem getEnergySystem() {
        return energySystem;
    }

    public HIRRecipeRegistry getRecipeRegistry() {
        return recipeRegistry;
    }
}
