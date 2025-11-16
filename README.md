KitchenSink
===========
A plugin for miscellaneous functionality that hasn't found a home elsewhere.

Configuration
-------------
* `buff-drops` Increase items dropped by animals by factor.
* `disable-buff` Disable specified item drops from being affected by `buff-drops`
* `buff-shear-drops` Increase wool dropped by sheared sheep by factor.
* `disable-snowgrow` Disable snow being generated.
* `disable-drops` Disable items, great for creative servers.
* `disable-tnt` Disable TNT from being ignited.
* `block-caps` Block people typing in caps too much.
* `block-villagers` Prevent players from trading with villagers.
* `block-johnny` Prevent players from renaming Vindicators to be "Johnny."
* `safe-ice` Prevent ice from turning into water when broken.
* `safe-dispensers` Prevent dispensers from dispensing items listed in `disabled-items.dispensed`.
* `safe-portals` Prevent players from creating nether portals.
* `allow-egg-hatching` Allow or deny the spawning of chickens using eggs.
* `disabled-items`
	- `left-click` Prevent players from left clicking with specified items.
	- `right-click` Prevent players from right clicking with specified items.
	- `dispensed` Prevent dispensers from dispensing specified items if `safe-dispensers` is true.
* `restart-times` List of times when the server restarts, each formatted as `HH-mm`.
* `warn-restart-on-join` Whether to warn the player when they join if a restart is less than a minute away.
* `warn-restart-on-inventory-open` Whether to warn the player when they open an inventory if a restart is less than a minute away.
* `countdown-max-time` Determines the maximum amount of seconds that can be used in a countdown.
* `countdown-format` Specifies the format the countdown takes in the chat window.
* `countdown-color` The text color for the countdown itself.
* `countdown-style` The text formatting used by the countdown.
* `countdown-msg-color` Color code used by the message displayed when the countdown is complete.
* `countdown-msg-style` The text formatting used by the message at countdown completion.
* `disable-player-damage-to-villagers` Disables player damage to villagers.
* `use-coreprotect` Enables the `/trace [playername]` and `/xray-top` commands.