package com.wiilink24.bot.commands.misc;

import com.wiilink24.bot.Bot;
import com.wiilink24.bot.Database;
import com.wiilink24.bot.WiiLinkBot;
import com.wiilink24.bot.utils.CodeType;
import com.wiilink24.bot.utils.FormatUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;

public class CodeHandler {
    private final Database db = new Database();

    public void codeHandler(SlashCommandInteractionEvent event) throws SQLException {
        String subcommand = event.getSubcommandName();

        switch (Objects.requireNonNull(subcommand)) {
            case "lookup" -> {
                User user = event.getOption("user").getAsUser();
                String name = user.getName();

                EmbedBuilder embed = new EmbedBuilder();
                embed.setAuthor("Profile for " + name, null, user.getEffectiveAvatarUrl());

                Map<CodeType, Map<String, String>> userCodes = db.getAllCodes(user.getIdLong());
                for (Map.Entry<CodeType, Map<String, String>> typeData : userCodes.entrySet()) {
                    Map<String, String> codes = typeData.getValue();
                    if (!(codes.isEmpty())) {
                        embed.addField(typeData.getKey().getFormattedName(), FormatUtil.getCodeLayout(codes), true);
                    }
                }
                if (embed.getFields().isEmpty()) {
                    event.reply("**" + name + "** has not added any codes!").setEphemeral(true).queue();
                    return;
                }

                event.replyEmbeds(embed.build()).queue();
            }
            case "add" -> {
                CodeType codeType = CodeType.fromCode(event.getOption("type").getAsString());
                long authorId = event.getMember().getIdLong();
                Map<String, String> codeTypes = db.getCodesForType(codeType, authorId);
                String code = event.getOption("code").getAsString();

                if (codeTypes != null) {
                    if (codeTypes.containsKey(code)) {
                        event.reply("You already added this code!").setEphemeral(true).queue();
                        return;
                    }
                }

                String name = event.getOption("name").getAsString();

                db.addCode(codeType, authorId, code, name);
                event.replyFormat("Added a code for %s. \n\nName:%s\nCode:`%s`", codeType.getDisplayName(), name, code).queue();
            }
            case "edit" -> {
                CodeType codeType = CodeType.fromCode(event.getOption("type").getAsString());
                long authorId = event.getMember().getIdLong();
                Map<String, String> codeTypes = db.getCodesForType(codeType, authorId);
                String code = event.getOption("code").getAsString();
                String name = event.getOption("name").getAsString();

                if (!(codeTypes.containsKey(name))) {
                    event.reply("A code for `" + name + "` is not registered.").setEphemeral(true).queue();
                    return;
                }

                db.editCode(codeType, authorId, code, name);
                event.reply("Edited the code for `" + name + "`").setEphemeral(true).queue();
            }
            case "remove" -> {

                CodeType codeType = CodeType.fromCode(event.getOption("type").getAsString());
                long authorId = event.getMember().getIdLong();
                Map<String, String> codeTypes = db.getCodesForType(codeType, authorId);
                String name = event.getOption("name").getAsString();

                if (!(codeTypes.containsKey(name))) {
                    event.reply("A code for `" + name + "` is not registered.").setEphemeral(true).queue();
                    return;
                }

                db.removeCode(codeType, authorId, name);
                event.reply("Removed the code for `" + name + "`").setEphemeral(true).queue();
            }
        }
    }
}
