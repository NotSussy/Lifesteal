# Lifesteal

A Fabric mod for Minecraft 26.2 implementing a classic "Lifesteal SMP" mechanic.

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
- A **Heart** item can also be crafted from scratch, but only while under **8**
  hearts — the recipe refuses to produce one at 8 hearts or above. This is a
  separate, lower limit than the 20-heart ceiling: it only stops crafting new
  hearts, not gaining more from kills or drops.

### Crafting a Heart

```
Diamond          | Netherite Scrap | Diamond
Golden Apple     | Totem of Undying| Golden Apple
Diamond          | Emerald Block   | Diamond
```

Note: a vanilla crafting grid can only hold one item per slot, so "3 diamonds" /
"4 golden apples" from the original spec were interpreted as "this ingredient in
this slot" (4 diamond slots, 2 golden apple slots) rather than a stack count in a
single slot, since Minecraft has no way to require a stack of >1 in one grid cell.

The Heart item's icon is the real vanilla heart sprite (extracted from Mojang's
own game assets), scaled up from its native 9x9 to 36x36 for a crisper item icon.

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
example mod template for 26.2. This project was written and structured by hand
in an offline sandbox with no access to the Minecraft/Fabric Maven repositories,
so it has **not** been compiled against the real 26.2 libraries. The code
follows current Fabric API conventions, but if a class, method, or field was
renamed in 26.2 you may need to fix a handful of references when you first
build it locally (the compiler errors will point exactly at them).
