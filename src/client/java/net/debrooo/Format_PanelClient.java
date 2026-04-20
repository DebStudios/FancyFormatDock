package net.debrooo.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Format_PanelClient implements ClientModInitializer {

	private static final String[] CODES_VANILLA = {
			"§4", "§c", "§6", "§e",
			"§2", "§a", "§b", "§3",
			"§1", "§9", "§d", "§5",
			"§f", "§7", "§8", "§0",
			"§l", "§n", "§o", "§r",
			"§m", "§k"
	};

	private static final String[] CODES_ESSENTIALS = {
			"&4", "&c", "&6", "&e",
			"&2", "&a", "&b", "&3",
			"&1", "&9", "&d", "&5",
			"&f", "&7", "&8", "&0",
			"&l", "&n", "&o", "&r",
			"&m", "&k"
	};

	private static final String[] CODES_MINIMESSAGE = {
			"<dark_red>", "<red>", "<gold>", "<yellow>",
			"<dark_green>", "<green>", "<aqua>", "<dark_aqua>",
			"<dark_blue>", "<blue>", "<light_purple>", "<dark_purple>",
			"<white>", "<gray>", "<dark_gray>", "<black>",
			"<bold>", "<underlined>", "<italic>", "<reset>",
			"<strikethrough>", "<obfuscated>"
	};

	private static final int[] COLORS = {
			0xFFAA0000, 0xFFFF5555, 0xFFFFAA00, 0xFFFFFF55,
			0xFF00AA00, 0xFF55FF55, 0xFF55FFFF, 0xFF00AAAA,
			0xFF0000AA, 0xFF5555FF, 0xFFFF55FF, 0xFFAA00AA,
			0xFFFFFFFF, 0xFFAAAAAA, 0xFF555555, 0xFF000000,
			0xFFC6C6C6, 0xFFC6C6C6, 0xFFC6C6C6, 0xFFC6C6C6,
			0xFFC6C6C6, 0xFFC6C6C6,
	};

	private static final String[] LABELS = {
			"§r", "§r", "§r", "§r",
			"§r", "§r", "§r", "§r",
			"§r", "§r", "§r", "§r",
			"§r", "§r", "§r", "§r",
			"§lb", "§nu", "§oi", "R",
			"§mS", "§kK"
	};

	private static final ResourceLocation TOGGLE_ICON =
			ResourceLocation.fromNamespaceAndPath("format_panel", "textures/gui/toggle.png");

	private final List<ColorButton> buttons = new CopyOnWriteArrayList<>();

	@Override
	public void onInitializeClient() {
		FormatPanelConfig.load();

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof ChatScreen chatScreen) {
				buttons.clear();

				// Toggle button — always visible
				int existingButtons = screen.children().stream()
						.filter(w -> w instanceof net.minecraft.client.gui.components.AbstractWidget)
						.map(w -> (net.minecraft.client.gui.components.AbstractWidget) w)
						.filter(w -> w.getY() >= scaledHeight - 30)
						.mapToInt(w -> 1)
						.sum();

				int toggleX = scaledWidth - 2 - (existingButtons * 24);
				int toggleY = scaledHeight - 40;

				buttons.add(new ColorButton(
						toggleX, toggleY, 20, 20,
						"", 0xFF8B8B8B, true,
						b -> {
							FormatPanelConfig.enabledStatic = !FormatPanelConfig.enabledStatic;
							FormatPanelConfig.save();
							Minecraft mc = Minecraft.getInstance();
							mc.execute(() -> mc.setScreen(new ChatScreen("")));
						}
				));

				if (FormatPanelConfig.enabledStatic) {
					int btnSize = 20;
					int margin  = 3;
					int cols    = 4;

					int startX = scaledWidth - (cols * (btnSize + margin)) - 6;
					int startY = 11;

					// Main color buttons
					for (int i = 0; i < 20; i++) {
						int col = i % cols;
						int row = i / cols;
						int x = startX + col * (btnSize + margin);
						int y = startY + row * (btnSize + margin);

						final int idx      = i;
						final int bgColor  = COLORS[i];
						final String label = LABELS[i];
						final boolean dark = isDark(bgColor);

						buttons.add(new ColorButton(
								x, y, btnSize, btnSize,
								label, bgColor, dark,
								b2 -> insertCode(chatScreen, getActiveCodes()[idx])
						));
					}

					// Extra row: +, strikethrough, obfuscated
					int extraY = startY + 5 * (btnSize + margin);
					int plusX  = startX + 1 * (btnSize + margin);

					buttons.add(new ColorButton(plusX, extraY, btnSize, btnSize,
							"+", 0xFF444444, true,
							b2 -> Minecraft.getInstance().execute(() ->
									Minecraft.getInstance().setScreen(
											new ColorPickerOverlay(chatScreen, chatScreen)
									)
							)));

					buttons.add(new ColorButton(startX + 2*(btnSize+margin), extraY, btnSize, btnSize,
							LABELS[20], COLORS[20], isDark(COLORS[20]),
							b2 -> insertCode(chatScreen, getActiveCodes()[20])));
					buttons.add(new ColorButton(startX + 3*(btnSize+margin), extraY, btnSize, btnSize,
							LABELS[21], COLORS[21], isDark(COLORS[21]),
							b2 -> insertCode(chatScreen, getActiveCodes()[21])));

					// Preset color buttons — rendered to the left of the main dock
					if (FormatPanelConfig.presetButtonsEnabled) {
						int presetX = startX - (btnSize + margin);
						int renderedCount = 0;

						for (int i = 0; i < 4; i++) {
							FormatPanelConfig.PresetButton pb = FormatPanelConfig.presetButtons[i];
							if (!pb.enabled) continue;

							int argbColor = (int) Long.parseLong("FF" + pb.hex, 16);
							final String hex = pb.hex;

							buttons.add(new ColorButton(
									presetX,
									startY + renderedCount * (btnSize + margin),
									btnSize, btnSize,
									" ", argbColor, isDark(argbColor),
									b2 -> insertCode(chatScreen, getColorCodeForHex(hex))
							));
							renderedCount++;
						}
					}
				}

				ScreenEvents.remove(screen).register(s -> buttons.clear());

				ScreenEvents.afterRender(screen).register(
						(s, context, mouseX, mouseY, tickDelta) -> {
							for (ColorButton btn : buttons) btn.render(context, (double) mouseX, (double) mouseY);
						}
				);

				ScreenMouseEvents.afterMouseClick(screen).register(
						(s, mouseX, mouseY, button) -> {
							for (ColorButton btn : buttons) btn.tryClick((double) mouseX, (double) mouseY);
						}
				);
			}
		});
	}

	private String[] getActiveCodes() {
		FormatPanelConfig.FormatMode mode = FormatPanelConfig.formatModeStatic;
		if (mode == FormatPanelConfig.FormatMode.Vanilla)     return CODES_VANILLA;
		if (mode == FormatPanelConfig.FormatMode.MiniMessage) return CODES_MINIMESSAGE;
		return CODES_ESSENTIALS;
	}

	private String getColorCodeForHex(String hex) {
		FormatPanelConfig.FormatMode mode = FormatPanelConfig.formatModeStatic;
		if (mode == FormatPanelConfig.FormatMode.MiniMessage) return "<color:#" + hex + ">";
		if (mode == FormatPanelConfig.FormatMode.EssentialsX) return "&#" + hex;
		StringBuilder sb = new StringBuilder("§x");
		for (char c : hex.toCharArray()) sb.append("§").append(c);
		return sb.toString();
	}

	private void insertCode(ChatScreen screen, String code) {
		EditBox chatField = ((net.debrooo.mixin.client.ChatScreenAccessor) screen).getChatField();
		String current = chatField.getValue();
		int cursor = chatField.getCursorPosition();
		String newText = current.substring(0, cursor) + code + current.substring(cursor);
		chatField.setValue(newText);
		chatField.setCursorPosition(cursor + code.length());
	}

	private boolean isDark(int argb) {
		int r = (argb >> 16) & 0xFF;
		int g = (argb >> 8) & 0xFF;
		int b = argb & 0xFF;
		return (0.299*r + 0.587*g + 0.114*b) / 255.0 < 0.5;
	}

	private static class ColorButton {
		private final int x, y, width, height;
		private final String label;
		private final int bgColor;
		private final boolean darkBg;
		private final java.util.function.Consumer<ColorButton> onClick;

		ColorButton(int x, int y, int w, int h, String label, int bgColor, boolean darkBg,
					java.util.function.Consumer<ColorButton> onClick) {
			this.x = x; this.y = y; this.width = w; this.height = h;
			this.label = label; this.bgColor = bgColor;
			this.darkBg = darkBg; this.onClick = onClick;
		}

		void render(GuiGraphics ctx, double mouseX, double mouseY) {
			boolean hovered = isHovered(mouseX, mouseY);
			ctx.fill(x, y, x+width, y+height, 0xFF373737);
			ctx.fill(x+1, y+1, x+width-1, y+2, 0xFF8B8B8B);
			ctx.fill(x+1, y+1, x+2, y+height-1, 0xFF8B8B8B);
			ctx.fill(x+1, y+height-2, x+width-1, y+height-1, 0xFF1A1A1A);
			ctx.fill(x+width-2, y+1, x+width-1, y+height-1, 0xFF1A1A1A);
			ctx.fill(x+2, y+2, x+width-2, y+height-2, hovered ? brighten(bgColor) : bgColor);

			if (label.isEmpty()) {
				ctx.blit(
						TOGGLE_ICON,
						x+2, y+2, 0, 0, width-4, height-4, width-4, height-4
				);
			} else {
				var font = Minecraft.getInstance().font;
				int textColor = darkBg ? 0xFFFFFFFF : 0xFF111111;
				ctx.pose().pushPose();
				ctx.pose().translate(x + width/2f, y + height/2f, 0);
				ctx.pose().scale(0.9f, 0.9f, 1f);
				ctx.drawString(font, label, -font.width(label)/2, -font.lineHeight/2, textColor, false);
				ctx.pose().popPose();
			}
		}

		void tryClick(double mx, double my) { if (isHovered(mx, my)) onClick.accept(this); }
		boolean isHovered(double mx, double my) { return mx>=x && mx<x+width && my>=y && my<y+height; }

		private int brighten(int argb) {
			int a=(argb>>24)&0xFF, r=Math.min(255,((argb>>16)&0xFF)+40),
					g=Math.min(255,((argb>>8)&0xFF)+40), b=Math.min(255,(argb&0xFF)+40);
			return (a<<24)|(r<<16)|(g<<8)|b;
		}
	}
}