execute if score mode TVC_var matches 2 run return fail
execute if score mode TVC_var matches 3 run return fail

execute as @a[scores={TVC_deaths=1..}] run function thevoidcult:center_end_spawn/custom-respawn
execute as @a[tag=!TVC_newplayer] run function thevoidcult:center_end_spawn/new-player

execute as @a run scoreboard players operation @s TVC_last_health = @s TVC_health

#wait until can actually place the island and haven't placed one yet
execute unless score alreadyPlacedIsland1 TVC_var matches 1 \
if score tryPlaceIsland TVC_var matches 1 \
in minecraft:the_end \
if entity @e[type=ender_dragon] \
run function thevoidcult:center_end_spawn/build_island

schedule function thevoidcult:center_end_spawn/loop 1t