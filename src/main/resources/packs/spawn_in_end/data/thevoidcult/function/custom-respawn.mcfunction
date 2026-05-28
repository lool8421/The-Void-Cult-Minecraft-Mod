
#stop if player had positive hp last tick
execute unless score @s TVC_last_health matches ..0 run return fail

#stop immediately if the player has 0 or less health, wait until they're alive
execute if score @s TVC_health matches ..0 run return fail

#mark the player as non-dead
scoreboard players set @s TVC_deaths 0

#check if their respawn point hasn't been deleted
execute if data entity @s SpawnDimension run return fail

#teleport player to the end dimension
execute in minecraft:the_end run tp @s 1608 200 8
effect give @s slow_falling 20 1 true

#announce that the island can be created by a different function
scoreboard players set tryPlaceIsland TVC_var 1