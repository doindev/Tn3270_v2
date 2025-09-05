package org.me.telnet.tn3270;

public class FieldAttribute {
    private static final byte PROTECTED_MASK = 0x20;
    private static final byte NUMERIC_MASK = 0x10;
    private static final byte DISPLAY_MASK = 0x0C;
    private static final byte MODIFIED_MASK = 0x01;
    private static final byte AUTOSKIP_MASK = 0x30;
    
    private FieldType type;
    private FieldIntensity intensity;
    private FieldColor color;
    private FieldHighlighting highlighting;
    private boolean modified;
    private boolean numeric;
    private boolean autoSkip;
    private boolean rightJustify;
    private boolean visible;
    
    public FieldAttribute(byte attribute) {
        parseAttribute(attribute);
    }
    
    public FieldAttribute() {
        this.type = FieldType.UNPROTECTED;
        this.intensity = FieldIntensity.NORMAL;
        this.color = FieldColor.DEFAULT;
        this.highlighting = FieldHighlighting.NORMAL;
        this.modified = false;
        this.numeric = false;
        this.autoSkip = false;
        this.rightJustify = false;
        this.visible = true;
    }
    
    private void parseAttribute(byte attribute) {
        boolean isProtected = (attribute & PROTECTED_MASK) != 0;
        this.numeric = (attribute & NUMERIC_MASK) != 0;
        this.autoSkip = (attribute & AUTOSKIP_MASK) == AUTOSKIP_MASK;
        
        if (autoSkip) {
            this.type = FieldType.SKIP_PROTECTED;
        } else if (isProtected) {
            this.type = FieldType.PROTECTED;
        } else {
            this.type = FieldType.UNPROTECTED;
        }
        
        int displayBits = (attribute & DISPLAY_MASK) >> 2;
        switch (displayBits) {
            case 0:
                this.intensity = FieldIntensity.NORMAL;
                this.visible = true;
                break;
            case 1:
                this.intensity = FieldIntensity.HIGH;
                this.visible = true;
                break;
            case 2:
                this.intensity = FieldIntensity.ZERO;
                this.visible = false;
                break;
            case 3:
                this.intensity = FieldIntensity.ZERO;
                this.visible = false;
                break;
        }
        
        this.modified = (attribute & MODIFIED_MASK) != 0;
        this.color = FieldColor.DEFAULT;
        this.highlighting = FieldHighlighting.NORMAL;
        this.rightJustify = false;
    }
    
    public FieldType type() {
        return type;
    }
    
    public FieldAttribute type(FieldType type) {
        this.type = type;
        return this;
    }
    
    public FieldIntensity intensity() {
        return intensity;
    }
    
    public FieldAttribute intensity(FieldIntensity intensity) {
        this.intensity = intensity;
        return this;
    }
    
    public FieldColor color() {
        return color;
    }
    
    public FieldAttribute color(FieldColor color) {
        this.color = color;
        return this;
    }
    
    public FieldHighlighting highlighting() {
        return highlighting;
    }
    
    public FieldAttribute highlighting(FieldHighlighting highlighting) {
        this.highlighting = highlighting;
        return this;
    }
    
    public boolean canInput() {
        return type == FieldType.UNPROTECTED;
    }
    
    public boolean isAutoSkip() {
        return autoSkip || type == FieldType.SKIP_PROTECTED;
    }
    
    public boolean isModified() {
        return modified;
    }
    
    public boolean isNumeric() {
        return numeric;
    }
    
    public boolean isVisible() {
        return visible;
    }
    
    public boolean isRightJustify() {
        return rightJustify;
    }
    
    public FieldAttribute modified(boolean b) {
        this.modified = b;
        return this;
    }
    
    public FieldAttribute numeric(boolean b) {
        this.numeric = b;
        return this;
    }
    
    public FieldAttribute autoSkip(boolean b) {
        this.autoSkip = b;
        return this;
    }
    
    public FieldAttribute rightJustify(boolean b) {
        this.rightJustify = b;
        return this;
    }
    
    public FieldAttribute visible(boolean b) {
        this.visible = b;
        return this;
    }
    
    public byte toAttributeByte() {
        byte result = 0;
        
        if (type == FieldType.SKIP_PROTECTED) {
            result |= AUTOSKIP_MASK;
        } else if (type == FieldType.PROTECTED) {
            result |= PROTECTED_MASK;
        }
        
        if (numeric) {
            result |= NUMERIC_MASK;
        }
        
        if (intensity == FieldIntensity.HIGH) {
            result |= 0x04;
        } else if (!visible || intensity == FieldIntensity.ZERO) {
            result |= 0x0C;
        }
        
        if (modified) {
            result |= MODIFIED_MASK;
        }
        
        return result;
    }
}