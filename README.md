# Lifesteal

A Fabric mod for Minecraft 26.3 implementing a classic "Lifesteal SMP" mechanic.

## Rules

- Every player has a max health floor of **5 hearts** — it can never drop below that.
- Every player has an overall max health ceiling of **20 hearts** — no way to exceed that.
- New players start at vanilla's normal 10 hearts, comfortably between the floor and ceiling.
- Dying costs you a heart: your max health permanently drops by one heart, and a
  **Heart** item drops on the ground at your death location, just like the rest of
  your inventory. If you were already at the 5-heart floor, nothing is lost and
  nothing drops.
- Picking up and using (right-click) a Heart item grants +1 max heart, capped at
  the 20-heart ceiling — this is how a killer actually "steals" a heart: by
  grabbing the one their victim dropped.
- A **Heart** item can also be crafted from scratch, but only while your total
  heart count stays under **9**. That total adds up your equipped hearts plus
  every loose Heart item within 8 blocks — your own inventory, your ender
  chest, a nearby chest (or any other container block), or one just sitting
  on the ground. As long as that grand total is under 9, crafting works; the
  moment it would hit 9 or more, it stops. This is a separate, lower limit
  than the 20-heart ceiling: it only stops crafting new hearts, not gaining
  more from kills or drops.
- `/withdraw <amount>` converts your own hearts back into carryable Heart
  items, without ever taking you below the 5-heart floor.

### Crafting a Heart

```
Diamond Block | Netherite Scrap  | Diamond
Trial Key     | Totem of Undying | Trial Key
Diamond       | Emerald Block    | Diamond
```

Every ingredient is a single item, so this is a plain vanilla shaped recipe with no
extra quantity enforcement needed.

## No client mod required

Players joining the server do **not** need Fabric Loader or Fabric API installed -
only the server does. This mod deliberately never registers a real custom item: a
truly custom item would need every connecting client to already understand that
registry entry, which is exactly what forces a "this server requires Fabric Loader
and Fabric API" disconnect. Instead, a Heart is a plain vanilla Nether Star carrying
a `minecraft:custom_data` marker and a custom name - both are stock vanilla item
components any client, modded or not, already understands. The tradeoff is visual:
without the mod (or a resource pack) installed client-side, a Heart just looks like
a Nether Star named "Heart" rather than the custom heart-shaped icon.

## Building

Requires JDK 25 and network access to the Fabric/Mojang Maven repositories.

```
./gradlew build
```

The mod jar will be in `build/libs/`.

## Notes on versioning

Minecraft ships unobfuscated starting with 26.1, so there are no mappings (Yarn
or Mojang) to declare at all anymore. This project uses Fabric's new
non-remapping Loom plugin (`net.fabricmc.fabric-loom`) and plain `implementation`
dependencies instead of `modImplementation`, matching FabricMC's own official
example mod template for 26.3. This project was written and structured by hand
in an offline sandbox with no access to the Minecraft/Fabric Maven repositories,
so it has **not** been compiled against the real 26.3 libraries. The code
follows current Fabric API conventions, but if a class, method, or field was
renamed in 26.3 you may need to fix a handful of references when you first
build it locally (the compiler errors will point exactly at them).
