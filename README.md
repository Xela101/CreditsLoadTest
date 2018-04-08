# CreditsLoadTest
Tests the TPS Load of the Credits Cryptocurrency(http://www.credits.com)

Credits is a Cryptocurrency that claims it will be the first to reach 1 million tps first with the potential of scaling even higher.


# Description
This was a quick dirty build just for the purpose of testing the credits network. 
It is hooked into the credits desktop wallet api for easy interaction with the thrift protocal credits uses for their node api.
I have created a custom socket client that constructs raw thrift transaction packets and bundles them by absuing the tcp network congestion to send multiple transactions within one packet.
This allows for me to flood more transactions to the node. This does use a replay attack as transactions can be send multiple times. I went down this route to minimize CPU usage building transaction packets (This will need to be changed when fixed).

# Installation
To build this project you can import it into the eclipse IDE (General -> Existing Projects into Workspace)

Make sure you point the ip address in the config file to look at the ip of your node and allow the ip/port entry in your firewall or portforward the entry.
