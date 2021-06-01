import os
import discord

from dotenv import load_dotenv
from discord.ext import commands

load_dotenv()
TOKEN = os.getenv('DISCORD_TOKEN')

intents = discord.Intents.default()
intents.members = True
bot = commands.Bot(command_prefix='/', intents=intents)

bot.load_extension("src.commands.misc")
bot.load_extension("src.commands.mod")
bot.load_extension("src.commands.events")
bot.load_extension("src.commands.converters")
bot.load_extension("src.commands.afk")

bot.run(TOKEN)
