
scoreboard players set setUpEnd TVC_var 1

execute in minecraft:the_end positioned 0 128 0 run kill @e[type=ender_dragon,distance=..128,limit=1]
execute in minecraft:the_end as @a[x=-3,y=0,z=-3,dx=6,dy=128,dz=6] at @s run tp ~ ~1 ~

give @a chorus_flower 8
give @a chorus_fruit 16

schedule function thevoidcult:center_end_spawn/finish-island 1t