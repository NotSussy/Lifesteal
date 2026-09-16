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
