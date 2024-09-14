package com.unascribed.fabrication.client;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import com.unascribed.fabrication.EarlyAgnos;
import com.unascribed.fabrication.FabConf;
import com.unascribed.fabrication.FabLog;
import com.unascribed.fabrication.FabricationMod;
import com.unascribed.fabrication.FabricationModClient;
import com.unascribed.fabrication.FeaturesFile;
import com.unascribed.fabrication.FeaturesFile.FeatureEntry;
import com.unascribed.fabrication.FeaturesFile.Sides;
import com.unascribed.fabrication.interfaces.GetServerConfig;
import com.unascribed.fabrication.support.ConfigValues;
import com.unascribed.fabrication.support.MixinConfigPlugin;
import com.unascribed.fabrication.util.ByteBufCustomPayload;
import io.netty.buffer.Unpooled;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexFormat.DrawMode;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.level.storage.SessionLock;
import org.apache.commons.lang3.ArrayUtils;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.unascribed.fabrication.client.FabricationConfigScreen.ConfigValueFlag.CLIENT_ONLY;
import static com.unascribed.fabrication.client.FabricationConfigScreen.ConfigValueFlag.HIGHLIGHT_QUERY_MATCH;
import static com.unascribed.fabrication.client.FabricationConfigScreen.ConfigValueFlag.REQUIRES_FABRIC_API;
import static com.unascribed.fabrication.client.FabricationConfigScreen.ConfigValueFlag.SHOW_SOURCE_SECTION;

public class FabricationConfigScreen extends Screen {

	public enum ConfigValueFlag {
		CLIENT_ONLY, REQUIRES_FABRIC_API, SHOW_SOURCE_SECTION, HIGHLIGHT_QUERY_MATCH
	}
	private final Map<String, String> SECTION_DESCRIPTIONS = Maps.newHashMap();
	private static final Identifier ID_LOCK = Identifier.of("fabrication", "lock.png");
	private static final Identifier ID_FSCRIPT = Identifier.of("fabrication", "fscript.png");
	private static final Identifier BG = Identifier.of("fabrication", "bg.png");
	private static final Identifier BG_DARK = Identifier.of("fabrication", "bg-dark.png");
	private static final Identifier BG_GRAD = Identifier.of("fabrication", "bg-grad.png");
	private static final Identifier BG_GRAD_DARK = Identifier.of("fabrication", "bg-grad-dark.png");

	private static long serverLaunchId = -1;

	private static final Set<String> newlyBannedKeysClient = Sets.newHashSet();
	private static final Set<String> newlyBannedKeysServer = Sets.newHashSet();

	private static final Set<String> newlyUnbannedKeysClient = Sets.newHashSet();
	private static final Set<String> newlyUnbannedKeysServer = Sets.newHashSet();

	private static boolean isFScriptLoaded = EarlyAgnos.isModLoaded("fscript");

	private final Screen parent;

	private final PrideFlagRenderer prideFlag;

	private float timeExisted;
	private boolean leaving = false;
	private float timeLeaving;
	private float sidebarScrollTarget;
	private float sidebarScroll;
	private float lastSidebarScroll;
	private float sidebarHeight;

	private boolean didClick;
	private boolean mouseDragging;
	private int lastDragY;
	private float selectTime;
	private String selectedSection;
	private String prevSelectedSection;
	private float selectedSectionHeight;
	private float prevSelectedSectionHeight;
	private float selectedSectionScroll;
	private float prevSelectedSectionScroll;
	private float lastSelectedSectionScroll;
	private float lastPrevSelectedSectionScroll;
	private float selectedSectionScrollTarget;
	private float prevSelectedSectionScrollTarget;

	private int tooltipBlinkTicks = 0;

	private boolean configuringServer;
	private boolean hasClonked = true;
	private boolean isSingleplayer;
	private boolean editingWorldPath;
	private float serverAnimateTime;
	private String whyCantConfigureServer = null;
	private Set<String> serverKnownConfigKeys = Sets.newHashSet();
	private boolean serverReadOnly;

	private final List<String> tabs = Lists.newArrayList();
	private final Multimap<String, String> options = Multimaps.newMultimap(Maps.newLinkedHashMap(), Lists::newArrayList);

	private final Map<String, ConfigValues.Feature> optionPreviousValues = Maps.newHashMap();
	private final Map<String, Float> optionAnimationTime = Maps.newHashMap();
	private final Map<String, Float> disabledAnimationTime = Maps.newHashMap();
	private final Map<String, Float> becomeBanAnimationTime = Maps.newHashMap();
	private final Set<String> knownDisabled = Sets.newHashSet();
	private final Set<String> onlyBannableds = Sets.newHashSet();
	private final Map<String, Map<String, FeatureSubmenu>> submenus = new HashMap<>();

	private boolean bufferTooltips = false;
	private final List<Runnable> bufferedTooltips = Lists.newArrayList();

	private int noteIndex = 0;

	private TextFieldWidget searchField;
	private Pattern queryPattern = Pattern.compile("");
	private boolean emptyQuery = true;
	private boolean searchingScriptable = false;
	private double lastMouseX, lastMouseY;


	public FabricationConfigScreen(Screen parent) {
		super(Text.literal(MixinConfigPlugin.MOD_NAME+" configuration"));
		this.parent = parent;
		prideFlag = OptionalPrideFlag.get();
		for (String sec : FabConf.getAllSections()) {
			SECTION_DESCRIPTIONS.put(sec, FeaturesFile.get(sec).desc);
		}
		for (String key : FabConf.getAllKeys()) {
			int dot = key.indexOf('.');
			String section = key.substring(0, dot);
			String name = key.substring(dot+1);
			options.put(section, name);
		}
		if (isFScriptLoaded) {
			for (Map.Entry<String, FeatureEntry> en : FeaturesFile.getAll().entrySet()) {
				String key = en.getKey();
				FeatureEntry feature = en.getValue();
				if (feature.fscript != null || feature.extend != null && FeaturesFile.get(FabConf.remap(feature.extend)).fscript != null) {
					defaultedSubmenu(key).put("FScript", OptionalFScriptScreen::construct);
				}
			}
		}
		defaultedSubmenu(FabConf.remap("*.block_logo")).put("Detailed Configs", BlockLogoScreen::new);
		defaultedSubmenu(FabConf.remap("*.yeet_recipes")).put("Detailed Configs", YeetRecipesScreen::new);
		defaultedSubmenu(FabConf.remap("*.taggable_players")).put("Detailed Configs", TaggablePlayersScreen::new);
		defaultedSubmenu(FabConf.remap("*.classic_block_drops")).put("Detailed Configs", ClassicBlockDropsScreen::new);

		tabs.add("search");
		tabs.addAll(options.keySet());
	}

	private Map<String, FeatureSubmenu> defaultedSubmenu(String key) {
		if (!submenus.containsKey(key)){
			Map<String, FeatureSubmenu> map = new HashMap<>();
			submenus.put(key, map);
			return map;
		}
		return submenus.get(key);
	}

	@Override
	protected void init() {
		super.init();
		isSingleplayer = false;
		if (client.world == null) {
			if (!editingWorldPath) {
				FabConf.setWorldPath(null);
			}
			//whyCantConfigureServer = "You're not connected to a server.";
		} else if (client.getServer() != null) {
			//whyCantConfigureServer = "The singleplayer server shares the client settings.";
			isSingleplayer = true;
		} else {
			CommandDispatcher<?> disp = client.player.networkHandler.getCommandDispatcher();
			CommandNode<?> root = disp.getRoot().getChild(MixinConfigPlugin.MOD_NAME_LOWER);
			if (root == null) root = disp.getRoot().getChild(MixinConfigPlugin.MOD_NAME_LOWER_OTHER);
			if (root == null) {
				whyCantConfigureServer = "This server doesn't have "+ MixinConfigPlugin.MOD_NAME+".";
			} else {
				ClientPlayNetworkHandler cpnh = client.getNetworkHandler();
				if (cpnh instanceof GetServerConfig) {
					GetServerConfig gsc = (GetServerConfig) cpnh;
					if (!gsc.fabrication$hasHandshook()) {
						whyCantConfigureServer = "This server's version of "+ MixinConfigPlugin.MOD_NAME+" is too old.";
					} else {
						CommandNode<?> config = root.getChild("config");
						serverReadOnly = config == null || config.getChild("set") == null;
						serverKnownConfigKeys.clear();
						serverKnownConfigKeys.addAll(gsc.fabrication$getServerTrileanConfig().keySet());
						serverKnownConfigKeys.addAll(gsc.fabrication$getServerStringConfig().keySet());
					}
				} else {
					whyCantConfigureServer = "An internal error prevented initialization of the syncer.";
				}
			}
		}
		searchField = new TextFieldWidget(textRenderer, 131, 1, width-252, 14, searchField, Text.literal("Search"));
		if (isFScriptLoaded) searchField.setWidth(searchField.getWidth()-16);

		searchField.setChangedListener((s) -> {
			s = s.trim();
			emptyQuery = s.isEmpty();
			queryPattern = Pattern.compile(s, Pattern.LITERAL | Pattern.CASE_INSENSITIVE);
		});
	}

