package com.terraformersmc.modmenu.api;

public interface ConfigScreenFactory<S> {
    S create(Object parent);
}
