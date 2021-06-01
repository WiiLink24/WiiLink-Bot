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
        if not message.author.bot:
            if message.channel.guild.id == 750581992223146074:
                user = message.author
                text = f"{timestamp} :x: **{user}** (ID: {user.id})'s message has been deleted from <#{message.channel.id}>:"
                embed = discord.Embed(color=0xFF0000)
                embed.add_field(name="Deleted Message:", value=message.content, inline=True)
                channel = self.bot.get_channel(755522585864962099)
                await channel.send(text, embed=embed)

    @commands.Cog.listener()
    async def on_message_edit(self, message_before, message_after):
        if not message_before.author.bot:
            if message_after.channel.guild.id == 750581992223146074:
                text = f"{timestamp} :warning: **{message_before.author}** (ID: {message_before.author.id}) edited a " \
                        f"message in <#{message_before.channel.id}>:"
                embed = discord.Embed(color=0xFFFF00)
                embed.add_field(name="From:", value=message_before.content, inline=False)
                embed.add_field(name="\nTo:", value=message_after.content, inline=False)
                channel = self.bot.get_channel(755522585864962099)
                await channel.send(text, embed=embed)

    @commands.Cog.listener()
    async def on_member_join(self, member: discord.Member):
        channel = self.bot.get_channel(755522585864962099)
        text = f"{timestamp} :inbox_tray: **{member}** (ID:{member.id}) joined the server.\n" \
               f"Creation: {member.created_at}"
        await channel.send(text)

    @commands.Cog.listener()
    async def on_member_remove(self, member: discord.Member):
        text = f"{timestamp} :outbox_tray: **{member}** (ID:{member.id}) has been kicked or left the server.\n" \
               f"Creation: {member.created_at}"
        channel = self.bot.get_channel(755522585864962099)
        await channel.send(text)


def setup(bot):
    bot.add_cog(Events(bot))
