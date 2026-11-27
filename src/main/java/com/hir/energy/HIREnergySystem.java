package com.hir.energy;

import com.hir.HIRPlugin;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.modules.blockanimation.BlockAnimationModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3i;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Sistema Unificado de Energia Mecanica do HIR.
 * Gerencia a propagacao de forca cinetica:
 * [Agua] -> [Roda d'Agua] -> [Eixo Mecanico 1] -> [Eixo Mecanico 2...]
 */
public class HIREnergySystem {

    public static final String BLOCK_WATER_WHEEL = "HIR_Water_Wheel";
    public static final String BLOCK_SHAFT = "HIR_Mechanical_Shaft";

    private final Set<Vector3i> knownMachines = ConcurrentHashMap.newKeySet();

    public void init() {
        HIRPlugin.getInstance().getLogger().at(Level.INFO).log("[HIR] Sistema de Energia Mecanica inicializado.");
    }

    public void shutdown() {
        knownMachines.clear();
        HIRPlugin.getInstance().getLogger().at(Level.INFO).log("[HIR] Sistema de Energia Mecanica descarregado.");
    }

    public void registerMachine(int x, int y, int z) {
        knownMachines.add(new Vector3i(x, y, z));
    }

    public Set<Vector3i> getKnownMachines() {
        return knownMachines;
    }

    /**
     * Retorna o ID do bloco em (x, y, z) ou null.
     */
    public String getBlockId(World world, int x, int y, int z) {
        if (world == null || world.getChunkStore() == null) {
            return null;
        }

        try {
            Ref<ChunkStore> ref = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
            if (ref == null || !ref.isValid()) {
                return null;
            }

            BlockSection blockSection = ref.getStore().getComponent(ref, BlockSection.getComponentType());
            if (blockSection == null) {
                return null;
            }

            int blockId = blockSection.get(x & 31, y & 31, z & 31);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            return (blockType != null) ? blockType.getId() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isMechanicalBlock(String id) {
        return BLOCK_WATER_WHEEL.equals(id) || BLOCK_SHAFT.equals(id);
    }

    /**
     * Atualiza todas as redes mecanicas conhecidas no mundo.
     */
    public void tickWorld(World world) {
        if (world == null || !world.isAlive() || knownMachines.isEmpty()) {
            return;
        }

        Set<Vector3i> visited = new HashSet<>();

        for (Vector3i startPos : knownMachines) {
            if (visited.contains(startPos)) {
                continue;
            }

            String id = getBlockId(world, startPos.x, startPos.y, startPos.z);
            if (!isMechanicalBlock(id)) {
                continue;
            }

            // Propaga e atualiza a rede conectada a este bloco
            propagateAndApply(world, startPos.x, startPos.y, startPos.z, visited);
        }
    }

    /**
     * Avalia a rede conectada a partir de uma coordenada,
     * detecta se alguma fonte (Roda d'Agua com agua) esta ativa
     * e ajusta a animacao visual de toda a cadeia conectada.
     */
    public NetworkSummary propagateAndApply(World world, int startX, int startY, int startZ, Set<Vector3i> globalVisited) {
        List<HIREnergyNode> networkNodes = new ArrayList<>();
        Queue<Vector3i> queue = new ArrayDeque<>();
        Set<Vector3i> localVisited = new HashSet<>();

        Vector3i initial = new Vector3i(startX, startY, startZ);
        queue.add(initial);
        localVisited.add(initial);

        boolean hasActiveSource = false;
        int activeWheels = 0;
        int shaftCount = 0;

        // BFS horizontal para encontrar componentes conectados
        int[][] horizontalNeighbors = {
            {1, 0, 0},
            {-1, 0, 0},
            {0, 0, 1},
            {0, 0, -1}
        };

        while (!queue.isEmpty()) {
            Vector3i current = queue.poll();
            if (globalVisited != null) {
                globalVisited.add(current);
            }
            knownMachines.add(current);

            String blockId = getBlockId(world, current.x, current.y, current.z);
            if (blockId == null || !isMechanicalBlock(blockId)) {
                continue;
            }

            if (BLOCK_WATER_WHEEL.equals(blockId)) {
                boolean water = HIRPlugin.getInstance().getMachineRegistry().getWaterWheelManager().hasWaterContact(world, current.x, current.y, current.z);
                HIREnergyNode node = new HIREnergyNode(current, HIREnergyNode.NodeType.SOURCE, blockId);
                node.setActive(water);
                networkNodes.add(node);

                if (water) {
                    hasActiveSource = true;
                    activeWheels++;
                }
            } else if (BLOCK_SHAFT.equals(blockId)) {
                HIREnergyNode node = new HIREnergyNode(current, HIREnergyNode.NodeType.TRANSMITTER, blockId);
                networkNodes.add(node);
                shaftCount++;
            }

            // Conectar aos vizinhos horizontais
            for (int[] offset : horizontalNeighbors) {
                Vector3i neighbor = new Vector3i(current.x + offset[0], current.y, current.z + offset[2]);
                if (!localVisited.contains(neighbor)) {
                    String neighborId = getBlockId(world, neighbor.x, neighbor.y, neighbor.z);
                    if (isMechanicalBlock(neighborId)) {
                        localVisited.add(neighbor);
                        queue.add(neighbor);
                    }
                }
            }
        }

        // Aplicar a velocidade na animacao para todos os componentes conectados
        float targetSpeed = hasActiveSource ? 1.0f : 0.0f;
        BlockAnimationModule animModule = BlockAnimationModule.get();

        for (HIREnergyNode node : networkNodes) {
            node.setActive(hasActiveSource);
            node.setSpeed(targetSpeed);

            if (animModule != null) {
                try {
                    animModule.setBlockAnimationSpeed(world, node.getPosition().x, node.getPosition().y, node.getPosition().z, targetSpeed);
                } catch (Exception ignored) {}
            }
        }

        int generatedStress = activeWheels * 16;
        return new NetworkSummary(hasActiveSource, activeWheels, shaftCount, generatedStress, targetSpeed, networkNodes);
    }

    public static class NetworkSummary {
        public final boolean energized;
        public final int activeWheels;
        public final int shaftCount;
        public final int stressUnits;
        public final float speed;
        public final List<HIREnergyNode> nodes;

        public NetworkSummary(boolean energized, int activeWheels, int shaftCount, int stressUnits, float speed, List<HIREnergyNode> nodes) {
            this.energized = energized;
            this.activeWheels = activeWheels;
            this.shaftCount = shaftCount;
            this.stressUnits = stressUnits;
            this.speed = speed;
            this.nodes = nodes;
        }
    }
}
