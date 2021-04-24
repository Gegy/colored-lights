const int COLOR_COUNT = 256;
const int SATURATION_LEVELS = 6;

const int HUES_PER_SATURATION_LEVEL = (COLOR_COUNT - 1) / (SATURATION_LEVELS - 1);

// adapted from <http://lolengine.net/blog/2013/07/27/rgb-to-hsv-in-glsl>
vec3 unpack_chunk_light_color(int color) {
    // we use a value of 0 to represent saturation=0, given hue is irrelevant here.
    if (color == 0) {
        return vec3(1.0);
    }

    color = color - 1;

    int saturationLevel = (color / HUES_PER_SATURATION_LEVEL) + 1;

    float hue = fract(float(color) / float(HUES_PER_SATURATION_LEVEL));
    float saturation = float(saturationLevel) / float(SATURATION_LEVELS - 1);

    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(vec3(hue) + K.xyz) * 6.0 - K.www);
    return mix(vec3(1.0), clamp(p - K.xxx, 0.0, 1.0), saturation);
}

vec3 compute_chunk_light_color(ivec2 colored_light, ivec2 light, vec3 position, sampler2D lightMap) {
    int high = colored_light.x;
    int low = colored_light.y;
    if ((high == 0 && low == 0) || light.x <= 0 || light.y >= 256) {
        return vec3(1.0);
    }

    position /= 16.0;

    vec3 x0y0z0 = unpack_chunk_light_color((high >> 24) & 255);
    vec3 x0y0z1 = unpack_chunk_light_color((high >> 16) & 255);
    vec3 x0y1z0 = unpack_chunk_light_color((high >> 8) & 255);
    vec3 x0y1z1 = unpack_chunk_light_color(high & 255);
    vec3 x1y0z0 = unpack_chunk_light_color((low >> 24) & 255);
    vec3 x1y0z1 = unpack_chunk_light_color((low >> 16) & 255);
    vec3 x1y1z0 = unpack_chunk_light_color((low >> 8) & 255);
    vec3 x1y1z1 = unpack_chunk_light_color(low & 255);
    vec3 color = mix(
        mix(
            mix(x0y0z0, x0y0z1, position.z),
            mix(x0y1z0, x0y1z1, position.z),
            position.y
        ),
        mix(
            mix(x1y0z0, x1y0z1, position.z),
            mix(x1y1z0, x1y1z1, position.z),
            position.y
        ),
        position.x
    );

    float max_sky_light = texelFetch(lightMap, ivec2(0, 15), 0).r;
    float sky_factor = 1.0 - (light.y / 256.0) * max_sky_light;
    float block_factor = light.x / 256.0;
    return mix(vec3(1.0), color, sky_factor * block_factor);
}

vec4 apply_color_to_light(vec4 normal_light, ivec2 colored_light, ivec2 light, vec3 position, sampler2D lightMap) {
    vec3 light_color = compute_chunk_light_color(colored_light, light, position, lightMap);
    return vec4(light_color, 1.0) * normal_light;
}
