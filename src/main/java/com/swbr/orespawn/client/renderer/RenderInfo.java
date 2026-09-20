package com.swbr.orespawn.client.renderer;

/**
 * Port of {@code danger.orespawn.RenderInfo}: a bag of four ints and four floats that entity
 * renderers with client-side IK or animation state (SeaMonster, SpitBug, ThePrinceTeen, the robot
 * info renderers) keep per entity. Purely client-side scratch space, never synchronised or saved -
 * the field names are the original's, because the renderers that fill them index by name.
 */
public class RenderInfo {
    public int ri1;
    public int ri2;
    public int ri3;
    public int ri4;
    public float rf1;
    public float rf2;
    public float rf3;
    public float rf4;
}
