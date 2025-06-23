package com.github.tatercertified.equicap;

public enum MobCapMerge {
    None("none"),
    Combine("combine"),
    VanillaLike("vanilla-like");

    private String name;
    MobCapMerge(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }
}
