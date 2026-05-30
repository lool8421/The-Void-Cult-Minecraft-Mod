execute as @a[scores={TVC_deaths=1..}] run function thevoidcult:spawninend/custom-respawn
execute as @a[tag=!TVC_newplayer] run function thevoidcult:spawninend/new-player

execute as @a run scoreboard players operation @s TVC_last_health = @s TVC_health

#wait until can actually place the island and haven't placed one yet
execute unless score alreadyPlacedIsland TVC_var matches 1 \
if score tryPlaceIsland TVC_var matches 1 \
in minecraft:the_end \
if loaded 1592 64 -8 \
if loaded 1624 88 24 \
run function thevoidcult:spawninend/build_island