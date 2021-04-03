# OperatingPcApp
I created the app for my personal use only.
Its functionalities are simple, though quite useful in my opinion.  
##### Functionalities: 
* Power on pc
* Power off pc

Personally I do not opt for the eye pleasing UI, especially with this project I only needed its simple features.
It was my first experience with developing an application.

#### Power on 
Its logic is rather simple, it sends a frame that is sent as a broadcast, which containst within its payload 6 bytes of FF, followed by 16 copies of mac address of target computer's 48-bit MAC address. 
##### Requirements:
* Destination computer MAC address 
* Being connected to the same local network
* Hardware support of Wake-on-LAN, when  it comes to pc motherboards most of the modern ones have it
* Configuration router so it does not filter ports that are used for magic packets, typically 7 or 9
When it comes to  wireless interfaces, most of them do not maintain a link in lower power states and cannot receive a magic packet.
Wake-on-lan packet is sent at layer 2 of OSI model, so it does not require ip of destination pc.qq!

#### Power off
##### Requirements:
* Ssh server on target pc
* Port that openssh daemon listens for connections from clients
* Being able to log in through ssh with password
* Username of pc account
* Password to the account
* Sudo
That feature works on linux, using sudo to shutdown the pc. User has to have privileged access to perform such an action.

Main UI                    |             Settings
:-------------------------:|:-------------------------:
![](https://user-images.githubusercontent.com/58633804/113486288-3e956600-94b2-11eb-89bf-b29a6a1cbc57.png?raw=true)  |  ![SmartHomePC_Settings](https://user-images.githubusercontent.com/58633804/113486419-e448d500-94b2-11eb-824f-3d03a0f44fea.png)

