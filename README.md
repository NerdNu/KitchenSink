KitchenSink
===========
A plugin for miscellaneous functionality that hasn't found a home elsewhere.

Configuration
-------------
* `animal-count` Print number of each mob every 10 minutes.
* `log-animals` Log passive animal kills to console.
* `log-player-drops` Log player drop sums to console.
* `buff-drops` Increase items dropped by animals by factor.
* `disable-buff` Disable specified item drops from being affected by `buff-drops`
* `buff-shear-drops` Increase wool dropped by sheared sheep by factor.
* `disable-snowgrow` Disable snow being generated.
* `disable-drops` Disable items, great for creative servers.
* `disable-tnt` Disable TNT from being ignited.
* `disable-entity-block-damage` A list of [EntityType](https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/entity/EntityType.html) names for which block damage is disabled if they explode.
* `disable-invisibility-on-combat` Remove invisibility potion effects when a player PvPs.
* `lower-strength-potion-damage` Reduce damage dealt with strength potions to Minecraft 1.5 levels.
* `health-potion-multiplier` The multiplicative factor applied to health from instant health potions (splashed and drunk).
* `regen-potion-multiplier` The multiplicative factor applied to health from regeneration potions (splashed and drunk).
* `disable-lingering-potion-pvp` If true, disable harmful lingering potion effects.
* `block-caps` Block people typing in caps too much.
* `block-villagers` Prevent players from trading with villagers.
* `block-johnny` Prevent players from renaming Vindicators to be "Johnny."
* `safe-ice` Prevent ice from turning into water when broken.
* `safe-dispensers` Prevent dispensers from dispensing items listed in `disabled-items.dispensed`.
* `sprint-max-time` Timeout to force player to stop sprinting after the specified amount of ticks. Set to zero or less to disable.
* `safe-portals` Prevent players from creating nether portals.
* `pearl-damage` Damage players when throwing an ender pearl.
* `leatherless-books` Allow books to be crafted using only sugar cane.
* `lock-horses` Allow players to prevent other players from riding horses they own using `/lock-horse`.  To unlock: `/unlock-horse`.
* `invulnerable-tame-horses` Make *riderless* tame horses invulnerable. Ridden horses can always be damaged.
* `untame-pets` Allow players to untame a pet by typing /untame and right clicking on a pet they own.
* `allow-egg-hatching` Allow or deny the spawning of chickens using eggs.
* `host-keys-check` If true (default), check that admins connect with a valid host key.
* `host-keys-drop-permissions` If true, drop permissions to default on invalid host key; otherwise kick.
* `disabled-items`
	- `left-click` Prevent players from left clicking with specified items.
	- `right-click` Prevent players from right clicking with specified items.
	- `dispensed` Prevent dispensers from dispensing specified items if `safe-dispensers` is true.
* `allow-enchant-items` If list is not empty, allow only specified items to be enchanted.
* `block-craft` Block specified crafting recipes.
* `block-brew` Block specified brewing ingredients.
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
