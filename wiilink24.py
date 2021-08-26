import os
import discord

from dotenv import load_dotenv
from discord.ext import commands
from discord_components import DiscordComponents

load_dotenv()
TOKEN = os.getenv('DISCORD_TOKEN')

intents = discord.Intents().all()
intents.members = True
bot = commands.Bot(command_prefix='/', intents=intents)
DiscordComponents(bot)

bot.load_extension("src.commands.misc")
bot.load_extension("src.commands.mod")
bot.load_extension("src.commands.events")
bot.load_extension("src.commands.converters")
bot.load_extension("src.commands.modmail")
bot.load_extension("src.commands.afk")
bot.load_extension("src.commands.music")

bot.run(TOKEN)
