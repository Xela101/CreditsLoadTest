# CreditsLoadTest
Tests the TPS Load of the Credits Cryptocurrency (http://www.credits.com)

Credits is a Cryptocurrency that claims it will be the first to reach 1 million tps first with the potential of scaling even higher.

# Changes

*New version*

You can now compile the project without having to install eclipse. Just start the "compile.bat" file and then start the "run.bat" file and it should run, keep in mind you will need to edit the "config.properties" file and point the ip property to the ip address of your node, if you are running the node locally you can leave it as "localhost".

Replay attack at a large scale has been prevented so I have removed the multipacket sends(Transaction speed per node may decrease a bit). Each transaction should now be unique.

Transaction spammer now sends between two different addresses. I added this just incase the beta prevents us from sending transactions to itself.

*Older version*

Added nonblocking sockets with selectors to send and receive data asynchronous, let me know how you find it might remove the writes from the other selector functions was just trying to save an extra thread but might not be worth it. 

If you get disconnected from the node try scale down the maxSampleSize the tcp send/receive buffer could be fulling up to the maximum and the node is forcing your socket close. 

# Description
This was a quick dirty build just for the purpose of testing the credits network.

It is hooked into the credits desktop wallet api for easy interaction with the thrift protocal credits uses for their node api.

I have created a custom socket client that constructs raw thrift transaction packets and bundles them by absuing the tcp network congestion to send multiple transactions within one packet.

This allows for me to flood more transactions to the node. This does use a replay attack as transactions can be send multiple times. I went down this route to minimize CPU usage building transaction packets (This will need to be changed when fixed).

# Installation
To build this project you can import it into the eclipse IDE (General -> Existing Projects into Workspace)

Make sure you point the ip address in the config file to look at the ip of your node and allow the ip/port entry in your firewall or portforward the entry if you are not running it on the local node machine.

# Additional Info
There is currently a block limit of 256 transactions per block, this can be increased by editing the bat file of the node.

I have managed to get over 300 transactions per block and around 1.5k tps with only one node. (Sending from New Zealand to America)

There are still quite a few bugs in the credits build.

A few include: Replay attack, 0 CS Transactions, Address funding errors and more.

The larger bugs are reported directly to the development team.
