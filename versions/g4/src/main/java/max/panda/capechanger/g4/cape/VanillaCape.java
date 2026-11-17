package max.panda.capechanger.g4.cape;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public enum VanillaCape {
    ANNIVERSARY_15TH("15th_Anniversary", "15th Anniversary", false, VanillaCape.Category.JAVA),
    AURORA("Aurora", "Aurora", false, VanillaCape.Category.JAVA),
    BIRTHDAY("Birthday", "Birthday", false, VanillaCape.Category.JAVA),
    BUILDER("Builder", "Builder", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    CHERRY("Cherry", "Cherry", false, VanillaCape.Category.JAVA),
    COBALT("Cobalt", "Cobalt", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    COMMON("Common", "Common", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    COPPER("Copper", "Copper", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    CORRUPTED_CREEPER("Corrupted_Creeper", "Corrupted Creeper", true, VanillaCape.Category.DUNGEONS),
    DB("Db", "Db", false, VanillaCape.Category.JAVA),
    FOLLOWERS("Follower_U2019s", "Follower's", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    FOUNDERS("Founder_s", "Founder's", false, VanillaCape.Category.JAVA),
    HERO("Hero", "Hero", false, VanillaCape.Category.JAVA),
    HOME("Home", "Home", false, VanillaCape.Category.JAVA),
    MCC_15TH_YEAR("MCC_15th_Year", "MCC 15th Year", false, VanillaCape.Category.JAVA),
    MENACE("Menace", "Menace", false, VanillaCape.Category.JAVA),
    MIGRATOR("Migrator", "Migrator", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    MILLIONTH_CUSTOMER("Millionth_Customer", "Millionth Customer", false, VanillaCape.Category.JAVA),
    MINECON_2011("Minecon_2011", "Minecon 2011", false, VanillaCape.Category.JAVA),
    MINECON_2012("Minecon_2012", "Minecon 2012", false, VanillaCape.Category.JAVA),
    MINECON_2013("Minecon_2013", "Minecon 2013", false, VanillaCape.Category.JAVA),
    MINECON_2015("Minecon_2015", "Minecon 2015", false, VanillaCape.Category.JAVA),
    MINECON_2016("Minecon_2016", "Minecon 2016", false, VanillaCape.Category.JAVA),
    MINECRAFT_EXPERIENCE("Minecraft_Experience", "Minecraft Experience", false, VanillaCape.Category.JAVA),
    MOJANG("Mojang", "Mojang", false, VanillaCape.Category.JAVA),
    MOJANG_CLASSIC("Mojang_Classic", "Mojang Classic", false, VanillaCape.Category.JAVA),
    MOJANG_OFFICE("Mojang_Office", "Mojang Office", false, VanillaCape.Category.JAVA),
    MOJANG_STUDIOS("Mojang_Studios", "Mojang Studios", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    MOJIRA_MODERATOR("Mojira_Moderator", "Mojira Moderator", false, VanillaCape.Category.JAVA),
    MOONLIGHT_TRAIL("Moonlight_Trail", "Moonlight Trail", false, VanillaCape.Category.JAVA),
    PAN("Pan", "Pan", false, VanillaCape.Category.JAVA),
    PRISMARINE("Prismarine", "Prismarine", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    PROGRESS_PRIDE("Progress_Pride", "Progress Pride", false, VanillaCape.Category.BEDROCK),
    PURPLE_HEART("Purple_Heart", "Purple Heart", false, VanillaCape.Category.JAVA),
    REALMS_MAPMAKER("Realms_Mapmaker", "Realms Mapmaker", false, VanillaCape.Category.JAVA),
    SCROLLS("Scrolls", "Scrolls", false, VanillaCape.Category.JAVA),
    SLICE_SLAYER("Slice_Slayer", "Slice Slayer", true, VanillaCape.Category.DUNGEONS),
    SNOWMAN("Snowman", "Snowman", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    SOUL("Soul", "Soul", true, VanillaCape.Category.DUNGEONS),
    SPADE("Spade", "Spade", false, VanillaCape.Category.JAVA),
    SPECIAL("Special", "Special", false, VanillaCape.Category.JAVA),
    TEST("Test", "Test", false, VanillaCape.Category.JAVA),
    TRANSLATOR("Translator", "Translator", false, VanillaCape.Category.JAVA),
    TRANSLATOR_CHINESE("Translator_Chinese", "Translator (Chinese)", false, VanillaCape.Category.JAVA),
    TRANSLATOR_JAPANESE("Translator_Japanese", "Translator (Japanese)", false, VanillaCape.Category.JAVA),
    TURTLE("Turtle", "Turtle", false, VanillaCape.Category.JAVA),
    TWISTED("Twisted", "Twisted", false, VanillaCape.Category.DUNGEONS),
    VALENTINE("Valentine", "Valentine", false, VanillaCape.Category.JAVA, VanillaCape.Category.BEDROCK),
    VANILLA("Vanilla", "Vanilla", false, VanillaCape.Category.JAVA),
    YEARN("Yearn", "Yearn", false, VanillaCape.Category.DUNGEONS),
    ZOMBIE_HORSE("Zombie_Horse", "Zombie Horse", false, VanillaCape.Category.JAVA);

    private final String id;
    private final String displayName;
    private final boolean missingTexture;
    private final VanillaCape.Category[] categories;
    private final Identifier texture;

    VanillaCape(String nullxx, String nullxxx, boolean nullxxxx, VanillaCape.Category... nullxxxxx) {
        this.id = nullxx;
        this.displayName = nullxxx;
        this.missingTexture = nullxxxx;
        this.categories = nullxxxxx;
        this.texture = Identifier.of("acvcape", "textures/cape/" + nullxx.toLowerCase(Locale.ROOT) + ".png");
    }

    public String getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Identifier getTexture() {
        return this.texture;
    }

    public boolean isMissingTexture() {
        return this.missingTexture;
    }

    public boolean isInCategory(VanillaCape.Category var1) {
        for (VanillaCape.Category var5 : this.categories) {
            if (var5 == var1) {
                return true;
            }
        }

        return false;
    }

    public boolean isDungeonsExclusive() {
        return this.categories.length == 1 && this.categories[0] == VanillaCape.Category.DUNGEONS;
    }

    public static VanillaCape byId(String var0) {
        for (VanillaCape var4 : values()) {
            if (var4.id.equalsIgnoreCase(var0)) {
                return var4;
            }
        }

        return null;
    }

    public enum Category {
        JAVA,
        BEDROCK,
        DUNGEONS;
    }
}
