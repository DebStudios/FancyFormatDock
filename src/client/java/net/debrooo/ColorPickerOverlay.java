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
    private boolean draggingSV1 = false, draggingH1 = false;

    private float hue2a = 0f, sat2a = 1f, val2a = 1f;
    private float hue2b = 0.66f, sat2b = 1f, val2b = 1f;
    private int activeGradPicker = 0;
    private boolean draggingSV2 = false, draggingH2 = false;
    private EditBox messageInput;

    private static final int PW = 290, PH = 270;
    private static final int SV_SIZE = 100, H_W = 14, H_H = SV_SIZE;

    // Vanilla MC button colors (exact like options screen)
    private static final int BTN_BORDER   = 0xFF000000;
    private static final int BTN_FILL     = 0xFF404040;
    private static final int BTN_FILL_HOV = 0xFF606090; // blueish hover
    private static final int BTN_HL       = 0xFFA0A0A0;
    private static final int BTN_SH       = 0xFF202020;
    private static final int BTN_TXT      = 0xFFFFFFFF;
    private static final int BTN_TXT_HOV  = 0xFFFFFFA0; // yellow on hover
    private static final int PANEL_BG     = 0xFF1E1E1E;
    private static final int PANEL_TITLE  = 0xFF2A2A2A;
    private static final int DIVIDER      = 0xFF555555;
    private static final int TXT_LABEL    = 0xFFAAAAAA;

    public ColorPickerOverlay(Screen ignored, ChatScreen chatScreen) {
        super(Component.literal("FancyFormatDock Colors"));
        this.chatScreen = chatScreen;
    }

    @Override
    protected void init() {
        int px = (this.width - PW) / 2, py = (this.height - PH) / 2;
        messageInput = new EditBox(this.font, px + 10, py + 206, PW - 20, 16,
                Component.literal("Message"));
        messageInput.setMaxLength(128);
        messageInput.setValue("hello world");
        this.addWidget(messageInput);
    }

    @Override
    public void render(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        int px = (this.width - PW) / 2, py = (this.height - PH) / 2;
        var font = Minecraft.getInstance().font;

        super.render(ctx, mouseX, mouseY, delta);

        // Dark panel
        ctx.fill(px, py, px + PW, py + PH, PANEL_BG);

        // Title bar
        ctx.fill(px, py, px + PW, py + 20, PANEL_TITLE);
        ctx.fill(px, py + 19, px + PW, py + 20, DIVIDER);
        ctx.drawCenteredString(font, "Custom Color", px + PW / 2, py + 6, BTN_TXT);

        // Tabs
        int half = (PW - 4) / 2;
        renderBtn(ctx, font, px + 2,             py + 22, half, 20, "Single Color", activeTab == 0, isIn(mouseX, mouseY, px + 2, py + 22, half, 20));
        renderBtn(ctx, font, px + 2 + half + 2,  py + 22, half, 20, "Gradient",     activeTab == 1, isIn(mouseX, mouseY, px + 2 + half + 2, py + 22, half, 20));
        ctx.fill(px, py + 43, px + PW, py + 44, DIVIDER);

        if (activeTab == 0) renderSingleTab(ctx, font, px, py, mouseX, mouseY);
        else                renderGradientTab(ctx, font, px, py, mouseX, mouseY);
    }

    private void renderSingleTab(GuiGraphics ctx, net.minecraft.client.gui.Font font, int px, int py, int mx, int my) {
        int svX = px + 10, svY = py + 52, hX = svX + SV_SIZE + 6;

        ctx.fill(svX-1, svY-1, svX+SV_SIZE+1, svY+SV_SIZE+1, BTN_BORDER);
        renderSVBox(ctx, svX, svY, SV_SIZE, SV_SIZE, hue1);
        int cx = svX+(int)(sat1*SV_SIZE), cy = svY+(int)((1-val1)*SV_SIZE);
        ctx.fill(cx-2, cy-2, cx+2, cy+2, 0xFFFFFFFF); ctx.fill(cx-1, cy-1, cx+1, cy+1, 0xFF000000);

        ctx.fill(hX-1, svY-1, hX+H_W+1, svY+H_H+1, BTN_BORDER);
        renderHueBar(ctx, hX, svY, H_W, H_H);
        int hy = svY+(int)(hue1*H_H);
        ctx.fill(hX-2, hy-1, hX+H_W+2, hy+1, 0xFFFFFFFF);

        int preX = hX + H_W + 10;
        int color1 = hsvToRgb(hue1, sat1, val1);
        ctx.fill(preX-1, svY-1, preX+56, svY+56, BTN_BORDER);
        ctx.fill(preX, svY, preX+55, svY+55, 0xFF000000|color1);

        ctx.drawString(font, String.format("#%06X", color1), preX, svY+58, TXT_LABEL, false);
        String preview = getColorCode(String.format("%06X", color1), FormatPanelConfig.formatModeStatic);
        ctx.drawString(font, truncate(preview, 14), preX, svY+70, 0xFF777777, false);

        boolean hovImp = isIn(mx, my, preX, svY+84, 68, 20);
        renderBtn(ctx, font, preX, svY+84, 68, 20, "IMPORT", false, hovImp);
    }

    private void renderGradientTab(GuiGraphics ctx, net.minecraft.client.gui.Font font, int px, int py, int mx, int my) {
        renderBtn(ctx, font, px+10,  py+48, 80, 18, "Color 1", activeGradPicker==0, isIn(mx,my,px+10,py+48,80,18));
        renderBtn(ctx, font, px+100, py+48, 80, 18, "Color 2", activeGradPicker==1, isIn(mx,my,px+100,py+48,80,18));

        float hue=activeGradPicker==0?hue2a:hue2b, sat=activeGradPicker==0?sat2a:sat2b, val=activeGradPicker==0?val2a:val2b;
        int svX=px+10, svY=py+72, hX=svX+SV_SIZE+6;

        ctx.fill(svX-1,svY-1,svX+SV_SIZE+1,svY+SV_SIZE+1,BTN_BORDER);
        renderSVBox(ctx,svX,svY,SV_SIZE,SV_SIZE,hue);
        int cx=svX+(int)(sat*SV_SIZE), cy=svY+(int)((1-val)*SV_SIZE);
        ctx.fill(cx-2,cy-2,cx+2,cy+2,0xFFFFFFFF); ctx.fill(cx-1,cy-1,cx+1,cy+1,0xFF000000);

        ctx.fill(hX-1,svY-1,hX+H_W+1,svY+H_H+1,BTN_BORDER);
        renderHueBar(ctx,hX,svY,H_W,H_H);
        int hy=svY+(int)(hue*H_H);
        ctx.fill(hX-2,hy-1,hX+H_W+2,hy+1,0xFFFFFFFF);

        int preX=hX+H_W+10;
        int c1=hsvToRgb(hue2a,sat2a,val2a), c2=hsvToRgb(hue2b,sat2b,val2b);
        ctx.fill(preX-1,svY-1,  preX+36,svY+36,  BTN_BORDER); ctx.fill(preX,svY,    preX+35,svY+35,  0xFF000000|c1);
        ctx.fill(preX-1,svY+40, preX+36,svY+76,  BTN_BORDER); ctx.fill(preX,svY+40, preX+35,svY+75,  0xFF000000|c2);
        ctx.drawString(font,String.format("#%06X",c1),preX+38,svY+10, TXT_LABEL,false);
        ctx.drawString(font,String.format("#%06X",c2),preX+38,svY+50, TXT_LABEL,false);

        ctx.fill(px+9,py+178,px+PW-9,py+192,BTN_BORDER);
        renderGradientStrip(ctx,px+10,py+179,PW-20,12,c1,c2);

        ctx.drawString(font,"Message:",px+10,py+196,TXT_LABEL,false);
        messageInput.setX(px+10); messageInput.setY(py+206);
        messageInput.render(ctx,0,0,0);

        boolean hovImp=isIn(mx,my,px+PW-82,py+PH-22,74,20);
        renderBtn(ctx,font,px+PW-82,py+PH-22,74,20,"IMPORT ->",false,hovImp);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int btn) {
        int px=(this.width-PW)/2, py=(this.height-PH)/2, half=(PW-4)/2;
        if (isIn(mx,my,px+2,           py+22,half,20)) { activeTab=0; return true; }
        if (isIn(mx,my,px+2+half+2,    py+22,half,20)) { activeTab=1; return true; }

        if (activeTab==0) {
            int svX=px+10,svY=py+52,hX=svX+SV_SIZE+6,preX=hX+H_W+10;
            if (isIn(mx,my,svX,svY,SV_SIZE,SV_SIZE)) { draggingSV1=true; updateSV1(mx,my,svX,svY); return true; }
            if (isIn(mx,my,hX,svY,H_W,H_H))          { draggingH1=true;  updateH1(my,svY); return true; }
            if (isIn(mx,my,preX,svY+84,68,20))        { importSingle(); return true; }
        } else {
            int svX=px+10,svY=py+72,hX=svX+SV_SIZE+6;
            if (isIn(mx,my,px+10, py+48,80,18)) { activeGradPicker=0; return true; }
            if (isIn(mx,my,px+100,py+48,80,18)) { activeGradPicker=1; return true; }
            if (isIn(mx,my,svX,svY,SV_SIZE,SV_SIZE)) { draggingSV2=true; updateSV2(mx,my,svX,svY); return true; }
            if (isIn(mx,my,hX,svY,H_W,H_H))          { draggingH2=true;  updateH2(my,svY); return true; }
            if (isIn(mx,my,px+PW-82,py+PH-22,74,20)) { importGradient(); return true; }
            if (isIn(mx,my,messageInput.getX(),messageInput.getY(),messageInput.getWidth(),16)) { messageInput.setFocused(true); return super.mouseClicked(mx,my,btn); }
        }
        return super.mouseClicked(mx,my,btn);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int btn, double dx, double dy) {
        int px=(this.width-PW)/2, py=(this.height-PH)/2;
        if (activeTab==0) { if(draggingSV1) updateSV1(mx,my,px+10,py+52); if(draggingH1) updateH1(my,py+52); }
        else              { if(draggingSV2) updateSV2(mx,my,px+10,py+72); if(draggingH2) updateH2(my,py+72); }
        return true;
    }

    @Override public boolean mouseReleased(double mx,double my,int btn) { draggingSV1=draggingH1=draggingSV2=draggingH2=false; return super.mouseReleased(mx,my,btn); }

    private void updateSV1(double mx,double my,int svX,int svY){sat1=clamp((float)(mx-svX)/SV_SIZE);val1=1-clamp((float)(my-svY)/SV_SIZE);}
    private void updateH1(double my,int svY){hue1=clamp((float)(my-svY)/H_H);}
    private void updateSV2(double mx,double my,int svX,int svY){float s=clamp((float)(mx-svX)/SV_SIZE),v=1-clamp((float)(my-svY)/SV_SIZE);if(activeGradPicker==0){sat2a=s;val2a=v;}else{sat2b=s;val2b=v;}}
    private void updateH2(double my,int svY){float h=clamp((float)(my-svY)/H_H);if(activeGradPicker==0)hue2a=h;else hue2b=h;}

    private void importSingle() {
        String hex=String.format("%06X",hsvToRgb(hue1,sat1,val1));
        String code=getColorCode(hex,FormatPanelConfig.formatModeStatic);
        Minecraft.getInstance().setScreen(chatScreen);
        Minecraft.getInstance().execute(()->insertIntoChat(code));
    }

    private void importGradient() {
        String msg=messageInput.getValue(); if(msg.isEmpty()) return;
        String hex1=String.format("%06X",hsvToRgb(hue2a,sat2a,val2a)), hex2=String.format("%06X",hsvToRgb(hue2b,sat2b,val2b));
        FormatPanelConfig.FormatMode mode=FormatPanelConfig.formatModeStatic;
        String code=mode==FormatPanelConfig.FormatMode.MiniMessage?"<gradient:#"+hex1+":#"+hex2+">"+msg+"</gradient>":buildLetterGradient(hex1,hex2,msg,mode);
        Minecraft.getInstance().setScreen(chatScreen);
        Minecraft.getInstance().execute(()->insertIntoChat(code));
    }

    private String getColorCode(String hex, FormatPanelConfig.FormatMode mode) {
        if(mode==FormatPanelConfig.FormatMode.MiniMessage) return "<color:#"+hex+">";
        if(mode==FormatPanelConfig.FormatMode.EssentialsX) return "&#"+hex;
        StringBuilder sb=new StringBuilder("§x"); for(char c:hex.toCharArray()) sb.append("§").append(c); return sb.toString();
    }

    private String buildLetterGradient(String hex1,String hex2,String msg,FormatPanelConfig.FormatMode mode){
        int r1=Integer.parseInt(hex1.substring(0,2),16),g1=Integer.parseInt(hex1.substring(2,4),16),b1=Integer.parseInt(hex1.substring(4,6),16);
        int r2=Integer.parseInt(hex2.substring(0,2),16),g2=Integer.parseInt(hex2.substring(2,4),16),b2=Integer.parseInt(hex2.substring(4,6),16);
        StringBuilder sb=new StringBuilder(); int len=msg.length();
        for(int i=0;i<len;i++){float t=len==1?0:(float)i/(len-1);int r=(int)(r1+t*(r2-r1)),g=(int)(g1+t*(g2-g1)),b=(int)(b1+t*(b2-b1));String h=String.format("%02X%02X%02X",r,g,b);if(mode==FormatPanelConfig.FormatMode.EssentialsX)sb.append("&#").append(h);else{sb.append("§x");for(char c:h.toCharArray())sb.append("§").append(c);}sb.append(msg.charAt(i));}
        return sb.toString();
    }

    private void insertIntoChat(String code){
        var f=((net.debrooo.mixin.client.ChatScreenAccessor)chatScreen).getChatField();
        String cur=f.getValue(); int cursor=f.getCursorPosition();
        f.setValue(cur.substring(0,cursor)+code+cur.substring(cursor)); f.setCursorPosition(cursor+code.length());
    }

    private String truncate(String s,int max){return s.length()>max?s.substring(0,max)+"...":s;}

    private void renderBtn(GuiGraphics ctx, net.minecraft.client.gui.Font font,
                           int bx, int by, int bw, int bh, String label, boolean active, boolean hovered) {
        // Outer black border
        ctx.fill(bx, by, bx+bw, by+bh, BTN_BORDER);
        // Fill — blue tint when hovered OR active tab
        int fill = (hovered || active) ? BTN_FILL_HOV : BTN_FILL;
        ctx.fill(bx+1, by+1, bx+bw-1, by+bh-1, fill);
        // Top-left highlight
        ctx.fill(bx+1, by+1, bx+bw-1, by+2, BTN_HL);
        ctx.fill(bx+1, by+1, bx+2, by+bh-1, BTN_HL);
        // Bottom-right shadow
        ctx.fill(bx+1, by+bh-2, bx+bw-1, by+bh-1, BTN_SH);
        ctx.fill(bx+bw-2, by+1, bx+bw-1, by+bh-1, BTN_SH);
        // Text: yellow when hovered, white otherwise
        ctx.drawCenteredString(font, label, bx+bw/2, by+(bh-font.lineHeight)/2, hovered ? BTN_TXT_HOV : BTN_TXT);
    }

    private void renderSVBox(GuiGraphics ctx, int x, int y, int w, int h, float hue) {
        int step = getStep();
        for (int px2 = 0; px2 < w; px2 += step) {
            float s = (float) px2 / w;
            for (int py2 = 0; py2 < h; py2 += step) {
                ctx.fill(x+px2, y+py2, x+px2+step, y+py2+step,
                        0xFF000000 | hsvToRgb(hue, s, 1f-(float)py2/h));
            }
        }
    }

    private void renderHueBar(GuiGraphics ctx, int x, int y, int w, int h) {
        int step = getStep();
        for (int i = 0; i < h; i += step)
            ctx.fill(x, y+i, x+w, y+i+step, 0xFF000000 | hsvToRgb((float)i/h, 1f, 1f));
    }

    private int getStep() {
        switch (FormatPanelConfig.colorPickerQuality) {
            case Low:  return 6;
            case High: return 2;
            default:   return 4;
        }
    }
    private void renderGradientStrip(GuiGraphics ctx,int x,int y,int w,int h,int c1,int c2){int r1=(c1>>16)&0xFF,g1=(c1>>8)&0xFF,b1=c1&0xFF,r2=(c2>>16)&0xFF,g2=(c2>>8)&0xFF,b2=c2&0xFF;for(int i=0;i<w;i++){float t=(float)i/w;ctx.fill(x+i,y,x+i+1,y+h,0xFF000000|((int)(r1+t*(r2-r1))<<16)|((int)(g1+t*(g2-g1))<<8)|(int)(b1+t*(b2-b1)));}}

    private static int hsvToRgb(float h,float s,float v){int hi=(int)(h*6)%6;float f=h*6-(int)(h*6),p=v*(1-s),q=v*(1-f*s),t=v*(1-(1-f)*s),r,g,b;switch(hi){case 0:r=v;g=t;b=p;break;case 1:r=q;g=v;b=p;break;case 2:r=p;g=v;b=t;break;case 3:r=p;g=q;b=v;break;case 4:r=t;g=p;b=v;break;default:r=v;g=p;b=q;break;}return((int)(r*255)<<16)|((int)(g*255)<<8)|(int)(b*255);}
    private static float clamp(float v){return Math.max(0,Math.min(1,v));}
    private boolean isIn(double mx,double my,int x,int y,int w,int h){return mx>=x&&mx<x+w&&my>=y&&my<y+h;}

    @Override public void onClose(){Minecraft.getInstance().setScreen(chatScreen);}
    @Override public boolean isPauseScreen(){return false;}
    @Override public boolean keyPressed(int key,int scan,int mods){if(messageInput!=null&&messageInput.isFocused())return messageInput.keyPressed(key,scan,mods);return super.keyPressed(key,scan,mods);}
    @Override public boolean charTyped(char c,int mods){if(messageInput!=null&&messageInput.isFocused())return messageInput.charTyped(c,mods);return super.charTyped(c,mods);}
}