package net.debrooo.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ColorPickerOverlay extends Screen {

    private final ChatScreen chatScreen;

    private int activeTab = 0;

    private float hue1 = 0f, sat1 = 1f, val1 = 1f;
    private boolean draggingSV1 = false;
    private boolean draggingH1  = false;

    private float hue2a = 0f,    sat2a = 1f, val2a = 1f;
    private float hue2b = 0.66f, sat2b = 1f, val2b = 1f;
    private int   activeGradPicker = 0;
    private boolean draggingSV2 = false;
    private boolean draggingH2  = false;
    private EditBox messageInput;

    private static final int PW = 280;
    private static final int PH = 260;
    private static final int SV_SIZE = 100;
    private static final int H_W    = 14;
    private static final int H_H    = SV_SIZE;

    public ColorPickerOverlay(Screen ignored, ChatScreen chatScreen) {
        super(Component.literal("FancyFormatDock Colors"));
        this.chatScreen = chatScreen;
    }

    @Override
    protected void init() {
        int px = (this.width  - PW) / 2;
        int py = (this.height - PH) / 2;

        messageInput = new EditBox(this.font, px + 10, py + 190, PW - 20, 16,
                Component.literal("Message for gradient"));
        messageInput.setMaxLength(128);
        messageInput.setValue("message (long messages wont fit in chat)");
        this.addWidget(messageInput);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        // super.render LAST — prevents vanilla blur
        int px = (this.width  - PW) / 2;
        int py = (this.height - PH) / 2;
        var font = Minecraft.getInstance().font;

        // Panel bg
        ctx.fill(px, py, px + PW, py + PH, 0xFF1E1E1E);
        ctx.fill(px, py, px + PW, py + 1,  0xFF555555);
        ctx.fill(px, py + PH - 1, px + PW, py + PH, 0xFF333333);
        ctx.fill(px, py, px + 1, py + PH,  0xFF555555);
        ctx.fill(px + PW - 1, py, px + PW, py + PH,  0xFF333333);

        // Title
        ctx.fill(px + 1, py + 1, px + PW - 1, py + 14, 0xFF111111);
        ctx.drawCenteredString(font, "§7Custom Color", px + PW / 2, py + 3, 0xFFFFFFFF);

        // Close X
        boolean hovX = isIn(mouseX, mouseY, px + PW - 16, py + 2, 13, 11);
        renderBtn(ctx, font, px + PW - 16, py + 2, 13, 11, "X", hovX);

        // Tabs
        renderTab(ctx, font, px + 1,   py + 15, 139, 14, "Single Color", activeTab == 0,
                isIn(mouseX, mouseY, px + 1, py + 15, 139, 14));
        renderTab(ctx, font, px + 140, py + 15, 139, 14, "Gradient",     activeTab == 1,
                isIn(mouseX, mouseY, px + 140, py + 15, 139, 14));

        if (activeTab == 0) renderSingleTab(ctx, font, px, py, mouseX, mouseY);
        else                renderGradientTab(ctx, font, px, py, mouseX, mouseY);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void renderSingleTab(GuiGraphics ctx, net.minecraft.client.gui.Font font,
                                 int px, int py, int mouseX, int mouseY) {
        int svX = px + 10, svY = py + 36;
        int hX  = svX + SV_SIZE + 6, hY = svY;

        renderSVBox(ctx, svX, svY, SV_SIZE, SV_SIZE, hue1);
        int cx = svX + (int)(sat1 * SV_SIZE);
        int cy = svY + (int)((1 - val1) * SV_SIZE);
        ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0xFFFFFFFF);
        ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFF000000);

        renderHueBar(ctx, hX, hY, H_W, H_H);
        int hy = hY + (int)(hue1 * H_H);
        ctx.fill(hX - 1, hy - 1, hX + H_W + 1, hy + 1, 0xFFFFFFFF);

        int color1 = hsvToRgb(hue1, sat1, val1);
        int preX = hX + H_W + 8;
        ctx.fill(preX, svY, preX + 40, svY + 40, 0xFF373737);
        ctx.fill(preX + 1, svY + 1, preX + 39, svY + 39, 0xFF000000 | color1);

        String hex1 = String.format("#%06X", color1);
        ctx.drawString(font, hex1, preX, svY + 44, 0xFFAAAAAA, false);

        // Show what will be inserted based on mode
        FormatPanelConfig.FormatMode mode = FormatPanelConfig.formatModeStatic;
        String preview = getColorCode(String.format("%06X", color1), mode);
        ctx.drawString(font, "§8" + truncate(preview, 18), preX, svY + 54, 0xFF666666, false);

        boolean hovImp = isIn(mouseX, mouseY, preX, svY + 66, 55, 16);
        renderBtn(ctx, font, preX, svY + 66, 55, 16, "IMPORT", hovImp);
    }

    private void renderGradientTab(GuiGraphics ctx, net.minecraft.client.gui.Font font,
                                   int px, int py, int mouseX, int mouseY) {
        boolean hovL = isIn(mouseX, mouseY, px + 10, py + 33, 60, 12);
        boolean hovR = isIn(mouseX, mouseY, px + 80, py + 33, 60, 12);
        renderBtn(ctx, font, px + 10, py + 33, 60, 12,
                activeGradPicker == 0 ? "§aColor 1" : "Color 1", hovL);
        renderBtn(ctx, font, px + 80, py + 33, 60, 12,
                activeGradPicker == 1 ? "§aColor 2" : "Color 2", hovR);

        float hue = activeGradPicker == 0 ? hue2a : hue2b;
        float sat = activeGradPicker == 0 ? sat2a : sat2b;
        float val = activeGradPicker == 0 ? val2a : val2b;

        int svX = px + 10, svY = py + 50;
        int hX  = svX + SV_SIZE + 6, hY = svY;

        renderSVBox(ctx, svX, svY, SV_SIZE, SV_SIZE, hue);
        int cx = svX + (int)(sat * SV_SIZE);
        int cy = svY + (int)((1 - val) * SV_SIZE);
        ctx.fill(cx - 2, cy - 2, cx + 2, cy + 2, 0xFFFFFFFF);
        ctx.fill(cx - 1, cy - 1, cx + 1, cy + 1, 0xFF000000);

        renderHueBar(ctx, hX, hY, H_W, H_H);
        int hy = hY + (int)(hue * H_H);
        ctx.fill(hX - 1, hy - 1, hX + H_W + 1, hy + 1, 0xFFFFFFFF);

        int preX = hX + H_W + 8;
        int c1 = hsvToRgb(hue2a, sat2a, val2a);
        int c2 = hsvToRgb(hue2b, sat2b, val2b);
        ctx.fill(preX, svY, preX + 25, svY + 25, 0xFF373737);
        ctx.fill(preX+1, svY+1, preX+24, svY+24, 0xFF000000|c1);
        ctx.fill(preX + 30, svY, preX + 55, svY + 25, 0xFF373737);
        ctx.fill(preX+31, svY+1, preX+54, svY+24, 0xFF000000|c2);

        ctx.drawString(font, String.format("#%06X", c1), preX,      svY + 28, 0xFFAAAAAA, false);
        ctx.drawString(font, String.format("#%06X", c2), preX + 30, svY + 28, 0xFFAAAAAA, false);

        renderGradientStrip(ctx, px + 10, py + 155, PW - 20, 12, c1, c2);

        ctx.drawString(font, "§8Message:", px + 10, py + 173, 0xFF888888, false);
        messageInput.setX(px + 10);
        messageInput.setY(py + 182);
        messageInput.render(ctx, 0, 0, 0);

        boolean hovImp = isIn(mouseX, mouseY, px + PW - 65, py + PH - 22, 58, 16);
        renderBtn(ctx, font, px + PW - 65, py + PH - 22, 58, 16, "IMPORT →", hovImp);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int px = (this.width  - PW) / 2;
        int py = (this.height - PH) / 2;

        if (isIn(mx, my, px + PW - 16, py + 2, 13, 11)) { onClose(); return true; }
        if (isIn(mx, my, px + 1, py + 15, 139, 14))     { activeTab = 0; return true; }
        if (isIn(mx, my, px + 140, py + 15, 139, 14))   { activeTab = 1; return true; }

        if (activeTab == 0) {
            int svX = px + 10, svY = py + 36;
            int hX  = svX + SV_SIZE + 6;
            int preX = hX + H_W + 8;

            if (isIn(mx, my, svX, svY, SV_SIZE, SV_SIZE)) { draggingSV1 = true; updateSV1(mx, my, svX, svY); return true; }
            if (isIn(mx, my, hX, svY, H_W, H_H))          { draggingH1  = true; updateH1(my, svY); return true; }
            if (isIn(mx, my, preX, svY + 66, 55, 16))     { importSingle(); return true; }
        } else {
            int svX = px + 10, svY = py + 50;
            int hX  = svX + SV_SIZE + 6;

            if (isIn(mx, my, px + 10, py + 33, 60, 12))   { activeGradPicker = 0; return true; }
            if (isIn(mx, my, px + 80, py + 33, 60, 12))   { activeGradPicker = 1; return true; }
            if (isIn(mx, my, svX, svY, SV_SIZE, SV_SIZE)) { draggingSV2 = true; updateSV2(mx, my, svX, svY); return true; }
            if (isIn(mx, my, hX, svY, H_W, H_H))          { draggingH2  = true; updateH2(my, svY); return true; }
            if (isIn(mx, my, px + PW - 65, py + PH - 22, 58, 16)) { importGradient(); return true; }

            if (isIn(mx, my, messageInput.getX(), messageInput.getY(), messageInput.getWidth(), 16)) {
                messageInput.setFocused(true);
                return super.mouseClicked(mx, my, btn);
            }
        }
        return super.mouseClicked(mx, my, btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        int px = (this.width  - PW) / 2;
        int py = (this.height - PH) / 2;
        if (activeTab == 0) {
            if (draggingSV1) updateSV1(mx, my, px + 10, py + 36);
            if (draggingH1)  updateH1(my, py + 36);
        } else {
            if (draggingSV2) updateSV2(mx, my, px + 10, py + 50);
            if (draggingH2)  updateH2(my, py + 50);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int btn) {
        draggingSV1 = draggingH1 = draggingSV2 = draggingH2 = false;
        return super.mouseReleased(mx, my, btn);
    }

    private void updateSV1(double mx, double my, int svX, int svY) {
        sat1 = clamp((float)(mx - svX) / SV_SIZE);
        val1 = 1 - clamp((float)(my - svY) / SV_SIZE);
    }
    private void updateH1(double my, int svY) { hue1 = clamp((float)(my - svY) / H_H); }
    private void updateSV2(double mx, double my, int svX, int svY) {
        float s = clamp((float)(mx - svX) / SV_SIZE);
        float v = 1 - clamp((float)(my - svY) / SV_SIZE);
        if (activeGradPicker == 0) { sat2a = s; val2a = v; } else { sat2b = s; val2b = v; }
    }
    private void updateH2(double my, int svY) {
        float h = clamp((float)(my - svY) / H_H);
        if (activeGradPicker == 0) hue2a = h; else hue2b = h;
    }

    // ── Import ─────────────────────────────────────────────────────────────

    private void importSingle() {
        String hex = String.format("%06X", hsvToRgb(hue1, sat1, val1));
        insertIntoChat(getColorCode(hex, FormatPanelConfig.formatModeStatic));
        onClose();
    }

    private void importGradient() {
        String msg = messageInput.getValue();
        if (msg.isEmpty()) return;
        String hex1 = String.format("%06X", hsvToRgb(hue2a, sat2a, val2a));
        String hex2 = String.format("%06X", hsvToRgb(hue2b, sat2b, val2b));
        FormatPanelConfig.FormatMode mode = FormatPanelConfig.formatModeStatic;
        String code = mode == FormatPanelConfig.FormatMode.MiniMessage
                ? "<gradient:#" + hex1 + ":#" + hex2 + ">" + msg + "</gradient>"
                : buildLetterGradient(hex1, hex2, msg, mode);
        insertIntoChat(code);
        onClose();
    }

    private String getColorCode(String hex, FormatPanelConfig.FormatMode mode) {
        if (mode == FormatPanelConfig.FormatMode.MiniMessage)  return "<color:#" + hex + ">";
        if (mode == FormatPanelConfig.FormatMode.EssentialsX)  return "&#" + hex;
        // Vanilla §x format
        StringBuilder sb = new StringBuilder("§x");
        for (char c : hex.toCharArray()) sb.append("§").append(c);
        return sb.toString();
    }

    private String buildLetterGradient(String hex1, String hex2, String msg,
                                       FormatPanelConfig.FormatMode mode) {
        int r1=Integer.parseInt(hex1.substring(0,2),16), g1=Integer.parseInt(hex1.substring(2,4),16), b1=Integer.parseInt(hex1.substring(4,6),16);
        int r2=Integer.parseInt(hex2.substring(0,2),16), g2=Integer.parseInt(hex2.substring(2,4),16), b2=Integer.parseInt(hex2.substring(4,6),16);
        StringBuilder sb = new StringBuilder();
        int len = msg.length();
        for (int i = 0; i < len; i++) {
            float t = len == 1 ? 0 : (float) i / (len - 1);
            int r=(int)(r1+t*(r2-r1)), g=(int)(g1+t*(g2-g1)), b=(int)(b1+t*(b2-b1));
            String h = String.format("%02X%02X%02X", r, g, b);
            if (mode == FormatPanelConfig.FormatMode.EssentialsX) sb.append("&#").append(h);
            else { sb.append("§x"); for (char c : h.toCharArray()) sb.append("§").append(c); }
            sb.append(msg.charAt(i));
        }
        return sb.toString();
    }

    private void insertIntoChat(String code) {
        var chatField = ((net.debrooo.mixin.client.ChatScreenAccessor) chatScreen).getChatField();
        String cur = chatField.getValue();
        int cursor = chatField.getCursorPosition();
        chatField.setValue(cur.substring(0, cursor) + code + cur.substring(cursor));
        chatField.setCursorPosition(cursor + code.length());
    }

    private String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    // ── Drawing helpers ────────────────────────────────────────────────────

    private void renderSVBox(GuiGraphics ctx, int x, int y, int w, int h, float hue) {
        for (int px2 = 0; px2 < w; px2++) {
            float s = (float) px2 / w;
            for (int py2 = 0; py2 < h; py2++) {
                float v = 1f - (float) py2 / h;
                ctx.fill(x+px2, y+py2, x+px2+1, y+py2+1, 0xFF000000 | hsvToRgb(hue, s, v));
            }
        }
    }

    private void renderHueBar(GuiGraphics ctx, int x, int y, int w, int h) {
        for (int i = 0; i < h; i++) {
            ctx.fill(x, y+i, x+w, y+i+1, 0xFF000000 | hsvToRgb((float)i/h, 1f, 1f));
        }
    }

    private void renderGradientStrip(GuiGraphics ctx, int x, int y, int w, int h, int c1, int c2) {
        int r1=(c1>>16)&0xFF, g1=(c1>>8)&0xFF, b1=c1&0xFF;
        int r2=(c2>>16)&0xFF, g2=(c2>>8)&0xFF, b2=c2&0xFF;
        for (int i = 0; i < w; i++) {
            float t = (float)i/w;
            int r=(int)(r1+t*(r2-r1)), g=(int)(g1+t*(g2-g1)), b=(int)(b1+t*(b2-b1));
            ctx.fill(x+i, y, x+i+1, y+h, 0xFF000000|(r<<16)|(g<<8)|b);
        }
    }

    private void renderBtn(GuiGraphics ctx, net.minecraft.client.gui.Font font,
                           int bx, int by, int bw, int bh, String label, boolean hovered) {
        ctx.fill(bx, by, bx+bw, by+bh, 0xFF000000);
        ctx.fill(bx+1, by+1, bx+bw-1, by+bh-1, hovered ? 0xFF3A3A3A : 0xFF2A2A2A);
        ctx.fill(bx+1, by+1, bx+bw-1, by+2, hovered ? 0xFF888888 : 0xFF555555);
        ctx.fill(bx+1, by+1, bx+2, by+bh-1, hovered ? 0xFF888888 : 0xFF555555);
        ctx.fill(bx+1, by+bh-2, bx+bw-1, by+bh-1, 0xFF111111);
        ctx.fill(bx+bw-2, by+1, bx+bw-1, by+bh-1, 0xFF111111);
        ctx.drawCenteredString(font, label, bx+bw/2, by+(bh-font.lineHeight)/2, 0xFFCCCCCC);
    }

    private void renderTab(GuiGraphics ctx, net.minecraft.client.gui.Font font,
                           int bx, int by, int bw, int bh, String label, boolean active, boolean hovered) {
        ctx.fill(bx, by, bx+bw, by+bh, active ? 0xFF2A2A2A : 0xFF1A1A1A);
        ctx.fill(bx, by+bh-1, bx+bw, by+bh, active ? 0xFF2A2A2A : 0xFF555555);
        ctx.drawCenteredString(font, active ? "§f"+label : "§7"+label, bx+bw/2, by+(bh-font.lineHeight)/2, 0xFFFFFFFF);
    }

    // ── Util ───────────────────────────────────────────────────────────────

    private static int hsvToRgb(float h, float s, float v) {
        int hi = (int)(h * 6) % 6;
        float f = h*6-(int)(h*6), p=v*(1-s), q=v*(1-f*s), t=v*(1-(1-f)*s);
        float r,g,b;
        switch(hi){case 0:r=v;g=t;b=p;break;case 1:r=q;g=v;b=p;break;case 2:r=p;g=v;b=t;break;case 3:r=p;g=q;b=v;break;case 4:r=t;g=p;b=v;break;default:r=v;g=p;b=q;break;}
        return ((int)(r*255)<<16)|((int)(g*255)<<8)|(int)(b*255);
    }

    private static float clamp(float v) { return Math.max(0, Math.min(1, v)); }
    private boolean isIn(double mx, double my, int x, int y, int w, int h) { return mx>=x&&mx<x+w&&my>=y&&my<y+h; }

    @Override public void onClose() { Minecraft.getInstance().setScreen(chatScreen); }
    @Override public boolean isPauseScreen() { return false; }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (messageInput != null && messageInput.isFocused()) return messageInput.keyPressed(key, scan, mods);
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean charTyped(char c, int mods) {
        if (messageInput != null && messageInput.isFocused()) return messageInput.charTyped(c, mods);
        return super.charTyped(c, mods);
    }
}