# Colored Lights
A compromise solution to colored lighting in Minecraft by tinting based on area.
Normally, computing colored lighting would require excessive memory requirements which makes it unfortunately quite impractical. 
This mod takes a compromising approach that calculates light colors instead for each chunk corner, and smoothly blends between them within the chunk.
Although this solution is not perfect and does have noticeable issues, it achieves the general effect with minimal performance impact.

This mod works entirely client-side for 1.17 and requires both Fabric Loader and API.

You can see some examples of the mod in action here:

![Example of lighting](https://i.imgur.com/mekeDny.png)

![Example of lighting](https://i.imgur.com/UG3IiH6.jpeg)


## Configuration & Mod Integration
The colors emitted by blocks can be configured with a resource pack! This means any mod can easily add colored light
compatibility by just adding a data file.

To modify or add light colors, create `assets/colored_lights/light_colors.json`:
```json
{
  "replace": false,
  "colors": {
    "redstone_torch": "#ff3333",
    "lava": "#ff9933"
  }
}
```

The configuration must provide a mapping between blocks that emit light and the corresponding hex color value that they should emit.
Note that colors aren't represented exactly due to the color space being compressed down.

If `replace` is specified as true, the default values will be cleared.
For any pack just adding to existing color mappings or just replacing specific entries, replace should be false.
