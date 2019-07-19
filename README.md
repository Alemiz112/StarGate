# StarGate
<a align="center" href="https://discord.gg/Fq8JWfB"><img src="https://discordapp.com/api/guilds/562263095888707614/embed.png" alt="Discord server"/></a> [![Build Status](https://travis-ci.org/Alemiz112/StarGate.svg?branch=master)](https://travis-ci.org/Alemiz112/StarGate)
>Plugin made for WaterDog, awesome proxy for mcbe (latest version)

## 🎯 Features:    
 ✔️ StaffChat ([moved](https://github.com/Alemiz112/StarGate-Addons))<br>
 ✔️ StaffFind/StaffList ([moved](https://github.com/Alemiz112/StarGate-Addons))<br>
 ✔️ PlayerFind ([moved](https://github.com/Alemiz112/StarGate-Addons))<br>
 ✔️ QueryUpdater ([moved](https://github.com/Alemiz112/StarGate-Addons))<br>
 ✔️ Communication between WaterDog clients/servers
  <br>
## 📝 TODO:
 ✔️ API for PocketMine developers<br>
 ✔️ PartyMode<br>
## 🔧 DEV Only:
 ✔️ Plugin Messaging System(stable)<br>
 
 
## 📘 Commands:

**StarGate Commands ([moved](https://github.com/Alemiz112/StarGate-Addons)):**  
  
| **Name/Command** | **Usage** |  
| --- | --- |  
| **/sc** | **StaffChat control** <br><br> Usage: `/sc`, `/sc <message>` <br><br> Using only `/sc` you will turn force StaffChat on|off|. You can also use `!` or any presetted character before your message to send staff message. <br> Permission: `stargate.staffchat`
| **/look** | **Find Player** <br><br> Usage: `/look <player>` <br><br> This command will help you find a players server. You dont need to write full name of player. <br> Permission: `stargate.staff`
| **/staff** | **Display list of Staffr** <br><br> Usage: `/staff` <br><br> This command will send you list of all online staff. <br> Permission: `stargate.staff`

## ⚙ StarGate Protocol:
StarGate Protocol is service that servers that are connected to WaterDog to communicate between
#####Server clients:
- [Nukkit](https://github.com/Alemiz112/StarGate-Universe)
- PMMP (soon)

## 🔨 Config:  
- Here you can change StaffChat caller
- Default configuration:

```yaml  
---
#Settings of StarGate Communication service
StarGate:
  port: 47007
  maxConnections: 50 
...  
```  
## 📋 Special Thanks To:
Everyone who downloaded this plugin. To persons who inspiraded me to start this plugin. Thanks to You, this project will be updated for a long time
