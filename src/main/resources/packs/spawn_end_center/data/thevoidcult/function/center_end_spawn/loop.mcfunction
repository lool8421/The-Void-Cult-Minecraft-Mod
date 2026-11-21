execute if score mode TVC_var matches 2 run return fail
execute if score mode TVC_var matches 3 run return fail

execute as @a[scores={TVC_deaths=1..}] run function thevoidcult:center_end_spawn/custom-respawn
execute as @a[tag=!TVC_newplayer] run function thevoidcult:center_end_spawn/new-player

execute as @a run scoreboard players operation @s TVC_last_health = @s TVC_health

#presence of the ender dragon boss is a signal by the game that the end island is loaded.
execute unless score setUpEnd TVC_var matches 1.. in minecraft:the_end positioned 0 128 0 if entity @e[type=ender_dragon,distance=..128] run function thevoidcult:center_end_spawn/build-island

schedule function thevoidcult:center_end_spawn/loop 1t