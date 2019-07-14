# StarGate
<a align="center" href="https://discord.gg/VsHXm2M"><img src="https://discordapp.com/api/guilds/574240965351571477/embed.png" alt="Discord server"/> </a>[![Build Status](https://travis-ci.org/Alemiz112/StarGate.svg?branch=master)](https://travis-ci.org/Alemiz112/StarGate)
>Plugin made for WaterDog, awesome proxy for mcbe (latest version)

## 🎯 Features:    
 ✔️ StaffChat<br>
 ✔️ StaffFind/StaffList<br>
 ✔️ PlayerFind<br>
 ✔️ QueryUpdater<br>
 ✔️ MOTD Changer<br>
  <br>
## 📝 TODO:
 ✔️ API for PocketMine developers<br>
 ✔️ PartyMode<br>
## 🔧 DEV Only:
 ✔️ Plugin Messaging System(not done yet)<br>
 
 
## 📘 Commands:

**StarGate Commands:**  
  
| **Name/Command** | **Usage** |  
| --- | --- |  
| **/sc** | **StaffChat control** <br><br> Usage: `/sc`, `/sc <message>` <br><br> Using only `/sc` you will turn force StaffChat on|off|. You can also use `!` or any presetted character before your message to send staff message. <br> Permission: `stargate.staffchat`
| **/look** | **Find Player** <br><br> Usage: `/look <player>` <br><br> This command will help you find a players server. You dont need to write full name of player. <br> Permission: `stargate.staff`
| **/staff** | **Display list of Staffr** <br><br> Usage: `/staff` <br><br> This command will send you list of all online staff. <br> Permission: `stargate.staff`

## 🔨 Config:  
- Here you can change StaffChat caller
- Default configuration:

```yaml  
---
#Set StaffChat caller
StaffChatCaller: "!"
#StaffChat format
#You can use: %player%, %message%, 
StaffChatFormat: "§7[§6Staff§7] §f%player% : §7%message%"    
#MOTD Changer Settings
#You can set here unlimited amount of MOTDSs
#Whithelist MOTD - coming soon
WMOTD:
  - "§6Server §7» §l§cWartung"
  - "§6Server §7» §l§cOffline"
#Normal non-whitelist MOTD
MOTD:
  - "Test"
  - "Test1"
...  
```  
## 📋 Special Thanks To:
Everyone who downloaded this plugin. To persons who inspiraded me to start this plugin. Thanks to You, this project will be updated for a long time
