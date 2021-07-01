from discord.ext import commands
from src.commands.helpers import generate_random
from src.commands.misc import Member

import requests
import discord


class Digicard(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.command(name="digicard", aliases=["card"])
    async def digicard(self, ctx, *, username: Member = None):
        if username is None:
            username = ctx.author
        randomizer = generate_random(6)
        if requests.get(f"https://card.wiilink24.com/cards/{username.id}.jpg?randomizer=0.{randomizer}").status_code != 404:
            user = username.id
            em = discord.Embed(color=0x00FF00)
            em.set_author(name=f"{username}'s Digicard", icon_url=username.avatar_url)
            em.set_image(
                url=f"https://card.wiilink24.com/cards/{user}.jpg?randomizer=0.{randomizer}"
            )
            await ctx.channel.send(embed=em)
        else:
            await ctx.send(f":x: **{username}** does not have a Digicard!")


def setup(bot):
    bot.add_cog(Digicard(bot))
