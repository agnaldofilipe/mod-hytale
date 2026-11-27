package com.hir.config;

/**
 * Gestao de configuracoes locais do mod HIR.
 */
public class HIRConfig {
    private boolean debugMode = true;
    private int energyTickRate = 20;

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public int getEnergyTickRate() {
        return energyTickRate;
    }

    public void setEnergyTickRate(int energyTickRate) {
        this.energyTickRate = energyTickRate;
    }
}
