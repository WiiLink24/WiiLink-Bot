import discord
import requests
import zipfile
import os
import lxml.etree as et

from discord.ext import commands


class GameTDB(commands.Cog):
    def __init__(self, bot):
        self.databases = {
            "wii": ["wii", None],
            "ps3": ["ps3", None],
            "nds": ["ds", None],
            "3ds": ["3ds", None],
            "wiiu": ["wiiu", None],
            "switch": ["switch", None]
        }
        self.bot = bot

    def download(self):
        # From ninfile2.py
        for k, v in self.databases.items():
            print("Downloading {} Database from GameTDB...".format(k))
            filename = v[0] + "tdb"
            if not os.path.exists(filename + ".xml"):
                url = "https://www.gametdb.com/{}".format(filename + ".zip")
                # It's blocked for the "python-requests" user-agent to encourage setting a different user-agent for different apps, to get an idea of the origin of the requests. (according to the GameTDB admin).
                r = requests.get(
                    url, headers={"User-Agent": "Nintendo Channel Info Downloader"})
                open(filename + ".zip", 'wb').write(r.content)
                self.zip = zipfile.ZipFile(filename + ".zip")
                self.zip.extractall(".")
                self.zip.close()

    def parse(self):
        for k, v in self.databases.items():
            filename = v[0] + "tdb"

            print("Loading {}...".format(k))
            v[1] = et.parse(filename + ".xml")

        return self.databases


    @commands.command()
    async def gametdb(self, ctx, system, game):
        self.download()
        self.parse()
        system = system.lower()
        for s in self.databases[system][1].findall("game"):

            if s.find("id").text == game:
                if s.find("id") is None:
                    await ctx.send("This title does not exist.")
                else:
                    title_id = s.find("id").text
                    embed = discord.Embed(color=0x00FF00)
                    url = f"https://art.gametdb.com/ds/box/US/{title_id}.png"
                    if system == "switch":
                        url = f"https://art.gametdb.com/switch/cover/US/{title_id}.jpg"
                    if system == "wiiu":
                        url = f"https://art.gametdb.com/wiiu/cover/US/{title_id}.jpg"
                    if system == "wii":
                        url = f"https://art.gametdb.com/wii/cover/US/{title_id}.png?1392931483"
                    if system == "3ds":
                        url = f"https://art.gametdb.com/3ds/box/US/{title_id}.png"
                    if system == "ps3":
                        url = f"https://art.gametdb.com/ps3/cover/US/{title_id}.jpg"
                    embed.set_thumbnail(url=url)
                    embed.add_field(name="Title: ", value=s.find("locale", {"lang": "EN"}).find("title").text,
                                    inline=False)
                    release_date = f"{s.find('date').get('year')}/{s.find('date').get('month')}/{s.find('date').get('day')}"
                    if s.find("locale", {"lang": "EN"}).find("synopsis").text is None:
                        synopsis = "No synopsis available."
                    else:
                        synopsis = s.find("locale", {"lang": "EN"}).find("synopsis").text[:900]
                    embed.add_field(name="Synopsis", value=synopsis, inline=False)
                    embed.add_field(name="Release Date: ", value=release_date, inline=False)
                    await ctx.send(embed=embed)


def setup(bot):
    bot.add_cog(GameTDB(bot))
