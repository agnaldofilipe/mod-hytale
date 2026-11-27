package com.hir.machines.waterwheel;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.modules.blockanimation.BlockAnimationModule;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import org.joml.Vector3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import com.hir.HIRPlugin;

/**
 * Gestor da Roda d'Agua (Era 1 - Mecanica).
 * Detecta fluxo de agua e controla velocidade da animacao do bloco.
 */
public class HIRWaterWheelManager {

    public static final String BLOCK_ID = "HIR_Water_Wheel";
    private final Set<Vector3i> knownWheels = ConcurrentHashMap.newKeySet();

    public void registerWheel(int x, int y, int z) {
        knownWheels.add(new Vector3i(x, y, z));
    }

    public Set<Vector3i> getKnownWheels() {
        return knownWheels;
    }

    /**
     * Verifica se o bloco em (x, y, z) e uma Roda d'Agua do HIR.
     */
    public boolean isWaterWheel(World world, int x, int y, int z) {
        if (world == null || world.getChunkStore() == null) {
            return false;
        }

        try {
            Ref<ChunkStore> ref = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
            if (ref == null || !ref.isValid()) {
                return false;
            }

            BlockSection blockSection = ref.getStore().getComponent(ref, BlockSection.getComponentType());
            if (blockSection == null) {
                return false;
            }

            int localX = x & 31;
            int localY = y & 31;
            int localZ = z & 31;

            int blockId = blockSection.get(localX, localY, localZ);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            return blockType != null && BLOCK_ID.equals(blockType.getId());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica contato com agua no bloco, abaixo ou nos lados imediatos.
     */
    public boolean hasWaterContact(World world, int x, int y, int z) {
        if (world == null || world.getChunkStore() == null) {
            return false;
        }

        // Vizinhanca: abaixo, no proprio bloco ou adjacente
        int[][] neighbors = {
            {0, -1, 0},
            {0, 0, 0},
            {1, 0, 0},
            {-1, 0, 0},
            {0, 0, 1},
            {0, 0, -1},
            {1, -1, 0},
            {-1, -1, 0},
            {0, -1, 1},
            {0, -1, -1}
        };

        for (int[] offset : neighbors) {
            if (isWaterAt(world, x + offset[0], y + offset[1], z + offset[2])) {
                return true;
            }
        }

        return false;
    }

    /**
     * Detecta se ha agua em uma coordenada exata.
     */
    public boolean isWaterAt(World world, int x, int y, int z) {
        if (world == null || world.getChunkStore() == null) {
            return false;
        }

        try {
            Ref<ChunkStore> ref = world.getChunkStore().getChunkSectionReferenceAtBlock(x, y, z);
            if (ref != null && ref.isValid()) {
                FluidSection fluidSection = ref.getStore().getComponent(ref, FluidSection.getComponentType());
                if (fluidSection != null) {
                    int localX = x & 31;
                    int localY = y & 31;
                    int localZ = z & 31;

                    Fluid fluid = fluidSection.getFluid(localX, localY, localZ);
                    if (fluid != null && !fluid.isUnknown()) {
                        String id = fluid.getId();
                        if (id != null && (id.equalsIgnoreCase("Water") || id.toLowerCase().contains("water"))) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            String blockId = HIRPlugin.getInstance().getEnergySystem().getBlockId(world, x, y, z);
            if (blockId != null && (blockId.equalsIgnoreCase("Water") || blockId.toLowerCase().contains("water"))) {
                return true;
            }
        } catch (Exception ignored) {
        }

        return false;
    }

    /**
     * Atualiza a velocidade de animacao no Hytale:
     * - Com agua: 1.0f (rotacao visual ativa)
     * - Sem agua: 0.0f (parada)
     */
    public boolean updateWheelAnimation(World world, int x, int y, int z) {
        boolean water = hasWaterContact(world, x, y, z);
        float speed = water ? 1.0f : 0.0f;

        try {
            BlockAnimationModule animModule = BlockAnimationModule.get();
            if (animModule != null) {
                animModule.setBlockAnimationSpeed(world, x, y, z, speed);
            }
        } catch (Exception e) {
            HIRPlugin.getInstance().getLogger().at(Level.WARNING).log("Falha ao ajustar animacao da roda d'agua em %d,%d,%d: %s", x, y, z, e.getMessage());
        }

        return water;
    }

    /**
     * Varre um raio ao redor do ponto indicado procurando blocos de Roda d'Agua.
     */
    public List<Vector3i> scanNearbyWheels(World world, int centerX, int centerY, int centerZ, int radius) {
        List<Vector3i> found = new ArrayList<>();
        if (world == null) {
            return found;
        }

        int minY = Math.max(0, centerY - radius);
        int maxY = Math.min(255, centerY + radius);

        for (int y = minY; y <= maxY; y++) {
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    if (isWaterWheel(world, x, y, z)) {
                        Vector3i pos = new Vector3i(x, y, z);
                        found.add(pos);
                        knownWheels.add(pos);
                    }
                }
            }
        }

        return found;
    }
}
