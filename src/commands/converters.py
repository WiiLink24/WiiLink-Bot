from discord.ext import commands
from src.commands.helpers import decrypt, encrypt


class Converters(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def hex_to_str(self, ctx, *, hex: str):
        no_whitespace = hex.replace(" ", "")

        de_hex = bytearray.fromhex(no_whitespace).decode()

        await ctx.channel.send(de_hex)

    @commands.command()
    async def hex_to_utf16le(self, ctx, *, hex: str):
        try:
            no_whitespace = hex.replace(" ", "")

            de_hex = bytes.fromhex(no_whitespace).decode('utf-16LE')

            await ctx.channel.send(de_hex)
        except UnicodeDecodeError:
            await ctx.send('Please enter more than 2 bytes.')

        except ValueError:
            await ctx.send('Please enter an even number of bytes.')

    @commands.command()
    async def hex_to_utf16be(self, ctx, *, hex):
        try:
            no_whitespace = hex.replace(" ", "")

            de_hex = bytes.fromhex(no_whitespace).decode('utf-16BE')

            await ctx.channel.send(de_hex)
        except UnicodeDecodeError:
            await ctx.send('Please enter more than 2 bytes.')
        except ValueError:
            await ctx.send('Please enter an even number of bytes.')

    @commands.command()
    async def morse_to_str(self, ctx, *, morse):
        await ctx.channel.send(decrypt(morse).lower())

    @commands.command()
    async def str_to_morse(self, ctx, *, morse):
        try:
            convert_to_uppercase = morse.upper()
            await ctx.channel.send(encrypt(convert_to_uppercase).upper())
        except KeyError:
            await ctx.send('Sorry, alphanumeric characters only!')


def setup(bot):
    bot.add_cog(Converters(bot))
