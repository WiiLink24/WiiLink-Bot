import asyncio
import datetime as dt
import enum
import random
import re
import typing as t
from src.commands.errors import *
import discord
import wavelink
from discord_components import Button, ButtonStyle
from discord.ext import commands

URL_REGEX = r"(?i)\b((?:https?://|www\d{0,3}[.]|[a-z0-9.\-]+[.][a-z]{2,4}/)(?:[^\s()<>]+|\(([^\s()<>]+|(\([^\s()<>]+\)))*\))+(?:\(([^\s()<>]+|(\([^\s()<>]+\)))*\)|[^\s`!()\[\]{};:'\".,<>?«»“”‘’]))"
TIME_REGEX = r"([0-9]{1,2})[:ms](([0-9]{1,2})s?)?"


class RepeatMode(enum.Enum):
    NONE = 0
    ONE = 1
    ALL = 2


class Queue:
    def __init__(self):
        self._queue = []
        self.position = 0
        self.repeat_mode = RepeatMode.NONE

    @property
    def is_empty(self):
        return not self._queue

    @property
    def current_track(self):
        if not self._queue:
            raise QueueIsEmpty

        if self.position <= len(self._queue) - 1:
            return self._queue[self.position]

    @property
    def upcoming(self):
        if not self._queue:
            raise QueueIsEmpty

        return self._queue[self.position + 1:]

    @property
    def history(self):
        if not self._queue:
            raise QueueIsEmpty

        return self._queue[:self.position]

    @property
    def length(self):
        return len(self._queue)

    def add(self, *args):
        self._queue.extend(args)

    def get_next_track(self):
        if not self._queue:
            raise QueueIsEmpty

        self.position += 1

        if self.position < 0:
            return None
        elif self.position > len(self._queue) - 1:
            if self.repeat_mode == RepeatMode.ALL:
                self.position = 0
            else:
                return None

        return self._queue[self.position]

    def shuffle(self):
        if not self._queue:
            raise QueueIsEmpty

        upcoming = self.upcoming
        random.shuffle(upcoming)
        self._queue = self._queue[:self.position + 1]
        self._queue.extend(upcoming)

    def set_repeat_mode(self, mode):
        if mode == "none":
            self.repeat_mode = RepeatMode.NONE
        elif mode == "1":
            self.repeat_mode = RepeatMode.ONE
        elif mode == "all":
            self.repeat_mode = RepeatMode.ALL

    def empty(self):
        self._queue.clear()
        self.position = 0


class Player(wavelink.Player):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.queue = Queue()
        self.eq_levels = [0.] * 15

    async def connect(self, ctx, channel=None):
        if self.is_connected:
            raise AlreadyConnectedToChannel

        if (channel := getattr(ctx.author.voice, "channel", channel)) is None:
            raise NoVoiceChannel

        await super().connect(channel.id)
        return channel

    async def teardown(self):
        try:
            await self.destroy()
        except KeyError:
            pass

    async def add_tracks(self, ctx, tracks):
        if not tracks:
            raise NoTracksFound

        if isinstance(tracks, wavelink.TrackPlaylist):
            self.queue.add(*tracks.tracks)
        elif len(tracks) == 1:
            self.queue.add(tracks[0])
            await ctx.send(f"Added {tracks[0].title} to the queue.")
        else:
            if (track := await self.choose_track(ctx, tracks)) is not None:
                self.queue.add(track)
                await ctx.send(f"Added {track.title} to the queue.")

        if not self.is_playing and not self.queue.is_empty:
            await self.start_playback()

    async def choose_track(self, ctx, tracks):
        def _check(selection, author):
            return (
                selection.custom_id in options
                and author == ctx.author.id
            )

        embed = discord.Embed(
            title="Choose a song",
            description=(
                "\n".join(
                    f"**{i+1}.** {t.title} ({t.length//60000}:{str(t.length%60).zfill(2)})"
                    for i, t in enumerate(tracks[:5])
                )
            ),
            colour=ctx.author.colour,
            timestamp=dt.datetime.utcnow()
        )
        embed.set_author(name="Query Results")
        embed.set_footer(text=f"Invoked by {ctx.author.display_name}", icon_url=ctx.author.avatar_url)
        buttons = [
            Button(
                style=ButtonStyle.blue,
                label="1",
                custom_id="0"
            ),
            Button(
                style=ButtonStyle.blue,
                label="2",
                custom_id="1"
            ),
            Button(
                style=ButtonStyle.blue,
                label="3",
                custom_id="2"
            ),
            Button(
                style=ButtonStyle.blue,
                label="4",
                custom_id="3"
            ),
            Button(
                style=ButtonStyle.blue,
                label="5",
                custom_id="4"
            )
        ]

        msg = await ctx.send(embed=embed, components=[buttons])
        options = ["0", "1", "2", "3", "4"]

        try:
            interaction = await self.bot.wait_for(
                "button_click", timeout=60.0, check=lambda inter: _check(inter, ctx.author.id)
            )
        except asyncio.TimeoutError:
            await msg.delete()
            await ctx.message.delete()
        else:
            await msg.delete()
            return tracks[int(interaction.custom_id)]

    async def start_playback(self):
        await self.play(self.queue.current_track)

    async def advance(self):
        try:
            if (track := self.queue.get_next_track()) is not None:
                await self.play(track)
        except QueueIsEmpty:
            pass

    async def repeat_track(self):
        await self.play(self.queue.current_track)


