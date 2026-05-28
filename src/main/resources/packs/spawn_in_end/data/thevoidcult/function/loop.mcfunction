execute as @a[scores={TVC_deaths=1..}] run function thevoidcult:custom-respawn
execute as @a[tag=!TVC_newplayer] run function thevoidcult:new-player

execute as @a run scoreboard players operation @s TVC_last_health = @s TVC_health

execute unless score alreadyPlacedIsland TVC_var matches 1 \
if score tryPlaceIsland TVC_var matches 1 \
in minecraft:the_end \
if loaded 1584 80 -24 \
if loaded 1632 80 24 \
run function thevoidcult:build_island