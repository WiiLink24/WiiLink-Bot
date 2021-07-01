import discord
import io
import contextlib
import requests
import subprocess

import textwrap
from traceback import format_exception
from discord.ext import commands
from bs4 import BeautifulSoup
from src.commands.helpers import generate_random, Pag, clean_code

fmt = "%a, %d %b %Y | %H:%M:%S %ZGMT"


class Member(commands.Converter):
    async def convert(self, ctx, argument):
        try:
            member_converter = commands.MemberConverter()
            member = await member_converter.convert(ctx, argument)
        except commands.MemberNotFound:
            member = discord.utils.find(
                lambda m: m.name.startswith(argument),
                ctx.guild.members
            )

        if member is None:
            raise commands.MemberNotFound(argument)

        return member


class Misc(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def mii(self, ctx, entry_number):
        if len(entry_number) >= 15 or len(entry_number) <= 13:
            await ctx.send("Please enter a real CMOC Entry Number.")
        else:
            global data
            table = []
            # Get Mii picture
            link = requests.get(f"https://miicontestp.wii.rc24.xyz/cgi-bin/htmlsearch.cgi?query={entry_number}").text
            bs = BeautifulSoup(link, "html.parser")
            for file in bs.find("a"):
                data = file.get("src")
            # Get Mii Artisan and nickname
            for child in bs.find("table").children:
                for td in child:
                    table.append(
                        td
                    )
            k, w = str(table[18]).split(">", 1)
            k, w = w.split("<", 1)
            a, b = str(table[26]).split('">', 1)
            a, b = b.split('<', 1)
            embed = discord.Embed(title=k, color=0x00FF00)
            embed.set_image(url=f"{data}")
            embed.set_footer(text=f"Created by: {a}", icon_url="https://cdn.discordapp.com/emojis/420052317690396673.png?v=1")
            await ctx.send(embed=embed)

    @mii.error
    async def mii_error(self, ctx, error):
        if isinstance(error, commands.MissingRequiredArgument):
            await ctx.send("Please enter a CMOC Entry Number.")
        # If someone decides to use quotes and not close them
        if isinstance(error, commands.InvalidEndOfQuotedStringError):
            await ctx.send("Please enter a real CMOC Entry Number.")

    @commands.command()
    async def about(self, message):
        embed = discord.Embed(title="<:wiilink:844609429239234640> About WiiLink24 Bot", color=0x00FF00)
        embed.set_thumbnail(url=message.guild.icon_url)
        embed.add_field(name="Created By:", value="SketchMaster2001")
        embed.add_field(
            name="Created For:",
            value="WiiLink24 under the GPL-3.0 License",
            inline=False,
        )
        embed.add_field(
            name="More Info",
            value="A Discord bot for the WiiLink24 Discord server.\nPrefix is `/`."
                  "It is written 100% in Python, using discord.py.\nIf you need help with the bot, "
                  "contact SketchMaster2001, or run `/help`.",
            inline=False,
        )
        await message.channel.send(embed=embed)

    @commands.command()
    async def credits(self, ctx):
        embed = discord.Embed(title="Credits", description="All the people who deserve to be credited for the existence of this bot.", colour=0x00FF00)
        embed.set_author(name="WiiLink24 Bot", icon_url=self.bot.user.avatar_url)
        embed.add_field(name="People", value="""
        <@239809536012058625>: Giving me the opportunity to be a developer as well as hosting the bot on cacti\n
        Stack Overflow: If it weren't for the people who use this site, half my bot would be non-existent\n
        discord.py: API wrapper for discord, in python\n
        <@667563245107937297>: Creator and developer of WiiLink24 Bot
        """, inline=False)
        await ctx.send(embed=embed)

    @commands.command(name="avy", aliases=["avatar"])
    async def avatar(self, ctx, *, user: Member = None):
        if user is None:
            user = ctx.author
        embed = discord.Embed(color=0x00FF00)
        embed.set_author(name=str(user), icon_url=user.avatar_url)
        embed.set_image(url=user.avatar_url)
        await ctx.channel.send(embed=embed)

    @commands.command(name="riitag", aliases=["tag"])
    async def riitag(self, ctx, *, username: Member = None):
        # Completely useless, but why not
        if username is None:
            username = ctx.author
        randomizer = generate_random(6)
        if requests.get(f"https://tag.rc24.xyz/{username.id}/tag.max.png?randomizer=0.{randomizer}").status_code != 404:
            em = discord.Embed(color=0x00FF00)
            em.set_author(name=f"{username}'s RiiTag", icon_url=username.avatar_url)
            em.set_image(
                url=f"https://tag.rc24.xyz/{username.id}/tag.max.png?randomizer=0.{randomizer}"
            )
            await ctx.send(embed=em)
        else:
            await ctx.send(f":x: **{username}** does not have a RiiTag!")


    @commands.command()
    async def table(self, ctx):
        embed = discord.Embed(colour=0x00FF00)
        embed.set_footer(text="Wii no Ma Tables", icon_url=ctx.guild.icon_url)
        embed.set_image(url="https://cdn.discordapp.com/attachments/750623609810190348/842808566003793940/NEWTables.png")
        await ctx.channel.send(embed=embed)

    @commands.command(pass_context=True)
    async def userinfo(self, ctx, *, user: Member = None):
        if user is None:
            user = ctx.author
        date_format = "%a, %d %b %Y %I:%M %p"
        embed = discord.Embed(color=0x00FF00)
        embed.set_author(name=str(user), icon_url=user.avatar_url)
        embed.set_thumbnail(url=user.avatar_url)
        embed.add_field(name="User ID", value=user.id, inline=False)
        embed.add_field(name="Nickname", value=user.display_name, inline=False)
        embed.add_field(name="Registered", value=user.created_at.strftime(date_format), inline=False)
        embed.add_field(name="Joined At", value=user.joined_at.strftime(date_format), inline=False)
        if len(user.roles) > 1:
            role_string = " ".join([r.mention for r in user.roles][1:])
            embed.add_field(
                name="Roles [{}]".format(len(user.roles) - 1),
                value=role_string,
                inline=False,
            )
        await ctx.send(embed=embed)

    # Service Stats
    @commands.command(pass_context=True)
    async def stats(self, message):
        embedVar = discord.Embed(
            title="<:wiilink24:818538825948987512> WiiLink24 Service Stats",
            color=0x00FF00,
        )
        embedVar.add_field(
            name="Public Beta:", value="```yaml\n+ Wii no Ma```", inline=False
        )
        embedVar.add_field(
            name="Private Beta:",
            value="```fix\n* Digicam Print Channel\n* Demae Channel```",
            inline=False,
        )
        embedVar.add_field(
            name="Development",
            value="```fix\n* Wii Fit Body Check Channel\n* Dokodemo Wii no Ma```",
            inline=False,
        )
        embedVar.add_field(
            name="Not in Development:",
            value="```diff\n- TV no Tomo Channel G Guide for Wii```",
            inline=False,
        )
        await message.channel.send(embed=embedVar)

    @commands.command(name="serverinfo")
    async def server_info(self, message):
        title = (
            f"<:info:818678491528036366> Information About **{message.guild.name}**:"
        )
        embed = discord.Embed(color=0x00FF00)
        text_channels = len(message.guild.text_channels)
        voice_channels = len(message.guild.voice_channels)
        categories = len(message.guild.categories)
        channels = text_channels + voice_channels
        embed.set_thumbnail(url=str(message.guild.icon_url))
        embed.add_field(
            name=f"**{message.guild.name}**",
            value=f":white_small_square: ID: **{message.guild.id}** \n:white_small_square: Owner: **{message.guild.owner}** \n:white_small_square: Location: **{message.guild.region}** \n:white_small_square: Creation: **{message.guild.created_at.strftime(fmt)}** \n:white_small_square: Members: **{message.guild.member_count}** \n:white_small_square: Channels: **{channels}** Channels; **{text_channels}** Text, **{voice_channels}** Voice, **{categories}** Categories \n:white_small_square: Verification: **{str(message.guild.verification_level).upper()}** \n:white_small_square: Features: {', '.join(f'**{x}**' for x in message.guild.features)}",
        )
        embed.set_image(
            url=f"https://cdn.discordapp.com/splashes/750581992223146074/{message.guild.splash}.jpg?size=1024"
        )
        await message.channel.send(title, embed=embed)

    @commands.command(name="eval", aliases=["exec"])
    @commands.is_owner()
    async def _eval(self, ctx, *, code):
        code = clean_code(code)

        local_variables = {
            "discord": discord,
            "commands": commands,
            "bot": self.bot,
            "ctx": ctx,
            "channel": ctx.channel,
            "author": ctx.author,
            "guild": ctx.guild,
            "message": ctx.message,
            "subprocess": subprocess
        }

        stdout = io.StringIO()

        try:
            with contextlib.redirect_stdout(stdout):
                exec(
                    f"async def func():\n{textwrap.indent(code, '    ')}", local_variables,
                )

                obj = await local_variables["func"]()
                result = f"{stdout.getvalue()}\n-- {obj}\n"
        except Exception as e:
            result = "".join(format_exception(e, e, e.__traceback__))

        pager = Pag(
            timeout=100,
            entries=[result[i: i + 2000] for i in range(0, len(result), 2000)],
            length=1,
            prefix="```py\n",
            suffix="```"
        )

        await pager.start(ctx)

    @_eval.error
    async def eval_error(self, ctx, error):
        if isinstance(error, commands.NotOwner):
            text = "You do not have the correct permissions to do that!"
            await ctx.send(text)
        elif isinstance(error, commands.MissingRequiredArgument):
            text = "Please enter the command you would like to execute, Captain Sketch"
            await ctx.send(text)


def setup(bot):
    bot.add_cog(Misc(bot))

