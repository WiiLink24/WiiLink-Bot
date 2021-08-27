from discord.ext import commands
from google.cloud import vision
import discord
import os


class OCR(commands.Cog):
    def __init__(self, bot):
        self.bot = bot

    @commands.command()
    async def ocr(self, ctx, image_url=None):
        global image
        text_list = ""
        final_text = ""

        client = vision.ImageAnnotatorClient()

        if image_url is None:
            # Save supplied image
            await ctx.message.attachments[0].save("./ocr.jpg")
            with open("./ocr.jpg", "rb") as f:
                content = f.read()

            image = vision.Image(content=content)
            # Now that we have the image's bytes, we can safely delete.
            os.remove("./ocr.jpg")
        else:
            image = vision.Image()
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
