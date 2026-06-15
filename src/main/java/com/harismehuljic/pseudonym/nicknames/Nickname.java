package com.harismehuljic.pseudonym.nicknames;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

public class Nickname {
    private final Component realName;
    private final CompoundTag nicknameData = new CompoundTag();

    private boolean dataLoaded = false;

    private Component nickname;
    private Component prefix;
    private ChatFormatting nickColor;
    private ChatFormatting prefixColor;

    private boolean italicizedNick;
    private boolean italicizedPrefix;
    private boolean boldNick;
    private boolean boldPrefix;

    private NicknameLabel nicknameLabel;

    public Nickname(GameProfile profile) {
        this.realName = Component.literal(profile.name());

        this.setNickColor(ChatFormatting.WHITE);
        this.setPrefixColor(ChatFormatting.WHITE);

        this.setItalicizedNick(false);
        this.setItalicizedPrefix(false);
        this.setBoldNick(false);
        this.setBoldPrefix(false);
    }

    public void loadNicknameData(CompoundTag nbt, ServerPlayer spe) {
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

    public Component getFinalStylizedName() {
        MutableComponent formattedName = this.formatName();
        MutableComponent formattedPrefix = this.formatPrefix();

        return formattedPrefix == null ? formattedName : formattedPrefix.append(formattedName);
    }

    private MutableComponent formatName() {
        MutableComponent formattedName = this.nickname != null ? this.nickname.copy().withStyle(this.nickColor) : this.realName.copy().withStyle(this.nickColor);

        if (this.italicizedNick) {
            formattedName.withStyle(ChatFormatting.ITALIC);
        }

        if (this.boldNick) {
            formattedName.withStyle(ChatFormatting.BOLD);
        }

        if (this.italicizedPrefix && !this.italicizedNick) {
            formattedName.setStyle(formattedName.getStyle().withItalic(false));
        }

        if (this.boldPrefix && !this.boldNick) {
            formattedName.setStyle(formattedName.getStyle().withBold(false));
        }

        return formattedName;
    }

    private MutableComponent formatPrefix() {
        if (this.prefix == null) {
            return null;
        }

        MutableComponent formattedPrefix = Component.literal("[")
                .append(this.prefix)
                .append(Component.literal("] "))
                .withStyle(this.prefixColor);

        if (this.italicizedPrefix) {
            formattedPrefix.withStyle(ChatFormatting.ITALIC);
        }

        if (this.boldPrefix) {
            formattedPrefix.withStyle(ChatFormatting.BOLD);
        }

        return formattedPrefix;
    }

    private void setNickname(Component nickname, @Nullable ChatFormatting color, boolean italicized, boolean bold) {
        this.nickname = nickname;
        this.nickColor = color == null ? ChatFormatting.WHITE : color;
        this.italicizedNick = italicized;
        this.boldNick = bold;

        if (this.nickname != null) {
            this.nicknameData.putString("nickname", nickname.getString());
        }

        this.nicknameData.putString("nick_color", color == null ? ChatFormatting.WHITE.getName() : color.getName());
        this.nicknameData.putBoolean("nick_italic", italicized);
        this.nicknameData.putBoolean("nick_bold", bold);

        if (this.dataLoaded) {
            nicknameLabel.updateLabel();
        }
    }

    private void setPrefix(Component prefix, @Nullable ChatFormatting color, boolean italicized, boolean bold) {
        this.prefix = prefix;
        this.prefixColor = color == null ? ChatFormatting.WHITE : color;
        this.italicizedPrefix = italicized;
        this.boldPrefix = bold;

        if (this.prefix != null) {
            this.nicknameData.putString("prefix", this.prefix.getString());
        }

        this.nicknameData.putString("prefix_color", color == null ? ChatFormatting.WHITE.getName() : color.getName());
        this.nicknameData.putBoolean("prefix_italic", italicizedPrefix);
        this.nicknameData.putBoolean("prefix_bold", boldPrefix);

        if (this.dataLoaded) {
            nicknameLabel.updateLabel();
        }
    }

    public void setNickname(String nickname) {
        this.setNickname(Component.literal(nickname), this.nickColor, this.italicizedNick, this.boldNick);
    }

    public void setNickColor(String color) {
        this.setNickname(this.nickname, ChatFormatting.getByName(color), this.italicizedNick, this.boldNick);
    }

    public void setNickname(Component nickname) {
        this.setNickname(nickname, this.nickColor, this.italicizedNick, this.boldNick);
    }

    public void setNickColor(ChatFormatting color) {
        this.setNickname(this.nickname, color, this.italicizedNick, this.boldNick);
    }

    public void setItalicizedNick(boolean italicized) {
        this.setNickname(this.nickname, this.nickColor, italicized, this.boldNick);
    }

    public void setBoldNick(boolean bold) {
        this.setNickname(this.nickname, this.nickColor, this.italicizedNick, bold);
    }

    public void setPrefix(String prefix) {
        this.setPrefix(Component.literal(prefix), this.prefixColor, this.italicizedPrefix, this.boldPrefix);
    }

    public void setPrefixColor(String color) {
        this.setPrefix(this.prefix, ChatFormatting.getByName(color), this.italicizedPrefix, this.boldPrefix);
    }

    public void setPrefix(Component prefix) {
        this.setPrefix(prefix, this.prefixColor, this.italicizedPrefix, this.boldPrefix);
    }

    public void setPrefixColor(ChatFormatting color) {
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

    public Component getRealName() {
        return realName;
    }

    public Component getNickname() {
        return nickname;
    }

    public Component getPrefix() {
        return prefix;
    }

    public ChatFormatting getNickColor() {
        return nickColor;
    }

    public ChatFormatting getPrefixColor() {
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

    public CompoundTag getNicknameData() {
        return nicknameData;
    }
}
