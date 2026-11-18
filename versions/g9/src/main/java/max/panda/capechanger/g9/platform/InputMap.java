package max.panda.capechanger.g9.platform;

public final class InputMap {
    private InputMap() {
    }

    public static int toLegacyButton(int var0) {
        return switch (var0) {
            case 1 -> 0;
            case 3 -> 1;
            default -> var0;
        };
    }

    public static int toNativeButton(int var0) {
        return switch (var0) {
            case 0 -> 1;
            case 1 -> 3;
            default -> var0;
        };
    }

    public static int toLegacyKey(int var0) {
        if (var0 >= 4 && var0 <= 29) {
            return 65 + var0 - 4;
        } else if (var0 >= 30 && var0 <= 38) {
            return 49 + var0 - 30;
        }

        return switch (var0) {
            case 39 -> 48;
            case 40 -> 257;
            case 41 -> 256;
            case 42 -> 259;
            case 43 -> 258;
            case 44 -> 32;
            case 45 -> 45;
            case 46 -> 61;
            case 76 -> 261;
            case 79 -> 262;
            case 80 -> 263;
            case 81 -> 264;
            case 82 -> 265;
            case 86 -> 333;
            case 87 -> 334;
            case 88 -> 335;
            case 224 -> 341;
            case 225 -> 340;
            default -> var0;
        };
    }

    public static int toNativeKey(int var0) {
        if (var0 >= 65 && var0 <= 90) {
            return 4 + var0 - 65;
        } else if (var0 >= 49 && var0<= 57) {
            return 30 + var0 - 49;
        }

        return switch (var0) {
            case 48 -> 39;
            case 257 -> 40;
            case 256 -> 41;
            case 259 -> 42;
            case 258 -> 43;
            case 32 -> 44;
            case 61 -> 46;
            case 261 -> 76;
            case 262 -> 79;
            case 263 -> 80;
            case 264 -> 81;
            case 265 -> 82;
            case 333 -> 86;
            case 334 -> 87;
            case 335 -> 88;
            case 341 -> 224;
            case 340 -> 225;
            default -> var0;
        };
    }
}
