scoreboard objectives add TVC_deaths deathCount
scoreboard objectives add TVC_health health
scoreboard objectives add TVC_last_health dummy
scoreboard objectives add TVC_var dummy

scoreboard players set mode TVC_var 1
function thevoidcult:center_end_spawn/loop

schedule function thevoidcult:center_end_spawn/clearmode 2t