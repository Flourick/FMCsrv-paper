# FMC - Paper plugin

Paper plugin initally made for my own SMP server. This repository is more of an archive than a project that is being worked on. Nevertheless it packs some very noice features.

## About

Modular plugin (meaning you can select what features you want or not and configure them to your likings) that adds a variety of vanilla friendlier features and several others to help server administrators maintain order and catch the damned griefer that always seemed to join when you were not around.

Once you run it for the first time configs will be created in `/plugins/FMC/` directory. The way this plugin works is it reads the `config.yml` first to determine which modules to load. Unfortunately the way that Bukkit works is that all the commands get registered even if you have the module associated with it disabled, so if you try to run such a command you will get a disabled command response. So I recommend a permissions plugin to customize who can use which command as permission manager is something I did not get around to add to this plugin.

## Modules

Features are divided in 10 modules for easier configuration of what you want to enable or not.

### Administration

### AFK

### Discord

### DynFMC

### Fun

### Chat

### Loot

### OnePlayerSleep

### Protection

### Stats
