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
- A **Heart** item can also be crafted from scratch, but only while under **9**
  hearts — the recipe refuses to produce one at 9 hearts or above. This is a
  separate, lower limit than the 20-heart ceiling: it only stops crafting new
  hearts, not gaining more from kills or drops.
- Crafting also refuses to produce a Heart if you already have one within 8
  blocks — in your own inventory, your ender chest, a nearby chest (or any
  other container block), or just sitting on the ground. Hearts have to
  actually be used, not stockpiled.
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

The Heart item's icon is composited from the real vanilla HUD heart sprites
(the dark outline layer plus the red fill layer, extracted from Mojang's own
game assets), scaled up from their native 9x9 to 36x36 for a crisper item icon.

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
