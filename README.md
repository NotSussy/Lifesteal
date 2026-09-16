# Lifesteal

A Fabric mod for Minecraft 26.2 implementing a classic "Lifesteal SMP" mechanic.

## Rules

- Every player has a max health floor of **5 hearts** — it can never drop below that.
- Every player has a max health ceiling of **8 hearts** — no way to exceed that.
- New players start at the 5-heart floor.
- Killing another player steals a heart, granting the killer +1 max heart, **unless**
  the victim was already at the 5-heart floor (nothing left to steal), or the killer
  is already at the 8-heart ceiling.
- A **Heart** item can be crafted and consumed (right-click) to gain a heart the same
  way, again capped at 8 hearts. The recipe itself refuses to produce a Heart while
  the crafter already has 8 or more hearts.

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

## Building

Requires JDK 25 and network access to the Fabric/Mojang Maven repositories.

```
./gradlew build
```

The mod jar will be in `build/libs/`.

## Notes on versioning

Minecraft 26.2 ships without Yarn mappings — Fabric mods for 26.1+ build against
Mojang's official mappings (`loom.officialMojangMappings()`), which is what this
project uses. This project was written and structured by hand in an offline
sandbox with no access to the Minecraft/Fabric Maven repositories, so it has
**not** been compiled against the real 26.2 libraries. The code follows the most
recent known-stable Mojang mapping names and Fabric API surface, but if a class,
method, or field was renamed in 26.2 you may need to fix a handful of references
when you first build it locally (the compiler errors will point exactly at them).

The Fabric Loom plugin version used here (`1.16-SNAPSHOT`) matches what other
public mods currently building for 26.2 use, since 1.16 has not been tagged as
a stable release yet as of this writing.
