package com.hir.command;

import com.hir.HIRPlugin;
import com.hir.energy.HIREnergyNode;
import com.hir.energy.HIREnergySystem;
import com.hir.machines.waterwheel.HIRWaterWheelManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;
import org.joml.Vector3i;

import java.util.List;

public class HIRCommand extends CommandBase {

    public HIRCommand() {
        super("hir", "Comando principal do Hytale Industrial Revolution (HIR)");
        addAliases(new String[]{"hirtest", "waterwheel", "shaft"});
    }

    @Override
    protected void executeSync(CommandContext context) {
        String input = context.getInputString();
        String[] parts = (input != null) ? input.trim().split("\\s+") : new String[0];
        String subCommand = (parts.length > 1) ? parts[1].toLowerCase() : "";

        if (subCommand.equals("wheel") || subCommand.equals("waterwheel")) {
            handleWheelCommand(context);
            return;
        }

        if (subCommand.equals("shaft") || subCommand.equals("energy") || subCommand.equals("network") || subCommand.equals("sync")) {
            handleEnergyNetworkCommand(context);
            return;
        }

        // Resumo geral do mod
        context.sendMessage(Message.raw("================================================="));
        context.sendMessage(Message.raw("[HIR] Hytale Industrial Revolution - Era 1: Mecanica"));
        context.sendMessage(Message.raw("[HIR] Componentes funcionais:"));
        context.sendMessage(Message.raw("  1. Roda d'Agua (HIR_Water_Wheel): Gerador cinetico"));
        context.sendMessage(Message.raw("  2. Eixo Mecanico (HIR_Mechanical_Shaft): Transmissor"));
        context.sendMessage(Message.raw("[HIR] Comandos disponiveis:"));
        context.sendMessage(Message.raw("  - /hir test : Testa a resposta do plugin"));
        context.sendMessage(Message.raw("  - /hir wheel : Inspeciona rodas d'agua proximas"));
        context.sendMessage(Message.raw("  - /hir network : Analisa a rede [Agua -> Roda -> Eixos]"));
        context.sendMessage(Message.raw("================================================="));
    }

    private void handleEnergyNetworkCommand(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("[HIR] Este comando deve ser executado por um jogador."));
            return;
        }

        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        if (playerRef == null || !playerRef.isValid()) {
            context.sendMessage(Message.raw("[HIR] Referencia de jogador invalida."));
            return;
        }

        Store<EntityStore> store = playerRef.getStore();
        EntityStore entityStore = store.getExternalData();
        World world = (entityStore != null) ? entityStore.getWorld() : null;

        if (world == null) {
            context.sendMessage(Message.raw("[HIR] Mundo nao encontrado."));
            return;
        }

        TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (transform == null || transform.getPosition() == null) {
            context.sendMessage(Message.raw("[HIR] Nao foi possivel obter a posicao do jogador."));
            return;
        }

        Vector3d pos = transform.getPosition();
        int px = (int) Math.floor(pos.x);
        int py = (int) Math.floor(pos.y);
        int pz = (int) Math.floor(pos.z);

        HIREnergySystem energySystem = HIRPlugin.getInstance().getEnergySystem();

        // Encontrar primeiro bloco mecanico no raio de 10 blocos
        Vector3i foundOrigin = null;
        for (int y = Math.max(0, py - 4); y <= Math.min(255, py + 4) && foundOrigin == null; y++) {
            for (int x = px - 8; x <= px + 8 && foundOrigin == null; x++) {
                for (int z = pz - 8; z <= pz + 8 && foundOrigin == null; z++) {
                    String blockId = energySystem.getBlockId(world, x, y, z);
                    if (energySystem.isMechanicalBlock(blockId)) {
                        foundOrigin = new Vector3i(x, y, z);
                    }
                }
            }
        }

        if (foundOrigin == null) {
            context.sendMessage(Message.raw("[HIR] Nenhuma maquina ou eixo mecanico encontrado no raio de 8 blocos."));
            context.sendMessage(Message.raw("[HIR] Coloque uma Roda d'Agua e conecte Eixos Mecanicos lado a lado para formar uma transmissao!"));
            return;
        }

        HIREnergySystem.NetworkSummary summary = energySystem.propagateAndApply(world, foundOrigin.x, foundOrigin.y, foundOrigin.z, null);

        context.sendMessage(Message.raw("================================================="));
        context.sendMessage(Message.raw("[HIR] REDE DE ENERGIA MECANICA (ERA 1)"));
        context.sendMessage(Message.raw(String.format("  Status Geral: %s", summary.energized ? "ENERGIZADA (Em Movimento)" : "DESENERGIZADA (Parada)")));
        context.sendMessage(Message.raw(String.format("  Potencia Gerada: %d SU (Stress Units)", summary.stressUnits)));
        context.sendMessage(Message.raw(String.format("  Velocidade de Rotacao: %.1fx", summary.speed)));
        context.sendMessage(Message.raw(String.format("  Rodas d'Agua com Agua: %d", summary.activeWheels)));
        context.sendMessage(Message.raw(String.format("  Eixos Mecanicos Conectados: %d", summary.shaftCount)));
        context.sendMessage(Message.raw("  Componentes na Rede:"));

        for (HIREnergyNode node : summary.nodes) {
            String role = (node.getType() == HIREnergyNode.NodeType.SOURCE) ? "Fonte (Roda)" : "Transmissor (Eixo)";
            String state = node.isActive() ? "GIRANDO" : "PARADO";
            context.sendMessage(Message.raw(String.format("    -> [%s] em (%d, %d, %d) | %s", role, node.getPosition().x, node.getPosition().y, node.getPosition().z, state)));
        }
        context.sendMessage(Message.raw("================================================="));
    }

    private void handleWheelCommand(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("[HIR] O comando /hir wheel deve ser executado por um jogador."));
            return;
        }

        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        if (playerRef == null || !playerRef.isValid()) {
            return;
        }

        Store<EntityStore> store = playerRef.getStore();
        EntityStore entityStore = store.getExternalData();
        World world = (entityStore != null) ? entityStore.getWorld() : null;
        if (world == null) return;

        TransformComponent transform = store.getComponent(playerRef, TransformComponent.getComponentType());
        if (transform == null || transform.getPosition() == null) return;

        Vector3d pos = transform.getPosition();
        int px = (int) Math.floor(pos.x);
        int py = (int) Math.floor(pos.y);
        int pz = (int) Math.floor(pos.z);

        HIRWaterWheelManager wheelManager = HIRPlugin.getInstance().getMachineRegistry().getWaterWheelManager();
        List<Vector3i> nearbyWheels = wheelManager.scanNearbyWheels(world, px, py, pz, 10);

        if (nearbyWheels.isEmpty()) {
            context.sendMessage(Message.raw("[HIR] Nenhuma Roda d'Agua encontrada proxima."));
            return;
        }

        context.sendMessage(Message.raw(String.format("[HIR] %d Roda(s) d'Agua encontrada(s):", nearbyWheels.size())));
        for (Vector3i wheelPos : nearbyWheels) {
            boolean hasWater = wheelManager.updateWheelAnimation(world, wheelPos.x, wheelPos.y, wheelPos.z);
            String status = hasWater ? "GIRANDO (Agua detectada! 1.0x)" : "PARADA (Sem contato com agua)";
            context.sendMessage(Message.raw(String.format("  -> (%d, %d, %d) : %s", wheelPos.x, wheelPos.y, wheelPos.z, status)));
        }
    }
}
