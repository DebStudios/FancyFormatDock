package net.debrooo.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.components.EditBox;

import java.util.ArrayList;
import java.util.List;

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
			0xFFAA0000, // &4 Dark Red
			0xFFFF5555, // &c Red
			0xFFFFAA00, // &6 Gold
			0xFFFFFF55, // &e Yellow
			0xFF00AA00, // &2 Dark Green
			0xFF55FF55, // &a Green
			0xFF55FFFF, // &b Aqua
			0xFF00AAAA, // &3 Dark Aqua
			0xFF0000AA, // &1 Dark Blue
			0xFF5555FF, // &9 Blue
			0xFFFF55FF, // &d Light Purple
			0xFFAA00AA, // &5 Dark Purple
			0xFFFFFFFF, // &f White
			0xFFAAAAAA, // &7 Gray
			0xFF555555, // &8 Dark Gray
			0xFF000000, // &0 Black
			0xFFC6C6C6, // &l Bold
			0xFFC6C6C6, // &n Underline
			0xFFC6C6C6, // &o Italic
			0xFFC6C6C6, // &r Reset
			0xFFC6C6C6, // &m Strikethrough
			0xFFC6C6C6, // &k Obfuscated
	};

	private static final String[] LABELS = {
			"§r", "§r", "§r", "§r",
			"§r", "§r", "§r", "§r",
			"§r", "§r", "§r", "§r",
			"§r", "§r", "§r", "§r",
			"§lb", "§nu", "§oi", "R",
			"§mS", "§kK"
	};

	private final List<ColorButton> buttons = new ArrayList<>();

	@Override
	public void onInitializeClient() {
		FormatPanelConfig.load();

		ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
			if (screen instanceof ChatScreen chatScreen) {

				// if mod is disabled, don't show buttons
				if (!FormatPanelConfig.enabledStatic) return;

				buttons.clear();

				int btnSize = 20;
				int margin  = 3;
				int cols    = 4;

				int startX = scaledWidth - (cols * (btnSize + margin)) - 6;
				int startY = 11;

				for (int i = 0; i < 20; i++) {
					int col = i % cols;
					int row = i / cols;
					int x = startX + col * (btnSize + margin);
					int y = startY + row * (btnSize + margin);

					final int   idx     = i;
					final int   bgColor = COLORS[i];
					final String label  = LABELS[i];
					final boolean dark  = isDark(bgColor);

					buttons.add(new ColorButton(
							x, y, btnSize, btnSize,
							label, bgColor, dark,
							b -> insertCode(chatScreen, getActiveCodes()[idx])
					));
				}

				// Row 5: S at col 2, K at col 3 (right-aligned)
				int extraY = startY + 5 * (btnSize + margin);
				int xS = startX + 2 * (btnSize + margin);
				int xK = startX + 3 * (btnSize + margin);

				buttons.add(new ColorButton(xS, extraY, btnSize, btnSize,
						LABELS[20], COLORS[20], isDark(COLORS[20]),
						b -> insertCode(chatScreen, getActiveCodes()[20])));

				buttons.add(new ColorButton(xK, extraY, btnSize, btnSize,
						LABELS[21], COLORS[21], isDark(COLORS[21]),
						b -> insertCode(chatScreen, getActiveCodes()[21])));

				ScreenEvents.remove(screen).register(s -> buttons.clear());

				ScreenEvents.afterRender(screen).register(
						(s, context, mouseX, mouseY, tickDelta) -> {
							if (!FormatPanelConfig.enabledStatic) return;
							for (ColorButton btn : buttons) {
								btn.render(context, mouseX, mouseY);
							}
						}
				);

				ScreenMouseEvents.afterMouseClick(screen).register(
						(s, mouseX, mouseY, button) -> {
							if (!FormatPanelConfig.enabledStatic) return;
							for (ColorButton btn : buttons) {
								btn.tryClick(mouseX, mouseY);
							}
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
		double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
		return luminance < 0.5;
	}

	private static class ColorButton {
		private final int x, y, width, height;
		private final String label;
		private final int bgColor;
		private final boolean darkBg;
		private final java.util.function.Consumer<ColorButton> onClick;

		ColorButton(int x, int y, int w, int h, String label, int bgColor, boolean darkBg,
					java.util.function.Consumer<ColorButton> onClick) {
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;
			this.label = label;
			this.bgColor = bgColor;
			this.darkBg = darkBg;
			this.onClick = onClick;
		}

		void render(GuiGraphics ctx, double mouseX, double mouseY) {
			boolean hovered = isHovered(mouseX, mouseY);

			ctx.fill(x, y, x + width, y + height, 0xFF373737);
			ctx.fill(x + 1, y + 1, x + width - 1, y + 2, 0xFF8B8B8B);
			ctx.fill(x + 1, y + 1, x + 2, y + height - 1, 0xFF8B8B8B);
			ctx.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, 0xFF1A1A1A);
			ctx.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, 0xFF1A1A1A);

			int bg = hovered ? brighten(bgColor) : bgColor;
			ctx.fill(x + 2, y + 2, x + width - 2, y + height - 2, bg);

			var textRenderer = Minecraft.getInstance().font;
			int textColor = darkBg ? 0xFFFFFFFF : 0xFF111111;

			ctx.pose().pushPose();
			float scale = 0.9f;
			float cx = x + width / 2f;
			float cy = y + height / 2f;
			ctx.pose().translate(cx, cy, 0);
			ctx.pose().scale(scale, scale, 1f);
			int textW = textRenderer.width(label);
			ctx.drawString(textRenderer, label, -textW / 2, -textRenderer.lineHeight / 2, textColor, false);
			ctx.pose().popPose();
		}

		void tryClick(double mouseX, double mouseY) {
			if (isHovered(mouseX, mouseY)) onClick.accept(this);
		}

		boolean isHovered(double mx, double my) {
			return mx >= x && mx < x + width && my >= y && my < y + height;
		}

		private int brighten(int argb) {
			int a = (argb >> 24) & 0xFF;
			int r = Math.min(255, ((argb >> 16) & 0xFF) + 40);
			int g = Math.min(255, ((argb >> 8) & 0xFF) + 40);
			int b = Math.min(255, (argb & 0xFF) + 40);
			return (a << 24) | (r << 16) | (g << 8) | b;
		}
	}
}