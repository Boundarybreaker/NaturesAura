package de.ellpeck.naturesaura.particles;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import de.ellpeck.naturesaura.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class ParticleHandler {

    public static boolean depthEnabled = true;
    public static int range = 32;
    private static final List<Particle> PARTICLES = new ArrayList<>();
    private static final List<Particle> PARTICLES_NO_DEPTH = new ArrayList<>();

    public static void spawnParticle(Supplier<Particle> particle, double x, double y, double z) {
        if (Minecraft.getInstance().player.getDistanceSq(x, y, z) <= range * range) {
            Minecraft mc = Minecraft.getInstance();
            if (ModConfig.instance.respectVanillaParticleSettings.get()) {
                ParticleStatus setting = mc.gameSettings.particles;
                if (setting != ParticleStatus.ALL &&
                        (setting != ParticleStatus.DECREASED || mc.world.rand.nextInt(3) != 0) &&
                        (setting != ParticleStatus.MINIMAL || mc.world.rand.nextInt(10) != 0))
                    return;
            }
            double setting = ModConfig.instance.particleAmount.get();
            if (setting < 1 && mc.world.rand.nextDouble() > setting)
                return;

            if (depthEnabled)
                PARTICLES.add(particle.get());
            else
                PARTICLES_NO_DEPTH.add(particle.get());
        }
    }

    public static void updateParticles() {
        updateList(PARTICLES);
        updateList(PARTICLES_NO_DEPTH);

        depthEnabled = true;
        range = 32;
    }

    private static void updateList(List<Particle> particles) {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle particle = particles.get(i);
            particle.tick();
            if (!particle.isAlive())
                particles.remove(i);
        }
    }

    public static void renderParticles(MatrixStack stack, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        ClientPlayerEntity player = mc.player;

        if (player != null) {
            ActiveRenderInfo info = mc.gameRenderer.getActiveRenderInfo();
            float f = MathHelper.cos(info.getYaw() * ((float) Math.PI / 180F));
            float f1 = MathHelper.sin(info.getYaw() * ((float) Math.PI / 180F));
            float f2 = -f1 * MathHelper.sin(info.getPitch() * ((float) Math.PI / 180F));
            float f3 = f * MathHelper.sin(info.getPitch() * ((float) Math.PI / 180F));
            float f4 = MathHelper.cos(info.getPitch() * ((float) Math.PI / 180F));

            RenderSystem.pushMatrix();
            RenderSystem.multMatrix(stack.getLast().getPositionMatrix());

            RenderSystem.enableAlphaTest();
            GlStateManager.enableBlend();
            RenderSystem.alphaFunc(516, 0.003921569F);
            GlStateManager.disableCull();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE.param);

            GlStateManager.depthMask(false);

            mc.getTextureManager().bindTexture(ParticleMagic.TEXTURE);
            Tessellator tessy = Tessellator.getInstance();
            BufferBuilder buffer = tessy.getBuffer();

            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
            for (Particle particle : PARTICLES)
                particle.renderParticle(buffer, info, partialTicks);
            tessy.draw();

            GlStateManager.disableDepthTest();
            buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
            for (Particle particle : PARTICLES_NO_DEPTH)
                particle.renderParticle(buffer, info, partialTicks);
            tessy.draw();
            GlStateManager.enableDepthTest();

            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA.param);
            GlStateManager.disableBlend();
            RenderSystem.alphaFunc(516, 0.1F);

            RenderSystem.popMatrix();
        }
    }

    public static int getParticleAmount(boolean depth) {
        return depth ? PARTICLES.size() : PARTICLES_NO_DEPTH.size();
    }

    public static void clearParticles() {
        if (!PARTICLES.isEmpty())
            PARTICLES.clear();
        if (!PARTICLES_NO_DEPTH.isEmpty())
            PARTICLES_NO_DEPTH.clear();
    }
}