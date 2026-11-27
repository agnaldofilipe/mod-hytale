package com.hir.energy;

import org.joml.Vector3i;

public class HIREnergyNode {

    public enum NodeType {
        SOURCE,
        TRANSMITTER,
        CONSUMER
    }

    private final Vector3i position;
    private final NodeType type;
    private final String blockId;
    private boolean active;
    private float speed;

    public HIREnergyNode(Vector3i position, NodeType type, String blockId) {
        this.position = position;
        this.type = type;
        this.blockId = blockId;
        this.active = false;
        this.speed = 0.0f;
    }

    public Vector3i getPosition() {
        return position;
    }

    public NodeType getType() {
        return type;
    }

    public String getBlockId() {
        return blockId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
        this.speed = active ? 1.0f : 0.0f;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
