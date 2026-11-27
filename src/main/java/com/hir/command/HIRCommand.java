package com.hir.command;

import com.hir.HIRPlugin;
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
        addAliases(new String[]{"hirtest", "waterwheel"});
    }

    @Override
    protected void executeSync(CommandContext context) {
        String input = context.getInputString();
        String[] parts = (input != null) ? input.trim().split("\\s+") : new String[0];
        String subCommand = (parts.length > 1) ? parts[1].toLowerCase() : "";

        if (subCommand.equals("wheel") || subCommand.equals("waterwheel") || subCommand.equals("sync")) {
            handleWheelCommand(context);
            return;
        }

        // Resposta padrao e de teste
        context.sendMessage(Message.raw("================================================="));
        context.sendMessage(Message.raw("[HIR] Hytale Industrial Revolution v0.0.1"));
        context.sendMessage(Message.raw("[HIR] Status: Sistema Ativo e Operacional"));
        context.sendMessage(Message.raw("[HIR] Maquinas disponiveis:"));
        context.sendMessage(Message.raw("  - Roda d'Agua (Bloco: HIR_Water_Wheel)"));
        context.sendMessage(Message.raw("[HIR] Subcomandos:"));
        context.sendMessage(Message.raw("  - /hir test : Testa a resposta do plugin"));
        context.sendMessage(Message.raw("  - /hir wheel : Sincroniza e detecta agua nas Rodas d'Agua proximas"));
        context.sendMessage(Message.raw("================================================="));
    }

    private void handleWheelCommand(CommandContext context) {
        if (!context.isPlayer()) {
            context.sendMessage(Message.raw("[HIR] O comando /hir wheel deve ser executado por um jogador no mundo."));
            return;
        }

        Ref<EntityStore> playerRef = context.senderAsPlayerRef();
        if (playerRef == null || !playerRef.isValid()) {
            context.sendMessage(Message.raw("[HIR] Referencia do jogador invalida."));
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

        HIRWaterWheelManager wheelManager = HIRPlugin.getInstance().getMachineRegistry().getWaterWheelManager();
        List<Vector3i> nearbyWheels = wheelManager.scanNearbyWheels(world, px, py, pz, 10);

        if (nearbyWheels.isEmpty()) {
            context.sendMessage(Message.raw("[HIR] Nenhuma Roda d'Agua (HIR_Water_Wheel) encontrada em um raio de 10 blocos."));
            context.sendMessage(Message.raw("[HIR] Dica: Pegue a 'Roda d'Agua' no inventario, coloque-a perto da agua e repita /hir wheel."));
            return;
        }

        context.sendMessage(Message.raw(String.format("[HIR] Foram encontradas %d Roda(s) d'Agua no raio de 10 blocos:", nearbyWheels.size())));

        for (Vector3i wheelPos : nearbyWheels) {
            boolean hasWater = wheelManager.updateWheelAnimation(world, wheelPos.x, wheelPos.y, wheelPos.z);
            String status = hasWater
                ? "GIRANDO (Agua detectada! Animacao em 1.0x)"
                : "PARADA (Sem agua em contato. Animacao em 0.0x)";

            context.sendMessage(Message.raw(String.format("  -> Posição (%d, %d, %d) | Status: %s", wheelPos.x, wheelPos.y, wheelPos.z, status)));
        }
    }
}
