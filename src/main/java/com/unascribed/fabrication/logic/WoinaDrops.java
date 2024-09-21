package com.unascribed.fabrication.logic;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

import com.unascribed.fabrication.FabConf;
import net.minecraft.block.MapColor;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.random.Random;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.ARBCopyImage;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.unascribed.fabrication.loaders.LoaderClassicBlockDrops;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MipmapHelper;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImage.Format;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;

public class WoinaDrops {

	public static float curTimer;

	private static AbstractTexture mippedBlocks;

	public static boolean mippedBlocksInvalid = true;

	public static int modifyOverlay(int hash, int overlay) {
		if (FabConf.isEnabled("*.blinking_drops")) {
			return OverlayTexture.getUv(Math.max(0, MathHelper.sin((curTimer+(hash%2000))/5.3f))*0.7f, false);
		}
		return overlay;
	}

	public static void interceptRender(ItemRenderer subject, ItemStack stack, ModelTransformationMode renderMode, boolean leftHanded, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay, BakedModel model) {
		if (FabConf.isEnabled("*.classic_block_drops")) {
			if (stack.getItem() instanceof BlockItem && model instanceof BasicBakedModel && model.hasDepth()) {
				matrices.push();
				model.getTransformation().getTransformation(renderMode).apply(leftHanded, matrices);
				matrices.translate(-0.5, -0.5, -0.5);
				if (LoaderClassicBlockDrops.isSafe(((BlockItem)stack.getItem()).getBlock())) {
					Random r = Random.create();
					long seed = 42;
					RenderLayer layer = RenderLayers.getItemLayer(stack, true);
					VertexConsumer vertices = vertexConsumers.getBuffer(layer);
					final int overlayf = overlay;
					for (Direction dir : Direction.values()) {
						r.setSeed(seed);
						model.getQuads(null, dir, r).forEach(new Consumer<BakedQuad>() {
							@Override
							public void accept(BakedQuad q) {
								drawExaggeratedQuad(stack, matrices, vertices, q, light, overlayf);
							}
						});
					}
					r.setSeed(seed);
					model.getQuads(null, null, r).forEach(new Consumer<BakedQuad>() {
						@Override
						public void accept(BakedQuad q) {
							drawExaggeratedQuad(stack, matrices, vertices, q, light, overlayf);
						}
					});
				} else {
					if (mippedBlocks == null || mippedBlocksInvalid) {
						mippedBlocksInvalid = false;
						mippedBlocks = new AbstractTexture() {

							@Override
							public void load(ResourceManager manager) throws IOException {
								clearGlId();
								SpriteAtlasTexture atlas = MinecraftClient.getInstance().getBakedModelManager().getAtlas(Identifier.of("textures/atlas/blocks.png"));
								RenderSystem.bindTexture(atlas.getGlId());
								int maxLevel = GL11.glGetTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL);
								if (maxLevel == 0 || !GL.getCapabilities().GL_ARB_copy_image) {
									int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
									int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
									ByteBuffer dest = MemoryUtil.memAlloc(w*h*4);
									try {
										GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, Format.RGBA.toGl(), GL11.GL_UNSIGNED_BYTE, MemoryUtil.memAddress(dest));
									} catch (Error | RuntimeException e) {
										MemoryUtil.memFree(dest);
										throw e;
									}
									NativeImage img = new NativeImage(Format.RGBA, w, h, false, MemoryUtil.memAddress(dest));
									try {
										NativeImage mipped = MipmapHelper.getMipmapLevelsImages(new NativeImage[]{img}, 1)[1];
										try {
											TextureUtil.prepareImage(getGlId(), mipped.getWidth(), mipped.getHeight());
											RenderSystem.bindTexture(getGlId());
											mipped.upload(0, 0, 0, true);
										} finally {
											mipped.close();
										}
									} finally {
										img.close();
									}
								} else {
									int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 1, GL11.GL_TEXTURE_WIDTH);
									int h = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 1, GL11.GL_TEXTURE_HEIGHT);
									TextureUtil.prepareImage(getGlId(), w, h);
									ARBCopyImage.glCopyImageSubData(
											atlas.getGlId(), GL11.GL_TEXTURE_2D, 1, 0, 0, 0,
											getGlId(), GL11.GL_TEXTURE_2D, 0, 0, 0, 0,
											w, h, 1);
								}
							}
						};
						MinecraftClient.getInstance().getTextureManager().registerTexture(Identifier.of("fabrication", "textures/atlas/blocks-mip.png"), mippedBlocks);
					}
					RenderLayer defLayer = RenderLayers.getItemLayer(stack, true);
					RenderLayer layer = defLayer == TexturedRenderLayers.getEntityCutout() ?
							RenderLayer.getEntityCutout(Identifier.of("fabrication", "textures/atlas/blocks-mip.png")) :
								RenderLayer.getEntityTranslucent(Identifier.of("fabrication", "textures/atlas/blocks-mip.png"));
					VertexConsumer vertices = vertexConsumers.getBuffer(layer);
					subject.renderBakedItemModel(model, stack, light, overlay, matrices, vertices);
				}
				matrices.pop();
				return;
			}
		}
		subject.renderItem(stack, renderMode, leftHanded, matrices, vertexConsumers, light, overlay, model);
	}

	private static void drawExaggeratedQuad(ItemStack is, MatrixStack matrices, VertexConsumer vertices, BakedQuad quad, int light, int overlay) {
		boolean isProbablyGrass = false;

		int packedColor = -1;
		if (quad.hasColor()) {
			packedColor = MinecraftClient.getInstance().itemColors.getColor(is, quad.getColorIndex());
			Block b = ((BlockItem)is.getItem()).getBlock();
			BlockSoundGroup sg = b.getDefaultState().getSoundGroup();
			isProbablyGrass = sg == BlockSoundGroup.GRASS || (sg == BlockSoundGroup.GRAVEL && b.getDefaultMapColor() == MapColor.DIRT_BROWN);
		}
		float tintR = (packedColor >> 16 & 0xFF) / 255.0f;
		float tintG = (packedColor >> 8 & 0xFF) / 255.0f;
		float tintB = (packedColor & 0xFF) / 255.0f;

		MatrixStack.Entry ent = matrices.peek();
		int[] data = quad.getVertexData();
		Vec3i faceVec = quad.getFace().getVector();
		Vector3f normal = new Vector3f(faceVec.getX(), faceVec.getY(), faceVec.getZ());
		Vector4f pos = new Vector4f(0, 0, 0, 1);
		Matrix4f mat = ent.getPositionMatrix();
		normal.mul(ent.getNormalMatrix());
		int j = data.length / 8;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			ByteBuffer buf = stack.malloc(VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL.getVertexSizeByte());
			IntBuffer iBuf = buf.asIntBuffer();
			float minU = Float.POSITIVE_INFINITY;
			float maxU = Float.NEGATIVE_INFINITY;
			float minV = Float.POSITIVE_INFINITY;
			float maxV = Float.NEGATIVE_INFINITY;
			for (int pass = 0; pass < 2; pass++) {
				for (int i = 0; i < j; ++i) {
					// IntBuffer-specific clear() override was added after Java 8
					// This cast avoids a NSME
					((Buffer)iBuf).clear();
					iBuf.put(data, i * 8, 8);
					float u = buf.getFloat(16);
					float v = buf.getFloat(20);

					if (pass == 0) {
						minU = Math.min(minU, u);
						maxU = Math.max(maxU, u);
						minV = Math.min(minV, v);
						maxV = Math.max(maxV, v);
					} else if (pass == 1) {
						float x = buf.getFloat(0);
						float y = buf.getFloat(4);
						float z = buf.getFloat(8);

						float r = ((buf.get(12) & 0xFF) / 255.0f) * tintR;
						float g = ((buf.get(13) & 0xFF) / 255.0f) * tintG;
						float b = ((buf.get(14) & 0xFF) / 255.0f) * tintB;

						pos.set(x, y, z, 1);
						pos.mul(mat);

						float uSize = maxU-minU;
						float vSize = maxV-minV;

						if (u == minU) {
							u = minU + (uSize*(4/16f));
						} else if (u == maxU) {
							u = minU + (uSize*(12/16f));
						}
						if (v == minV) {
							v = minV + (vSize*((isProbablyGrass ? 1 : 4)/16f));
						} else if (v == maxV) {
							v = minV + (vSize*((isProbablyGrass ? 9 : 12)/16f));
						}

						vertices.vertex(pos.x, pos.y, pos.z,
								ColorHelper.Argb.fromFloats(r, g, b, 1),
								u, v,
								overlay, light,
								normal.x, normal.y, normal.z);
					}
				}
			}
		}
	}

}
