from discord.ext import commands
from src.commands.helpers import timestamp

import discord


class Events(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.Cog.listener()
    async def on_ready(self):
        game = discord.Game("Type /help")
        await self.bot.change_presence(status=discord.Status.online, activity=game)

    @commands.Cog.listener()
    async def on_message_delete(self, message):
        user = message.author
        text = f"{timestamp} :x: **{user}** (ID: {user.id})'s message has been deleted from <#{message.channel.id}>:"
        embed = discord.Embed(color=0xFF0000)
        embed.add_field(name="Deleted Message:", value=message.content, inline=True)
        channel = self.bot.get_channel(819217765188501536)
        await channel.send(text, embed=embed)

    @commands.Cog.listener()
    async def on_message_edit(self, message_before, message_after):
        text = f"{timestamp} :warning: **{message_before.author}** (ID: {message_before.author.id}) edited a message in <#{message_before.channel.id}>:"
        embed = discord.Embed(color=0xFFFF00)
        embed.add_field(name="From:", value=message_before.content, inline=False)
        embed.add_field(name="\nTo:", value=message_after.content, inline=False)
        channel = self.bot.get_channel(819217765188501536)
        await channel.send(text, embed=embed)


def setup(bot):
    bot.add_cog(Events(bot))
