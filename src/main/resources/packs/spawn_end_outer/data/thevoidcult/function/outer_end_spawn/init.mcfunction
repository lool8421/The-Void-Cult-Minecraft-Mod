scoreboard objectives add TVC_deaths deathCount
scoreboard objectives add TVC_health health
scoreboard objectives add TVC_last_health dummy
scoreboard objectives add TVC_var dummy


scoreboard players set mode TVC_var 2
function thevoidcult:outer_end_spawn/loop

schedule function thevoidcult:outer_end_spawn/clearmode 2t