	@Override
	public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		if (timeExisted == 0 && !FabConf.isEnabled("*.reduced_motion")) {
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ENTITY_WITHER_SHOOT, 2f, 0.1f));
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BARREL_OPEN, 1.2f));
		}
		timeExisted += delta;
		if (leaving) {
			timeLeaving += delta;
		}
		if ((leaving || timeExisted < 10) && !FabConf.isEnabled("*.reduced_motion")) {
			float a = sCurve5((leaving ? Math.max(0, 10 - timeLeaving) : timeExisted) / 10);
			MatrixStack matrices = drawContext.getMatrices();
			matrices.push();
				matrices.translate(width / 2f, height, 0);
				matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(a * (leaving ? -180 : 180)));
				matrices.translate(-width / 2, -height, 0);
				matrices.push();
					matrices.translate(0, height, 0);
					matrices.translate(width / 2f, height / 2f, 0);
					matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
					matrices.translate(-width / 2f, -height / 2f, 0);
					drawContext.fill(-width, -height, width * 2, 0, FabConf.isEnabled("general.dark_mode") ? 0xFF212020 : 0xFF2196F3);
					matrices.push();
						drawBackground(drawContext, -200, -200, delta, 0, 0);
						drawForeground(drawContext, -200, -200, delta);
					matrices.pop();
				matrices.pop();
			matrices.pop();

			// background rendering ignores the matrixstack, so we have to Make A Mess in the projection matrix instead
			if (parent != null) {
				MatrixStack projection = new MatrixStack();
				projection.multiplyPositionMatrix(RenderSystem.getProjectionMatrix());
				projection.push();
					projection.translate(width / 2f, height, 0);
					projection.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(a * (leaving ? -180 : 180)));
					projection.translate(-width / 2, -height, 0);
					for (int x = -1; x <= 1; x++) {
						for (int y = -1; y <= 0; y++) {
							if (x == 0 && y == 0) continue;
							projection.push();
							projection.translate(width * x, height * y, 0);
							RenderSystem.setProjectionMatrix(projection.peek().getPositionMatrix(), VertexSorter.BY_Z);
							parent.renderInGameBackground(drawContext); // TODO: parent.renderBackgroundTexture(drawContext);
							projection.pop();
						}
					}
					RenderSystem.setProjectionMatrix(projection.peek().getPositionMatrix(), VertexSorter.BY_Z);
					parent.render(drawContext, -200, -200, delta);
				projection.pop();
				RenderSystem.setProjectionMatrix(projection.peek().getPositionMatrix(), VertexSorter.BY_Z);
			}
		} else {
			drawContext.getMatrices().push();
				drawBackground(drawContext, mouseX, mouseY, delta, 0, 0);
				drawForeground(drawContext, mouseX, mouseY, delta);
			drawContext.getMatrices().pop();
		}
		if (leaving && timeLeaving > 10) {
			client.setScreen(parent);
		}
	}

	@Override
	public void renderBackground(DrawContext matrices, int mouseX, int mouseY, float delta) {
		//I don't know why this was even here
		//drawBackground(height, width, client, prideFlag, 0, matrices, 0, 0, 0, 0, 0);
	}

	private void drawBackground(DrawContext drawContext, int mouseX, int mouseY, float delta, int cutoffX, int cutoffY) {
		drawBackground(height, width, client, prideFlag, selectedSection == null ? 10-selectTime : prevSelectedSection == null ? selectTime : 0, drawContext, mouseX, mouseY, delta, cutoffX, cutoffY);
	}

	public static void drawBackground(int height, int width, MinecraftClient client, PrideFlagRenderer prideFlag, float time, DrawContext drawContext, int mouseX, int mouseY, float delta, int cutoffX, int cutoffY) {
		float cutoffV = cutoffY/(float)height;
		Identifier bg = FabConf.isEnabled("general.dark_mode") ? BG_DARK : BG;
		Identifier bgGrad = FabConf.isEnabled("general.dark_mode") ? BG_GRAD_DARK : BG_GRAD;
		Tessellator tessellator = Tessellator.getInstance();
		Matrix4f mat = drawContext.getMatrices().peek().getPositionMatrix();

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableCull();

		RenderSystem.setShaderColor(1, 1, 1, 1);
		RenderSystem.setShaderTexture(0, bgGrad);
		client.getTextureManager().bindTexture(bgGrad);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);

		int startX = cutoffX == 0 ? -width : cutoffX;

		BufferBuilder bb = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bb.vertex(mat, startX, cutoffY, 0).texture(0, cutoffV);
		bb.vertex(mat, width*2, cutoffY, 0).texture(1, cutoffV);
		bb.vertex(mat, width*2, height, 0).texture(1, 1);
		bb.vertex(mat, startX, height, 0).texture(0, 1);
		BufferRenderer.drawWithGlobalProgram(bb.end());
		float ratio = 502/1080f;

		float w = height*ratio;
		float brk = Math.min(width-w, (width*2/3f)-(w/3));
		float brk2 = brk+w;
		float border = (float)(20/(client.getWindow().getScaleFactor()));
		if (brk < cutoffX) brk = cutoffX;


		float top = (570/1080f)*height;
		float bottom = (901/1080f)*height;
		if (cutoffY < bottom) {
			float h = bottom-top;
			float flagCutoffV = 0;
			if (top < cutoffY) {
				top = cutoffY;
				flagCutoffV = 1-((bottom-top)/h);
			}
			RenderSystem.setShader(GameRenderer::getPositionColorProgram);
			if (prideFlag != null) {
				prideFlag.render(drawContext, brk, top, w, bottom-top);
			} else {
				bb = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
				float r = MathHelper.lerp(flagCutoffV, 0.298f, 0.475f);
				float g = MathHelper.lerp(flagCutoffV, 0.686f, 0.333f);
				float b = MathHelper.lerp(flagCutoffV, 0.314f, 0.282f);
				bb.vertex(mat, brk, top, 0).color(r, g, b, 1);
				bb.vertex(mat, brk2, top, 0).color(r, g, b, 1);
				bb.vertex(mat, brk2, bottom, 0).color(0.475f, 0.333f, 0.282f, 1);
				bb.vertex(mat, brk, bottom, 0).color(0.475f, 0.333f, 0.282f, 1);
				BufferRenderer.drawWithGlobalProgram(bb.end());
			}
		}

		RenderSystem.setShaderTexture(0, bg);
		client.getTextureManager().bindTexture(bg);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		RenderSystem.setShader(GameRenderer::getPositionTexProgram);
		bb = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
		bb.vertex(mat, Math.max(cutoffX, border), cutoffY, 0).texture(0, cutoffV);
		bb.vertex(mat, brk, cutoffY, 0).texture(0, cutoffV);
		bb.vertex(mat, brk, height, 0).texture(0, 1);
		bb.vertex(mat, Math.max(cutoffX, border), height, 0).texture(0, 1);

		bb.vertex(mat, brk, cutoffY, 0).texture(0, cutoffV);
		bb.vertex(mat, brk2, cutoffY, 0).texture(1, cutoffV);
		bb.vertex(mat, brk2, height, 0).texture(1, 1);
		bb.vertex(mat, brk, height, 0).texture(0, 1);

		bb.vertex(mat, brk2, cutoffY, 0).texture(1, cutoffV);
		bb.vertex(mat, width-border, cutoffY, 0).texture(1, cutoffV);
		bb.vertex(mat, width-border, height, 0).texture(1, 1);
		bb.vertex(mat, brk2, height, 0).texture(1, 1);

		BufferRenderer.drawWithGlobalProgram(bb.end());

		float a = 1-(0.3f+(sCurve5(time/10f)*0.7f));
		if (a > 0) {
			RenderSystem.setShaderColor(1, 1, 1, a);
			RenderSystem.setShaderTexture(0, bgGrad);
			bb = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
			bb.vertex(mat, startX, cutoffY, 0).texture(0, cutoffV);
			bb.vertex(mat, width*2, cutoffY, 0).texture(1, cutoffV);
			bb.vertex(mat, width*2, height, 0).texture(1, 1);
			bb.vertex(mat, startX, height, 0).texture(0, 1);
			BufferRenderer.drawWithGlobalProgram(bb.end());
		}

		RenderSystem.setShaderColor(1, 1, 1, 1);
	}

	private int lerpColor(int from, int to, float delta) {
		float a = MathHelper.lerp(delta, ((from>>24)&0xFF)/255f, ((to>>24)&0xFF)/255f);
		float r = MathHelper.lerp(delta, ((from>>16)&0xFF)/255f, ((to>>16)&0xFF)/255f);
		float g = MathHelper.lerp(delta, ((from>>8 )&0xFF)/255f, ((to>>8 )&0xFF)/255f);
		float b = MathHelper.lerp(delta, ((from>>0 )&0xFF)/255f, ((to>>0 )&0xFF)/255f);
		int c = 0;
		c |= ((int)(a*255)&0xFF)<<24;
		c |= ((int)(r*255)&0xFF)<<16;
		c |= ((int)(g*255)&0xFF)<<8;
		c |= ((int)(b*255)&0xFF)<<0;
		return c;
	}

	private void drawForeground(DrawContext drawContext, int mouseX, int mouseY, float delta) {
		if (serverAnimateTime > 0) {
			serverAnimateTime -= delta;
		}
		if (serverAnimateTime < 0) {
			serverAnimateTime = 0;
		}
		if (selectTime > 0) {
			selectTime -= delta;
		}
		if (selectTime < 0) {
			selectTime = 0;
		}
		float a = sCurve5(serverAnimateTime/10f);
		if (a <= 0.05 && !hasClonked) {
			hasClonked = true;
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BONE_BLOCK_STEP, 1f, 0.5f));
		}
		if (configuringServer || editingWorldPath) {
			a = 1-a;
		}

		drawContext.fill(-width, -height, 130, height, 0x44000000);
		float scroll = sidebarHeight < height ? 0 : lastSidebarScroll+((sidebarScroll-lastSidebarScroll)*client.getRenderTickCounter().getTickDelta(true));
		scroll = (float) (Math.floor((scroll*client.getWindow().getScaleFactor()))/client.getWindow().getScaleFactor());
		float y = 8-scroll;
		int newHeight = 8;
		int i = 0;
		float selectedChoiceY = -60;
		float prevSelectedChoiceY = -60;
		MatrixStack matrices = drawContext.getMatrices();
		for (String s : tabs) {
			int thisHeight = 8;
			float selectA;
			if (s.equals(selectedSection)) {
				selectA = sCurve5((10-selectTime)/10f);
				selectedChoiceY = y;
			} else if (s.equals(prevSelectedSection)) {
				selectA = sCurve5(selectTime/10f);
				prevSelectedChoiceY = y;
			} else {
				selectA = 0;
			}
			float startY = y;
			int icoY = 0;
			int size = 28;
			if ("search".equals(s)) {
				size = 12;
				icoY = -4;
			}
			Identifier id = Identifier.of("fabrication", "category/"+s+".png");
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			client.getTextureManager().bindTexture(id);
			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			RenderSystem.setShaderColor(1, 1, 1, 0.3f);
			RenderSystem.setShaderTexture(0, id);
			matrices.push();
			matrices.translate((130-4-size), icoY+y, 0);
			matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(5));
			drawContext.drawTexture(id, 0, 0, 0, 0, 0, size, Math.min(size, (int)Math.ceil(height-y)), size, size);
			matrices.pop();
			RenderSystem.setShaderColor(1, 1, 1, 1);
			drawContext.drawText(textRenderer, "§l"+FeaturesFile.get(s).shortName, 4, (int) y, -1, false);
			y += 12;
			thisHeight += 12;
			if (!"search".equals(s)) {
				String desc = SECTION_DESCRIPTIONS.getOrDefault(s, "No description available");
				int x = 8;
				int line = 0;
				for (String word : Splitter.on(CharMatcher.whitespace()).split(desc)) {
					int w = textRenderer.getWidth(word);
					if (x+w > 100 && line == 0) {
						x = 8;
						y += 12;
						newHeight += 12;
						line = 1;
					}
					x = drawContext.drawText(textRenderer, word+" ", x, (int) y, -1, false);
				}
				y += 12;
				thisHeight += 12;
			}
			if (didClick) {
				if (mouseX >= 0 && mouseX <= 130 && mouseY > startY-4 && mouseY < y) {
					boolean deselect = s.equals(selectedSection);
					if ("search".equals(s) && !deselect) {
						client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 1.2f, 1f));
					} else {
						client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BELL.value(), deselect ? 0.5f : 0.6f+(i*0.1f), 1f));
					}
					prevSelectedSection = selectedSection;
					selectedSection = deselect ? null : s;

					prevSelectedSectionScroll = selectedSectionScroll;
					lastPrevSelectedSectionScroll = lastSelectedSectionScroll;
					prevSelectedSectionHeight = selectedSectionHeight;
					prevSelectedSectionScrollTarget = selectedSectionScrollTarget;

					selectedSectionScroll = 0;
					lastSelectedSectionScroll = 0;
					selectedSectionHeight = 0;
					selectedSectionScrollTarget = 0;

					selectTime = 10-selectTime;
				}
			}
			if (selectA > 0) {
				RenderSystem.disableCull();
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.setShader(GameRenderer::getPositionColorProgram);
				Tessellator tessellator = Tessellator.getInstance();
				Matrix4f mat = matrices.peek().getPositionMatrix();
				BufferBuilder bb = tessellator.begin(DrawMode.QUADS, VertexFormats.POSITION_COLOR);
				bb.vertex(mat, 0, y-thisHeight-8, 0).color(1, 1, 1, 0.2f);
				bb.vertex(mat, 130*selectA, y-thisHeight-8, 0).color(1, 1, 1, 0.2f+((1-selectA)*0.8f));
				bb.vertex(mat, 130*selectA, y, 0).color(1, 1, 1, 0.2f+((1-selectA)*0.8f));
				bb.vertex(mat, 0, y, 0).color(1, 1, 1, 0.2f);
				BufferRenderer.drawWithGlobalProgram(bb.end());
			}
			y += 8;
			newHeight += thisHeight;
			i++;
		}
		sidebarHeight = newHeight;
		if (sidebarHeight >= height) {
			float knobHeight = (height/sidebarHeight)*height;
			float knobY = (scroll/(sidebarHeight-height))*(height-knobHeight);
			drawContext.fill(128, (int)knobY, 130, (int)(knobY+knobHeight), 0xAAFFFFFF);
		}

		bufferTooltips = true;
		float selectedA = sCurve5((10-selectTime)/10f);
		float prevSelectedA = sCurve5(selectTime/10f);
		drawSection(drawContext, selectedSection, mouseX, mouseY, selectedChoiceY, selectedA, true);
		if (!FabConf.isEnabled("general.reduced_motion") && !Objects.equal(selectedSection, prevSelectedSection)) {
			drawSection(drawContext, prevSelectedSection, -200, -200, prevSelectedChoiceY, prevSelectedA, false);
		}

		boolean searchSelected = "search".equals(selectedSection);
		boolean searchWasSelected = "search".equals(prevSelectedSection);
		if (searchSelected) {
			RenderSystem.setShaderColor(1, 1, 1, selectedA);
			searchField.setAlpha(selectedA);
			searchField.render(drawContext, mouseX, mouseY, delta);
		} else if (searchWasSelected && prevSelectedA > 0) {
			RenderSystem.setShaderColor(1, 1, 1, prevSelectedA);
			searchField.setAlpha(prevSelectedA);
			searchField.render(drawContext, mouseX, mouseY, delta);
		}
		searchField.setFocused(searchSelected);
		RenderSystem.setShaderColor(1, 1, 1, 1);

		matrices.push();
		RenderSystem.disableDepthTest();
		drawContext.fill(width-120, 0, width*2, 16, 0x33000000);
		matrices.push();
		matrices.translate(width-60, 8, 0);
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(a*-180));
		float h = (40+(a*-100))/360f;
		if (h < 0) {
			h = 1+h;
		}
		matrices.push();
		matrices.scale((float)(1-(Math.abs(Math.sin(a*Math.PI))/2)), 1, 1);
		drawContext.fill(-60, -8, 0, 8, MathHelper.hsvToRgb(h, 0.9f, 0.9f)|0xFF000000);
		matrices.pop();
		matrices.push();
		matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(45));
		// 8 / sqrt(2)
		float f = 5.6568542f;
		matrices.scale(f, f, 1);
		drawContext.fill(-1, -1, 1, 1, 0xFFFFFFFF);
		matrices.pop();
		drawContext.fill(-6, -1, -2, 1, 0xFF000000);
		matrices.pop();
		drawContext.fill(-2, -2, 2, 2, 0xFF000000);
		matrices.pop();

		boolean darkMode = FabConf.isEnabled("general.dark_mode");

		drawContext.drawText(textRenderer, "CLIENT", width-115, 4, 0xFF000000, false);
		if (client.world == null || isSingleplayer) {
			drawContext.drawText(textRenderer, "WORLD", width - 40, 4, 0xFF000000, false);
		} else {
			drawContext.drawText(textRenderer, "SERVER", width - 40, 4, whyCantConfigureServer == null ? 0xFF000000 : darkMode ? 0x44FFFFFF : 0x44000000, false);
			if (serverReadOnly && whyCantConfigureServer == null) {
				RenderSystem.setShaderTexture(0, ID_LOCK);
				RenderSystem.setShaderColor(0, 0, 0, 1);
				drawContext.drawTexture(ID_LOCK, width-49, 3, 0, 0, 0, 8, 8, 8, 8);
			}
		}
		if (searchSelected && isFScriptLoaded) {
			if(didClick && mouseX >= width-136 && mouseX < width-120 && mouseY <= 16) {
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
				searchingScriptable = !searchingScriptable;
			}
			RenderSystem.setShaderTexture(0, ID_FSCRIPT);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			drawContext.fill(width-136, 0, width-120, 16, searchingScriptable? 0xFF0AA000 : 0x55000000);
			drawContext.drawTexture(ID_FSCRIPT, width-136, 0, 0, 0, 0, 16, 16, 16, 16);
		}
		drawBackground(drawContext, mouseX, mouseY, delta, 130, height-20);

		List<String> notes = Lists.newArrayList();

		Set<String> newlyBannedKeys;
		Set<String> newlyUnbannedKeys;

		boolean hasYellowNote = false;
		boolean hasRedNote = false;

		if (configuringServer) {
			checkServerData();
			newlyBannedKeys = newlyBannedKeysServer;
			newlyUnbannedKeys = newlyUnbannedKeysServer;
		} else {
			newlyBannedKeys = newlyBannedKeysClient;
			newlyUnbannedKeys = newlyUnbannedKeysClient;
		}
		if (!newlyUnbannedKeys.isEmpty()) {
			notes.add("§c"+newlyUnbannedKeys.size()+" newly unbanned option"+(newlyUnbannedKeys.size() == 1 ? "" : "s")+" will\n§cnot activate until the {} is\n§crestarted.");
			hasRedNote = true;
		}
		if (!newlyBannedKeys.isEmpty()) {
			notes.add(newlyBannedKeys.size()+" newly banned option"+(newlyBannedKeys.size() == 1 ? "" : "s")+" will be\nunloaded when the {} is\nrestarted.");
		}
		if (noteIndex < 0) {
			noteIndex = 0;
		}
		if (noteIndex >= notes.size()) {
			noteIndex = 0;
		}
		int textHeight = drawWrappedText(drawContext, 136, height,
				(hasRedNote ? "§c\u26A0 " : hasYellowNote ? "§e" : "")+notes.size()+" note"+(notes.size() == 1 ? "" : "s")+
				(notes.isEmpty() ? " ☺" : " - hover to see "+(notes.size() == 1 ? "it" : "them")), width-250, -1, true);
		if (mouseX >= 136 && mouseX <= width-100 && mouseY >= height-textHeight) {
			if (!notes.isEmpty()) {
				List<Text> lines = Lists.newArrayList();
				for (String s : notes.get(noteIndex).replace("{}", configuringServer ? "server" : "client").split("\n")) {
					lines.add(Text.literal(s));
				}
				if (notes.size() > 1) {
					lines.add(Text.literal("§7Click to see other notes"));
				}
				drawContext.drawTooltip(textRenderer, lines, mouseX, mouseY);
				if (didClick && notes.size() > 1) {
					noteIndex++;
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_LOOM_SELECT_PATTERN, 1f));
				}
			}
		}

		if (drawButton(drawContext, width-100, height-20, 100, 20, "Done", mouseX, mouseY)) {
			close();
		}
		if (didClick) {
			didClick = false;
			lastDragY = 0;
		}
		if (mouseDragging) mouseDragging = false;

		super.render(drawContext, mouseX, mouseY, delta);

		bufferTooltips = false;
		for (Runnable r : bufferedTooltips) {
			r.run();
		}
		bufferedTooltips.clear();

		if (mouseX > width-120 && mouseY < 16) {
			String msg;
			if (whyCantConfigureServer != null) {
				msg = ((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "§e")+whyCantConfigureServer;
			} else if (editingWorldPath) {
				msg = "Click to unselect world";
			} else if (client.world == null || isSingleplayer) {
				msg = "Click to select world";
			} else {
				int srv = serverKnownConfigKeys.size();
				int cli = FabConf.getAllKeys().size();
				msg = "§dServer has "+ MixinConfigPlugin.MOD_NAME+" and is recognized.";
				if (srv != cli) {
					msg += "\n§oMismatch: Server has "+srv+" options. Client has "+cli+".";
					if (srv > cli) {
						msg += "\n§cOptions unknown to the client will not appear.";
					} else if (cli > srv) {
						msg += "\n§eOptions unknown to the server will be disabled.";
					}
				}
			}
			if (serverReadOnly) {
				msg += "\n§fYou cannot configure this server.";
				if (configuringServer) {
					msg += "\n§fChanges cannot be made.";
				}
			}
			if (!isSingleplayer && (!serverReadOnly || !configuringServer)) {
				msg += "\n§fChanges will apply to the "+(configuringServer ? "§dSERVER" : "§6CLIENT")+"§f.";
			}
			drawContext.drawTooltip(textRenderer, Lists.transform(Lists.newArrayList(msg.split("\n")),
					Text::of), mouseX+10, 20+mouseY);
		}
		matrices.pop();
	}

	private void checkServerData() {
		ClientPlayNetworkHandler cpnh = client.getNetworkHandler();
		if (cpnh != null && cpnh instanceof GetServerConfig) {
			long launchId = ((GetServerConfig)cpnh).fabrication$getLaunchId();
			if (launchId != serverLaunchId) {
				newlyBannedKeysServer.clear();
				newlyUnbannedKeysServer.clear();
				serverLaunchId = launchId;
			}
		}
	}

	private int drawWrappedText(DrawContext drawContext, float x, float y, String str, int width, int color, boolean fromBottom) {
		int height = 0;
		List<OrderedText> lines = textRenderer.wrapLines(Text.literal(str), width);
		if (fromBottom) {
			y -= 12;
			lines = Lists.reverse(lines);
		}
		for (OrderedText ot : lines) {
			drawContext.drawText(textRenderer, ot, (int) x, (int) y, color, false);
			y += (fromBottom ? -12 : 12);
			height += 12;
		}
		return height;
	}

	private void drawSection(DrawContext drawContext, String section, float mouseX, float mouseY, float choiceY, float a, boolean selected) {
		if (a <= 0) return;
		if (FabConf.isEnabled("general.reduced_motion")) {
			a = 1;
		}
		MatrixStack matrices = drawContext.getMatrices();
		matrices.push();
		matrices.translate(60, choiceY+16, 0);
		matrices.scale(a, a, 1);
		matrices.translate(-60, -(choiceY+16), 0);
		float lastScrollOfs = (selected ? lastSelectedSectionScroll : lastPrevSelectedSectionScroll);
		float scrollOfs = (selected ? selectedSectionScroll : prevSelectedSectionScroll);
		float scroll = (selected ? selectedSectionHeight : prevSelectedSectionHeight) < height-36 ? 0 : lastScrollOfs+((scrollOfs-lastScrollOfs)*client.getRenderTickCounter().getTickDelta(true));
		int startY = 16-(int)(scroll);
		int y = startY;
		if (section == null) {
			String v = getVersion();
			String blurb = "§l"+ MixinConfigPlugin.MOD_NAME+" v"+v+" §rby unascribed and SFort\nRunning under Minecraft "+SharedConstants.getGameVersion().getName()+"\n"+(configuringServer ? "(Local version: v"+ EarlyAgnos.getModVersion()+")" : "")
					+ "\nClick a category on the left to change settings.";
			int height = drawWrappedText(drawContext, 140, 20, blurb, width-130, -1, false);
			if (!configuringServer && drawButton(drawContext, 140, 20+height+32, 120, 20, "Reload files", mouseX, mouseY)) {
				FabConf.reload();
			}
			if (drawButton(drawContext, 140, 42+height+32, 80, 20, "Summary", mouseX, mouseY)) {
				Screen screen = FabricationSummaryScreen.tryCreate(this);
				if (screen != null) client.setScreen(screen);
			}
			y += height;
			y += 44;
		} else {
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			Identifier tex = Identifier.of("fabrication", "category/"+section+".png");
			RenderSystem.setShaderTexture(0, tex);
			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
			RenderSystem.setShaderColor(1, 1, 1, 0.1f);
			matrices.push();
			matrices.translate(130+((width-130)/2f), height/2f, 0);
			drawContext.drawTexture(tex, -80, -80, 0, 0, 0, 160, 160, 160, 160);
			matrices.pop();
			RenderSystem.setShaderColor(1, 1, 1, 1);
			if ("general".equals(section)) {
				if (y > 0) {
					drawContext.drawText(client.textRenderer, "§lGeneral", 135, y-12, -1, false);
				}
				y = drawConfigValues(drawContext, y, mouseX, mouseY, (en) -> !en.key.startsWith("general.category.") && en.key.startsWith("general."));
				y += 25;
				drawContext.drawText(client.textRenderer, "§lCategory Defaults", 135, y-12, -1, false);
				RenderSystem.setShaderColor(1, 1, 1, 1);
				List<Map.Entry<String, FeatureEntry>> categories = FeaturesFile.getAll().entrySet().stream()
						.filter((en) -> en.getKey().startsWith("general.category."))
						.sorted(Comparator.comparing(e -> {
							int i = tabs.indexOf(e.getKey().substring(17));
							return i == -1 ? Integer.MAX_VALUE : i;
						})).toList();
				if (editingWorldPath) {
					y += drawWrappedText(drawContext, 200, y, "Categories are not available in world settings", width-200, 0xFFFFFF, false) + 6;
				} else {
					for (Map.Entry<String, FeatureEntry> en : categories) {
						FeatureEntry fe = en.getValue();
						y = drawCategoryValue(drawContext, en.getKey(), fe.name, fe.desc, y, mouseX, mouseY);
					}
				}
			} else if ("search".equals(section)) {
				y += 4;
				Predicate<FeatureEntry> pen;
				if ("#failed".equals(searchField.getText())) {
					pen = fe -> isFailed(fe.key);
				} else {
					pen = (en) -> emptyQuery || (queryPattern.matcher(en.name).find() || queryPattern.matcher(en.shortName).find() || queryPattern.matcher(en.desc).find());
				}
				if (isFScriptLoaded && searchingScriptable) pen = ((Predicate<FeatureEntry>) en -> en.fscript != null).and(pen);
				y = drawConfigValues(drawContext, y, mouseX, mouseY, pen, SHOW_SOURCE_SECTION, emptyQuery ? null : HIGHLIGHT_QUERY_MATCH);
			} else {
				String name = FeaturesFile.get(section).name;
				if (y > 0) {
					drawContext.drawText(client.textRenderer, "§l"+name, 135, y-12, -1, false);
				}
				y = drawConfigValues(drawContext, y, mouseX, mouseY, (en) -> en.key.startsWith(section+".") && !en.extra);
				int titleY = y;
				y += 22;
				int endY = drawConfigValues(drawContext, y, mouseX, mouseY, (en) -> en.key.startsWith(section+".") && en.extra);
				if (endY != y && y < height-8) {
					drawContext.drawText(client.textRenderer, "§l"+name+" §oExtra", 135, titleY+10, -1, false);
				}
				y = endY;
			}
		}
		if (y == startY) {
			drawContext.drawText(client.textRenderer, "There are no available features in this category", 136, startY+14, -1, false);
		}
		float h = y-startY;
		if (selected) {
			selectedSectionHeight = h;
		} else {
			prevSelectedSectionHeight = h;
		}
		int sh = height-36;
		if (h > sh) {
			float knobHeight = (sh/h)*sh;
			float knobY = ((selected ? selectedSectionScroll : prevSelectedSectionScroll)/(h-sh))*(sh-knobHeight)+16;
			drawContext.fill(width-2, Math.max(16, (int)knobY), width, Math.min(height-20, (int)(knobY+knobHeight)), 0xAAFFFFFF);
		}
		matrices.pop();
	}

	private int drawConfigValues(DrawContext drawContext, int y, float mouseX, float mouseY, Predicate<FeatureEntry> pred, ConfigValueFlag... defaultFlags) {
		RenderSystem.setShaderColor(1, 1, 1, 1);
		for (Map.Entry<String, FeatureEntry> en : FeaturesFile.getAll().entrySet()) {
			FeatureEntry fe = en.getValue();
			if (fe.meta || fe.section) continue;
			if (!pred.test(fe)) continue;
			ConfigValueFlag[] flags = defaultFlags;
			if (fe.sides == Sides.CLIENT_ONLY) flags = ArrayUtils.add(flags, CLIENT_ONLY);
			y = drawConfigValue(drawContext, en.getKey(), fe.name, fe.desc, y, mouseX, mouseY, flags);
		}
		return y;
	}

	private boolean drawButton(DrawContext drawContext, int x, int y, int w, int h, String text, float mouseX, float mouseY) {
		return drawButton(drawContext, x, y, w, h, text, mouseX, mouseY, didClick, client);
	}

	public static boolean drawButton(DrawContext drawContext, int x, int y, int w, int h, String text, float mouseX, float mouseY, boolean didClick, MinecraftClient client) {
		boolean click = false;
		boolean hover = mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h;
		drawContext.fill(x, y, x+w, y+h, FabConf.isEnabled("general.dark_mode") ? 0x44FFFFFF : 0x55000000);
		int textColor = -1;
		if (hover) {
			if (FabConf.isEnabled("*.yellow_button_hover")) textColor = 0xFFFFFFA0;
			drawContext.fill(x, y, x+w, y+1, -1);
			drawContext.fill(x, y, x+1, y+h, -1);
			drawContext.fill(x, y+h-1, x+w, y+h, -1);
			drawContext.fill(x+w-1, y, x+w, y+h, -1);
			if (didClick) {
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
				click = true;
			}
		}
		int textWidth = client.textRenderer.getWidth(text);
		drawContext.drawText(client.textRenderer, text, x+((w-textWidth)/2), y+((h-8)/2), textColor, false);
		return click;
	}

	public static boolean drawToggleButton(DrawContext drawContext, int x, int y, int w, int h, String text, float mouseX, float mouseY, boolean toggle, boolean didClick, MinecraftClient client) {
		boolean click = false;
		boolean hover = mouseX >= x && mouseX <= x+w && mouseY >= y && mouseY <= y+h;
		int textColor = -1;
		if (hover ^ toggle) {
			if (FabConf.isEnabled("*.yellow_button_hover")) textColor = 0xFFFFFFA0;
			drawContext.fill(x, y, x + w, y + 1, -1);
			drawContext.fill(x, y, x + 1, y + h, -1);
			drawContext.fill(x, y + h - 1, x + w, y + h, -1);
			drawContext.fill(x + w - 1, y, x + w, y + h, -1);
		}
		if (hover && didClick) {
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
			click = true;
		}
		int textWidth = client.textRenderer.getWidth(text);
		drawContext.drawText(client.textRenderer, text, (int) (x+((w-textWidth)/2f)), (int) (y+((h-8)/2f)), textColor, false);
		return click;
	}
	private int drawCategoryValue(DrawContext drawContext, String key, String title, String desc, int y, float mouseX, float mouseY) {
		MatrixStack matrices = drawContext.getMatrices();
		matrices.push();
		matrices.translate(0, y, 0);
		int startY = y;
		y += drawWrappedText(drawContext, 200, 2, title, width-200, 0xFFFFFFFF, false) + 6;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderTexture(0, Identifier.of("fabrication", "coffee_bean.png"));
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		int x = 0;
		ConfigValues.Category hovered = null;
		for (ConfigValues.Category p : ConfigValues.Category.values()) {
			boolean profSel;
			try {
				profSel = ConfigValues.Category.parse(getRawValue(key)) == p;
			} catch (IllegalArgumentException e) {
				profSel = p == ConfigValues.Category.GREEN;
			}
			if (mouseX >= 134+x && mouseX <= 134+x+16 && mouseY >= startY && mouseY <= startY+16) {
				hovered = p;
			}
			clicky:
			if ((didClick || mouseDragging) && mouseX >= 134+x && mouseX <= 134+x+16 && mouseY >= startY && mouseY <= startY+16) {
				if (!didClick && mouseDragging) {
					if (lastDragY != startY) {
						lastDragY = startY;
					} else {
						break clicky;
					}
				}
				if (p == ConfigValues.Category.ASH) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(), 2f, 1f));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_SAND_BREAK, 1f, 1f));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_SAND_BREAK, 1f, 1.2f));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_SAND_BREAK, 1f, 0.7f));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_SAND_BREAK, 1f, 0.5f));
				} /*else if (p == ConfigValues.Category.BURNT) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_CHIME, 1.8f, 1f));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_FLINTANDSTEEL_USE, 1f));
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_FIRE_AMBIENT, 1f, 1f));
				}*/ else if (p == ConfigValues.Category.GREEN) {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_BASS.value(), 0.5f, 1f));
				} else {
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value(), 0.707107f+(p.ordinal()*0.22f), 1f));
				}
				setValue(key, p.name().toLowerCase(Locale.ROOT));
			}
			color(p.getColor(), profSel ? 1f : hovered == p ? 0.6f : 0.3f);
			drawContext.drawTexture(Identifier.of("fabrication", "coffee_bean.png"), 134+x, 0, 0, 0, 0, 16, 16, 16, 16);
			color(-1);
			x += 18;
		}
		if (mouseX >= 200 && mouseX <= width-200 && mouseY >= startY && mouseY <= y-6) {
			renderWrappedTooltip(drawContext, desc, mouseX, mouseY);
		}
		if (hovered != null) {
			renderWrappedTooltip(drawContext, "§l"+hovered.displayName()+"\n§f"+hovered.displayDesc(), mouseX, mouseY);
		}
		matrices.pop();
		return y;
	}
	private int drawConfigValue(DrawContext drawContext, String key, String title, String desc, int y, float mouseX, float mouseY, ConfigValueFlag... flags) {
		if (y < -12 || y > height-16) return y+14;
		boolean clientOnly = ArrayUtils.contains(flags, CLIENT_ONLY);
		boolean onlyBannable = clientOnly && configuringServer;
		boolean requiresFabricApi = ArrayUtils.contains(flags, REQUIRES_FABRIC_API);
		boolean showSourceSection = ArrayUtils.contains(flags, SHOW_SOURCE_SECTION);
		boolean highlightQueryMatch = ArrayUtils.contains(flags, HIGHLIGHT_QUERY_MATCH);
		boolean noFabricApi = !configuringServer && requiresFabricApi && !FabricLoader.getInstance().isModLoaded("fabric");
		String failed = getFailed(key);
		boolean banned = !configuringServer && FabricationModClient.isBannedByServer(key);
		boolean disabled = banned || noFabricApi || (configuringServer && serverReadOnly) || !isValid(key);
		boolean noValue = noFabricApi || (configuringServer && clientOnly || !isValid(key));
		float time = optionAnimationTime.getOrDefault(key, 0f);
		float disabledTime = disabledAnimationTime.getOrDefault(key, 0f);
		float becomeBanTime = becomeBanAnimationTime.getOrDefault(key, 0f);
		if (onlyBannable && !onlyBannableds.contains(key)) {
			becomeBanTime = becomeBanAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			onlyBannableds.add(key);
		} else if (!onlyBannable && onlyBannableds.contains(key)) {
			becomeBanTime = becomeBanAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			onlyBannableds.remove(key);
		}
		boolean animateDisabled = disabledTime > 0;
		if (disabled && !knownDisabled.contains(key)) {
			disabledTime = disabledAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			knownDisabled.add(key);
		} else if (!disabled && knownDisabled.contains(key)) {
			disabledTime = disabledAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
			knownDisabled.remove(key);
		}
		if (time > 0) {
			time -= client.getRenderTickCounter().getLastFrameDuration();
			if (time <= 0) {
				optionAnimationTime.remove(key);
				time = 0;
			} else {
				optionAnimationTime.put(key, time);
			}
		}
		if (disabledTime > 0) {
			disabledTime -= client.getRenderTickCounter().getLastFrameDuration();
			if (disabledTime <= 0) {
				disabledAnimationTime.remove(key);
				disabledTime = 0;
			} else {
				disabledAnimationTime.put(key, disabledTime);
			}
		}
		if (becomeBanTime > 0) {
			becomeBanTime -= client.getRenderTickCounter().getLastFrameDuration();
			if (becomeBanTime <= 0) {
				becomeBanAnimationTime.remove(key);
				becomeBanTime = 0;
			} else {
				becomeBanAnimationTime.put(key, becomeBanTime);
			}
		}
		MatrixStack matrices = drawContext.getMatrices();
		matrices.push();
		matrices.translate(0, y, 0);
		float dia = sCurve5((5-becomeBanTime)/5f);
		float scale = 1;
		boolean noBan = key.startsWith("general.");
		boolean noUnset = noBan && !editingWorldPath;
		ConfigValues.Feature currentValue = noUnset ? (isEnabled(key) ? ConfigValues.Feature.TRUE : ConfigValues.Feature.FALSE) : onlyBannable ? getValue(key) == ConfigValues.Feature.BANNED ? ConfigValues.Feature.BANNED : ConfigValues.Feature.UNSET : getValue(key);
		boolean keyEnabled = getResolvedValue(key) == ConfigValues.ResolvedFeature.DEFAULT_TRUE;
		ConfigValues.Feature prevValue = animateDisabled ? currentValue : optionPreviousValues.getOrDefault(key, currentValue);
		int[] xes;
		if (noUnset) {
			xes = new int[] { 0, 23, 0, 0 };
		} else if (noBan) {
			xes = new int[] { 15, 30, 0, 0 };
		} else if (onlyBannable) {
			xes = new int[] { 30, 30, 30, 0 };
		} else {
			xes = new int[] { 30, 45, 15, 0 };
		}
		int[] hues = { 50, 130, -10, -90 };
		int[] values = { 90, 85, 90, 20 };
		int prevX = xes[prevValue.ordinal()];
		int prevHue = hues[prevValue.ordinal()];
		int prevHSValue = values[prevValue.ordinal()];
		int curX = xes[currentValue.ordinal()];
		int curHue = hues[currentValue.ordinal()];
		int curHSValue = values[currentValue.ordinal()];
		float a = sCurve5((5-time)/5f);
		float da = sCurve5((5-disabledTime)/5f);
		if (!(disabled || failed != null)) {
			da = 1-da;
		}
		int trackSize = (noUnset||noBan?45:60);
		if (clientOnly) {
			drawContext.fill(133, 0, 134+trackSize+1, 11, 0xFFFFAA00);
		} else {
			drawContext.fill(133, 0, 134+trackSize+1, 11, 0xFFFFFFFF);
		}
		drawContext.fill(134, 1, 134+trackSize, 10, 0x66000000);
		if (!noUnset && !onlyBannable && !noBan) {
			drawContext.fill(134+15, 1, 134+15+15, 10, 0x33000000);
			drawContext.fill(134+45, 1, 134+45+15, 10, 0x33000000);
		}
		matrices.push();
		matrices.translate(134 + (prevX + ((curX - prevX) * a)), 0, 0);
		int knobAlpha = ((int) ((noValue ? 1 - da : 1) * 255)) << 24;
		int selectedWidth = noUnset ? 22 : onlyBannable ? 30 : 15;
		drawContext.fill(0, 1, selectedWidth, 10, MathHelper.hsvToRgb(Math.floorMod((int) (prevHue + ((curHue - prevHue) * a)), 360) / 360f, 0.9f, (prevHSValue + ((curHSValue - prevHSValue) * a)) / 100f) | knobAlpha);
		if (!noUnset && a >= 1 && (currentValue == ConfigValues.Feature.UNSET || editingWorldPath) && !onlyBannable && !(noBan && currentValue != ConfigValues.Feature.UNSET)) {
			drawContext.fill(keyEnabled ? selectedWidth : -1, 1, keyEnabled ? selectedWidth+1 : 0, 10, MathHelper.hsvToRgb((keyEnabled ? 120 : 0) / 360f, 0.9f, 0.8f) | knobAlpha);
		}
		matrices.pop();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		Identifier tex =  Identifier.of("fabrication", "configvalue.png");
		RenderSystem.setShaderTexture(0, tex);
		RenderSystem.setShaderColor(1, 1, 1, 0.5f+((1-da)*0.5f));
		if (noUnset) {
			drawContext.drawTexture(tex, 134+3, 1, 15, 0, 15, 9, 60, 9);
			drawContext.drawTexture(tex, 134+4+22, 1, 45, 0, 15, 9, 60, 9);
		} else if (noBan) {
			drawContext.drawTexture(tex, 134, 1, 15, 0, 45, 9, 60, 9);
		}else if (onlyBannable) {
			drawContext.drawTexture(tex, 134+7, 1, 0, 0, 15, 9, 60, 9);
			drawContext.drawTexture(tex, 134+38, 1, 30, 0, 15, 9, 60, 9);
		} else {
			drawContext.drawTexture(tex, 134, 1, 0, 0, 60, 9, 60, 9);
		}

		int clickedIndex =(int)(mouseX - 134) / (noUnset ? 22 : onlyBannable ? 30 : 15);
		clicky:
		if ((didClick || mouseDragging) && mouseX >= 134 && mouseX <= 134+trackSize && mouseY >= y+1 && mouseY <= y+10) {
			if (!didClick && mouseDragging) {
				if (lastDragY != y) {
					lastDragY = y;
				} else {
					break clicky;
				}
			}
			float pitch = y * 0.005f;
			if (disabled) {
				playErrorFeedback();
			} else {
				ConfigValues.Feature newValue;
				if (noUnset) {
					newValue = clickedIndex == 0 ? ConfigValues.Feature.FALSE : ConfigValues.Feature.TRUE;
				} else if (noBan) {
					switch (clickedIndex) {
						case 0:
							newValue = ConfigValues.Feature.FALSE;
							break;
						case 2:
							newValue = ConfigValues.Feature.TRUE;
							break;
						case 1:
						default:
							newValue = ConfigValues.Feature.UNSET;
							break;
					}
				} else if (onlyBannable) {
					newValue = clickedIndex == 0 ? ConfigValues.Feature.BANNED : ConfigValues.Feature.UNSET;
				} else {
					switch (clickedIndex) {
						case 0:
							newValue = ConfigValues.Feature.BANNED;
							break;
						case 1:
							newValue = ConfigValues.Feature.FALSE;
							break;
						case 2:
							newValue = ConfigValues.Feature.UNSET;
							break;
						case 3:
							newValue = ConfigValues.Feature.TRUE;
							break;
						default:
							newValue = ConfigValues.Feature.UNSET;
							break;
					}
				}
				client.getSoundManager().play(PositionedSoundInstance.master(
					newValue == ConfigValues.Feature.BANNED ? SoundEvents.BLOCK_NOTE_BLOCK_BASEDRUM.value() :
						newValue == ConfigValues.Feature.FALSE ? SoundEvents.BLOCK_NOTE_BLOCK_BASS.value() :
							newValue == ConfigValues.Feature.UNSET ? SoundEvents.BLOCK_NOTE_BLOCK_COW_BELL.value() :
								SoundEvents.BLOCK_NOTE_BLOCK_CHIME.value(),
					0.6f + pitch, 1f));
				if (newValue != currentValue || (editingWorldPath && !FabConf.doesWorldContainValue(key))) {
					optionPreviousValues.put(key, currentValue);
					optionAnimationTime.compute(key, (k, f) -> 5 - (f == null ? 0 : f));
					setValue(key, newValue.toString().toLowerCase(Locale.ROOT));
				}
			}
		}

		int textAlpha = ((int)((0.7f+((1-da)*0.3f)) * 255))<<24;
		int startY = y;
		int startX = 136+(noUnset||noBan ? 45 : 60)+5;
		int startStartX = startX;
		String section = null;
		if (showSourceSection && key.contains(".")) {
			section = key.substring(0, key.indexOf('.'));
			Identifier id = Identifier.of("fabrication", "category/"+section+".png");
			RenderSystem.setShaderTexture(0, id);
			RenderSystem.setShaderColor(1, 1, 1, 1);
			drawContext.drawTexture(id, startX-2, 0, 0, 0, 12, 12, 12, 12);
			startX += 14;
		}
		String drawTitle = title;
		String drawDesc = desc;
		if (highlightQueryMatch) {
			drawTitle = queryPattern.matcher(drawTitle).replaceAll("§e§l$0§r");
			drawDesc = queryPattern.matcher(drawDesc).replaceAll("§e§l$0§r");
		}
		if (failed != null) drawTitle += (failed.startsWith("Requires") || failed.startsWith("Not Ported") ? " §e " : " §4 ")+failed;
		y += drawWrappedText(drawContext, startX, 2, drawTitle, width-startX-6, 0xFFFFFF | textAlpha, false)*scale;
		int endX = startY == y-8 ? width - 6 : startX+textRenderer.getWidth(title);
		//		int endX = textRenderer.draw(matrices, title, startX, 2, 0xFFFFFF | textAlpha);
		if (mouseX >= 134+trackSize && mouseX <= endX && mouseY >= startY+1 && mouseY <= startY+10 && submenus.containsKey(key)){
			if (didClick){
				client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1f));
				Map<String, FeatureSubmenu> menus = submenus.get(key);
				if (menus.size() == 1){
					client.setScreen(menus.values().stream().findAny().get().construct(this, prideFlag, title, key));
				} else {
					client.setScreen(new SelectionScreen(this, new ArrayList<>(menus.keySet()), s -> menus.get(s).construct(this, prideFlag, title, key)));
				}
			}
			drawContext.fill(startX-2, 9, endX, 10, -1);
		}
		matrices.pop();
		if ((("search".equals(selectedSection) ? false : mouseX <= width-120) || mouseY >= 16) && mouseY < height-20) {
			if (section != null && mouseX >= startStartX && mouseX <= startX && mouseY >= startY && mouseY <= y) {
				renderWrappedTooltip(drawContext, FeaturesFile.get(section).shortName, mouseX, mouseY);
			} else if (mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= y) {
				String prefix = "";
				if (clientOnly) {
					prefix += "§6Client Only ";
				}
				if (requiresFabricApi) {
					prefix += "§bRequires Fabric API ";
				}
				if (!prefix.isEmpty()) {
					prefix += "§r\n";
				}
				renderWrappedTooltip(drawContext, prefix+drawDesc, mouseX, mouseY);
			} else if (mouseX >= 134 && mouseX <= 134+trackSize && mouseY >= startY && mouseY <= startY+10) {
				if (disabled) {
					if (noFabricApi) {
						drawContext.drawTooltip(textRenderer, Text.literal(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"This option requires Fabric API"), (int)mouseX, (int)mouseY);
					} else if (noValue) {
						drawContext.drawTooltip(textRenderer, Text.literal(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"The server does not recognize this option"), (int)mouseX, (int)mouseY);
					} else if (banned) {
						drawContext.drawTooltip(textRenderer, Text.literal(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"This feature is banned by the server"), (int)mouseX, (int)mouseY);
					} else {
						drawContext.drawTooltip(textRenderer, Text.literal(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"You cannot configure this server"), (int)mouseX, (int)mouseY);
					}
				} else if (failed != null) {
					drawContext.drawTooltip(textRenderer, Text.literal(((tooltipBlinkTicks/5)%2 == 1 ? "§c" : "")+"This feature failed to initialize, reason: "+failed), (int)mouseX, (int)mouseY);
				} else {
					int index = (int)((mouseX-134)/(noUnset ? 22 : onlyBannable ? 30 : 15));
					if (onlyBannable) {
						if (clickedIndex == 0) {
							drawContext.drawTooltip(textRenderer, Lists.newArrayList(
									Text.literal("§7Ban"),
									Text.literal("Disallow use by clients")
									), (int)mouseX, (int)mouseY);
						} else {
							drawContext.drawTooltip(textRenderer, Lists.newArrayList(
									Text.literal("§eUnset"),
									Text.literal("Allow use by clients")
									), (int)mouseX, (int)mouseY);
						}
					} else {
						if (clickedIndex == (noUnset || noBan ? 0 : 1)) {
							drawContext.drawTooltip(textRenderer, Text.literal("§cDisable"), (int)mouseX, (int)mouseY);
						} else if (clickedIndex == (noUnset ? -99 : noBan ? 1 : 2)) {
							if (currentValue == ConfigValues.Feature.UNSET) {
								drawContext.drawTooltip(textRenderer, Lists.newArrayList(
										Text.literal("§eUse default value §f(see General > Profile)"),
										Text.literal("§rCurrent default: "+(keyEnabled ? "§aEnabled" : "§cDisabled"))
										), (int)mouseX, (int)mouseY);
							} else {
								drawContext.drawTooltip(textRenderer, Text.literal("§eUse default value §f(see General > Profile)"), (int)mouseX, (int)mouseY);
							}
						} else if (clickedIndex == 0) {
							List<Text> li = Lists.newArrayList(
									Text.literal("§7Ban"),
									Text.literal("Prevent feature from loading entirely")
									);
							if (configuringServer) {
								li.add(Text.literal("and disallow usage by clients"));
							}
							drawContext.drawTooltip(textRenderer, li, (int)mouseX, (int)mouseY);
						} else {
							drawContext.drawTooltip(textRenderer, Text.literal("§aEnable"), (int)mouseX, (int)mouseY);
						}
					}
				}
			}
		}
		return (y+2);
	}

	private void renderWrappedTooltip(DrawContext drawContext, String str, float mouseX, float mouseY) {
		renderOrderedTooltip(drawContext, textRenderer.wrapLines(Text.literal(str), mouseX < width/2 ? (int)(width-mouseX-30) : (int)mouseX-20), (int)(mouseX), (int)(20+mouseY));
	}

	private void playErrorFeedback(){
		client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.8f, 1));
		client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_DIDGERIDOO.value(), 0.7f, 1));
		tooltipBlinkTicks = 20;
	}

	public static String formatTitleCase(String in) {
		String[] pieces = new String[] { in };
		if (in.contains(" ")) {
			pieces = in.toLowerCase().split(" ");
		} else if (in.contains("_")) {
			pieces = in.toLowerCase().split("_");
		}

		StringBuilder result = new StringBuilder();
		for (String s : pieces) {
			if (s == null)
				continue;
			String t = s.trim().toLowerCase();
			if (t.isEmpty())
				continue;
			result.append(Character.toUpperCase(t.charAt(0)));
			if (t.length() > 1)
				result.append(t.substring(1));
			result.append(" ");
		}
		return result.toString().trim();
	}

	private void color(int packed) {
		color(packed, ((packed>>24)&0xFF)/255f);
	}

	private void color(int packed, float alpha) {
		RenderSystem.setShaderColor(((packed>>16)&0xFF)/255f, ((packed>>8)&0xFF)/255f, ((packed>>0)&0xFF)/255f, alpha);
	}

	@Override
	public void close() {
		if (client.world == null) {
			FabConf.setWorldPath(null);
		}
		if (!FabConf.isEnabled("*.reduced_motion") && !leaving) {
			leaving = true;
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_BARREL_CLOSE, 0.7f));
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_SHROOMLIGHT_PLACE, 2f, 1f));
		} else {
			client.setScreen(parent);
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (sidebarHeight > height) {
			lastSidebarScroll = sidebarScroll;
			sidebarScroll += (sidebarScrollTarget-sidebarScroll)/2;
			if (sidebarScrollTarget < 0) sidebarScrollTarget /= 2;
			float h = sidebarHeight-height;
			if (sidebarScrollTarget > h) sidebarScrollTarget = h+((sidebarScrollTarget-h)/2);
		}
		if (selectedSectionHeight > height-36) {
			lastSelectedSectionScroll = selectedSectionScroll;
			selectedSectionScroll += (selectedSectionScrollTarget-selectedSectionScroll)/2;
			if (selectedSectionScrollTarget < 0) selectedSectionScrollTarget /= 2;
			float h = selectedSectionHeight-(height-36);
			if (selectedSectionScrollTarget > h) selectedSectionScrollTarget = h+((selectedSectionScrollTarget-h)/2);
		}
		if (prevSelectedSectionHeight > height-36) {
			lastPrevSelectedSectionScroll = prevSelectedSectionScroll;
			prevSelectedSectionScroll += (prevSelectedSectionScrollTarget-prevSelectedSectionScroll)/2;
			if (prevSelectedSectionScrollTarget < 0) prevSelectedSectionScrollTarget /= 2;
			float h = prevSelectedSectionHeight-(height-36);
			if (prevSelectedSectionScrollTarget > h) prevSelectedSectionScrollTarget = h+((prevSelectedSectionScrollTarget-h)/2);
		}
		if (tooltipBlinkTicks > 0) {
			tooltipBlinkTicks--;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizon, double amount) {
		if (mouseX <= 120) {
			sidebarScrollTarget -= amount*20;
		} else {
			selectedSectionScrollTarget -= amount*20;
		}
		return super.mouseScrolled(mouseX, mouseY, horizon, amount);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (button == 0) {
			if (mouseX > (width - 120) && mouseY < 16) {
				if (configuringServer) {
					serverAnimateTime = 10 - serverAnimateTime;
					configuringServer = false;
					hasClonked = false;
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE.value(), 0.8f, 1));
				} else if (client.world == null) {
					hasClonked = false;
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE.value(), 0.8f, 1));
					serverAnimateTime = 10 - serverAnimateTime;
					FabConf.setWorldPath(null);
					if (editingWorldPath) {
						editingWorldPath = false;
					} else {
						openWorldSelector();
						return super.mouseClicked(mouseX, mouseY, button);
					}
				} else if (isSingleplayer) {
					hasClonked = false;
					serverAnimateTime = 10 - serverAnimateTime;
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE.value(), 0.8f, 1));
					editingWorldPath = !editingWorldPath;
				} else if (whyCantConfigureServer == null) {
					hasClonked = false;
					serverAnimateTime = 10 - serverAnimateTime;
					configuringServer = true;
					client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE.value(), 1.2f, 1));
				} else {
					playErrorFeedback();
				}
			}

			didClick = true;
		}
		if ("search".equals(selectedSection)) {
			searchField.mouseClicked(mouseX, mouseY, button);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	public class DrawableSave implements SelectionScreen.PreciseDrawable<File> {
		final File file;
		Identifier icon;

		public DrawableSave(File file) {
			this.file = file;
			File iconFile = new File(file, "icon.png");
			this.icon = Identifier.of("fabrication", "worlds/" + this.hashCode());
			if (iconFile.isFile()) {
				try {
					FileInputStream inputStream = new FileInputStream(iconFile);
					NativeImage nativeImage = NativeImage.read(inputStream);
					NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(nativeImage);
					client.getTextureManager().registerTexture(this.icon, nativeImageBackedTexture);
					inputStream.close();
				} catch (Throwable e) {
					FabLog.error("Invalid icon for world {}", e);
					icon = null;
				}
			} else {
				client.getTextureManager().destroyTexture(this.icon);
				icon = null;
			}
		}

		@Override
		public void render(DrawContext drawContext, float x, float y, float delta) {
			drawContext.drawText(textRenderer, file.getName(), (int) (38+x), (int) y, -1, true);
			drawContext.drawText(textRenderer, file.getPath(), (int) (38+x), (int) (y+20), -1, true);
			MatrixStack matrices = drawContext.getMatrices();
			if (icon != null) {
				RenderSystem.setShaderTexture(0, icon);
				matrices.push();
					matrices.translate(x%1, y%1, 0);
					drawContext.drawTexture(icon, (int) x, (int) y, 0, 0, 0, 32, 32, 32, 32);
				matrices.pop();
			}
		}

		@Override
		public int width() {
			return 38 + textRenderer.getWidth(file.getPath());
		}

		@Override
		public int height() {
			return 38;
		}

		@Override
		public File val() {
			return file;
		}
	}

	public void openWorldSelector() {
		try {
			Path savesDir = client.getLevelStorage().getSavesDirectory();
			if (Files.isDirectory(savesDir)) {
				List<DrawableSave> files = Arrays.stream(savesDir.toFile().listFiles()).filter(f -> {
					if (!f.isDirectory()) return false;
					try {
						return !SessionLock.isLocked(f.toPath());
					} catch (Exception var10) {
						return false;
					}
				}).map(DrawableSave::new).collect(Collectors.toList());
				if (files.isEmpty()) return;
				if (files.size() == 1) setWorldMode(files.get(0).val());
				else client.setScreen(new SelectionScreen<>(this, files, this::setWorldMode));
			}
		}catch (Exception e) {
			FabLog.error("Failed to load levels", e);
		}
	}
	public void setWorldMode(Object toFile) {
		editingWorldPath = true;
		FabConf.setWorldPath(((File)toFile).toPath());
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if ("search".equals(selectedSection)) {
			searchField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
		} else {
			mouseDragging = true;
		}
		return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
	}

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		lastMouseX = mouseX;
		lastMouseY = mouseY;
		if ("search".equals(selectedSection)) {
			searchField.mouseMoved(mouseX, mouseY);
		}
		super.mouseMoved(mouseX, mouseY);
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if ("search".equals(selectedSection)) {
			searchField.mouseReleased(mouseX, mouseY, button);
		}
		return super.mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if ("search".equals(selectedSection)) {
			searchField.charTyped(chr, modifiers);
		}
		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		switch (keyCode) {
			case GLFW.GLFW_KEY_PAGE_UP: mouseScrolled(lastMouseX, lastMouseY, 0, 20); break;
			case GLFW.GLFW_KEY_PAGE_DOWN: mouseScrolled(lastMouseX, lastMouseY, 0, -20); break;
			case GLFW.GLFW_KEY_UP: mouseScrolled(lastMouseX, lastMouseY, 0, 2); break;
			case GLFW.GLFW_KEY_DOWN: mouseScrolled(lastMouseX, lastMouseY, 0, -2); break;
		}
		if ("search".equals(selectedSection)) {
			searchField.keyPressed(keyCode, scanCode, modifiers);
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if ("search".equals(selectedSection)) {
			searchField.keyReleased(keyCode, scanCode, modifiers);
		}
		return super.keyReleased(keyCode, scanCode, modifiers);
	}

	private String getVersion() {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerVersion();
		} else {
			return EarlyAgnos.getModVersion();
		}
	}

	private boolean isFailed(String key) {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerFailedConfig().containsKey(key);
		} else {
			return FabConf.isFailed(key);
		}
	}
	private String getFailed(String key) {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerFailedConfig().get(key);
		} else {
			return FabConf.getFailed(key);
		}
	}

	private boolean isValid(String key) {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerTrileanConfig().containsKey(key) ||
					((GetServerConfig)client.getNetworkHandler()).fabrication$getServerStringConfig().containsKey(key);
		} else {
			return FabConf.isValid(key);
		}
	}

	private ConfigValues.ResolvedFeature getResolvedValue(String key) {
		if (configuringServer) {
			return ((GetServerConfig)client.getNetworkHandler()).fabrication$getServerTrileanConfig().getOrDefault(key, ConfigValues.ResolvedFeature.DEFAULT_FALSE);
		} else {
			return FabConf.getResolvedValue(key, editingWorldPath);
		}
	}

	private ConfigValues.Feature getValue(String key) {
		return getResolvedValue(key).feature;
	}

	private boolean isEnabled(String key) {
		return getResolvedValue(key).value;
	}

	private String getRawValue(String key) {
		if (configuringServer) {
			return getValue(key).toString().toLowerCase(Locale.ROOT);
		} else {
			return FabConf.getRawValue(key);
		}
	}

	private void setValue(String key, String value) {
		Set<String> newlyBannedKeys;
		Set<String> newlyUnbannedKeys;

		if (configuringServer) {
			checkServerData();
			newlyBannedKeys = newlyBannedKeysServer;
			newlyUnbannedKeys = newlyUnbannedKeysServer;
		} else {
			newlyBannedKeys = newlyBannedKeysClient;
			newlyUnbannedKeys = newlyUnbannedKeysClient;
		}
		String oldValue = getRawValue(key);
		//TODO count banned world values
		if (!FabConf.isRuntimeConfigurable(key) && !(configuringServer && FeaturesFile.get(key).sides == Sides.CLIENT_ONLY) && !editingWorldPath) {
			if (value.equals("banned")) {
				if (newlyUnbannedKeys.contains(key)) {
					newlyUnbannedKeys.remove(key);
				} else {
					newlyBannedKeys.add(key);
				}
			} else if (oldValue.equals("banned")) {
				if (newlyBannedKeys.contains(key)) {
					newlyBannedKeys.remove(key);
				} else {
					newlyUnbannedKeys.add(key);
				}
			}
		}
		if (configuringServer) {
			PacketByteBuf data = new PacketByteBuf(Unpooled.buffer());
			data.writeVarInt(1);
			data.writeString(key);
			data.writeString(value);
			client.getNetworkHandler().sendPacket(new CustomPayloadC2SPacket(new ByteBufCustomPayload(Identifier.of("fabrication", "config"), data)));
		} else if (editingWorldPath && FabConf.hasWorldPath()) {
			FabConf.worldSet(key, value);
			if (FabricationMod.isAvailableFeature(key)) {
				FabricationMod.updateFeature(key);
			}
		} else {
			FabConf.set(key, value);
			if (FabricationMod.isAvailableFeature(key)) {
				FabricationMod.updateFeature(key);
			}
		}
	}

	public static float sCurve5(float a) {
		float a3 = a * a * a;
		float a4 = a3 * a;
		float a5 = a4 * a;
		return (6 * a5) - (15 * a4) + (10 * a3);
	}

	public void renderOrderedTooltip(DrawContext drawContext, List<? extends OrderedText> lines, int x, int y) {
		if (!lines.isEmpty()) {
			if (bufferTooltips) {
				final int yf = y;
				bufferedTooltips.add(() -> renderOrderedTooltip(drawContext, lines, x, yf));
				return;
			}
			if (y < 20) {
				y += 20;
			}
			int maxWidth = 0;

			for (OrderedText line : lines) {
				int width = textRenderer.getWidth(line);
				if (width > maxWidth) {
					maxWidth = width;
				}
			}

			int innerX = x + 12;
			int innerY = y - 12;
			int totalHeight = 8;
			if (lines.size() > 1) {
				totalHeight += /*2 +*/ (lines.size() - 1) * 10;
			}

			if (innerX + maxWidth > width) {
				innerX -= 28 + maxWidth;
			}

			if (innerY + totalHeight + 6 > height) {
				innerY = height - totalHeight - 6;
			}
			MatrixStack matrices = drawContext.getMatrices();
			matrices.push();
			drawContext.fill(innerX-3, innerY-3, innerX+maxWidth+3, innerY+totalHeight+3, 0xAA000000);
			VertexConsumerProvider.Immediate vcp = drawContext.getVertexConsumers();
			matrices.translate(0, 0, 400);

			for (int i = 0; i < lines.size(); ++i) {
				OrderedText line = lines.get(i);
				if (line != null) {
					textRenderer.draw(line, innerX, innerY, -1, false, matrices.peek().getPositionMatrix(), vcp, TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
				}
				//				if (i == 0) {
				//					innerY += 2;
				//				}
				innerY += 10;
			}

			vcp.draw();
			matrices.pop();
		}
	}

	@FunctionalInterface
	public interface FeatureSubmenu {
		Screen construct(Screen parent, PrideFlagRenderer prideFlag, String title, String configKey);
	}
}