class Music(commands.Cog, wavelink.WavelinkMixin):
    def __init__(self, bot):
        self.bot = bot
        self.wavelink = wavelink.Client(bot=bot)
        self.bot.loop.create_task(self.start_nodes())

    @commands.Cog.listener()
    async def on_voice_state_update(self, member, before, after):
        if not member.bot and after.channel is None:
            if not [m for m in before.channel.members if not m.bot]:
                await self.get_player(member.guild).teardown()

    @wavelink.WavelinkMixin.listener()
    async def on_node_ready(self, node):
        print(f" Wavelink node `{node.identifier}` ready.")

    @wavelink.WavelinkMixin.listener("on_track_stuck")
    @wavelink.WavelinkMixin.listener("on_track_end")
    @wavelink.WavelinkMixin.listener("on_track_exception")
    async def on_player_stop(self, node, payload):
        if payload.player.queue.repeat_mode == RepeatMode.ONE:
            await payload.player.repeat_track()
        else:
            await payload.player.advance()

    async def cog_check(self, ctx):
        if isinstance(ctx.channel, discord.DMChannel):
            await ctx.send("Music commands are not available in DMs.")
            return False

        return True

    async def start_nodes(self):
        await self.bot.wait_until_ready()

        nodes = {
            "MAIN": {
                "host": "127.0.0.1",
                "port": 2333,
                "rest_uri": "http://127.0.0.1:2333",
                "password": "youshallnotpass",
                "identifier": "MAIN",
                "region": "us",
            }
        }

        for node in nodes.values():
            await self.wavelink.initiate_node(**node)

    def get_player(self, obj):
        if isinstance(obj, commands.Context):
            return self.wavelink.get_player(obj.guild.id, cls=Player, context=obj)
        elif isinstance(obj, discord.Guild):
            return self.wavelink.get_player(obj.id, cls=Player)

    @commands.command(name="connect", aliases=["join"])
    async def connect(self, ctx, *, channel: t.Optional[discord.VoiceChannel]):
        player = self.get_player(ctx)
        channel = await player.connect(ctx, channel)
        await ctx.send(f"Connected to {channel.name}.")

    @connect.error
    async def connect_error(self, ctx, exc):
        if isinstance(exc, AlreadyConnectedToChannel):
            await ctx.send("Already connected to a voice channel.")
        elif isinstance(exc, NoVoiceChannel):
            await ctx.send("No suitable voice channel was provided.")

    @commands.command(name="disconnect", aliases=["leave"])
    async def disconnect_command(self, ctx):
        player = self.get_player(ctx)
        await player.teardown()
        await ctx.send("Disconnected.")

    @commands.command()
    async def play(self, ctx, *, query: t.Optional[str]):
        player = self.get_player(ctx)

        if not player.is_connected:
            await player.connect(ctx)

        if query is None:
            if player.queue.is_empty:
                raise QueueIsEmpty

            await player.set_pause(False)
            await ctx.send("Playback resumed.")

        else:
            query = query.strip("<>")
            if not re.match(URL_REGEX, query):
                query = f"ytsearch:{query}"

            await player.add_tracks(ctx, await self.wavelink.get_tracks(query))

    @play.error
    async def play_error(self, ctx, exc):
        if isinstance(exc, QueueIsEmpty):
            await ctx.send("No songs to play as the queue is empty.")
        elif isinstance(exc, NoVoiceChannel):
            await ctx.send("No suitable voice channel was provided.")

    @commands.command()
    async def pause(self, ctx):
        player = self.get_player(ctx)

        if player.is_paused:
            raise PlayerIsAlreadyPaused

        await player.set_pause(True)
        await ctx.send("Playback paused.")

    @pause.error
    async def pause_error(self, ctx, exc):
        if isinstance(exc, PlayerIsAlreadyPaused):
            await ctx.send("Already paused.")

    @commands.command()
    async def stop(self, ctx):
        player = self.get_player(ctx)
        player.queue.empty()
        await player.stop()
        await ctx.send("Playback stopped.")

    @commands.command(name="next", aliases=["skip"])
    async def next_command(self, ctx):
        player = self.get_player(ctx)

        if not player.queue.upcoming:
            raise NoMoreTracks

        await player.stop()
        await ctx.send("Playing next track in queue.")

    @next_command.error
    async def next_command_error(self, ctx, exc):
        if isinstance(exc, QueueIsEmpty):
            await ctx.send("This could not be executed as the queue is currently empty.")
        elif isinstance(exc, NoMoreTracks):
            await ctx.send("There are no more tracks in the queue.")

    @commands.command()
    async def previous(self, ctx):
        player = self.get_player(ctx)

        if not player.queue.history:
            raise NoPreviousTracks

        player.queue.position -= 2
        await player.stop()
        await ctx.send("Playing previous track in queue.")

    @previous.error
    async def previous_error(self, ctx, exc):
        if isinstance(exc, QueueIsEmpty):
            await ctx.send("This could not be executed as the queue is currently empty.")
        elif isinstance(exc, NoPreviousTracks):
            await ctx.send("There are no previous tracks in the queue.")

    @commands.command(name="shuffle")
    async def shuffle_command(self, ctx):
        player = self.get_player(ctx)
        player.queue.shuffle()
        await ctx.send("Queue shuffled.")

    @shuffle_command.error
    async def shuffle_command_error(self, ctx, exc):
        if isinstance(exc, QueueIsEmpty):
            await ctx.send("The queue could not be shuffled as it is currently empty.")

    @commands.command()
    async def repeat(self, ctx, mode: str):
        if mode not in ("none", "1", "all"):
            raise InvalidRepeatMode

        player = self.get_player(ctx)
        player.queue.set_repeat_mode(mode)
        await ctx.send(f"The repeat mode has been set to {mode}.")

    @commands.command()
    async def queue(self, ctx, show: t.Optional[int] = 10):
        player = self.get_player(ctx)

        if player.queue.is_empty:
            raise QueueIsEmpty

        embed = discord.Embed(
            title="Queue",
            description=f"Showing up to next {show} tracks",
            colour=ctx.author.colour,
            timestamp=dt.datetime.utcnow()
        )
        embed.set_author(name="Query Results")
        embed.set_footer(text=f"Requested by {ctx.author.display_name}", icon_url=ctx.author.avatar_url)
        embed.add_field(
            name="Currently playing",
            value=getattr(player.queue.current_track, "title", "No tracks currently playing."),
            inline=False
        )
        if upcoming := player.queue.upcoming:
            embed.add_field(
                name="Next up",
                value="\n".join(t.title for t in upcoming[:show]),
                inline=False
            )

        msg = await ctx.send(embed=embed)

    @queue.error
    async def queue_command_error(self, ctx, exc):
        if isinstance(exc, QueueIsEmpty):
            await ctx.send("The queue is currently empty.")

    # Requests -----------------------------------------------------------------

    @commands.group(name="volume", invoke_without_command=True)
    async def volume_group(self, ctx, volume: int):
        player = self.get_player(ctx)

        if volume < 0:
            raise VolumeTooLow

        if volume > 150:
            raise VolumeTooHigh

        await player.set_volume(volume)
        await ctx.send(f"Volume set to {volume:,}%")

    @volume_group.error
    async def volume_group_error(self, ctx, exc):
        if isinstance(exc, VolumeTooLow):
            await ctx.send("The volume must be 0% or above.")
        elif isinstance(exc, VolumeTooHigh):
            await ctx.send("The volume must be 150% or below.")

    @volume_group.command(name="up")
    async def volume_up_command(self, ctx):
        player = self.get_player(ctx)

        if player.volume == 150:
            raise MaxVolume

        await player.set_volume(value := min(player.volume + 10, 150))
        await ctx.send(f"Volume set to {value:,}%")

    @volume_up_command.error
    async def volume_up_command_error(self, ctx, exc):
        if isinstance(exc, MaxVolume):
            await ctx.send("The player is already at max volume.")

    @volume_group.command(name="down")
    async def volume_down_command(self, ctx):
        player = self.get_player(ctx)

        if player.volume == 0:
            raise MinVolume

        await player.set_volume(value := max(0, player.volume - 10))
        await ctx.send(f"Volume set to {value:,}%")

    @volume_down_command.error
    async def volume_down_command_error(self, ctx, exc):
        if isinstance(exc, MinVolume):
            await ctx.send("The player is already at min volume.")

    @commands.command()
    async def eq(self, ctx, preset: str):
        player = self.get_player(ctx)

        eq = getattr(wavelink.eqs.Equalizer, preset, None)
        if not eq:
            raise InvalidEQPreset

        await player.set_eq(eq())
        await ctx.send(f"Equaliser adjusted to the {preset} preset.")

    @eq.error
    async def eq_command_error(self, ctx, exc):
        if isinstance(exc, InvalidEQPreset):
            await ctx.send("The EQ preset must be either 'flat', 'boost', 'metal', or 'piano'.")

    @commands.command(name="playing", aliases=["np"])
    async def playing_command(self, ctx):
        player = self.get_player(ctx)

        if not player.is_playing:
            raise PlayerIsAlreadyPaused

        embed = discord.Embed(
            title="Now playing",
            colour=ctx.author.colour,
            timestamp=dt.datetime.utcnow(),
        )
        embed.set_author(name="Playback Information")
        embed.set_footer(text=f"Requested by {ctx.author.display_name}", icon_url=ctx.author.avatar_url)
        embed.add_field(name="Track title", value=player.queue.current_track.title, inline=False)
        embed.add_field(name="Artist", value=player.queue.current_track.author, inline=False)

        position = divmod(player.position, 60000)
        length = divmod(player.queue.current_track.length, 60000)
        embed.add_field(
            name="Position",
            value=f"{int(position[0])}:{round(position[1]/1000):02}/{int(length[0])}:{round(length[1]/1000):02}",
            inline=False
        )

        await ctx.send(embed=embed)

    @playing_command.error
    async def playing_command_error(self, ctx, exc):
        if isinstance(exc, PlayerIsAlreadyPaused):
            await ctx.send("There is no track currently playing.")

    @commands.command(name="skipto", aliases=["playindex"])
    async def skipto_command(self, ctx, index: int):
        player = self.get_player(ctx)

        if player.queue.is_empty:
            raise QueueIsEmpty

        if not 0 <= index <= player.queue.length:
            raise NoMoreTracks

        player.queue.position = index - 2
        await player.stop()
        await ctx.send(f"Playing track in position {index}.")

    @skipto_command.error
    async def skipto_command_error(self, ctx, exc):
        if isinstance(exc, QueueIsEmpty):
            await ctx.send("There are no tracks in the queue.")
        elif isinstance(exc, NoMoreTracks):
            await ctx.send("That index is out of the bounds of the queue.")

    @commands.command(name="restart")
    async def restart_command(self, ctx):
        player = self.get_player(ctx)

        if player.queue.is_empty:
            raise QueueIsEmpty

        await player.seek(0)
        await ctx.send("Track restarted.")

    @restart_command.error
    async def restart_command_error(self, ctx, exc):
        if isinstance(exc, QueueIsEmpty):
            await ctx.send("There are no tracks in the queue.")

    @commands.command(name="seek")
    async def seek_command(self, ctx, position: str):
        player = self.get_player(ctx)

        if player.queue.is_empty:
            raise QueueIsEmpty

        if not (match := re.match(TIME_REGEX, position)):
            raise InvalidTimeString

        if match.group(3):
            secs = (int(match.group(1)) * 60) + (int(match.group(3)))
        else:
            secs = int(match.group(1))

        await player.seek(secs * 1000)
        await ctx.send("Seeked.")


def setup(bot):
    bot.add_cog(Music(bot))
