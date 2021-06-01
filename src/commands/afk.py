from src.commands.database import session
from models import UserInfo
from discord.ext import commands

import discord


class AFK(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def afk(self, ctx, *, reason="No reason provided"):
        db = session.query(UserInfo).filter_by(userid=ctx.author.id).first()

        if db is None:
            striked = UserInfo(userid=ctx.author.id, is_afk=True, afk_reason=reason)
            session.add(striked)
            session.commit()
        else:
            db.is_afk = True
            db.afk_reason = reason
            session.commit()

        await ctx.send(f"**{ctx.author.display_name}** is now AFK.")

    @commands.Cog.listener()
    async def on_message(self, message):
        # Will be used to check if a member is AFK when they are pinged.
        if "<@" in message.content:
            k, v = message.content.split("@", 1)
            k, v = v.split(">", 1)
            if "!" in k:
                k = k.strip("!")

            k = int(k)

            db = session.query(UserInfo).filter_by(userid=k).first()

            if db is None:
                return None

            if db.is_afk:
                member = self.bot.get_user(k)
                create_dm = await member.create_dm()
                embed = discord.Embed(color=0x00FF00)
                embed.set_author(name=f"{member.display_name} is AFK", icon_url=member.avatar_url)
                embed.add_field(name="Reason", value=db.afk_reason, inline=False)
                await message.channel.send(embed=embed)
                embed2 = discord.Embed(color=0x00FF00)
                embed2.set_author(name=message.author.display_name, icon_url=message.author.avatar_url)
                embed2.add_field(name="Message", value=message.content, inline=False)
                embed2.set_footer(text=f"{message.channel}, {message.channel.guild}",
                                  icon_url=message.channel.guild.icon_url)
                await create_dm.send(embed=embed2)

    @commands.Cog.listener()
    async def on_typing(self, channel, member, when):
        create_dm = await member.create_dm()
        db = session.query(UserInfo).filter_by(userid=member.id).first()

        if db is None:
            return None

        if db.is_afk:
            db.is_afk = False
            db.afk_reason = ""
            session.commit()
            await create_dm.send("I have removed your AFK status.")


def setup(bot):
    bot.add_cog(AFK(bot))
