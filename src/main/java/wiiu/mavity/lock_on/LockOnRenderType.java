package wiiu.mavity.lock_on;

import net.minecraft.client.render.*;

public class LockOnRenderType extends RenderPhase {

    public LockOnRenderType(String string, Runnable runnable, Runnable runnable2) {
        super(string, runnable, runnable2);
    }

    public static final RenderLayer RENDER_TYPE = getRenderType();

    private static RenderLayer getRenderType() {
        RenderLayer.MultiPhaseParameters renderTypeState = RenderLayer.MultiPhaseParameters.builder()
                .program(COLOR_PROGRAM)
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .build(false);
        return RenderLayer.of(LockOnRefabricatedClient.MOD_ID, VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.TRIANGLES, 256, true, true, renderTypeState);
    }
}