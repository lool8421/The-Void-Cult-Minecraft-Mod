tag @s add TVC_newplayer

execute in minecraft:the_end run tp @s 1608 200 8
effect give @s slow_falling 20 1 true

scoreboard players set tryPlaceIsland TVC_var 1