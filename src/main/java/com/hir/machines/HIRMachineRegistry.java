package com.hir.machines;

import com.hir.machines.waterwheel.HIRWaterWheelManager;

/**
 * Modulo de gestao e ciclo de processamento de maquinas do HIR.
 */
public class HIRMachineRegistry {

    private final HIRWaterWheelManager waterWheelManager;

    public HIRMachineRegistry() {
        this.waterWheelManager = new HIRWaterWheelManager();
    }

    public void init() {
        // Inicializacao dos gerenciadores de maquinas da Era 1
    }

    public HIRWaterWheelManager getWaterWheelManager() {
        return waterWheelManager;
    }
}
