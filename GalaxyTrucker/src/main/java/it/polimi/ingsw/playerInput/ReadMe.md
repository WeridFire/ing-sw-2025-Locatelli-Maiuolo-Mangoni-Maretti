# PlayerInput - Documentation
The following documentation is to detail
the way the game handles player-server interactions, controllerwise.

The interactions detailed here are intended as game-wise interactions,
where a player needs to perform an action. Some examples are:
- Choosing which tiles to activate (Shields, Cannons, Thrusters)
- Choosing where to remove Crew
- Choosing where to put loaded cargo
- Choosing if to accept or not credits or cargo
- Choosing from where to remove cargo (crew or batteries)
- Choosing where to land on planets

These interactions have a lifecycle: they are created, they are executed, and are "terminated",
either by a timeout (because the player didn't fulfill a request on time) or by input of the user.
Let's go through how this type of interaction happens.
During the interactions, we are not waiting for a single message by the player. But instead we 
are waiting for the player to take (maybe multiple) actions on the server (and therefore on the model, through the controller).
Once we know that the player has finished his turn, the interaction ends and move onto the next one.

## One "interaction cycle" at a time

First of all, there can only be one _pending_ interaction at a time.
In the real game, whenever there are choices to be taken, it's always one at a time.
For example when choosing the thrusters power in OpenSpace, it's in route order.
For the StarDust it's reversed. But it's always one at a time.

## Creating an interaction
Let's simulate the interaction of the OpenSpace card. The open space card 
iterates through each single Player (in route order). For each player, they will need
to tell:
- What thrusters to activate
- What batteries to use

These two information we will get from the player can be split and formalized in 2 different
interactions:
- PlayerActivateTilesRequest ---> we obtain which tiles the player wants to activate
- PlayerRemoveLoadableRequest(Batteries, #tiles) --> we wait for the player to remove as many batteries as the number of activated thrusters

Note that these interactions are run on the gameLoop thread, but don't block the game connections, because, well, everything is on it's own
thread. You can see AdventurePhase where the cards are played to verify this.

These interactions, will automatically update the shipboard! So for example RemoveLoadableRequest will have removed the
batteries (you can check the IServer implementation for the controller)

This will be finished tomorrow - davide