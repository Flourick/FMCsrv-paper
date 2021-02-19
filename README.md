# FMC - Paper plugin

Paper plugin initally made for my own SMP server. This repository is more of an archive than a project that is being worked on. Nevertheless it packs some very *noice* features.

## About

Modular plugin (meaning you can select what features you want or not and configure them to your likings) that adds a variety of vanilla friendlier features and several others to help server administrators maintain order and catch the damned griefer.

Once you run it for the first time configs will be created in `/plugins/FMC/` directory. The way this plugin works is it reads the `config.yml` first to determine which modules to load. Unfortunately the way that Bukkit works is that all the commands get registered even if you have the module associated with it disabled, so if you try to run such a command you will get a disabled command response. I recommend a permissions plugin to customize who can use which command as permissions manager is something I did not get around to add to this plugin.

## Modules

Features are divided into 10 modules for easier configuration of what you want to enable or not.

* ### Administration

    Unfinished module for various administration features, as it is you can change the TAB list header & footer and also adds two commands `/deluser` that will delete given users *.dat* files, which basically resets all of his progress on the server.

    Second command is `/inactive` which is basically a wrapper for three commands. It removes given player from whitelist, then marks his base on dynmap as abandoned and updates the inactive tag in database. The last two are only performed if their corresponding modules are enabled.

* ### AFK

    Simple AFK module, by default players will get a TAB AFK prefix after five minutes of inactivity, optionally it can also be announced in chat when someone is AFK. Also add a `/afk` command to toggle the AFK state manually.

* ### Discord

    Minecraft-Discord chat integration. Sends all minecraft player chat messages to a specified discord channel and vice versa. You can also optionally set the join/leave messages and death messages to be sent to the discord channel aswell. Prefix for users sending from discord is also customizable.

* ### DynFMC

    Module for marking bases/towns and points of interest on DynMap. The command to do so is `/dynfmc`. Basically works as a wrapper for `/dmarker` command. By default all players can use it to mark one base, infinite towns and infinite points of interest. This module does not have a config.

* ### Fun

    Only one feature and that is a `/hat` command that puts whatever block you have in your main hand onto your head.

* ### Chat

    Adds some chat related customizations. Players can use `/colorme` command to change their names color to any of the vanilla predefined ones excluding black as it's not really readable. If enabled they can also use the `&` symbol in chat for formatting. You can also set custom player join/leave messages, if you set more than one a random one will be picked each time someone joins/leaves.

* ### Loot

    Various loot/drops related features. Shulkers drop an extra shell. Adds an invisible item frame recipe (Item Frames in chest pattern and Fermented Spider Eye in the middle), however once placed it does not drop the invisible one back.

    Then there is this big feature that allows you to completely customize what trades the Wandering Trader has. Instead of trying to explain in detail how to do so I present an example (and yes I know I could have used a list for the trades but eh):

    ```YAML
    ...
    wandering-trader:
        no-wandering-trader-spawn: false
        custom-trades:
            enabled: true
            remove-vanilla-trades: true
            trades:
                trade-one:
                    group-id: 0 # default group where propabilities don't matter (as if it was 100%)
                    propability-in-group: 1337 # will be ignored becouse group 0
                    first-ingredient:
                        name: minecraft:diamond
                        count: 1
                    second-ingredient:
                        name: null
                        count: 0
                    output:
                        name: minecraft:emerald
                        count: 1
                    max-uses: 5
                    drop-xp: true
                trade-two:
                    first-ingredient: # group-id and propability can be omitted (automatically added to group 0)
                        name: minecraft:dirt
                        count: 64
                    second-ingredient:
                        name: minecraft:emerald
                        count: 1
                    output:
                        name: minecraft:grass_block
                        count: 64
                    max-uses: 10
                    drop-xp: false
                trade-three:
                    group-id: 1
                    propability-in-group: 50
                    first-ingredient:
                        name: minecraft:diamond
                        count: 3
                    second-ingredient:
                        name: null
                        count: 0
                    output:
                        name: minecraft:diamond_pickaxe
                        count: 1
                    max-uses: 2
                    drop-xp: true
                trade-four:
                    group-id: 1
                    propability-in-group: 50
                    first-ingredient:
                        name: minecraft:diamond
                        count: 3
                    second-ingredient:
                        name: null
                        count: 0
                    output:
                        name: minecraft:diamond_axe
                        count: 1
                    max-uses: 2
                    drop-xp: true
    ...
    ```

    So what does this example do? First of all removes all vanilla trades from any trader that will spawn. Then you can see four available trades in the config (trade-one to trade-four). First two trades are in group 0 which means that they will ALWAYS be present in any trader. As is in the comments you can omit the group and propability. Then we have *trade-three* and *trade-four*, those are in the same group (1 in this example but can be any number larger than zero). The important part is that they are in the same group which means **only one** trade from a group will be added to the trader! As in the example there is a 50/50 chance of the final trade to either be *trade-three* or *trade-four*. This also means that the cumulative propability in a defined group has to be **exactly** 100. To sum this example up Wandering Trader will have **three** trades, first two are given and the third one is determined from the group 1.

    If still unsure what exactly does it do just plop this in your loot config and spawn a few traders, then it should be crystal clear.

* ### OnePlayerSleep

    Only one player needs to sleep through the night, however this time there is a twist! If there is someone on the server who needs the night (hunting mobs, taking epic night screenshots...), then he can simply kick him out of the bed. Cruel but effective. Kicking works by clicking on the chat message that pops-up when someone wants to sleep or you can type the `/wakeplayer` command manually.

* ### Protection

    Adds three features. First smaller one prevents endermen from picking up blocks. Then as an administrator you can open and edit enderchests of any player that has ever played on the server, yes offline ones too. You can also drop them directly on the ground. You can also drop their inventories but those cannot be opened.

    A big plusplus of this is the logger. You can set it to log pet deaths, creeper explosions, block placing and block breaking and all things container related such as items being taken/put into them and opening/closing.

    If you enable everything then be warned that the daily log files can get pretty big. They are also stored in `/plugins/FMC/logs` to prevent cluttering the normal logs.

* ### Stats

    Also a bit unfinished. Stores some user info in a MySQL database. Mainly good for keeping track how many times a user joined and when was the last time he did so.

---
