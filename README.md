KitchenSink
-----------

Configuration:

* *animal-count* Print number of each mob every 10 minutes
* *log-animals* Log passive animal kills to console
* *log-player-drops* Log player drop sums to console
* *buff-drops* Increase items dropped by animals by factor
* *disable-buff* Disable specified item drops from being affected by *buff-drops*
* *buff-shear-drops* Increase wool dropped by sheared sheep by factor
* *disable-snowgrow* Disable snow being generated
* *disable-drops* Disable items, great for creative servers
* *disable-tnt* Disable TNT from being ignited
* *disable-invisibility-on-combat* Remove invisibility potion effects when a player PvPs.
* *lower-strength-potion-damage* Reduce damage dealt with strength potions to Minecraft 1.5 levels.
* *health-potion-multiplier* The multiplicative factor applied to health from instant health potions (splashed and drunk).
* *regen-potion-multiplier* The multiplicative factor applied to health from regeneration potions (splashed and drunk).
* *block-caps* Block people typing in caps too much
* *block-villagers* Prevent players from trading with villagers
* *safe-ice* Prevent ice from turning into water when broken
* *safe-dispensers* Prevent dispensers from dispensing items listed in *disabled-items.dispensed*
* *safe-boats* Schedule a task to clear empty boats every *safe-boats-delay* seconds
* *safe-boats-delay* The time between clearing empty boats
* *safe-boats-drop* Drop a boat item when a boat is removed due to *safe-boats*
* *safe-minecarts* Schedule a task to clear empty minecarts every *safe-minecarts-delay* seconds
* *safe-minecarts-delay* The time between clearing empty minecarts
* *safe-minecarts-drop* Drop a minecart item when a minecart is removed due to *safe-minecarts*
* *safe-special-carts* Prevent storage and powered minecarts from being affected by *safe-minecarts*
* *sprint-max-time* Timeout to force player to stop sprinting after the specified amount of ticks. Set to zero or less to disable.
* *remove-on-exit* Remove a vehicle when a player exits it
* *safe-portals* Prevent players from creating nether portals
* *pearl-damage* Damage players when throwing an ender pearl
* *leatherless-books* Allow books to be crafted using only sugar cane.
* *lock-horses* Allow players to prevent other players from riding horses they own using /lock-horse.  To unlock: /unlock-horse.
* *invulnerable-tame-horses* Make *riderless* tame horses invulnerable. Ridden horses can always be damaged.
* *untame-pets* Allow players to untame a pet by typing /untame and right clicking on a pet they own.
* *horse-recipes* Enable custom crafting recipes for horse armour and saddles.
* *host-keys-check* If true (default), check that admins connect with a valid host key.
* *host-keys-drop-permissions* If true, drop permissions to default on invalid host key; otherwise kick.
* *disabled-items*
	- *left-click* Prevent players from left clicking with specified items
	- *right-click* Prevent players from right clicking with specified items
	- *dispensed* Prevent dispensers from dispensing specified items if *safe-dispensers* is true
* *allow-enchant-items* If list is not empty, allow only specified items to be enchanted
* *block-brew* Block specified brewing ingredients
* *next-restart* Number of seconds between server restarts
* *warn-restart-on-join* Whether to warn the player when they join if a restart is less than a minute away
* *warn-restart-on-inventory-open* Whether to warn the player when they open an inventory if a restart is less than a minute away
