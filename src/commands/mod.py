import random

from sqlalchemy.orm import session
import discord
import asyncio
import re
import time

from models import UserInfo
from discord.ext import commands
from src.commands.helpers import timestamp, check_role
from src.commands.database import session

time_regex = re.compile("(?:(\d{1,5})(h|s|m|d))+?")
time_dict = {"h": 3600, "s": 1, "m": 60, "d": 86400}
tempo = {}


class TimeConverter(commands.Converter):
    async def convert(self, ctx, argument):
        args = argument.lower()
        matches = re.findall(time_regex, args)
        time = 0
        for v, k in matches:
            try:
                time += time_dict[k]*float(v)
            except KeyError:
                raise commands.BadArgument(f"{k} is an invalid time-key! h/m/s/d are valid!")
            except ValueError:
                raise commands.BadArgument(f"{v} is not a number!")
        return time


class Mods(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    @commands.has_permissions(ban_members=True)
    async def clear(self, ctx, number: int):
        clear = number
        username = ctx.author.mention
        channel = self.bot.get_channel(755522585864962099)
        cleared_messages = discord.Embed(color=0x00FF00)
        cleared_messages.add_field(
            name="Cleared Messages",
            value=f"{username}has cleared {clear} messages.",
        )
        await ctx.channel.purge(limit=clear)
        await asyncio.sleep(1)
        await ctx.channel.send(f"I have cleared {number} messages.")

        await channel.send(timestamp, embed=cleared_messages)

    @clear.error
    async def kick_error(self, ctx, error):
        if isinstance(error, commands.MissingPermissions):
            text = "You do not have the correct permissions to do that!"
            await ctx.send(text)

    @commands.command()
    @commands.has_permissions(ban_members=True)
    async def strike(self, ctx, member: discord.Member, strikes=None, *, reason: str = "No reason provided"):
        if strikes is None:
            strikes = 1
        channel = self.bot.get_channel(755522585864962099)
        db = session.query(UserInfo).filter_by(userid=member.id).first()
        create_dm = await member.create_dm()

        if db is None:
            striked = UserInfo(userid=member.id, strikes=strikes)
            session.add(striked)
            session.commit()
        else:
            db.strikes += strikes
            session.commit()

        await create_dm.send(f"You were striked in WiiLink24 for **`{reason}`**")
        await ctx.send(f"Successfully gave {strikes} strikes to **{member}**")
        await channel.send(
            f"{timestamp} :triangular_flag_on_post: **{ctx.author}** gave {strikes} strikes to **{member}**(ID:{member.id})\n`[Reason]:` `[{reason}]`")

        # Re-query to load the updated strikes
        db = session.query(UserInfo).filter_by(userid=member.id).first()

        if db.strikes >= 2:
            mute = discord.Embed(
                title=f":hammer:Due to having 2 strikes, {member} is muted for 10 minutes.",
                description=f"Reason: {reason}\nBy: {ctx.author.mention}",
            )
            role = discord.utils.get(ctx.guild.roles, name="Muted")
            await member.add_roles(role)
            await create_dm.send(f"You are muted for 10 minutes in WiiLink24 for **`{reason}`**")
            await channel.send(embed=mute)
            await asyncio.sleep(600)
            await member.remove_roles(role)

        if db.strikes >= 3:
            mute = discord.Embed(
                title=f":hammer:Due to having 3 strikes, {member} is muted for 2 hours.",
                description=f"Reason: {reason}\nBy: {ctx.author.mention}",
            )
            role = discord.utils.get(ctx.guild.roles, name="Muted")
            await create_dm.send(f"You are muted for 2 hours in WiiLink24 for **`{reason}`**")
            await member.add_roles(role)
            await channel.send(embed=mute)
            await asyncio.sleep(7200)
            await member.remove_roles(role)

        if db.strikes >= 4:
            kick = discord.Embed(
                title=f":hammer:Due to having 4 strikes, {member} was kicked.",
                description=f"Reason: {reason}\nBy: {ctx.author.mention}",
            )
            await create_dm.send(f"You were kicked from WiiLink24 for **`{reason}`**")
            await member.kick(reason=reason)
            await channel.send(embed=kick)
        if db.strikes >= 5:
            ban = discord.Embed(
                title=f":hammer:Due to having 5 or more strikes, {member} was banned.",
                description=f"Reason: {reason}\nBy: {ctx.author.mention}",
            )
            await create_dm.send(f"You are banned from WiiLink24 for **`{reason}`**")
            await member.ban(reason=reason)
            await channel.send(embed=ban)

    @commands.command()
    @commands.has_permissions(ban_members=True)
    async def unmute(self, ctx, member: discord.Member, *, reason="No reason provided"):
        role = discord.utils.get(ctx.guild.roles, name="Muted")
        await member.remove_roles(role)
        create_dm = await member.create_dm()
        await create_dm.send(f"You were unmuted in WiiLink24 for **`{reason}`**")
        await ctx.send(f":white_check_mark: Successfully unmuted {member}")


    @commands.command()
    @commands.has_permissions(ban_members=True)
    async def mute(self, ctx, member: discord.Member, *, time_muted: TimeConverter = None):
        time_muted: float
        role = discord.utils.get(ctx.guild.roles, name="Muted")
        await member.add_roles(role)
        await ctx.send((":white_check_mark: Successfully muted **{}** for **{}**" if time_muted else ":white_check_mark: Successfully muted **{}**").format(member, time_muted))
        if member.id not in tempo:
            tempo[member.id] = time.time() + time_muted  # store end time
            await asyncio.sleep(time_muted)
            del tempo[member.id]
        print(tempo)
        if time_muted:
            await asyncio.sleep(time_muted)
            await member.remove_roles(role)
            await ctx.send("")

    @commands.command(name="ban")
    @commands.has_permissions(ban_members=True)
    async def ban(self, ctx, user: discord.Member, *, reason="No reason provided"):
        create_dm = await user.create_dm()
        ban = discord.Embed(
            title=f":hammer:Successfully banned {user}",
            description=f"Reason: {reason}\nBy: {ctx.author.mention}",
        )
        channel = self.bot.get_channel(755522585864962099)
        await create_dm.send(f"You were banned in WiiLink24 for **`{reason}`**")
        await user.ban(reason=reason)
        await ctx.channel.send(embed=ban)

        await channel.send(embed=ban)


    @commands.command(name="unban")
    @commands.has_permissions(ban_members=True)
    async def unban(self, ctx, member):
        banned_users = await ctx.guild.bans()
        member_name, member_discriminator = member.split("#")

        if ctx.author.guild_permissions.ban_members:
            for ban_entry in banned_users:
                user = ban_entry.user

                if (user.name, user.discriminator) == (
                        member_name,
                        member_discriminator,
                ):
                    unban = discord.Embed(
                        title=f"Successfully Unbanned {user}",
                        description=f"By: {ctx.author.mention}",
                    )
                    channel = self.bot.get_channel(819217765188501536)
                    await ctx.guild.unban(user)
                    await ctx.channel.send(embed=unban)

                    await channel.send(embed=unban)
        else:
            await ctx.channel.send("You don't have the correct permissions.")

    @commands.command()
    @commands.has_permissions(ban_members=True)
    async def kick(self, ctx, user: discord.Member, *, reason="No reason provided"):
        kick = discord.Embed(
            title=f":hammer:Successfully kicked {user}",
            description=f"Reason: {reason}\nBy: {ctx.author.mention}",
        )
        channel = self.bot.get_channel(819217765188501536)
        await user.kick(reason=reason)
        await ctx.channel.send(embed=kick)

        await channel.send(embed=kick)

    @commands.command()
    @commands.has_permissions(ban_members=True)
    async def check(self, ctx, *, user: discord.User):
        db = session.query(UserInfo).filter_by(userid=user.id).first()
        role = ctx.guild.get_role(770836633419120650)
        muted = "**No**"

        if ctx.guild.get_member(user.id) is None:
            muted = "Not in Server"
        else:
            if check_role(ctx, user, role):
                muted = "**Yes**"

        try:
            entry = await ctx.guild.fetch_ban(user)
            banned = f"**Yes** (`{entry.reason}`)"
        except discord.NotFound:
            banned = f"**No**"
        if db != None:
            strikes = db.strikes
        else:
            strikes = 0

        await ctx.send(f":white_check_mark: Moderation Information for **{user}** (ID:{user.id}):\n"
           f":triangular_flag_on_post: Strikes: **{strikes}**\n:mute: Muted: **{muted}**\n:hammer: Banned: {banned}"
        )


def setup(bot):
    bot.add_cog(Mods(bot))
