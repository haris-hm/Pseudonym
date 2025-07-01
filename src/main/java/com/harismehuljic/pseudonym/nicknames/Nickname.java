package com.harismehuljic.pseudonym.nicknames;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Nickname {
    private final Text realName;
    private final NbtCompound nicknameData = new NbtCompound();

    private boolean dataLoaded = false;

    private Text nickname;
    private Text prefix;
    private Formatting nickColor;
    private Formatting prefixColor;

    private boolean italicizedNick;
    private boolean italicizedPrefix;
    private boolean boldNick;
    private boolean boldPrefix;

    private NicknameLabel nicknameLabel;

    public Nickname(GameProfile profile) {
        this.realName = Text.literal(profile.getName());

        this.setNickColor(Formatting.WHITE);
        this.setPrefixColor(Formatting.WHITE);

        this.setItalicizedNick(false);
        this.setItalicizedPrefix(false);
        this.setBoldNick(false);
        this.setBoldPrefix(false);
    }

    public void loadNicknameData(NbtCompound nbt, ServerPlayerEntity spe) {
        Optional<String> nickname = nbt.getString("nickname");
        Optional<String> nickColor = nbt.getString("nick_color");
        Optional<Boolean> nickItalicized = nbt.getBoolean("nick_italic");
        Optional<Boolean> nickBold = nbt.getBoolean("nick_bold");

        Optional<String> prefix = nbt.getString("prefix");
        Optional<String> prefixColor = nbt.getString("prefix_color");
        Optional<Boolean> prefixItalicized = nbt.getBoolean("prefix_italic");
        Optional<Boolean> prefixBold = nbt.getBoolean("prefix_bold");

        nickname.ifPresent(this::setNickname);
        nickColor.ifPresent(this::setNickColor);
        nickItalicized.ifPresent(this::setItalicizedNick);
        nickBold.ifPresent(this::setBoldNick);

        prefix.ifPresent(this::setPrefix);
        prefixColor.ifPresent(this::setPrefixColor);
        prefixItalicized.ifPresent(this::setItalicizedPrefix);
        prefixBold.ifPresent(this::setBoldPrefix);

        this.nicknameLabel = new NicknameLabel(spe);
        this.nicknameLabel.createCustomLabel();
        this.dataLoaded = true;
    }

    public void removeNick() {
        this.nickname = null;
        this.nicknameData.remove("nickname");
        this.nicknameLabel.updateLabel();
    }

    public void removePrefix() {
        this.prefix = null;
        this.nicknameData.remove("prefix");
        this.nicknameLabel.updateLabel();
    }

    // Getters & Setters

    public Text getFinalStylizedName() {
        MutableText formattedName = this.formatName();
        MutableText formattedPrefix = this.formatPrefix();

        return formattedPrefix == null ? formattedName : formattedPrefix.append(formattedName);
    }

    private MutableText formatName() {
        MutableText formattedName = this.nickname != null ? this.nickname.copy().formatted(this.nickColor) : this.realName.copy().formatted(this.nickColor);

        if (this.italicizedNick) {
            formattedName.formatted(Formatting.ITALIC);
        }

        if (this.boldNick) {
            formattedName.formatted(Formatting.BOLD);
        }

        if (this.italicizedPrefix && !this.italicizedNick) {
            formattedName.setStyle(formattedName.getStyle().withItalic(false));
        }

        if (this.boldPrefix && !this.boldNick) {
            formattedName.setStyle(formattedName.getStyle().withBold(false));
        }

        return formattedName;
    }

    private MutableText formatPrefix() {
        if (this.prefix == null) {
            return null;
        }

        MutableText formattedPrefix = Text.literal("[")
                .append(this.prefix)
                .append(Text.literal("] "))
                .formatted(this.prefixColor);

        if (this.italicizedPrefix) {
            formattedPrefix.formatted(Formatting.ITALIC);
        }

        if (this.boldPrefix) {
            formattedPrefix.formatted(Formatting.BOLD);
        }

        return formattedPrefix;
    }

    private void setNickname(Text nickname, @Nullable Formatting color, boolean italicized, boolean bold) {
        this.nickname = nickname;
        this.nickColor = color == null ? Formatting.WHITE : color;
        this.italicizedNick = italicized;
        this.boldNick = bold;

        if (this.nickname != null) {
            this.nicknameData.putString("nickname", nickname.getString());
        }

        this.nicknameData.putString("nick_color", color == null ? Formatting.WHITE.getName() : color.getName());
        this.nicknameData.putBoolean("nick_italic", italicized);
        this.nicknameData.putBoolean("nick_bold", bold);

        if (this.dataLoaded) {
            nicknameLabel.updateLabel();
        }
    }

    private void setPrefix(Text prefix, @Nullable Formatting color, boolean italicized, boolean bold) {
        this.prefix = prefix;
        this.prefixColor = color == null ? Formatting.WHITE : color;
        this.italicizedPrefix = italicized;
        this.boldPrefix = bold;

        if (this.prefix != null) {
            this.nicknameData.putString("prefix", this.prefix.getString());
        }

        this.nicknameData.putString("prefix_color", color == null ? Formatting.WHITE.getName() : color.getName());
        this.nicknameData.putBoolean("prefix_italic", italicizedPrefix);
        this.nicknameData.putBoolean("prefix_bold", boldPrefix);

        if (this.dataLoaded) {
            nicknameLabel.updateLabel();
        }
    }

    public void setNickname(String nickname) {
        this.setNickname(Text.literal(nickname), this.nickColor, this.italicizedNick, this.boldNick);
    }

    public void setNickColor(String color) {
        this.setNickname(this.nickname, Formatting.byName(color), this.italicizedNick, this.boldNick);
    }

    public void setNickname(Text nickname) {
        this.setNickname(nickname, this.nickColor, this.italicizedNick, this.boldNick);
    }

    public void setNickColor(Formatting color) {
        this.setNickname(this.nickname, color, this.italicizedNick, this.boldNick);
    }

    public void setItalicizedNick(boolean italicized) {
        this.setNickname(this.nickname, this.nickColor, italicized, this.boldNick);
    }

    public void setBoldNick(boolean bold) {
        this.setNickname(this.nickname, this.nickColor, this.italicizedNick, bold);
    }

    public void setPrefix(String prefix) {
        this.setPrefix(Text.literal(prefix), this.prefixColor, this.italicizedPrefix, this.boldPrefix);
    }

    public void setPrefixColor(String color) {
        this.setPrefix(this.prefix, Formatting.byName(color), this.italicizedPrefix, this.boldPrefix);
    }

    public void setPrefix(Text prefix) {
        this.setPrefix(prefix, this.prefixColor, this.italicizedPrefix, this.boldPrefix);
    }

    public void setPrefixColor(Formatting color) {
        this.setPrefix(this.prefix, color, this.italicizedPrefix, this.boldPrefix);
    }

    public void setItalicizedPrefix(boolean italicized) {
        this.setPrefix(this.prefix, this.prefixColor, italicized, this.boldPrefix);
    }

    public void setBoldPrefix(boolean bold) {
        this.setPrefix(this.prefix, this.prefixColor, this.italicizedPrefix, bold);
    }

    public void setNicknameLabel(NicknameLabel nicknameLabel) {
        this.nicknameLabel = nicknameLabel;
    }

    public Text getRealName() {
        return realName;
    }

    public Text getNickname() {
        return nickname;
    }

    public Text getPrefix() {
        return prefix;
    }

    public Formatting getNickColor() {
        return nickColor;
    }

    public Formatting getPrefixColor() {
        return prefixColor;
    }

    public boolean isItalicizedNick() {
        return italicizedNick;
    }

    public boolean isItalicizedPrefix() {
        return italicizedPrefix;
    }

    public boolean isBoldNick() {
        return boldNick;
    }

    public boolean isBoldPrefix() {
        return boldPrefix;
    }

    public NicknameLabel getNicknameLabel() {
        return nicknameLabel;
    }

    public NbtCompound getNicknameData() {
        return nicknameData;
    }
}
