from discord.ext import commands
from google.cloud import vision
import discord


class OCR(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def ocr(self, ctx, image_url):
        text_list = ""
        final_text = ""
        client = vision.ImageAnnotatorClient()
        image = vision.Image()
        # TODO: Allow for images without URL
        image.source.image_uri = image_url

        response = client.text_detection(image=image)
        texts = response.text_annotations

        for text in texts:
            text_list += f"{text.description} "

        # Google Vision sends the text twice. I split the newlines as the divider between the duplicate text
        # is a double new line. Then, we iterate over the list to get the text once.
        k = text_list.splitlines()
        # We iterate over the entire list except for the last index as that is where the duplicate text is
        for i in range(len(k) - 1):
            final_text += f"{k[i]}\n"

        # Build the embed
        embed = discord.Embed(title="Optical Character Recognition", colour=0xADD8E6)
        embed.description = f"```{final_text}```"
        await ctx.send(embed=embed)


def setup(bot):
    bot.add_cog(OCR(bot))
