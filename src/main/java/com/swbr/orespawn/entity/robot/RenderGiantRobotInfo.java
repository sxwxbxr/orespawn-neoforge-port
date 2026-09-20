package com.swbr.orespawn.entity.robot;

/**
 * Port of {@code danger.orespawn.RenderGiantRobotInfo} (RenderGiantRobotInfo.java:3-15): the leg angles that
 * {@code ModelGiantRobot.render} computes each frame and keeps per entity.
 *
 * <p>A plain data class without client imports, so it can stay a field of {@link GiantRobot} like in the
 * original (verhalten/entity-08.md, "Portierung"). Only the client model writes it; it is never synchronised or
 * saved. The fields were {@code volatile} in 1.7.10 and stay so.
 */
public class RenderGiantRobotInfo {

    public volatile float hipydisplayangle;
    public volatile float hipxdisplayangle;
    public volatile float[] thighdisplayangle;
    public volatile float[] shindisplayangle;
    /** Set to 2000000 by {@code GiantRobot.initLegData} and read by nothing (grep over src-20.2). */
    public int gpcounter;

    public RenderGiantRobotInfo() {
        this.thighdisplayangle = new float[2];
        this.shindisplayangle = new float[2];
    }
}
