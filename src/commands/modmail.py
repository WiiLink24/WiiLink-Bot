from discord.ext import commands

import discord


class ModMail(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.Cog.listener()
    async def on_message(self, message):
        fmt = "%m/%d/%Y"
        if not message.author.bot:
            if str(message.channel.type) == "private":
                member = self.bot.get_user(int(message.author.id))
                guild = self.bot.get_guild(750581992223146074)
                create_dm = await member.create_dm()
                if guild.get_member(message.author.id) is not None:
                    modmail_channel = discord.utils.get(self.bot.get_all_channels(), name="mod-mail")
                    embed = discord.Embed(colour=0x00FF00)
                    embed.set_author(name=f"{message.author.display_name} #{message.author.discriminator} (ID: {message.author.id})", icon_url=message.author.avatar_url)
                    embed.add_field(name="Mail Received", value=message.content, inline=False)
                    embed.set_footer(text=f"Submitted • {message.created_at.strftime(fmt)}")
                    await modmail_channel.send(embed=embed)
                    await create_dm.send("Message sent to staff.")
                else:
                    await create_dm.send("You cannot use Mod Mail as you are not in the WiiLink server.")

    @commands.command()
    @commands.has_permissions(ban_members=True)
    async def modreply(self, ctx, user_id, *, reply_msg):
        member = self.bot.get_user(int(user_id))
        create_dm = await member.create_dm()
        embed = discord.Embed(colour=0x00FF00)
        embed.set_author(name="Mod Mail", icon_url=str(ctx.guild.icon_url))
        embed.add_field(name="Staff Response", value=reply_msg, inline=False)
        await create_dm.send(embed=embed)
        await ctx.send("Reply sent.")


def setup(bot):
    bot.add_cog(ModMail(bot))
