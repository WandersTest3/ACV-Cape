# ACV Cape

Mod Fabric de capas para Minecraft Java. Um único jar cobre várias versões do jogo: cada grupo de versões tem o seu próprio código em `versions/` e o grupo certo é escolhido ao iniciar.

## Estrutura

| Pasta | Conteúdo |
| --- | --- |
| `src/main/java` | Núcleo comum: seleção do grupo (`AcvGroup`), entrada do mod (`AcvLauncher`), Mod Menu e gates de mixin |
| `src/main/resources` | `fabric.mod.json`, configs de mixin, idiomas e texturas |
| `versions/g1` | Minecraft 1.21 – 1.21.1 |
| `versions/g2` | Minecraft 1.21.2 – 1.21.4 |
| `versions/g3` | Minecraft 1.21.5 |
| `versions/g4` | Minecraft 1.21.6 – 1.21.8 |
| `versions/g5` | Minecraft 1.21.9 – 1.21.10 |
| `versions/g6` | Minecraft 1.21.11 |

`src/stubs` contém apenas assinaturas de APIs de terceiros (Mod Menu, Wavey Capes) usadas na compilação; nada disso entra no jar.

## Compilar

Requer JDK 25.

```
./gradlew mergedJar
```

O jar final fica em `build/merged/`.

## Autor

Wanders_Test3
