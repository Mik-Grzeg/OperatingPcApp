# OperatingPcApp
I created the app for my personal use only.
Its functionalities are simple, though quite useful in my opinion.  
#### Functionalities: 
* Power on pc
* Power off pc
* Reboot

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

#### Power off and Reboot
##### Requirements:
* Ssh server on target pc
* Port that openssh daemon listens for connections from clients
* Being able to log in through ssh with password
* Username of pc account
* Password to the account
* Sudo
These features work on linux, using sudo to shutdown / reboot the pc. User has to have privileged access to perform such an action.

Main UI                    |             Settings
:-------------------------:|:-------------------------:
![MainMenu](https://user-images.githubusercontent.com/58633804/113493276-3b17d400-94de-11eb-878a-baace6403b09.png)  |  ![Settings](https://user-images.githubusercontent.com/58633804/113493316-816d3300-94de-11eb-9e84-d3520810af37.png)

