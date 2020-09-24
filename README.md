# Gradle Template

Tento template obsahuje základní konfiguraci pro pluginy v Gradle + Gitlab-CI.

## Co je potřeba změnit
1. `settins.gradle` - Název pluginu
2. `build.gradle` - Název + package
3. `cz.craftmania.template` - Přejmenovat na něco jiného.
4. `plugin.yml` - Název + package

## Building
Zakladní build pluginu se dělá:

### InteliJ
* View -> Gradle Tool Windows -> Gradle -> `others` -> compilePlugin task

### Konzole
```bash
./gradlew compilePlugin
```

## Clean vybuilděných jarů
### InteliJ
* View -> Gradle Tool Windows -> Gradle -> `build` -> clean

### Konzole
```bash
./gradlew clean
```