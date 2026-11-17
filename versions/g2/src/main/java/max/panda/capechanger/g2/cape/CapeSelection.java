package max.panda.capechanger.g2.cape;

public record CapeSelection(CapeSelection.Type type, String hash) {
    public static CapeSelection noneSelection() {
        return new CapeSelection(CapeSelection.Type.NONE, "");
    }

    public static CapeSelection defaultSelection() {
        return new CapeSelection(CapeSelection.Type.DEFAULT, "");
    }

    public static CapeSelection vanillaSelection(String var0) {
        return new CapeSelection(CapeSelection.Type.VANILLA, var0);
    }

    public static CapeSelection customSelection(String var0) {
        return new CapeSelection(CapeSelection.Type.CUSTOM, var0);
    }

    public static CapeSelection ownedSelection(String var0) {
        return new CapeSelection(CapeSelection.Type.OWNED, var0);
    }

    public boolean isNone() {
        return this.type == CapeSelection.Type.NONE;
    }

    public boolean isDefault() {
        return this.type == CapeSelection.Type.DEFAULT;
    }

    public boolean isCustom() {
        return this.type == CapeSelection.Type.CUSTOM;
    }

    public String encode() {
        switch (this.type) {
            case NONE:
                return "none";
            case VANILLA:
                return "vanilla:" + this.hash;
            case CUSTOM:
                return "custom:" + this.hash;
            case DEFAULT:
            default:
                return "default";
            case OWNED:
                return "owned:" + this.hash;
        }
    }

    public static CapeSelection decode(String var0) {
        if (var0 == null || var0.isEmpty()) {
            return defaultSelection();
        } else if (var0.equals("none")) {
            return noneSelection();
        } else if (var0.startsWith("vanilla:")) {
            return vanillaSelection(var0.substring(8));
        } else if (var0.startsWith("custom:")) {
            return customSelection(var0.substring(7));
        } else {
            return var0.startsWith("owned:") ? ownedSelection(var0.substring(6)) : defaultSelection();
        }
    }

    public enum Type {
        NONE,
        VANILLA,
        CUSTOM,
        DEFAULT,
        OWNED;
    }
}
