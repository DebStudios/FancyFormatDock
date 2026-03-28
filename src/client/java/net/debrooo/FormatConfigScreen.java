package net.debrooo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class FormatConfigScreen extends Screen {

    private static final String TITLE = "Format Panel Settings";
    private static final String[] MODE_NAMES  = { "Vanilla  §", "EssentialsX  &", "MiniMessage  <>" };
    private static final int[]    MODE_COLORS = { 0xFF55FF55, 0xFF55FFFF, 0xFFFF55FF };

    public FormatConfigScreen() {
        super(Component.literal(TITLE));
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        // Vanilla dirt background
        this.renderBackground(ctx, mouseX, mouseY, delta);

        var font = Minecraft.getInstance().font;

        // Title
        ctx.drawCenteredString(font, TITLE, this.width / 2, 15, 0xFFFFFFFF);

        // Divider under title
        ctx.fill(this.width / 2 - 100, 25, this.width / 2 + 100, 26, 0xFFAAAAAA);

        // Enabled row
        drawRow(ctx, font, mouseX, mouseY, this.width / 2 - 150, 40, 300, 20,
                "Format Panel: " + (FormatPanelConfig.get().enabled ? "§aENABLED" : "§cDISABLED"),
                isHovered(mouseX, mouseY, this.width / 2 - 150, 40, 300, 20));

        // Mode label
        ctx.drawString(font, "Format Mode:", this.width / 2 - 150, 72, 0xFFAAAAAA, false);

        // 3 mode buttons
        for (int i = 0; i < 3; i++) {
            int bx = this.width / 2 - 150 + i * 102;
            int by = 83;
            boolean selected = FormatPanelConfig.get().formatMode == i;
            boolean hovered  = isHovered(mouseX, mouseY, bx, by, 98, 20);
            drawVanillaButton(ctx, font, bx, by, 98, 20, MODE_NAMES[i], selected, hovered, MODE_COLORS[i]);
        }

        // Divider above done button
        ctx.fill(this.width / 2 - 100, this.height - 35, this.width / 2 + 100, this.height - 34, 0xFF555555);

        // Done button
        drawVanillaButton(ctx, font,
                this.width / 2 - 75, this.height - 28, 150, 20,
                "Done", false, isHovered(mouseX, mouseY, this.width / 2 - 75, this.height - 28, 150, 20), 0xFFAAAAAA);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        // Toggle enabled
        if (isHovered(mx, my, this.width / 2 - 150, 40, 300, 20)) {
            FormatPanelConfig.get().enabled = !FormatPanelConfig.get().enabled;
            FormatPanelConfig.save();
            return true;
        }

        // Mode buttons
        for (int i = 0; i < 3; i++) {
            int bx = this.width / 2 - 150 + i * 102;
            if (isHovered(mx, my, bx, 83, 98, 20)) {
                FormatPanelConfig.get().formatMode = i;
                FormatPanelConfig.save();
                return true;
            }
        }

        // Done
        if (isHovered(mx, my, this.width / 2 - 75, this.height - 28, 150, 20)) {
            this.onClose();
            return true;
        }

        return super.mouseClicked(mx, my, button);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    // ---- Helpers ----

    private void drawRow(GuiGraphics ctx, net.minecraft.client.gui.Font font,
                         int mx, int my, int x, int y, int w, int h,
                         String label, boolean hovered) {
        int bg = hovered ? 0xFF3A3A3A : 0xFF2A2A2A;
        ctx.fill(x, y, x + w, y + h, 0xFF000000);
        ctx.fill(x + 1, y + 1, x + w - 1, y + h - 1, bg);
        ctx.fill(x + 1, y + 1, x + w - 1, y + 2, 0xFF666666);
        ctx.fill(x + 1, y + 1, x + 2, y + h - 1, 0xFF666666);
        ctx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, 0xFF111111);
        ctx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, 0xFF111111);
        ctx.drawString(font, label, x + 6, y + (h - font.lineHeight) / 2, 0xFFFFFFFF, false);
    }

    private void drawVanillaButton(GuiGraphics ctx, net.minecraft.client.gui.Font font,
                                   int bx, int by, int bw, int bh,
                                   String label, boolean selected, boolean hovered, int accentColor) {
        int bg = selected ? 0xFF3A5A3A : (hovered ? 0xFF3A3A3A : 0xFF2A2A2A);
        ctx.fill(bx, by, bx + bw, by + bh, 0xFF000000);
        ctx.fill(bx + 1, by + 1, bx + bw - 1, by + bh - 1, bg);
        // highlight
        ctx.fill(bx + 1, by + 1, bx + bw - 1, by + 2,
                selected ? 0xFF88CC88 : 0xFF666666);
        ctx.fill(bx + 1, by + 1, bx + 2, by + bh - 1,
                selected ? 0xFF88CC88 : 0xFF666666);
        // shadow
        ctx.fill(bx + 1, by + bh - 2, bx + bw - 1, by + bh - 1,
                selected ? 0xFF224422 : 0xFF111111);
        ctx.fill(bx + bw - 2, by + 1, bx + bw - 1, by + bh - 1,
                selected ? 0xFF224422 : 0xFF111111);
        // label
        int textColor = selected ? accentColor : 0xFFCCCCCC;
        ctx.drawCenteredString(font, label, bx + bw / 2, by + (bh - font.lineHeight) / 2, textColor);
    }

    private boolean isHovered(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}