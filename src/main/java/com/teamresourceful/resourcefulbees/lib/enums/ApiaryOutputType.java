package com.teamresourceful.resourcefulbees.lib.enums;

import com.mojang.serialization.Codec;

public enum ApiaryOutputType {
    COMB,
    BLOCK;

    public static final Codec<ApiaryOutputType> CODEC = Codec.STRING.xmap(ApiaryOutputType::valueOf, ApiaryOutputType::toString);
}
